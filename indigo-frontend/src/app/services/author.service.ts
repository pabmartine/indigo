import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
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
    return this.http.get<any>(this.endpoint + "/all?page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }

  getByName(author: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/sort?sort=" + author);
  }

  getFavorite(author: string, user: string): Observable<any> {
    return this.http.get(this.endpoint + "/favorite?user=" + user + "&author=" + author);
  }

  getFavorites(user: string): Observable<any> {
    return this.http.get(this.endpoint + "/favorites?user=" + user);
  }

  addFavorite(author: string, user: string): Observable<any> {
    return this.http.post(this.endpoint + "/favorite?user=" + user + "&author=" + author, null);
  }

  deleteFavorite(author: string, user: string): Observable<any> {
    return this.http.delete(this.endpoint + "/favorite?user=" + user + "&author=" + author);
  }

}