import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class SerieService {

  private service: string = "serie";
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


  getCover(serie: string): Observable<any> {
    serie = serie.replace('&', '@_@');
    serie = serie.replace('[', '@-@');
    serie = serie.replace(']', '@¡@');
    serie = serie.replace('`', '@!@');
    return this.http.get(this.endpoint + "/cover?serie=" + serie);
  }

}