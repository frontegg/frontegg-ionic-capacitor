import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { FronteggNative } from '@frontegg/ionic-capacitor'
import { FronteggState } from '../../../../src';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: [ 'tab1.page.scss' ]
})
export class Tab1Page implements OnInit {


  state: Partial<FronteggState> = {
    showLoader: true,
    isAuthenticated: false,
    isLoading: true,
    initializing: true,
  }

  constructor(private ngZone: NgZone) {
  }

  ngOnInit() {
    console.log('start listening')
    FronteggNative.addListener('onFronteggAuthEvent', (data: FronteggState) => {
      this.ngZone.run(() => {
        this.state = data
      })
    })

  }


  login() {
    console.log('login()');

    FronteggNative.login();
  }

  logout() {
    console.log('logout()');
    FronteggNative.logout()
  }
}
