import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Tag } from '../domain/tag';


@Injectable({
  providedIn: 'root'
})
export class TagService {

  private service: string = "tag";
  private endpoint: string;

  constructor(public http:HttpClient) {
    this.endpoint=environment.endpoint+this.service;
   }

 getAll(sort: string, order: string) : Observable<any> {
    return this.http.get<any>(this.endpoint+"/numbooks?sort=" + sort + "&order=" + order);
  }  

  getTags(id:number) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/book?id="+id);
  }  

  getTag(name:string) : Observable<any> {
    return this.http.get<any>(this.endpoint+"/tag?name="+name);
  }  

  rename(source:number, target: string) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/rename?source="+source+"&target="+target);
  }  

  merge(source:number, target: number) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/merge?source="+source+"&target="+target);
  }  

  saveImage(source:number, image: string) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/image?source="+source+"&image="+image);
  }  

  getImage(source:number) : Observable<any> {
    return this.http.get<string>(this.endpoint+"/image/id?source="+source);
  }  
  
}