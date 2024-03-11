import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MailService {

  private service: string = "mail";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

 
  public sendTestMail(address:string):Observable<any> {
    return this.http.get<any>(this.endpoint + "/test?address="+address);
  }

  sendMail(path:string, address:string) : Observable<any> {
    path = path.replace('&', '@_@');
    path = path.replace('[', '@-@');
    path = path.replace(']', '@¡@');
    path = path.replace('`', '@!@');
    return this.http.get(this.endpoint+"/send?path="+path + "&address="+address);
  }
}
