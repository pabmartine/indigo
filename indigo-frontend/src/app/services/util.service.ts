import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { User } from '../domain/user';
import { Observable } from 'rxjs';
import { Config } from '../domain/config';

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  private service: string = "util";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

 
  public sendTestMail(user:number):Observable<any> {
    return this.http.get<any>(this.endpoint + "/testmail&user="+user);
  }

  sendMail(path:string, user:number) : Observable<any> {
    path = path.replace('&', '@_@');
    path = path.replace('[', '@-@');
    path = path.replace(']', '@¡@');
    path = path.replace('`', '@!@');
    return this.http.get(this.endpoint+"/mail?path="+path + "&user="+user);
  }
}
