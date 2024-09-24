import { Component, Inject, NgZone, OnDestroy, OnInit } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';
import { Router } from '@angular/router';
import { Subscriber, Observable, fromEvent, combineLatest, Subject, takeUntil } from 'rxjs';
import { filter, map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-auth-guard',
  template: '<router-outlet></router-outlet>',
})
export class AuthGuardComponent implements OnInit, OnDestroy {
  private isAuthenticated$: Observable<boolean>;
  private isLoading$: Observable<boolean>;
  private destroy$ = new Subject<void>();

  constructor(
    @Inject('Frontegg') private fronteggService: FronteggService,
    private router: Router,
    private ngZone: NgZone
  ) {
    // Convert custom observables to RxJS observables
    this.isAuthenticated$ = new Observable<boolean>((observer: Subscriber<boolean>) => {
      // Emit the initial value
      observer.next(this.fronteggService.$isAuthenticated.value);

      // Subscribe to changes
      const unsubscribe = this.fronteggService.$isAuthenticated.subscribe(
        (newValue) => {
          observer.next(newValue);
        }
      );

      // Return the unsubscribe function
      return unsubscribe;
    });

    this.isLoading$ = new Observable<boolean>((observer: Subscriber<boolean>) => {
      observer.next(this.fronteggService.$isLoading.value);

      const unsubscribe = this.fronteggService.$isLoading.subscribe(
        (newValue) => {
          observer.next(newValue);
        }
      );

      return unsubscribe;
    });
  }

  ngOnInit(): void {
    // Observable for visibility changes
    const visibility$ = fromEvent(document, 'visibilitychange').pipe(
      startWith(null), // Emit initial value to trigger immediate check
      filter(() => document.visibilityState === 'visible')
    );

    // Combine visibility changes with auth state changes
    combineLatest([ this.isAuthenticated$, this.isLoading$, visibility$ ])
      .pipe(
        takeUntil(this.destroy$),
        filter(([ isAuthenticated, isLoading ]) => !isLoading && !isAuthenticated),
        map(([ isAuthenticated, isLoading ]) => {
          console.warn('User is not authenticated, redirecting to login');
          this.ngZone.run(() => {
            this.router.navigate([ '/login' ]);
          });
        })
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    console.warn('Clean up subscriptions')
    this.destroy$.next();
    this.destroy$.complete();
  }
}
