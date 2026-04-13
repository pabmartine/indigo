import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DetailSharedModule } from 'src/app/shared/detail-shared.module';
import { ProfileComponent } from './profile.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: ProfileComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    ProfileComponent
  ],
  imports: [
    DetailSharedModule,
    RouterModule.forChild(routes)
  ]
})
export class ProfileModule { }
