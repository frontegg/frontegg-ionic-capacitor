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

    /**
     * Listens to application visibility changes
     * Reload the page to trigger canActivate
     * when application returns from login page without authentication
     */
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && !this.fronteggService.getState().isAuthenticated) {
        window.location.reload()
      }
    });
  }

  /**
   * Wait for loader to finish
   * @private
   */
  private waitForLoader() {
    return new Promise((resolve) => {
      console.log('checking is loading')
      if (!this.fronteggService.$isLoading.value) {
        resolve(true)
      }
      console.log('isLoading is true, waiting for it to be false')
      const unsubscribe = this.fronteggService.$isLoading.subscribe((isLoading) => {
        console.log('isLoading', isLoading)

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
    await this.waitForLoader()
    console.log('checking is authenticated')
    const { isAuthenticated, isLoading } = this.fronteggService.getState();
    if (!isAuthenticated) {
      console.log('not authenticated')

      // use await to hold application until login is completed or canceled
      await this.fronteggService.login()

      // recheck if user is authenticated after login
      return this.navigateToLoginIfNeeded()
    }
    return true // activate navigation
  }


  canActivate: CanActivateFn = () => {
    const { isLoading } = this.fronteggService.getState();

    if (!isLoading) {
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
