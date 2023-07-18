import { WebPlugin } from '@capacitor/core';

import type { FronteggNativePlugin } from './definitions';

export class FronteggNativeWeb
  extends WebPlugin
  implements FronteggNativePlugin {

  async login(): Promise<void> {
    console.log('login');
  }

  async logout(): Promise<void> {
    console.log('logout');
  }

  // async subscribe(callback: (state: FronteggState) => void): Promise<PluginListenerHandle> {
  //   return super.addListener('onFronteggAuthListener', callback);
  // }

  async getConstants(): Promise<Record<string, string>> {
    console.log('getConstants');
    return { value: 'value' };
  }
}
