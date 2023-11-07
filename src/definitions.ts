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
}


export type SubscribeFunc<T, K extends keyof T> = (value: T[K]) => void
export type SubscribeMap<T> = {
  [K in keyof T]: Set<SubscribeFunc<T, K>>
}


export interface FronteggNativePlugin {
  addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle> & PluginListenerHandle

  getConstants(): Promise<Record<string, string>>;

  getAuthState(): Promise<FronteggState>;

  login(): Promise<void>;

  logout(): void;

  switchTenant(payload:{tenantId: string}): Promise<void>;

  refreshToken(): Promise<void>;

}
