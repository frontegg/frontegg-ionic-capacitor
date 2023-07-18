import { Component, OnInit } from '@angular/core';
import { FronteggNative } from '@frontegg/ionic-capacitor'
import { FronteggState } from '../../../../src';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: [ 'tab1.page.scss' ]
})
export class Tab1Page implements OnInit {

  constructor() {
  }

  ngOnInit() {
    FronteggNative.addListener('onFronteggAuthListener', (data: FronteggState) => {
      console.log("test", data)
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
