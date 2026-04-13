import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DetailSharedModule } from 'src/app/shared/detail-shared.module';
import { BooksComponent } from './books.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: BooksComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    BooksComponent
  ],
  imports: [
    DetailSharedModule,
    RouterModule.forChild(routes)
  ]
})
export class BooksModule { }
