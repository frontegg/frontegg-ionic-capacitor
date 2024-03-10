import { Component, Inject, NgZone, OnInit } from '@angular/core';
import { FronteggService, FronteggState } from '@frontegg/ionic-capacitor';
import { ITenantsResponse } from '@frontegg/rest-api';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: [ 'tab2.page.scss' ]
})
export class Tab2Page implements OnInit {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService) {
  }

  switching: string = '';
  user: FronteggState['user'] = null
  tenants: ITenantsResponse[] = []
  activeTenantId: string | undefined;

  ngOnInit() {
    const byName = (a: any, b: any) => a.name.localeCompare(b.name)
    this.user = this.fronteggService.getState().user;
    this.tenants = (this.user?.tenants || []).sort(byName);
    this.activeTenantId = this.user?.tenantId
    this.fronteggService.$user.subscribe((user) => {
      this.ngZone.run(() => {
        this.user = user
        this.tenants = (user?.tenants || []).sort(byName);
        this.activeTenantId = user?.tenantId
      })
    })
  }

  switchTenant(tenantId: string) {
    this.switching = tenantId;
    this.fronteggService.switchTenant(tenantId).then(() => {
      this.switching = ''
    })
  }

}
