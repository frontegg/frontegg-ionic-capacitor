import type { ListenerCallback, PluginListenerHandle } from '@capacitor/core';
import type { ITenantsResponse, IUserProfile } from '@frontegg/rest-api';

import type { LogLevel } from './logger';

export type User = IUserProfile & {
  tenants: ITenantsResponse[];
  activeTenant: ITenantsResponse;
};

/**
 * Represents the state of the Frontegg authentication.
 */
export interface FronteggState {
  /**
   * The access token used for authenticating API requests.
   * This token is typically a JWT and should be refreshed periodically before it expires.
   * It is `null` when the user is not authenticated.
   */
  accessToken: string | null;

  /**
   * The refresh token used to obtain a new access token.
   * This token is used when the access token has expired and a new token is required.
   * It is `null` when the user is not authenticated.
   */
  refreshToken: string | null;

  /**
   * Indicates whether the user is currently authenticated.
   * It is `true` if the user has successfully logged in and holds a valid access token; otherwise, `false`.
   */
  isAuthenticated: boolean;

  /**
   * Represents the current authenticated user.
   * It is an object containing user details if the user is authenticated; otherwise, `null`.
   */
  user: User | null;

  /**
   * Controls whether the loader or spinner should be displayed.
   * This is `true` when the application is loading or performing an action that requires user feedback.
   * @deprecated use isLoading instead
   */
  showLoader: boolean;

  /**
   * Controls whether the loader or spinner should be displayed.
   * This is `true` when the application is loading or performing an action that requires user feedback.
   */
  isLoading: boolean;

  /**
   * The selected region configuration.
   * This is typically used in multi-regional setups where different regions have different configurations.
   * It is `null` if no region is selected or if the application is not configured for multiple regions.
   */
  selectedRegion: string | null;

  /**
   * Indicates whether a token refresh operation is currently in progress.
   * This is `true` when the refresh token is being used to obtain a new access token.
   */
  refreshingToken: boolean;

  /**
   * Indicates whether the application is in the initialization phase.
   * This is `true` when the application is initializing, such as during the initial load or when setting up the authentication state.
   */
  initializing: boolean;
}


export type SubscribeFunc<T, K extends keyof T> = (value: T[K]) => void;
export type SubscribeMap<T> = {
  [K in keyof T]: Set<SubscribeFunc<T, K>>;
};

/**
 * Represents the constant configuration values used by the Frontegg authentication module.
 */
export interface FronteggConstants {
  /**
   * The base URL of the Frontegg API.
   * This is the primary endpoint for all API requests.
   */
  baseUrl: string;

  /**
   * The client ID used for authenticating API requests.
   * This is a unique identifier for the client application.
   */
  clientId: string;

  /**
   * The application ID associated with the Frontegg setup.
   * It may be `null` if the application ID is not required.
   */
  applicationId: string | null;

  /**
   * The bundle ID of the application.
   * This is typically used to uniquely identify the application in a multi-app environment.
   */
  bundleId: string;

  /**
   * Indicates whether the application is configured for regional deployment.
   * This is `true` if the application supports multiple regions, otherwise `false`.
   */
  isRegional: boolean;

  /**
   * The data associated with each region.
   * This is an optional array containing configuration details for each region, including the region key, base URL, and client ID.
   * It is present only if `isRegional` is `true`.
   */
  regionData?: { key: string; baseUrl: string; clientId: string }[];
}

/**
 * Represents the Frontegg Native Plugin interface that provides methods for interacting with the Frontegg authentication system.
 */
export interface FronteggNativePlugin {
  /**
   * Adds an event listener for the specified event name.
   * @param eventName - The name of the event to listen for.
   * @param listenerFunc - The callback function that will be called when the event is triggered.
   * @returns A promise that resolves to a PluginListenerHandle, which can be used to manage the listener.
   */
  addListener(
    eventName: string,
    listenerFunc: ListenerCallback,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Retrieves the constant configuration values used by the Frontegg authentication module.
   * @returns A promise that resolves to an object containing the FronteggConstants.
   */
  getConstants(): Promise<FronteggConstants>;

  /**
   * Retrieves the current authentication state.
   * @returns A promise that resolves to an object containing the current FronteggState.
   */
  getAuthState(): Promise<FronteggState>;

  /**
   * Initiates the login process.
   * @returns A promise that resolves when the login process is completed.
   */
  login(): Promise<void>;

  /**
   * Used to log in with a social login provider directly without visiting the login page.
   * @param payload - The payload containing the details for the direct login action.
   * @param payload.type - The direct login type (e.g., direct, social-login, custom-social-login).
   * @param payload.data - The direct login data (e.g., SAML URL request, provider name, provider entity ID).
   * @param payload.ephemeralSession - If true, the session will be ephemeral and will not be saved in the browser.
   * @returns A promise that resolves when the direct login action is completed.
   */
  directLoginAction(payload: { type: string; data: string; ephemeralSession: boolean }): Promise<boolean>;

  /**
   * Logs out the current user.
   * This method does not return a promise, and the logout process is performed asynchronously.
   */
  logout(): void;

  /**
   * Switches the current tenant in the application.
   * @param payload - The payload containing the tenant ID to switch to.
   * @returns A promise that resolves when the tenant switch is completed.
   */
  switchTenant(payload: { tenantId: string }): Promise<void>;

  /**
   * Initializes the plugin with the specified region.
   * This method is used to set up the plugin for multi-region support.
   * For more information, see the documentation for iOS and Android multi-region support.
   * @param payload - The payload containing the region key to initialize.
   * @returns A promise that resolves when the initialization with the region is completed.
   */
  initWithRegion(payload: { regionKey: string }): Promise<void>;

  /**
   * Refreshes the authentication token.
   * @returns A promise that resolves when the token refresh is completed.
   */
  refreshToken(): Promise<void>;
}

/**
 * Represents the configuration for a specific region.
 * This is used in multi-regional setups where different regions have different configurations.
 */
export type RegionConfig = {
  /**
   * The unique key identifying the region.
   */
  key: string;

  /**
   * The base URL for the API endpoint in this region.
   */
  baseUrl: string;

  /**
   * The client ID used for authenticating API requests in this region.
   */
  clientId: string;

  /**
   * The application ID associated with this region.
   * This is optional and may be undefined.
   */
  applicationId?: string;
};

/**
 * Represents the standard options for initializing the Frontegg Native Plugin.
 */
type FronteggNativeStandardOptions = {
  /**
   * The base URL of the Frontegg API.
   */
  baseUrl: string;

  /**
   * The client ID used for authenticating API requests.
   */
  clientId: string;

  /**
   * The application ID associated with the Frontegg setup.
   * This is optional and may be undefined.
   */
  applicationId?: string;
};

type FronteggNativeRegionOptions = {
  /**
   * This is an array of regions to be used as frontegg app.
   *
   * @since 1.0.0
   * @example [{key: "us", baseUrl: "https://us-api.frontegg.com", clientId: "us-client-id"}]
   */
  regions: RegionConfig[];
};
type FronteggNativeOptions = (
  | FronteggNativeStandardOptions
  | FronteggNativeRegionOptions
  ) & {
  /**
   * Weather to handle login with social login in external browser.
   * If set to false, the plugin will navigate to the social login page with application webview.
   *
   * NOTE: some of the social login providers prevent login from embedded webview.
   *
   *  @default true
   */
  handleLoginWithSocialLogin?: boolean;
  /**
   *  Weather to handle login with SSO in external browser.
   *  If set to false, the plugin will navigate to the sso page with application webview.
   *
   *  @default false
   */
  handleLoginWithSSO?: boolean;
  /**
   * Weather to use the assetlinks to for oauth/callback, this is the default behavior.
   * disabling this will cause the plugin to use custom url scheme for oauth/callback.
   *
   * NOTE: custom url scheme require user interaction to return to the app.
   * @default true
   */
  useAssetLinks?: boolean;

  /**
   * Weather to To enable social login via Chrome Custom Tabs, set the useChromeCustomTabs flag to true.
   * By default, the SDK uses the Chrome browser for social login.
   *
   * NOTE: custom url scheme require user interaction to return to the app.
   * @default true
   */
  useChromeCustomTabs?: boolean;
};

export interface FronteggServiceOptions  {
  logLevel: LogLevel
}

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
declare module '@capacitor/cli' {
  export interface PluginsConfig {
    /**
     * You can configure the way the push notifications are displayed when the app is in foreground.
     */
    FronteggNative?: FronteggNativeOptions;
  }
}
