import { Component, Inject, NgZone, OnDestroy, OnInit } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor'
import { Router } from '@angular/router';

@Component({
  selector: 'app-select-region',
  templateUrl: 'select-region.page.html',
  styleUrls: [ 'select-region.component.css' ],
})
export class SelectRegionComponent implements OnInit, OnDestroy {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService, private router: Router) {
  }

  loading: boolean = false
  unsubscribe: ()=>void = ()=>{}
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

    this.unsubscribe = this.fronteggService.$selectedRegion.subscribe(() => {
      console.log("selected region changed")
      console.log(this.fronteggService.getState())
      if(this.fronteggService.$selectedRegion.value!=null){
        this.router.navigate([ '/' ], { replaceUrl: true,  })
      } else {
        throw new Error('selected region is null')
      }
    })

  }

  ngOnDestroy() {
    this.loading = false
    this.unsubscribe()
  }


  selectRegion(key: string) {
    this.loading=true
    this.fronteggService.initWithRegion(key)
  }
}
