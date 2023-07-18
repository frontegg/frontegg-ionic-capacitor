import { WebPlugin } from '@capacitor/core';

import type { FronteggNativePlugin } from './definitions';

export class FronteggNativeWeb
  extends WebPlugin
  implements FronteggNativePlugin {

  login(): void {
    console.log('async',);
  }

  logout(): void {
    console.log('async',);
  }

  subscribe(): void {
    console.log('async',);
  }

  async getConstants(): Promise<Record<string, string>> {
    console.log('async',);
    return { value: 'value' };
  }
}
