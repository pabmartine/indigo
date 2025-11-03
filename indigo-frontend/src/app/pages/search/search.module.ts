import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { SearchComponent } from './search.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: SearchComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    SearchComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class SearchModule { }
