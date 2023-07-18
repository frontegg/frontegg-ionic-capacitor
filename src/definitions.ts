import type { ListenerCallback, PluginListenerHandle } from '@capacitor/core';
import type { IUserProfile } from '@frontegg/rest-api';


export interface FronteggState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  user: IUserProfile | null;
  initializing: boolean;
  showLoader: boolean;
  logout: () => void;
  login: () => void;
}

export interface FronteggNativePlugin {
  login(): void;

  logout(): void;

  addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle> & PluginListenerHandle

  getConstants(): Promise<Record<string, string>>;

}
