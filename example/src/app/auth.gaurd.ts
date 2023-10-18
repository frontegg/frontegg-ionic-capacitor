import { CanActivateFn } from '@angular/router';
import { Inject, Injectable } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard {

  constructor(@Inject('Frontegg') private fronteggService: FronteggService) {

    /**
     * Listens to $isAuthenticated changes
     * Reload the page to trigger canActivate function again
     */
    this.fronteggService.$isAuthenticated.subscribe(async () => {
      window.location.reload()
    });
  }

  /**
   * Wait for loader to finish
   * @private
   */
  private waitForLoader() {
    return new Promise((resolve) => {
      const unsubscribe = this.fronteggService.$isLoading.subscribe((isLoading) => {
        if (!isLoading) {
          resolve(true);
          unsubscribe();
        }
      });
    })
  }

  /**
   * Navigate to login page if user is not authenticated
   * @private
   */
  private async navigateToLoginIfNeeded(): Promise<boolean> {
    const { isAuthenticated } = this.fronteggService.getState();
    if (!isAuthenticated) {
      await this.fronteggService.login()
      return false // prevent navigation
    }
    return true // activate navigation
  }


  canActivate: CanActivateFn = () => {
    const { showLoader } = this.fronteggService.getState();

    if (!showLoader) {
      // if showLoader false
      // check if user is authenticated
      return this.navigateToLoginIfNeeded()
    }


    // if showLoader true
    // wait for loader to finish and then check if user is authenticated
    return new Promise<boolean>(async (resolve) => {
      await this.waitForLoader()
      const activated = await this.navigateToLoginIfNeeded()
      resolve(activated)
    })
  }
}
