import { registerPlugin } from '@capacitor/core';

import type { FronteggConstants, FronteggNativePlugin, FronteggState, SubscribeMap } from './definitions';
import { createObservable } from './observables';


const FronteggNative = registerPlugin<FronteggNativePlugin>('FronteggNative', {
  web: () => import('./web').then(m => new m.FronteggNativeWeb()),
});


export class FronteggService {
  private state: FronteggState;
  private mapListeners: SubscribeMap<FronteggState>;
  private readonly orderedListenerKeys: (keyof FronteggState)[] = [
    'refreshToken',
    'accessToken',
    'user',
    'isAuthenticated',
    'selectedRegion',
    'showLoader',
  ]

  constructor() {
    this.state = {
      isAuthenticated: false,
      showLoader: true,
      user: null,
      accessToken: null,
      refreshToken: null,
      selectedRegion: null,
    }

    this.mapListeners = {
      'isAuthenticated': new Set(),
      'showLoader': new Set(),
      'user': new Set(),
      'accessToken': new Set(),
      'refreshToken': new Set(),
      'selectedRegion': new Set(),
    }
    FronteggNative.addListener('onFronteggAuthEvent', (state: FronteggState) => {
      console.log('onFronteggAuthEvent', {
        isAuthenticated: state.isAuthenticated,
        showLoader: state.showLoader,
        user: `${state.user}`, // prevent log full user object // null | undefined | [object Object]
        accessToken: state.accessToken && state.accessToken.length > 50 ? `${state.accessToken.slice(0, 50)}...` : state.accessToken,
        refreshToken: state.refreshToken,
      })

      const keys = this.orderedListenerKeys;
      keys.forEach(key => {
        if (this.state[key] !== state[key]) {
          console.log('onFronteggAuthEvent key: ', key);
          (this.state as any)[key] = state[key];
          this.mapListeners[key].forEach((listener: any) => listener(state[key]))
        }
      });
      this.state = state;
    })

    FronteggNative.getAuthState().then((state: FronteggState) => {
      console.log('getAuthState()', state)

      const keys = Object.keys(this.mapListeners)
      for (const item of keys) {
        const key = item as keyof FronteggState
        if (this.state[key] !== state[key]) {
          (this.state as any)[key] = state[key]
          this.mapListeners[key].forEach((listener: any) => listener(state[key]))
        }
      }
    })
  }

  public getState() {
    return this.state;
  }

  public getNativeState(): Promise<FronteggState> {
    return FronteggNative.getAuthState();
  }

  public get $isLoading() {
    return createObservable(this.mapListeners, this.state, 'showLoader')
  }

  public get $isAuthenticated() {
    return createObservable(this.mapListeners, this.state, 'isAuthenticated')
  }

  public get $user() {
    return createObservable(this.mapListeners, this.state, 'user')
  }

  public get $accessToken() {
    return createObservable(this.mapListeners, this.state, 'accessToken')
  }

  public get $selectedRegion() {
    return createObservable(this.mapListeners, this.state, 'selectedRegion')
  }

  /**
   * Call frontegg native login method
   */
  public login(): Promise<void> {
    return FronteggNative.login();
  }

  public logout(): void {
    FronteggNative.logout();
  }

  public getConstants(): Promise<FronteggConstants> {
    return FronteggNative.getConstants();
  }

  public switchTenant(tenantId: string): Promise<void> {
    return FronteggNative.switchTenant({ tenantId })
  }

  /**
   * used to initialize the plugin with multiple regions
   * for more information see:
   * iOS: https://github.com/frontegg/frontegg-ios-swift#multi-region-support
   * Android: https://github.com/frontegg/frontegg-android-kotlin#multi-region-support
   */
  public initWithRegion(regionKey: string): Promise<void> {
    return FronteggNative.initWithRegion({ regionKey })
  }

  public refreshToken(): Promise<void> {
    return FronteggNative.refreshToken()
  }
}
