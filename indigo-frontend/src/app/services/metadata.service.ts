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

  getSummary(): Observable<{ books: number; authors: number; reviews: number }> {
    return this.http.get<{ books: number; authors: number; reviews: number }>(this.endpoint + "/summary", {
      headers: { ignoreLoadingBar: '' }
    });
  }

  stop(entity?: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/stop", { params: entity ? { entity } : {} });
  }

  activity(): Observable<any[]> { return this.http.get<any[]>(this.endpoint + '/activity'); }
  libraryIndex(): Observable<any> { return this.http.get(this.endpoint + '/openlibrary/index'); }
  controlLibraryIndex(action: string): Observable<any> { return this.http.post(this.endpoint + '/openlibrary/index/' + action, {}); }
  reviewQueue(): Observable<any> { return this.http.get(this.endpoint + '/review-queue'); }
  startReviewQueue(all: boolean, replace: boolean): Observable<any> {
    return this.http.post(this.endpoint + '/review-queue/start?all=' + all + '&replace=' + replace + '&lang=es', {});
  }
  controlReviewQueue(action: string): Observable<any> { return this.http.post(this.endpoint + '/review-queue/' + action, {}); }
  configureReviewQueue(amazon: number, goodreads: number): Observable<any> {
    return this.http.post(this.endpoint + '/review-queue/settings?amazon=' + amazon + '&goodreads=' + goodreads, {});
  }
  pendingImports(): Observable<any[]> { return this.http.get<any[]>(this.endpoint + '/pending-imports'); }
  retryImport(id: string): Observable<any> {
    return this.http.post(this.endpoint + '/pending-imports/' + encodeURIComponent(id) + '/retry', {});
  }
  history(): Observable<any[]> { return this.http.get<any[]>(this.endpoint + '/activity/history'); }
  historyPage(page: number, size: number, type = '', status = '', search = '', entityId = ''): Observable<any> {
    return this.http.get(this.endpoint + '/activity/history/page', {
      params: { page, size, type, status, search, entityId }, headers: { ignoreLoadingBar: '' }
    });
  }
  historyEntry(id: string): Observable<any> {
    return this.http.get(this.endpoint + '/activity/history/' + encodeURIComponent(id), {headers: {ignoreLoadingBar: ''}});
  }
  retryItem(key: string): Observable<any> { return this.http.post(this.endpoint + '/activity/retry/' + encodeURIComponent(key), {}); }
  undoItem(id: string): Observable<any> { return this.http.post(this.endpoint + '/activity/undo/' + encodeURIComponent(id), {}); }
  lockItem(type: string, id: string, locked: boolean): Observable<any> {
    return this.http.post(this.endpoint + '/activity/lock/' + encodeURIComponent(type) + '/' + encodeURIComponent(id) + '?locked=' + locked, {});
  }

  findAuthor(lang: string, author: string): Observable<any> {
    return this.http.get(this.endpoint + "/author?lang=" + lang + "&author=" + author);
  }

  findBook(book: string, lang: string): Observable<any> {
    return this.http.get(this.endpoint + "/book?book=" + book + "&lang=" + lang);
  }

}
