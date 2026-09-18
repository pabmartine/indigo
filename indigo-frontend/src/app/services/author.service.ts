import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthorSummaryPage } from '../domain/authorSummaryPage';


@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  private service: string = "author";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }

  count(languages: string[]): Observable<any> {
    return this.http.get<any>(this.endpoint + "/count?languages=" + languages.map(x=>x).join(","));
  }

  getAll(languages: string[], page: number,
    size: number,
    sort: string,
    order: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/all?languages=" + languages.map(x=>x).join(",") + "&page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }

  getSummaryPage(languages: string[], page: number,
    size: number,
    sort: string,
    order: string): Observable<AuthorSummaryPage> {
    const langParam = languages && languages.length > 0 ? languages.map(x => x).join(",") : "";
    return this.http.get<AuthorSummaryPage>(this.endpoint + "/summary/page?languages=" + langParam + "&page=" + page + "&size=" + size + "&sort=" + sort + "&order=" + order);
  }

  public buildCoverImageUrl(authorId?: string | null): string | null {
    if (!authorId) {
      return null;
    }
    return `${this.endpoint}/cover/${authorId}`;
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