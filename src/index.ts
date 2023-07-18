import { registerPlugin } from '@capacitor/core';

import type { FronteggNativePlugin } from './definitions';

const FronteggNative = registerPlugin<FronteggNativePlugin>('FronteggNative', {
  web: () => import('./web').then(m => new m.FronteggNativeWeb()),
});

export * from './definitions';



export { FronteggNative };
