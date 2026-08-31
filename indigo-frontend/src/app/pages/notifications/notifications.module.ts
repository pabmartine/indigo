import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { NotificationsComponent } from './notifications.component';
import { AdminGuard } from 'src/app/utils/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: NotificationsComponent,
    canActivate: [AdminGuard]
  }
];

@NgModule({
  declarations: [
    NotificationsComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class NotificationsModule { }
