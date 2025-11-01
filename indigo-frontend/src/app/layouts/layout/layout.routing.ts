import { Routes } from "@angular/router";

/**
 * Layout Routes with Lazy Loading
 *
 * Each feature module is loaded on-demand when the user navigates to that route.
 * This improves initial load time by splitting the application into smaller chunks.
 */
export const LayoutRoutes: Routes = [
    {
        path: "recommendations",
        loadChildren: () => import('../../pages/recommendations/recommendations.module').then(m => m.RecommendationsModule)
    },
    {
        path: "books",
        loadChildren: () => import('../../pages/books/books.module').then(m => m.BooksModule)
    },
    {
        path: "authors",
        loadChildren: () => import('../../pages/authors/authors.module').then(m => m.AuthorsModule)
    },
    {
        path: "categories",
        loadChildren: () => import('../../pages/categories/categories.module').then(m => m.CategoriesModule)
    },
    {
        path: "series",
        loadChildren: () => import('../../pages/series/series.module').then(m => m.SeriesModule)
    },
    {
        path: "profile",
        loadChildren: () => import('../../pages/profile/profile.module').then(m => m.ProfileModule)
    },
    {
        path: "settings",
        loadChildren: () => import('../../pages/settings/settings.module').then(m => m.SettingsModule)
    },
    {
        path: "notifications",
        loadChildren: () => import('../../pages/notifications/notifications.module').then(m => m.NotificationsModule)
    },
    {
        path: "search",
        loadChildren: () => import('../../pages/search/search.module').then(m => m.SearchModule)
    }
];

