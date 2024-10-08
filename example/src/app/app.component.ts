import { Component, Inject } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: [ 'app.component.scss' ],
})
export class AppComponent {

  isLoading = false;

  constructor(@Inject('Frontegg') private fronteggService: FronteggService,) {

    this.fronteggService.$isLoading.subscribe(() => {
      const {isLoading, selectedRegion} = this.fronteggService.getState();
      this.isLoading = selectedRegion == null ? false : isLoading;
    });
  }
}
