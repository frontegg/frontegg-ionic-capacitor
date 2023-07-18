export interface FronteggNativePlugin {
  login(): void;
  logout(): void;
  subscribe(): void;
  getConstants(): Promise<Record<string, string>>;
}
