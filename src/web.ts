import { WebPlugin } from '@capacitor/core';

import type { FronteggNativePlugin, FronteggState } from './definitions';

export class FronteggNativeWeb
  extends WebPlugin
  implements FronteggNativePlugin {

  async login(): Promise<void> {
    throw Error('FronteggNative.login not implemented in web')
  }

  async logout(): Promise<void> {
    throw Error('FronteggNative.logout not implemented in web')
  }

  async switchTenant(payload: { tenantId: string }): Promise<void> {
    throw Error(`FronteggNative.switchTenant ${payload} not implemented in web, `)
  }

  async getConstants(): Promise<Record<string, string>> {
    throw Error('FronteggNative.getConstants not implemented in web')
  }

  async getAuthState(): Promise<FronteggState> {
    throw Error('FronteggNative.getAuthState not implemented in web')
  }
}
