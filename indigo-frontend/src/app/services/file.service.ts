import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class FileService {

  private service: string = "file";
  private endpoint: string;

  constructor(public http: HttpClient) {
    this.endpoint = environment.endpoint + this.service;
  }

  getUploadsPath(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/path");
  }

  count(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/count");
  }

  upload(badge: number): Observable<any> {
    return this.http.post(this.endpoint + "/upload?number=" + badge, null);
  }
}
