import { registerPlugin } from '@capacitor/core';

import type {
  FronteggConstants,
  FronteggNativePlugin,
  FronteggServiceOptions,
  FronteggState,
  SubscribeMap,
} from './definitions';
import { Logger } from './logger';
import type { FronteggObservable } from './observables';
import { createObservable } from './observables';

const FronteggNative = registerPlugin<FronteggNativePlugin>('FronteggNative', {
  web: () => import('./web').then(m => new m.FronteggNativeWeb()),
});

export class FronteggService {
  private state: FronteggState;
  private logger: Logger;
  private mapListeners: SubscribeMap<FronteggState>;
  private readonly orderedListenerKeys: (keyof FronteggState)[] = [
    'refreshToken',
    'accessToken',
    'user',
    'isAuthenticated',
    'selectedRegion',
    'showLoader',
    'refreshingToken',
    'initializing',
    'isLoading',
  ];

  constructor(options?: FronteggServiceOptions) {
    this.logger = new Logger(options?.logLevel);

    this.logger.info('FronteggService created');
    this.state = {
      isAuthenticated: false,
      showLoader: true,
      isLoading: true,
      user: null,
      accessToken: null,
      refreshToken: null,
      selectedRegion: null,
      refreshingToken: false,
      initializing: true,
    };

    this.mapListeners = {
      isAuthenticated: new Set(),
      showLoader: new Set(),
      isLoading: new Set(),
      user: new Set(),
      accessToken: new Set(),
      refreshToken: new Set(),
      selectedRegion: new Set(),
      refreshingToken: new Set(),
      initializing: new Set(),
    };

    FronteggNative.addListener(
      'onFronteggAuthEvent',
      (state: FronteggState) => {
        this.logger.info(
          'onFronteggAuthEvent',
          JSON.stringify({
            isAuthenticated: state.isAuthenticated,
            showLoader: state.isLoading,
            isLoading: state.isLoading,
            user: `${state.user}`, // prevent log full user object // null | undefined | [object Object]
            accessToken: state.accessToken ? '****' : null,
            refreshToken: state.refreshToken,
            selectedRegion: state.selectedRegion,
            refreshingToken: state.refreshingToken,
            initializing: state.initializing,
          }),
        );

        const keys = this.orderedListenerKeys;
        keys.forEach(key => {
          if (this.isChanged(this.state[key], state[key])) {
            this.logger.info('onFronteggAuthEvent', key, state[key]);
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
      this.logger.info(`getAuthState(): \n ${JSON.stringify(state)} DONE`);

      const keys = Object.keys(this.mapListeners);
      for (const item of keys.filter(key => key !== 'initializing')) {
        const key = item as keyof FronteggState;

        (this.state as any)[key] = state[key];
        this.mapListeners[key].forEach((listener: any) => listener(state[key]));
      }

      this.state.initializing = false;
      this.mapListeners.initializing.forEach((listener: any) =>
        listener(false),
      );
    });
  }

  private isChanged(obj1: any, obj2: any): boolean {
    // Strict equality check
    if (obj1 === obj2) {
      return false;
    }

    // Check for null and undefined
    if (
      obj1 === null ||
      obj1 === undefined ||
      obj2 === null ||
      obj2 === undefined
    ) {
      return obj1 !== obj2;
    }

    // Handle primitive types (number, string, boolean)
    if (typeof obj1 !== 'object' && typeof obj2 !== 'object') {
      return obj1 !== obj2;
    }

    // Handle arrays
    if (Array.isArray(obj1) && Array.isArray(obj2)) {
      if (obj1.length !== obj2.length) {
        return true;
      }
      for (let i = 0; i < obj1.length; i++) {
        if (this.isChanged(obj1[i], obj2[i])) {
          return true;
        }
      }
      return false;
    }

    // Handle objects
    if (typeof obj1 === 'object' && typeof obj2 === 'object') {
      const keys1 = Object.keys(obj1);
      const keys2 = Object.keys(obj2);

      if (keys1.length !== keys2.length) {
        return true;
      }

      for (const key of keys1) {
        if (!keys2.includes(key) || this.isChanged(obj1[key], obj2[key])) {
          return true;
        }
      }
      return false;
    }

    // If none of the above conditions match, the values have changed
    return true;
  }

  public getState(): FronteggState {
    return this.state;
  }

  public getNativeState(): Promise<FronteggState> {
    return FronteggNative.getAuthState();
  }

  public get $isLoading(): FronteggObservable<'isLoading'> {
    return createObservable(this.mapListeners, this.state, 'isLoading');
  }

  // @deprecated use $isLoading instead
  public get $showLoader(): FronteggObservable<'showLoader'> {
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

  public get $refreshingToken(): FronteggObservable<'refreshingToken'> {
    return createObservable(this.mapListeners, this.state, 'refreshingToken');
  }

  public get $selectedRegion(): FronteggObservable<'selectedRegion'> {
    return createObservable(this.mapListeners, this.state, 'selectedRegion');
  }

  /**
   * Call frontegg native login method
   */
  public login(loginHint?: string): Promise<void> {
    return FronteggNative.login({ loginHint });
  }

  /**
   * Wait for loader to finish
   * @private
   */
  public async waitForLoader(): Promise<boolean> {
    // eslint-disable-next-line no-async-promise-executor
    return new Promise<boolean>(async resolve => {
      console.log('checking is loading');
      const state = await this.getNativeState();
      let isLoading = state.isLoading;

      console.log('checking is loading', JSON.stringify({ isLoading }));
      if (!isLoading) {
        resolve(true);
        return;
      }
      console.log('isLoading is true, waiting for it to be false');
      while (isLoading) {
        const { isLoading: newIsLoading } = await this.getNativeState();
        if (!newIsLoading) {
          resolve(true);
          return;
        }
        isLoading = newIsLoading;
        await new Promise(r => setTimeout(r, 100));
      }
    });
  }

  /**
   * Used to log in with social login provider directly without visiting the login page
   * @param type - the direct login type (direct, social-login, custom-social-login)
   * @param data - the direct login data (for direct it's saml url request, for social-login it's the provider name, for custom-social-login it's the provider entity id)
   * @param ephemeralSession - if true, the session will be ephemeral and will not be saved in the browser
   */
  public async directLoginAction(
    type: string,
    data: string,
    ephemeralSession = true,
  ): Promise<boolean> {
    const state = await this.getNativeState();
    console.log('direct login action', state);
    await this.waitForLoader();
    if (state.isAuthenticated) {
      return true;
    }

    this.state.isLoading = true;
    this.mapListeners.isLoading.forEach((listener: any) => listener(true));
    return FronteggNative.directLoginAction({ type, data, ephemeralSession });
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
