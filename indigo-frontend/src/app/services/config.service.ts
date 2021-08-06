import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { User } from '../domain/user';
import { Observable } from 'rxjs';
import { Config } from '../domain/config';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private service: string = "config";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

  public get(key:string) : Observable<Config> {
    return this.http.get<Config>(this.endpoint+"/get?key="+key);
  }


  public save(configs: Config[]):Observable<any> {
    return this.http.put<Config>(this.endpoint + "/save",configs);
  }

  

}
