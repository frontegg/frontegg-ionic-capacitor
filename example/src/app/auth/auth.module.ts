import { IonicModule } from '@ionic/angular';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login.component';
import { FormsModule } from '@angular/forms';
import { AuthGuardComponent } from './auth-guard.component';
import { RouterOutlet } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterOutlet,
  ],
  declarations: [ LoginComponent, AuthGuardComponent ]
})
export class AuthModule {
}
