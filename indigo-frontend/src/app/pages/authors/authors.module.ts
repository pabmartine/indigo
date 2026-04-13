import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DetailSharedModule } from 'src/app/shared/detail-shared.module';
import { AuthorsComponent } from './authors.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: AuthorsComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    AuthorsComponent
  ],
  imports: [
    DetailSharedModule,
    RouterModule.forChild(routes)
  ]
})
export class AuthorsModule { }
