import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { RegionGuard } from './region.guard';
import { SelectRegionComponent } from './select-region/select-region.component';
import { LoginComponent } from './auth/login.component';
import { AuthGuardComponent } from './auth/auth-guard.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [ RegionGuard ],
    children: [
      {
        path: '',
        component: AuthGuardComponent,
        canActivate: [ AuthGuard ],
        loadChildren: () => import('./tabs/tabs.module').then(m => m.TabsPageModule)
      },
    ]
  }, {
    path: 'login',
    component: LoginComponent
  }, {
    path: 'select-region',
    component: SelectRegionComponent
  }

];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {
}
