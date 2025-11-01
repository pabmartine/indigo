import { Component, HostListener, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';
import { AuthStateService } from './services/auth-state.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnDestroy {
  title = 'indigo-client';

  sessionDialog = false;
  userActivity: number | null;
  userInactive: Subject<any> = new Subject();

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    public translate: TranslateService,
    private authState: AuthStateService
  ){

    //Locale
    translate.addLangs(['en-GB', 'fr-FR', 'es-ES']);
    translate.setDefaultLang('en-GB');

    const user = this.authState.getCurrentUser();
    if (user) {
      translate.use(user.language);
    } else {
      const browserLang = translate.getBrowserLang();
      translate.use(browserLang.match(/en-GB|fr-FR|es-ES/) ? browserLang : 'en-GB');
    }



    //Control for session idle
    this.setTimeout();
    this.userInactive
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (sessionStorage.user){
            console.log('User has been inactive for 30 seg');
            this.sessionDialog = true;
        }
      });
  }

  //Control for session idle
  setTimeout() {
    this.userActivity = setTimeout(() => this.userInactive.next(undefined), 1800000);
  }

  @HostListener('window:mousemove') refreshUserState() {
    clearTimeout(this.userActivity);
    this.setTimeout();
  }

  logout(){
    this.authState.clearUser();
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}


