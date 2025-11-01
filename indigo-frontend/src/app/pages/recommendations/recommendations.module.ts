import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { RecommendationsComponent } from './recommendations.component';
import { AuthGuard } from 'src/app/utils/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: RecommendationsComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    RecommendationsComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class RecommendationsModule { }
