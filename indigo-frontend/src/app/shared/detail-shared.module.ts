import { NgModule } from '@angular/core';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputMaskModule } from 'primeng/inputmask';
import { SpeedDialModule } from 'primeng/speeddial';
import { DividerModule } from 'primeng/divider';
import { ChipsModule } from 'primeng/chips';
import { DetailComponent } from '../pages/detail/detail.component';
import { AuthorComponent } from '../pages/author/author.component';
import { SharedModule } from './shared.module';

@NgModule({
  declarations: [
    DetailComponent,
    AuthorComponent
  ],
  imports: [
    SharedModule,
    InputTextareaModule,
    InputMaskModule,
    SpeedDialModule,
    DividerModule,
    ChipsModule
  ],
  exports: [
    SharedModule,
    InputTextareaModule,
    InputMaskModule,
    SpeedDialModule,
    DividerModule,
    ChipsModule,
    DetailComponent,
    AuthorComponent
  ]
})
export class DetailSharedModule { }
