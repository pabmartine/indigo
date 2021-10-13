import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Search } from '../domain/search';
import { Book } from '../domain/book';


@Injectable({
  providedIn: 'root'
})
export class BookService {

  private service: string = "book";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }

  public count(adv_search: Search): Observable<any> {

    let url = this.endpoint + "/count/search";

    return this.http.post<Search>(url + "/advance", adv_search);

  }

  public getAll(adv_search: Search, page: number,
    size: number,
    sort: string,
    order: string): Observable<any> { 

    let url = this.endpoint + "/all/advance?" + "page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order;
    return this.http.post(url, adv_search);

  }

  getCover(path: string): Observable<any> {
    return this.http.get(this.endpoint + "/cover?path=" + path);
  }

  getBookInfo(id: number, local: boolean): Observable<any> {
    return this.http.get(this.endpoint + "/info?id=" + id + "&local=" + local);
  }


  getSimilar(similar: string): Observable<any> {
    return this.http.post(this.endpoint + "/similar", similar);
  }

  getRecommendationsByBook(id: number): Observable<any> {
    return this.http.get(this.endpoint + "/recommendations/book?id=" + id);
  }

  getRecommendationsByUser(user: number): Observable<any> {
    return this.http.get(this.endpoint + "/recommendations/user?user=" + user);
  }

  getFavorite(book: number, user: number): Observable<any> {
    return this.http.get(this.endpoint + "/favorite?user=" + user + "&book=" + book);
  }

  getFavorites(user: number): Observable<any> {
    return this.http.get(this.endpoint + "/favorites?user=" + user);
  }

  addFavorite(book: number, user: number): Observable<any> {
    return this.http.post(this.endpoint + "/favorite?user=" + user + "&book=" + book, null);
  }

  deleteFavorite(book: number, user: number): Observable<any> {
    return this.http.delete(this.endpoint + "/favorite?user=" + user + "&book=" + book);
  }

  getBookTitle(id: number): Observable<Book> {
    return this.http.get(this.endpoint + "/title?id=" + id);
  }

  getSent(user: number): Observable<any> {
    return this.http.get(this.endpoint + "/sent?user=" + user);
  }
}