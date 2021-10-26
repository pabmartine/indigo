import { Routes } from "@angular/router";
import { BooksComponent } from 'src/app/pages/books/books.component';
import { AuthGuard } from 'src/app/utils/auth.guard';
import { AuthorsComponent } from 'src/app/pages/authors/authors.component';
import { CategoriesComponent } from 'src/app/pages/categories/categories.component';
import { SeriesComponent } from 'src/app/pages/series/series.component';
import { ProfileComponent } from 'src/app/pages/profile/profile.component';
import { SettingsComponent } from 'src/app/pages/settings/settings.component';
import { SearchComponent } from 'src/app/pages/search/search.component';
import { NotificationsComponent } from 'src/app/pages/notifications/notifications.component';
import { DetailComponent } from 'src/app/pages/detail/detail.component';
import { RecommendationsComponent } from "src/app/pages/recommendations/recommendations.component";

export const LayoutRoutes: Routes = [
    { path: "recommendations", component: RecommendationsComponent, canActivate: [AuthGuard]},
    { path: "books", component: BooksComponent, canActivate: [AuthGuard], data: {reuseRoute: true}}, //cache
    { path: "authors", component: AuthorsComponent, canActivate: [AuthGuard]},
    { path: "categories", component: CategoriesComponent, canActivate: [AuthGuard]},
    { path: "series", component: SeriesComponent, canActivate: [AuthGuard]},
    { path: "profile", component: ProfileComponent, canActivate: [AuthGuard]},
    { path: "settings", component: SettingsComponent, canActivate: [AuthGuard]},
    { path: "notifications", component: NotificationsComponent, canActivate: [AuthGuard]},
    { path: "search", component: SearchComponent, canActivate: [AuthGuard]},
    { path: "detail", component: DetailComponent, canActivate: [AuthGuard]}

];

