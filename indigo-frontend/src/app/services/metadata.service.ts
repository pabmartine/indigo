import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MetadataService {

  private service: string = "metadata";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }


  start(lang: string, type: string, entity: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/start?lang=" + lang + "&type=" + type + '&entity=' + entity);
  }

  getDataStatus(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/status", {
      headers: { ignoreLoadingBar: '' }
    });
  }

  stop(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/stop");
  }

  findAuthor(lang: string, author: string): Observable<any> {
    return this.http.get(this.endpoint + "/author?lang=" + lang + "&author=" + author);
  }

  findBook(book: string, lang: string): Observable<any> {
    return this.http.get(this.endpoint + "/book?book=" + book + "&lang=" + lang);
  }

}