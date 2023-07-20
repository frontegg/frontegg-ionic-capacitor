import type { ListenerCallback, PluginListenerHandle } from '@capacitor/core';
import type { IUserProfile } from '@frontegg/rest-api';


export interface FronteggState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  user: IUserProfile | null;
  showLoader: boolean;
}


export type SubscribeFunc<T, K extends keyof T> = (value: T[K]) => void
export type SubscribeMap<T> = {
  [K in keyof T]: Set<SubscribeFunc<T, K>>
}


export interface FronteggNativePlugin {
  login(): void;

  logout(): void;

  addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle> & PluginListenerHandle

  getConstants(): Promise<Record<string, string>>;

  getAuthState(): Promise<FronteggState>;

}
