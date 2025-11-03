import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { TieredMenuModule } from 'primeng/tieredmenu';
import { MenuModule } from 'primeng/menu';
import { ToastModule } from 'primeng/toast';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputMaskModule } from 'primeng/inputmask';
import { CheckboxModule } from 'primeng/checkbox';
import { AccordionModule } from 'primeng/accordion';
import { PanelModule } from 'primeng/panel';
import { ProgressBarModule } from 'primeng/progressbar';
import { RatingModule } from 'primeng/rating';
import { TableModule } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputNumberModule } from 'primeng/inputnumber';
import { CalendarModule } from 'primeng/calendar';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { SpeedDialModule } from 'primeng/speeddial';
import { DividerModule } from 'primeng/divider';
import { ChipsModule } from 'primeng/chips';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

// Shared Components
import { DetailComponent } from '../pages/detail/detail.component';
import { AuthorComponent } from '../pages/author/author.component';
import { BookGridComponent } from './book-grid/book-grid.component';

/**
 * Shared Module
 *
 * Contains common modules and components used across the application.
 * Import this module in feature modules to avoid duplicating imports.
 */
@NgModule({
  declarations: [
    DetailComponent,
    AuthorComponent,
    BookGridComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    // PrimeNG
    CardModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    MenuModule,
    TieredMenuModule,
    ToastModule,
    MessagesModule,
    MessageModule,
    InputTextModule,
    InputTextareaModule,
    InputMaskModule,
    CheckboxModule,
    AccordionModule,
    PanelModule,
    ProgressBarModule,
    RatingModule,
    TableModule,
    MultiSelectModule,
    InputNumberModule,
    CalendarModule,
    AutoCompleteModule,
    OverlayPanelModule,
    SpeedDialModule,
    DividerModule,
    ChipsModule,
    ConfirmDialogModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    // PrimeNG
    CardModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    MenuModule,
    TieredMenuModule,
    ToastModule,
    MessagesModule,
    MessageModule,
    InputTextModule,
    InputTextareaModule,
    InputMaskModule,
    CheckboxModule,
    AccordionModule,
    PanelModule,
    ProgressBarModule,
    RatingModule,
    TableModule,
    MultiSelectModule,
    InputNumberModule,
    CalendarModule,
    AutoCompleteModule,
    OverlayPanelModule,
    SpeedDialModule,
    DividerModule,
    ChipsModule,
    ConfirmDialogModule,
    // Shared components
    DetailComponent,
    AuthorComponent,
    BookGridComponent
  ]
})
export class SharedModule { }
