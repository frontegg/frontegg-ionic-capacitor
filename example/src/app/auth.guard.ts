import { ActivatedRouteSnapshot, RouterStateSnapshot, Router, CanActivateFn } from '@angular/router';
import { Injectable, Inject } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {

  constructor(
    @Inject('Frontegg') private fronteggService: FronteggService,
    private router: Router
  ) {
  }

  canActivate: CanActivateFn = async () => {

    await this.fronteggService.waitForLoader()
    const { isAuthenticated, isLoading } = await this.fronteggService.getNativeState()
    console.log('AuthGuard#canActivate called', { isAuthenticated, isLoading });

    if (isAuthenticated) {
      return true;
    } else {
      this.router.navigate([ '/login' ]);
      return false;
    }


  }
}
