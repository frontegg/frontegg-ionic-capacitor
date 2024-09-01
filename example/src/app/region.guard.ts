import { CanActivateFn, Router } from '@angular/router';
import { Inject, Injectable } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Injectable({
  providedIn: 'root'
})
export class RegionGuard {

  constructor(@Inject('Frontegg') private fronteggService: FronteggService, private router: Router) {

    /**
     * Listens to $isAuthenticated changes
     * Reload the page to trigger canActivate function again
     */
    this.fronteggService.$selectedRegion.subscribe(async () => {
      console.log("selected region changed")
      window.location.reload()
    });
  }

  canActivate: CanActivateFn = async () => {
    const { isRegional } = await this.fronteggService.getConstants();
    const nativeState = await this.fronteggService.getNativeState()

    if (!isRegional || nativeState.selectedRegion != null) {
      return true
    }

    return this.router.navigate([ '/select-region' ])
  }
}
