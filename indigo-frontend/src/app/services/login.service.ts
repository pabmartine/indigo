import { Injectable } from '@angular/core';
import { User } from '../domain/user';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private service: string = "login";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

  public login(user: User) {
    return this.http.post<User>(this.endpoint, user, {
      headers: new HttpHeaders()
        .set('Content-Type', 'application/json'),
      observe: 'response'
    });
  }
}
