import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { User } from '../domain/user';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private service: string = "user";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

  public getAll() : Observable<any> {
    return this.http.get<any>(this.endpoint+"/getAll");
  }

  public get(username:string) : Observable<User> {
    return this.http.get<User>(this.endpoint+"/get?username="+username);
  }

  public update(user:User):Observable<any> {
    return this.http.put<User>(this.endpoint + "/update",user);
  }

  public save(user:User):Observable<any> {
    return this.http.put<User>(this.endpoint + "/save",user);
  }

  public delete(id:string):Observable<any> {
    return this.http.delete<any>(this.endpoint + "/delete?id="+id);
  }

  public getById(id:string):Observable<User> {
    return this.http.get<any>(this.endpoint + "/getById?id="+id);
  }

}
