import { WebPlugin } from '@capacitor/core';

import type {
  FronteggConstants,
  FronteggNativePlugin,
  FronteggState,
} from './definitions';

export class FronteggNativeWeb
  extends WebPlugin
  implements FronteggNativePlugin {
  async getConstants(): Promise<FronteggConstants> {
    throw Error('FronteggNative.getConstants not implemented in web');
  }

  async getAuthState(): Promise<FronteggState> {
    throw Error('FronteggNative.getAuthState not implemented in web');
  }

  async login(): Promise<void> {
    throw Error('FronteggNative.login not implemented in web');
  }

  async directLoginAction(payload: { type: string, data: string }): Promise<void> {
    throw Error(
      `FronteggNative.directLoginAction ${payload} not implemented in web`,
    );
  }

  async logout(): Promise<void> {
    throw Error('FronteggNative.logout not implemented in web');
  }

  async switchTenant(payload: { tenantId: string }): Promise<void> {
    throw Error(
      `FronteggNative.switchTenant ${payload} not implemented in web`,
    );
  }

  /**
   * used to initialize the plugin with multiple regions
   * for more information see:
   * iOS: https://github.com/frontegg/frontegg-ios-swift#multi-region-support
   * Android: https://github.com/frontegg/frontegg-android-kotlin#multi-region-support
   */
  async initWithRegion(payload: { regionKey: string }): Promise<void> {
    throw Error(
      `FronteggNative.initWithRegion ${payload} not implemented in web`,
    );
  }

  async refreshToken(): Promise<void> {
    throw Error(`FronteggNative.refreshToken not implemented in web`);
  }
}
