import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { User } from 'src/app/domain/user';
import { Router } from '@angular/router';
import { LoginService } from 'src/app/services/login.service';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  providers: [MessageService]
})
export class LoginComponent implements OnInit {

  user: User = new User();
  error: string;
  rememberMe: boolean;

  constructor(private loginService: LoginService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    public userService: UserService,
    private changeDetectorRef: ChangeDetectorRef) { }

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

  login(user) {

    if (this.validate(user)) {

      if (this.rememberMe){
          localStorage.setItem('username', this.user.username);
          localStorage.setItem('password', this.user.password);
          localStorage.setItem('rememberMe', this.rememberMe.toString());
      }

      this.loginService.login(user).subscribe(response => {

        if (response != null && response.headers.get("Authorization") != null) {


          this.userService.get(this.user.username).subscribe(
            data => {
              user = data;

              //Asign token
              user.token = response.headers.get("Authorization").slice(7);

              //Store user in session
              sessionStorage.setItem('user', JSON.stringify(user));

              //change language accord to user preferences
              this.translate.use(user.language);

              this.router.navigate(["books"]);
            },
            error => {
              this.error = "locale.login.error";
            }
          );


        } else {
          this.error = "locale.login.error";
        }
      },
        error => {
          console.log(error);
          if (error.status === 401 || error.status === 403) {
            this.error = "locale.login.userpass.wrong";
          } else {
            this.error = error;
          }
        }).add(() => {
          //Called when operation is complete (both success and error)
          if (this.error != null) {
            this.translate.get(this.error).subscribe((text: string) => {
              this.messageService.add({ severity: 'error', detail: text });
            });
            this.changeDetectorRef.detectChanges();
          }
     });

    }

  }


  validate(user: User): boolean {

    this.messageService.clear();

    if (!user.username) {
      this.error = "locale.login.username.required";
      return false;
    }

    if (!user.password) {
      this.error = "locale.login.password.required";
      return false;
    }

    return true;
  }

}
