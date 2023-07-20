import { registerPlugin } from '@capacitor/core';
import type { FronteggNativePlugin } from './definitions';
import { FronteggService } from './frontegg.service';

const FronteggNative = registerPlugin<FronteggNativePlugin>('FronteggNative', {
  web: () => import('./web').then(m => new m.FronteggNativeWeb()),
});

export { FronteggState } from './definitions';

const Frontegg = new FronteggService(FronteggNative)

export {
  Frontegg, FronteggNative
}
