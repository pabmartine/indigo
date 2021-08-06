import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Notif } from '../domain/notif';

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

  public findAllByUser(user: number): Observable<any> {
    return this.http.get(this.endpoint + "/user?user=" + user);
  }

  public findAllNotReadByUser(user: number): Observable<any> {
    return this.http.get(this.endpoint + "/not_read_user?user=" + user);
  }

  public save(notification: Notif): Observable<any> {
    return this.http.put<Notif>(this.endpoint + "/save", notification);
  }

  public read(id: number, user:number): Observable<any> {
    return this.http.get<Notif>(this.endpoint + "/read?id=" + id + "&user=" + user);
  }

  public delete(id: number): Observable<any> {
    return this.http.delete<Notif>(this.endpoint + "/delete?id=" + id);
  }
}
