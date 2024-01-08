import type { FronteggState, SubscribeFunc, SubscribeMap } from './definitions';

export interface FronteggObservable<T extends keyof FronteggState> {
  value: FronteggState[T];
  subscribe: (listener: SubscribeFunc<FronteggState, T>) => () => void;
}

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const createObservable = <T extends keyof FronteggState>(
  map: SubscribeMap<FronteggState>,
  state: FronteggState,
  key: T,
): FronteggObservable<T> => {
  return {
    value: state[key],
    subscribe(listener: SubscribeFunc<FronteggState, T>) {
      const mapKey = map[key] as Set<SubscribeFunc<FronteggState, T>>;
      mapKey.add(listener);
      return () => {
        mapKey.delete(listener);
      };
    },
  };
};
