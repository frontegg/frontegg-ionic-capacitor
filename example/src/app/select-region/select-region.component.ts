import { Component, Inject, NgZone, OnInit } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor'
import { Router } from '@angular/router';

@Component({
  selector: 'app-select-region',
  templateUrl: 'select-region.page.html',
})
export class SelectRegionComponent implements OnInit {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService, private router: Router) {
  }

  regions: { key: string, baseUrl: string, clientId: string }[] = []

  async ngOnInit() {
    console.log('onInit() select region ')

    const { selectedRegion } = await this.fronteggService.getNativeState()

    if (selectedRegion != null) {
      this.router.navigate([ '/' ], { replaceUrl: true })
      return;
    }
    const { regionData } = await this.fronteggService.getConstants();
    this.regions = regionData ?? []
  }


  selectRegion(key: string) {

    this.fronteggService.initWithRegion(key).then(() => {
      this.ngZone.run(() => {
        this.router.navigate([ '/' ], { replaceUrl: true })
      })
    });
  }
}
