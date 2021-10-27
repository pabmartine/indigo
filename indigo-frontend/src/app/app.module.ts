import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
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
import {CardModule} from 'primeng/card';


//auth
import { JwtModule } from "@auth0/angular-jwt";

//translate
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient } from '@angular/common/http';

// AoT requires an exported function for factories
export function HttpLoaderFactory(httpClient: HttpClient) {
  return new TranslateHttpLoader(httpClient);
}

//Loaginbar
import { LoadingBarHttpClientModule } from '@ngx-loading-bar/http-client';
import { LoadingBarRouterModule } from '@ngx-loading-bar/router';
import { LoadingBarModule } from '@ngx-loading-bar/core';

//Cache
import { CustomReuseStrategy } from './utils/cache.routes';

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
      }
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
        whitelistedDomains: ["localhost:8080", "127.0.0.1:8080", "192.168.1.40:8080", "krahen.synology.me:8080"],
        blacklistedRoutes: ["localhost:8080/rest/login", "127.0.0.1:8080/rest/login", "192.168.1.40:8080/rest/login", "krahen.synology.me:8080/rest/login"],
      },
    }),
  ],
  providers: [{ provide: RouteReuseStrategy, useClass: CustomReuseStrategy }],
  exports: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
