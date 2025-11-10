import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/domain/user';
import { Router } from '@angular/router';
import { LoginService } from 'src/app/services/login.service';
import { TranslateService } from '@ngx-translate/core';
import { UserService } from 'src/app/services/user.service';
import { AuthStateService } from 'src/app/services/auth-state.service';

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
      this.user.password = localStorage.getItem("password");
    }
  }

  login(): void {
    this.clearErrorMessage();
    if (this.validate(this.user)) {
      if (this.rememberMe) {
        localStorage.setItem('username', this.user.username);
        localStorage.setItem('password', this.user.password);
        localStorage.setItem('rememberMe', this.rememberMe.toString());
      }

      this.loginService.login(this.user).subscribe({
        next: (response) => {
          if (response != null && response.headers.get("Authorization") != null) {
            this.userService.get(this.user.username).subscribe({
              next: (data) => {
                let user = data;
                user.token = response.headers.get("Authorization").slice(7);
                this.authState.setUser(user).then(() => {
                  sessionStorage.setItem('token', user.token);
                  this.translate.use(user.language);
                  this.router.navigate(["books"]);
                });
              },
              error: () => {
                this.setErrorMessage("locale.login.error");
              }
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
