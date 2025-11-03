import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { SettingsComponent } from './settings.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: SettingsComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    SettingsComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class SettingsModule { }
