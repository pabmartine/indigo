import { Component, HostListener } from '@angular/core';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'indigo-client';

  sessionDialog = false;
  userActivity:any;
  userInactive: Subject<any> = new Subject();

  constructor(private router: Router, public translate: TranslateService){
    
    //Locale
    translate.addLangs(['en-GB', 'fr-FR', 'es-ES']);
    translate.setDefaultLang('en-GB');

    if (sessionStorage.user) {
      const user = JSON.parse(sessionStorage.user);
      translate.use(user.language);
      console.log("a");
      console.log(user.language);
    } else {
      const browserLang = translate.getBrowserLang();
      translate.use(browserLang.match(/en-GB|fr-FR|es-ES/) ? browserLang : 'en-GB');
      console.log("b");
      console.log(browserLang.match(/en-GB|fr-FR|es-ES/) ? browserLang : 'en-GB');
    }
   
    
      
    //Control for session idle
    this.setTimeout();
    this.userInactive.subscribe(() => {
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
    sessionStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}


