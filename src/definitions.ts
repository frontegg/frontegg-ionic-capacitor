
import type { ListenerCallback, PluginListenerHandle } from '@capacitor/core';
import type { ITenantsResponse, IUserProfile } from '@frontegg/rest-api';

export type User = IUserProfile & {
  tenants: ITenantsResponse[];
  activeTenant: ITenantsResponse
}

export interface FronteggState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  user: User | null;
  showLoader: boolean;
  selectedRegion: string | null;
}


export type SubscribeFunc<T, K extends keyof T> = (value: T[K]) => void
export type SubscribeMap<T> = {
  [K in keyof T]: Set<SubscribeFunc<T, K>>
}


export interface FronteggConstants {
  baseUrl: string;
  clientId: string;
  bundleId: string;
  isRegional: boolean;
  regionData?: { key: string, baseUrl: string, clientId: string }[]
}

export interface FronteggNativePlugin {
  addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle> & PluginListenerHandle

  getConstants(): Promise<FronteggConstants>;

  getAuthState(): Promise<FronteggState>;

  login(): Promise<void>;

  logout(): void;

  switchTenant(payload: { tenantId: string }): Promise<void>;

  /**
   * used to initialize the plugin with multiple regions
   * for more information see:
   * iOS: https://github.com/frontegg/frontegg-ios-swift#multi-region-support
   * Android: https://github.com/frontegg/frontegg-android-kotlin#multi-region-support
   */
  initWithRegion(payload: { regionKey: string }): Promise<void>;

  refreshToken(): Promise<void>;

}



export type RegionConfig = {
  key: string;
  baseUrl: string;
  clientId: string;
};



type FronteggNativeStandardOptions = {
  baseUrl: string;
  clientId: string;

}
type FronteggNativeRegionOptions = {
  /**
   * This is an array of regions to be used as frontegg app.
   *
   * @since 1.0.0
   * @example [{key: "us", baseUrl: "https://us-api.frontegg.com", clientId: "us-client-id"}]
   */
  regions?: RegionConfig[];
}
type FronteggNativeOptions = (FronteggNativeStandardOptions | FronteggNativeRegionOptions) & {
  handleLoginWithSocialLogin?: boolean;
  handleLoginWithSSO?: boolean;
}
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
declare module "@capacitor/cli" {
  export interface PluginsConfig {
    /**
     * You can configure the way the push notifications are displayed when the app is in foreground.
     */
    FronteggNative?: FronteggNativeOptions;
  }
}
