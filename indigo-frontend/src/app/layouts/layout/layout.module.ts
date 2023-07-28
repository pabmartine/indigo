import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LayoutRoutes } from './layout.routing';
import { RouterModule, RouteReuseStrategy } from '@angular/router';
import { FormsModule } from '@angular/forms';

//components
import { BooksComponent } from 'src/app/pages/books/books.component';
import { RecommendationsComponent } from 'src/app/pages/recommendations/recommendations.component';
import { AuthorsComponent } from 'src/app/pages/authors/authors.component';
import { CategoriesComponent } from 'src/app/pages/categories/categories.component';
import { SeriesComponent } from 'src/app/pages/series/series.component';
import { ProfileComponent } from 'src/app/pages/profile/profile.component';
import { SettingsComponent } from 'src/app/pages/settings/settings.component';
import { SearchComponent } from 'src/app/pages/search/search.component';
import { NotificationsComponent } from 'src/app/pages/notifications/notifications.component';
import { DetailComponent } from 'src/app/pages/detail/detail.component';

//primeng
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
import {SpeedDialModule} from 'primeng/speeddial';
import {DividerModule} from 'primeng/divider';
import { ChipsModule } from 'primeng/chips';


//translate
import { TranslateModule } from '@ngx-translate/core';

//scroll
import { InfiniteScrollModule } from 'ngx-infinite-scroll';

//cache
import { DatePipe } from '@angular/common';
import { AuthorComponent } from 'src/app/pages/author/author.component';

//epub
// import { AngularEpubViewerModule } from 'angular-epub-viewer';

@NgModule({
  declarations: [
    BooksComponent,
    RecommendationsComponent,
    AuthorsComponent,
    CategoriesComponent,
    SeriesComponent,
    ProfileComponent,
    SettingsComponent,
    SearchComponent,
    NotificationsComponent,
    DetailComponent,
    AuthorComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(LayoutRoutes),
    CardModule,
    ButtonModule,
    DialogModule,
    InfiniteScrollModule,
    DropdownModule,
    FormsModule,
    MenuModule,
    TieredMenuModule,
    TranslateModule,
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
    ChipsModule
    // AngularEpubViewerModule
  ],
  exports: [

  ],
  providers: [
    DatePipe
  ]
})
export class LayoutModule { }
