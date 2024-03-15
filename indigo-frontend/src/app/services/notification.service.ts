import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Notification } from '../domain/notification';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private service: string = "notification";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }


  public findAll(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/all");
  }

  public findAllNotRead(): Observable<any> {
    return this.http.get(this.endpoint + "/not_read");
  }

  public findAllByUser(user: string): Observable<any> {
    return this.http.get(this.endpoint + "/user?user=" + user);
  }

  //public save(notification: Notification): Observable<any> {
  //  return this.http.put<Notification>(this.endpoint + "/save", notification);
  // }

  public read(id: string, user:string): Observable<any> {
    return this.http.get<Notification>(this.endpoint + "/read?id=" + id + "&user=" + user);
  }

  public delete(id: string): Observable<any> {
    return this.http.delete<Notification>(this.endpoint + "/delete?id=" + id);
  }
}
