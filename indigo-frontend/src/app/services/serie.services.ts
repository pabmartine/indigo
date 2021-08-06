import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class SerieService {

  private service: string = "serie";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

   count() : Observable<any> {
    return this.http.get<any>(this.endpoint+"/count");
  }  

   getAll(page: number,
    size: number,
    sort: string,
    order: string) : Observable<any> {
    return this.http.get<any>(this.endpoint+"/numbooks?page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }  

  getSerie(id:number) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/book?id="+id);
  }  
  
  getCover(id:number) : Observable<any> {
    return this.http.get(this.endpoint+"/cover?id="+id);
  }

}