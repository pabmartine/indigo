import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ScrollingModule } from '@angular/cdk/scrolling';

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
import { CheckboxModule } from 'primeng/checkbox';
import { PanelModule } from 'primeng/panel';
import { ProgressBarModule } from 'primeng/progressbar';
import { RatingModule } from 'primeng/rating';
import { TableModule } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputNumberModule } from 'primeng/inputnumber';
import { CalendarModule } from 'primeng/calendar';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PasswordModule } from 'primeng/password';

// Shared Components
import { BookGridComponent } from './book-grid/book-grid.component';
import { LazyImageDirective } from './directives/lazy-image.directive';

/**
 * Shared Module
 *
 * Contains common modules and components used across the application.
 * Import this module in feature modules to avoid duplicating imports.
 */
@NgModule({
  declarations: [
    BookGridComponent,
    LazyImageDirective
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ScrollingModule,
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
    PasswordModule,
    CheckboxModule,
    PanelModule,
    ProgressBarModule,
    RatingModule,
    TableModule,
    MultiSelectModule,
    InputNumberModule,
    CalendarModule,
    OverlayPanelModule,
    ConfirmDialogModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ScrollingModule,
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
    PasswordModule,
    CheckboxModule,
    PanelModule,
    ProgressBarModule,
    RatingModule,
    TableModule,
    MultiSelectModule,
    InputNumberModule,
    CalendarModule,
    OverlayPanelModule,
    ConfirmDialogModule,
    // Shared components
    BookGridComponent,
    LazyImageDirective
  ]
})
export class SharedModule { }
