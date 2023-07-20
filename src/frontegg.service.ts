import { FronteggNativePlugin, FronteggState, SubscribeMap } from './definitions';
import { createObservable } from './observables';

export class FronteggService {
  private fronteggNative: FronteggNativePlugin;
  private state: FronteggState = {
    isAuthenticated: false,
    showLoader: true,
    user: null,
    accessToken: null,
    refreshToken: null,
  };
  private mapListeners: SubscribeMap<FronteggState> = {
    'isAuthenticated': new Set(),
    'showLoader': new Set(),
    'user': new Set(),
    'accessToken': new Set(),
    'refreshToken': new Set(),
  }

  constructor(fronteggNative: FronteggNativePlugin) {
    this.fronteggNative = fronteggNative;

    fronteggNative.addListener('onFronteggAuthEvent', (state: FronteggState) => {
      this.state = state;
    })

    this.fronteggNative.getAuthState().then((state: FronteggState) => {
      this.state = state
    })
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
    this.fronteggNative.login();
  }

  public logout(): void {
    this.fronteggNative.logout();
  }
}
