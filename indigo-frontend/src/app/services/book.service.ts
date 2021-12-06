import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Book } from '../domain/book';
import { Search } from '../domain/search';


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

  getEpub(path: string): Observable<Blob> {
    path = path.replace('&', '@_@');
    path = path.replace('[', '@-@');
    path = path.replace(']', '@¡@');
    path = path.replace('`', '@!@');
    return this.http.get(this.endpoint + "/epub?path=" + path, {
      responseType: 'blob'
    });
  }

  getSimilar(similar: string[], languages: string[]): Observable<any> {
    return this.http.get(this.endpoint + "/similar?similar=" + similar + "&languages=" + languages);
  }

  getRecommendationsByBook(recommendations: string[], languages: string[]): Observable<any> {
    return this.http.get(this.endpoint + "/recommendations/book?recommendations=" + recommendations + "&languages=" + languages);
  }

  countRecommendationsByUser(user: string): Observable<any> {
    return this.http.get(this.endpoint + "/recommendations/user/count?user=" + user);
  }

  getRecommendationsByUser(user: string, page: number,
    size: number,
    sort: string,
    order: string): Observable<any> {
    return this.http.get(this.endpoint + "/recommendations/user?user=" + user + "&page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }

  getFavorite(book: string, user: string): Observable<any> {
    return this.http.get(this.endpoint + "/favorite?user=" + user + "&book=" + book);
  }

  getFavorites(user: string): Observable<any> {
    return this.http.get(this.endpoint + "/favorites?user=" + user);
  }

  addFavorite(book: string, user: string): Observable<any> {
    return this.http.post(this.endpoint + "/favorite?user=" + user + "&book=" + book, null);
  }

  deleteFavorite(book: string, user: string): Observable<any> {
    return this.http.delete(this.endpoint + "/favorite?user=" + user + "&book=" + book);
  }

  getBookId(id: string): Observable<Book> {
    return this.http.get(this.endpoint + "/id?id=" + id);
  }

  getBookByPath(path: string): Observable<Book> {
    return this.http.get(this.endpoint + "/path?path=" + path);
  }

  getSent(user: string): Observable<any> {
    return this.http.get(this.endpoint + "/sent?user=" + user);
  }

  view(book: string, user: string): Observable<any> {
    return this.http.post(this.endpoint + "/view?user=" + user + "&book=" + book, null);
  }

  getLanguages(): Observable<any> {
    return this.http.get(this.endpoint + "/languages");
  }
}