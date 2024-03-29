import { registerPlugin } from '@capacitor/core';

import type {
  FronteggConstants,
  FronteggNativePlugin,
  FronteggState,
  SubscribeMap,
} from './definitions';
import type { FronteggObservable } from './observables';
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
  ];

  constructor() {
    this.state = {
      isAuthenticated: false,
      showLoader: true,
      user: null,
      accessToken: null,
      refreshToken: null,
      selectedRegion: null,
    };

    this.mapListeners = {
      isAuthenticated: new Set(),
      showLoader: new Set(),
      user: new Set(),
      accessToken: new Set(),
      refreshToken: new Set(),
      selectedRegion: new Set(),
    };
    FronteggNative.addListener(
      'onFronteggAuthEvent',
      (state: FronteggState) => {
        console.log('onFronteggAuthEvent', {
          isAuthenticated: state.isAuthenticated,
          showLoader: state.showLoader,
          user: `${state.user}`, // prevent log full user object // null | undefined | [object Object]
          accessToken:
            state.accessToken && state.accessToken.length > 50
              ? `${state.accessToken.slice(0, 50)}...`
              : state.accessToken,
          refreshToken: state.refreshToken,
          selectedRegion: state.selectedRegion,
        });

        const keys = this.orderedListenerKeys;
        keys.forEach(key => {
          if (this.isChanged(this.state[key], state[key])) {
            console.log('onFronteggAuthEvent key: ', key);
            (this.state as any)[key] = state[key];
            this.mapListeners[key].forEach((listener: any) =>
              listener(state[key]),
            );
          }
        });
        this.state = state;
      },
    );

    FronteggNative.getAuthState().then((state: FronteggState) => {
      console.log('getAuthState()', state);

      const keys = Object.keys(this.mapListeners);
      for (const item of keys) {
        const key = item as keyof FronteggState;
        if (this.isChanged(this.state[key], state[key])) {
          (this.state as any)[key] = state[key];
          this.mapListeners[key].forEach((listener: any) =>
            listener(state[key]),
          );
        }
      }
    });
  }

  private isChanged(value1: any, value2: any): boolean {
    if (value1 == value2) {
      return false;
    }
    return JSON.stringify(value1) != JSON.stringify(value2);
  }

  public getState(): FronteggState {
    return this.state;
  }

  public getNativeState(): Promise<FronteggState> {
    return FronteggNative.getAuthState();
  }

  public get $isLoading(): FronteggObservable<'showLoader'> {
    return createObservable(this.mapListeners, this.state, 'showLoader');
  }

  public get $isAuthenticated(): FronteggObservable<'isAuthenticated'> {
    return createObservable(this.mapListeners, this.state, 'isAuthenticated');
  }

  public get $user(): FronteggObservable<'user'> {
    return createObservable(this.mapListeners, this.state, 'user');
  }

  public get $accessToken(): FronteggObservable<'accessToken'> {
    return createObservable(this.mapListeners, this.state, 'accessToken');
  }

  public get $selectedRegion(): FronteggObservable<'selectedRegion'> {
    return createObservable(this.mapListeners, this.state, 'selectedRegion');
  }

  /**
   * Call frontegg native login method
   */
  public login(): Promise<void> {
    return FronteggNative.login();
  }

  /**
   * Used to log in with social login provider directly without visiting the login page
   * @param type - the direct login type (direct, social-login, custom-social-login)
   * @param data - the direct login data (for direct it's saml url request, for social-login it's the provider name, for custom-social-login it's the provider entity id)
   */
  public directLoginAction(type: string, data: string): Promise<void> {
    return FronteggNative.directLoginAction({ type, data });
  }

  public logout(): void {
    FronteggNative.logout();
  }

  public getConstants(): Promise<FronteggConstants> {
    return FronteggNative.getConstants();
  }

  public switchTenant(tenantId: string): Promise<void> {
    return FronteggNative.switchTenant({ tenantId });
  }

  /**
   * used to initialize the plugin with multiple regions
   * for more information see:
   * iOS: https://github.com/frontegg/frontegg-ios-swift#multi-region-support
   * Android: https://github.com/frontegg/frontegg-android-kotlin#multi-region-support
   */
  public initWithRegion(regionKey: string): Promise<void> {
    // check if current region is the same as the new region
    if (this.getState().selectedRegion === regionKey) {
      return Promise.resolve();
    }
    return FronteggNative.initWithRegion({ regionKey });
  }

  public refreshToken(): Promise<void> {
    return FronteggNative.refreshToken();
  }
}
