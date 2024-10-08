import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FronteggService, LogLevel } from '@frontegg/ionic-capacitor';
import { SelectRegionModule } from './select-region/select-region.module';
import { AuthModule } from './auth/auth.module';

@NgModule({
  declarations: [ AppComponent ],
  imports: [ BrowserModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    SelectRegionModule,
    AuthModule
  ],
  providers: [ {
    provide: 'Frontegg',
    useValue: new FronteggService({ logLevel: LogLevel.INFO }),
  }, {
    provide: RouteReuseStrategy,
    useClass: IonicRouteStrategy
  } ],
  bootstrap: [ AppComponent ],
})
export class AppModule {
}
