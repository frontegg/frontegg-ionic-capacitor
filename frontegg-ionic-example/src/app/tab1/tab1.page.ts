import { Component } from '@angular/core';
import { FronteggNative } from '@frontegg/ionic-capacitor'
import { SecureStoragePlugin } from 'capacitor-secure-storage-plugin';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: [ 'tab1.page.scss' ]
})
export class Tab1Page {

  constructor() {
  }


  login() {
    console.log('async login');

    FronteggNative.login();
  }

  async test() {

    try {
      console.log('saving to secure storage');
      await SecureStoragePlugin.set({ key: 'key', value: 'value' });

      console.log('waiting');
      await (new Promise(resolve => setTimeout(resolve, 2000)));
      console.log('get from secure storage');
      console.log(await SecureStoragePlugin.get({ key: 'key' }))
    }catch (e){
      console.log(e);
    }
  }
}
