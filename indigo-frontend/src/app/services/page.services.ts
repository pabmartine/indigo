import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class PageService {

  private service: string = "page";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }
   
  getPages(id:number) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/book?id="+id);
  }  
  
}