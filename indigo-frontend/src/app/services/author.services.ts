import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  private service: string = "author";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }

  count(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/count");
  }

  getAll(page: number,
    size: number,
    sort: string,
    order: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/numbooks?page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }

  getInfoById(id: number): Observable<any> {
    return this.http.get<any>(this.endpoint + "/info/id?id=" + id);
  }

  getInfoByName(author: string, lang: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/info/name?author=" + author + "&lang=" + lang);
  }

  startData(lang: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/data?lang=" + lang);
  }

  startNoData(lang: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/nodata?lang=" + lang);
  }

  getDataStatus(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/status", {
      headers: { ignoreLoadingBar: '' }
    });
  }

  stopData(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/stop");
  }

  getFavorite(author:number, user:number) : Observable<any> {
    return this.http.get(this.endpoint+"/favorite?user="+user+"&author="+author);
  }

  getFavorites(user:number) : Observable<any> {
    return this.http.get(this.endpoint+"/favorites?user="+user);
  }

  addFavorite(author:number, user:number) : Observable<any> {
    return this.http.post(this.endpoint+"/favorite?user="+user+"&author="+author, null);
  }

  deleteFavorite(author:number, user:number) : Observable<any> {
    return this.http.delete(this.endpoint+"/favorite?user="+user+"&author="+author);
  }

}