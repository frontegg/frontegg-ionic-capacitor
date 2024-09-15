import { Component, Inject, NgZone, OnInit } from '@angular/core';
import { FronteggService, FronteggState } from '@frontegg/ionic-capacitor'

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: [ 'tab1.page.scss' ]
})
export class Tab1Page implements OnInit {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService) {
  }

  user: FronteggState['user'] = null
  isLoading: boolean = true
  refreshingToken: boolean = false
  isAuthenticated: boolean = false
  accessToken: string | null = null


  ngOnInit() {
    console.log('start listening')

    const { user, isAuthenticated, accessToken, isLoading, refreshingToken } = this.fronteggService.getState();
    this.user = user;
    this.isAuthenticated = isAuthenticated;
    this.accessToken = accessToken;
    this.isLoading = isLoading;
    this.refreshingToken = refreshingToken;

    this.fronteggService.$user.subscribe((user) => {
      console.log('change user', user)
      this.ngZone.run(() => this.user = user)
    })
    this.fronteggService.$isLoading.subscribe((isLoading) => {
      console.log('change isLoading', isLoading)
      this.ngZone.run(() => this.isLoading = isLoading)
    })
    this.fronteggService.$isAuthenticated.subscribe((isAuthenticated) => {
      console.log('change isAuthenticated', isAuthenticated)
      this.ngZone.run(() => this.isAuthenticated = isAuthenticated)
    })
    this.fronteggService.$accessToken.subscribe((accessToken) => {
      console.log('change accessToken', accessToken)
      this.ngZone.run(() => this.accessToken = accessToken)
    })
    this.fronteggService.$refreshingToken.subscribe((refreshingToken) => {
      console.log('change refreshingToken', refreshingToken)
      this.ngZone.run(() => this.refreshingToken = refreshingToken)
    })
  }


  login() {
    console.log('login()');
    this.fronteggService.login()
  }

  logout() {
    console.log('logout()');
    this.fronteggService.logout()
  }

  refreshToken() {
    console.log('refreshToken()');
    this.fronteggService.refreshToken()
  }
}
