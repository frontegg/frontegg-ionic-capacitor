import { Component, NgZone, OnInit } from '@angular/core';
import { Frontegg, FronteggState } from '@frontegg/ionic-capacitor'

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: [ 'tab1.page.scss' ]
})
export class Tab1Page implements OnInit {

  constructor(private ngZone: NgZone) {
  }

  user: FronteggState['user'] = null
  isLoading: boolean = true
  isAuthenticated: boolean = false
  accessToken: string | null = null


  ngOnInit() {
    console.log('start listening')
    Frontegg.$user.subscribe((user) => this.ngZone.run(() => this.user = user))
    Frontegg.$isLoading.subscribe((isLoading) => this.ngZone.run(() => this.isLoading = isLoading))
    Frontegg.$isAuthenticated.subscribe((isAuthenticated) => this.ngZone.run(() => this.isAuthenticated = isAuthenticated))
    Frontegg.$accessToken.subscribe((accessToken) => this.ngZone.run(() => this.accessToken = accessToken))
  }


  login() {
    console.log('login()');

    Frontegg.login()
  }

  logout() {
    console.log('logout()');
    Frontegg.logout()
  }
}
