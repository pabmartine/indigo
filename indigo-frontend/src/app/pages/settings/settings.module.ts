import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { SettingsComponent } from './settings.component';
import { AdminGuard } from 'src/app/utils/admin.guard';
import { MetadataHistoryComponent } from './metadata-history/metadata-history.component';

const routes: Routes = [
  { path: 'metadata-history', component: MetadataHistoryComponent, canActivate: [AdminGuard] },
  {
    path: '',
    component: SettingsComponent,
    canActivate: [AdminGuard]
  }
];

@NgModule({
  declarations: [
    SettingsComponent,
    MetadataHistoryComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class SettingsModule { }
