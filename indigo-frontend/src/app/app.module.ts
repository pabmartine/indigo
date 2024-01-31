import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, Injector, NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { RouterModule, RouteReuseStrategy } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

//components
import { AppComponent } from './app.component';
import { LayoutComponent } from './layouts/layout/layout.component';
import { SidebarComponent } from './layouts/sidebar/sidebar.component';
import { TopbarComponent } from './layouts/topbar/topbar.component';
import { LoginComponent } from './pages/login/login.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { HeaderComponent } from './layouts/header/header.component';

//primeng
import { SidebarModule } from 'primeng/sidebar';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { TieredMenuModule } from 'primeng/tieredmenu';
import { MenuModule } from 'primeng/menu';
import { DialogModule } from 'primeng/dialog';
import { CheckboxModule } from 'primeng/checkbox';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { CardModule } from 'primeng/card';


//auth
import { JwtModule } from "@auth0/angular-jwt";

//translate
import { TranslateModule, TranslateLoader, MissingTranslationHandler, MissingTranslationHandlerParams, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient } from '@angular/common/http';

import { lastValueFrom, take } from 'rxjs';


// AoT requires an exported function for factories
export function HttpLoaderFactory(httpClient: HttpClient) {
  return new TranslateHttpLoader(httpClient, 'assets/i18n/', '.json');
}

export class MyMissingTranslationHandler implements MissingTranslationHandler {
  handle(params: MissingTranslationHandlerParams) {
    return '';
  }
}

export function mapLanguageCode(languageCode: string): string {
  if (languageCode === 'es') {
    return 'es-ES';
  } else if (languageCode === 'en') {
    return 'en-GB';
  } else if (languageCode === 'FR') {
    return 'fr-FR';
  }
  // Si no hay coincidencia, devuelve el código original
  return languageCode;
}

export function appInitializerFactory(translateService: TranslateService, injector: Injector): () => Promise<any> {
  return () => new Promise<any>((resolve: any) => {
    // Obtener la promesa que indica cuándo se ha inicializado la ubicación
    const locationInitialized = injector.get(LOCATION_INITIALIZED, Promise.resolve(null));
    
    // Esperar hasta que la ubicación se haya inicializado
    locationInitialized.then(async () => {

      // Obtener el lenguaje del navegador del usuario
      const browserLanguage = window.navigator.language;

      // Mapear el código de lenguaje utilizando la función
      const mappedLanguage = mapLanguageCode(browserLanguage);

      // Usar el idioma almacenado en el almacenamiento local o el idioma mapeado,
      // y suscribirse al resultado
      await lastValueFrom(
        translateService.use(localStorage.getItem("language") || mappedLanguage)
          .pipe(take(1))
      );
      
      resolve(null); // Resolver la promesa una vez que la operación esté completa
    });
  });
}

//Loaginbar
import { LoadingBarHttpClientModule } from '@ngx-loading-bar/http-client';
import { LoadingBarRouterModule } from '@ngx-loading-bar/router';
import { LoadingBarModule } from '@ngx-loading-bar/core';

//Cache
import { CustomReuseStrategy } from './utils/cache.routes';
import { environment } from 'src/environments/environment';
import { LOCATION_INITIALIZED } from '@angular/common';
import { AuthorComponent } from './pages/author/author.component';

@NgModule({
  declarations: [
    AppComponent,
    LayoutComponent,
    FooterComponent,
    HeaderComponent,
    SidebarComponent,
    TopbarComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule,
    BrowserAnimationsModule,
    NgbModule,
    FormsModule,
    HttpClientModule,
    SidebarModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessagesModule,
    MessageModule,
    TieredMenuModule,
    MenuModule,
    DialogModule,
    LoadingBarHttpClientModule,
    LoadingBarRouterModule,
    LoadingBarModule,
    CheckboxModule,
    OverlayPanelModule,
    CardModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      },
      missingTranslationHandler: { provide: MissingTranslationHandler, useClass: MyMissingTranslationHandler },
      useDefaultLang: false
    }),
    JwtModule.forRoot({
      config: {
        tokenGetter: () => {
          let token: string;
          if (sessionStorage.user) {
            const user = JSON.parse(sessionStorage.user);
            token = user.token;
          }
          return token;
        },
        allowedDomains: environment.whiteList,
        disallowedRoutes: environment.blackList,
      },
    }),
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: CustomReuseStrategy },
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFactory,
      deps: [TranslateService, Injector],
      multi: true
    }
  ],
  exports: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
