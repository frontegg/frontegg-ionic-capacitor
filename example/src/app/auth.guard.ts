import { CanActivateFn } from '@angular/router';
import { Inject, Injectable } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard {

  constructor(
    @Inject('Frontegg') private fronteggService: FronteggService
  ) {
    /**
     * Listens to $isAuthenticated changes
     * Reload the page to trigger canActivate function again
     */
    this.fronteggService.$isAuthenticated.subscribe(async () => {
      window.location.reload()
    });

  }

  /**
   * Navigate to login page if user is not authenticated
   * @private
   */
  private async navigateToLoginIfNeeded(): Promise<boolean> {
    await this.fronteggService.waitForLoader()
    console.log('checking is authenticated')
    const { isAuthenticated, isLoading } = await this.fronteggService.getNativeState();
    if (!isAuthenticated) {
      console.log('not authenticated')

      // use await to hold application until login is completed or canceled
      await this.fronteggService.directLoginAction('social-login', 'google', false)
      // recheck if user is authenticated after login
      return this.navigateToLoginIfNeeded()
    }
    return true // activate navigation
  }


  canActivate: CanActivateFn = async () => {
    const { isLoading } = await this.fronteggService.getNativeState();

    if (!isLoading) {
      // if showLoader false
      // check if user is authenticated
      return this.navigateToLoginIfNeeded()
    }


    // if showLoader true
    // wait for loader to finish and then check if user is authenticated
    return new Promise<boolean>(async (resolve) => {
      await this.fronteggService.waitForLoader()
      const activated = await this.navigateToLoginIfNeeded()
      resolve(activated)
    })
  }
}
