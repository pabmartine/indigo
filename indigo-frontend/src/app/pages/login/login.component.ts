import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/domain/user';
import { Router } from '@angular/router';
import { LoginService } from 'src/app/services/login.service';
import { TranslateService } from '@ngx-translate/core';
import { UserService } from 'src/app/services/user.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {

  user: User = new User();
  rememberMe: boolean;
  errorMessage: string | null = null;

  constructor(private loginService: LoginService,
    private router: Router,
    public translate: TranslateService,
    public userService: UserService,
    private authState: AuthStateService) { }

  ngOnInit(): void {
    // CHECK THE isLoggedIn STATE HERE
    if (sessionStorage.user) {
      this.router.navigate(["books"]);
    }

    this.rememberMe = JSON.parse(localStorage.getItem("rememberMe"));
    if (this.rememberMe){
      this.user.username = localStorage.getItem("username");
    }
  }

  login(): void {
    this.clearErrorMessage();
    if (this.validate(this.user)) {
      if (this.rememberMe) {
        localStorage.setItem('username', this.user.username);
        localStorage.setItem('rememberMe', this.rememberMe.toString());
      } else {
        localStorage.removeItem('username');
        localStorage.removeItem('rememberMe');
      }

      this.loginService.login(this.user).subscribe({
        next: (response) => {
          if (response != null && response.headers.get("Authorization") != null) {
            const token = response.headers.get("Authorization").slice(7);
            
            // Set partial user with token first, so JwtModule can see it for subsequent requests
            const partialUser = new User();
            partialUser.token = token;
            partialUser.username = this.user.username;
            
            this.authState.setUser(partialUser).then(() => {
              this.findLoggedUser().subscribe({
                next: (data) => {
                  if (!data) {
                    this.setErrorMessage("locale.login.error");
                    return;
                  }
                  const user = data;
                  user.token = token;
                  this.authState.setUser(user).then(() => {
                    this.translate.use(user.language);
                    this.router.navigate(["books"]);
                  });
                },
                error: () => {
                  this.setErrorMessage("locale.login.error");
                }
              });
            });
          } else {
            this.setErrorMessage("locale.login.error");
          }
        },
        error: (error) => {
          let errorMessage = "locale.login.error";
          if (error.status === 401 || error.status === 403) {
            errorMessage = "locale.login.userpass.wrong";
          }
          this.setErrorMessage(errorMessage);
        }
      });
    }
  }

  private findLoggedUser(): Observable<User> {
    return this.userService.getCurrent().pipe(
      catchError((error) => {
        if (this.user.username && (error.status === 404 || error.status === 500)) {
          return this.userService.get(this.user.username);
        }
        return throwError(() => error);
      })
    );
  }

  validate(user: User): boolean {

    if (!user.username) {
      this.setErrorMessage("locale.login.username.required");
      return false;
    }

    if (!user.password) {
      this.setErrorMessage("locale.login.password.required");
      return false;
    }

    return true;
  }

  private setErrorMessage(key: string): void {
    this.translate.get(key).subscribe((text: string) => {
      this.errorMessage = text;
    });
  }

  private clearErrorMessage(): void {
    this.errorMessage = null;
  }

}
