import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Injectable, Inject } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';
import {  Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  isAuthenticated$: Observable<boolean>;
  isLoading$: Observable<boolean>;

  constructor(
    @Inject('Frontegg') private fronteggService: FronteggService,
    private router: Router
  ) {
    this.isAuthenticated$ = new Observable<boolean>((observer: any) => {
      // Emit the initial value
      observer.next(this.fronteggService.$isAuthenticated.value);

      // Subscribe to changes
      const unsubscribe = this.fronteggService.$isAuthenticated.subscribe((newValue) => {
        observer.next(newValue);
      });

      // Return the unsubscribe function
      return unsubscribe;
    });

    this.isLoading$ = new Observable<boolean>((observer: any) => {
      // Emit the initial value
      observer.next(this.fronteggService.$isLoading.value);

      // Subscribe to changes
      const unsubscribe = this.fronteggService.$isLoading.subscribe((newValue) => {
        observer.next(newValue);
      });

      // Return the unsubscribe function
      return unsubscribe;
    });
  }

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {

    await this.fronteggService.waitForLoader()
    const {isAuthenticated, isLoading} = this.fronteggService.getState()
    console.log('AuthGuard#canActivate called', { isAuthenticated, isLoading });

    if (isAuthenticated) {
      return true;
    } else {
      this.router.navigate([ '/login' ]);
      return false;
    }


  }
}
