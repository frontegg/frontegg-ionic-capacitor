import type { FronteggState, SubscribeFunc, SubscribeMap } from './definitions';


export const createObservable = <T extends keyof FronteggState>(map: SubscribeMap<FronteggState>, state: FronteggState, key: T) => {
  return {
    value: state[key],
    subscribe(listener: SubscribeFunc<FronteggState, T>) {
      const mapKey = map[key] as Set<SubscribeFunc<FronteggState, T>>
      mapKey.add(listener)
      return () => {
        mapKey.delete(listener)
      }
    }
  }
}
