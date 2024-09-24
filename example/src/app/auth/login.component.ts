import { Component, Inject, NgZone, OnDestroy, OnInit } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor'
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: 'login.page.html',
})
export class LoginComponent implements OnInit, OnDestroy {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService, private router: Router) {
  }

  loading: boolean = false
  unsubscribeLoading?: (() => void)
  unsubscribeIsAuthenticated?: (() => void)
  visibilityChangeListener?: (() => void)
  regions: { key: string, baseUrl: string, clientId: string }[] = []

  ngOnInit() {
    console.log('AuthComponent#ngOnInit called');
    this.loginIfNeeded()

    this.unsubscribeIsAuthenticated = this.fronteggService.$isAuthenticated.subscribe(async (isAuthenticated) => {
      if (isAuthenticated) {
        console.log('AuthComponent#ngOnInit isAuthenticated');
        this.ngZone.run(() => {
          this.router.navigate([ '/' ], { replaceUrl: true })
        })
      }
    })

    // Listen for visibility changes
    this.visibilityChangeListener = this.handleVisibilityChange.bind(this);
    document.addEventListener('visibilitychange', this.visibilityChangeListener);
  }


  handleVisibilityChange(): void {
    if (document.visibilityState === 'visible') {
      console.log('App resumed, rechecking authentication');
      this.loginIfNeeded();
    }
  }

  async loginIfNeeded() {
    const { isLoading, isAuthenticated } = await this.fronteggService.getNativeState()
    console.log('AuthComponent#loginIfNeeded called', { isLoading, isAuthenticated });

    if (isLoading) {
      console.log('AuthComponent#loginIfNeeded isLoading');
      this.unsubscribeLoading?.()
      this.unsubscribeLoading = this.fronteggService.$isLoading.subscribe(() => {
        this.loginIfNeeded()
      })
      return
    }
    console.log('AuthComponent#loginIfNeeded not isLoading');
    if (isAuthenticated) {
      console.log('AuthComponent#loginIfNeeded isAuthenticated');
      this.router.navigate([ '/' ], { replaceUrl: true })
      return;
    }

    console.log('AuthComponent#loginIfNeeded not isAuthenticated');

    this.fronteggService.directLoginAction('social-login', 'google')
  }

  ngOnDestroy() {
    this.unsubscribeLoading?.()
    this.unsubscribeIsAuthenticated?.()
    if(this.visibilityChangeListener) {
      document.removeEventListener('visibilitychange', this.visibilityChangeListener);
    }
  }


}
