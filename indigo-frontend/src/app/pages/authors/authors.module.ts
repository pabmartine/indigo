import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
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
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class AuthorsModule { }
