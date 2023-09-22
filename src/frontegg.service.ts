import { FronteggNativePlugin, FronteggState, SubscribeMap } from './definitions';
import { createObservable } from './observables';
import { registerPlugin } from '@capacitor/core';

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
    'showLoader',
  ]

  constructor() {
    this.state = {
      isAuthenticated: false,
      showLoader: true,
      user: null,
      accessToken: null,
      refreshToken: null,
    }

    this.mapListeners = {
      'isAuthenticated': new Set(),
      'showLoader': new Set(),
      'user': new Set(),
      'accessToken': new Set(),
      'refreshToken': new Set(),
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
          console.log('onFronteggAuthEvent key: ', key)
          this.mapListeners[key].forEach((listener: any) => listener(state[key]))
        }
      });
      this.state = state;
    })

    FronteggNative.getAuthState().then((state: FronteggState) => {
      console.log('getAuthState()', state)

      const keys = Object.keys(this.mapListeners)
      for (let i = 0; i < keys.length; i++) {
        const key = keys[i] as keyof FronteggState
        if (this.state[key] !== state[key]) {
          this.mapListeners[key].forEach((listener: any) => listener(state[key]))
        }
      }
      this.state = state
    })
  }

  public getState() {
    return this.state;
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

  /**
   * Call frontegg native login method
   */
  public login(): void {
    FronteggNative.login();
  }

  public logout(): void {
    FronteggNative.logout();
  }

  public switchTenant(tenantId: string): Promise<void> {
    console.log('test')
    return FronteggNative.switchTenant({ tenantId })
  }
}
