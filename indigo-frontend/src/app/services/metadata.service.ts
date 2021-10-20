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


  startFull(lang: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/full?lang=" + lang);
  }

  startPartial(lang: string): Observable<any> {
    return this.http.get<any>(this.endpoint + "/partial?lang=" + lang);
  }

  getDataStatus(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/status", {
      headers: { ignoreLoadingBar: '' }
    });
  }

  stopData(): Observable<any> {
    return this.http.get<any>(this.endpoint + "/stop");
  }


}