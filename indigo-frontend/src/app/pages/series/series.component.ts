import { Component, OnInit, HostListener } from '@angular/core';
import { SelectItem } from 'primeng/api/selectitem';
import { Serie } from 'src/app/domain/serie';
import { SerieService } from 'src/app/services/serie.service';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';
import { lastValueFrom } from 'rxjs';


@Component({
  selector: 'app-series',
  templateUrl: './series.component.html',
  styleUrls: ['./series.component.css'],
  providers: [MessageService]
})
export class SeriesComponent implements OnInit {

  series: Serie[] = [];

  title: string;

  total: number;

  private page: number;
  private lastPage: number;

  private size: number;
  private sort: string;
  private order: string;

  sorts: SelectItem[] = [];
  selectedSort: string;

  showGoUpButton: boolean;
  private showScrollHeight = 400;
  private hideScrollHeight = 200;

  user = JSON.parse(sessionStorage.user);


  constructor(private serieService: SerieService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService) {


    //defines the number of elements to retrieve according to the width of the screen
    if (window.screen.width < 640) {
      this.size = 10;
    } else if (window.screen.width < 1024) {
      this.size = 20;
    } else {
      this.size = 60;
    }

    this.sorts.push(
      { label: this.translate.instant('locale.series.order_by.total.desc'), value: 'numBooks,desc' },
      { label: this.translate.instant('locale.series.order_by.total.asc'), value: 'numBooks,asc' },
      { label: this.translate.instant('locale.series.order_by.sort.asc'), value: '_id,asc' },
      { label: this.translate.instant('locale.series.order_by.sort.desc'), value: '_id,desc' }
    );

  }

  ngOnInit(): void {
    this.showGoUpButton = false;

    this.reset();
    this.count();
    //this.getAll();
  }

  onChange(event) {

    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('series_order', this.selectedSort);

    this.page = 0;
    this.series.length = 0;

    this.getAll();
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    if ((window.pageYOffset ||
      document.documentElement.scrollTop ||
      document.body.scrollTop) > this.showScrollHeight) {
      this.showGoUpButton = true;
    } else if (this.showGoUpButton &&
      (window.pageYOffset ||
        document.documentElement.scrollTop ||
        document.body.scrollTop)
      < this.hideScrollHeight) {
      this.showGoUpButton = false;
    }
  }

  onScroll() {
    if (this.series.length < this.total) {
      this.getAll();
    }
  }

  scrollTop() {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  async count() {
    try {
      const data = await lastValueFrom(this.serieService.count(this.user.languageBooks));

      this.total = data;
      this.lastPage = this.total / this.size;
      this.title = this.translate.instant('locale.series.title') + " (" + this.total + ")";

      this.getAll();

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.series.error.data'), closable: false, life: 5000 });
    }
  }


  async getAll() {
    try {
      const data = await lastValueFrom(this.serieService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order));

      // Get cover
      for (const serie of data) {
        await this.getCover(serie);
      }

      Array.prototype.push.apply(this.series, data);
      this.page++;

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.series.error.data'), closable: false, life: 5000 });
    }
  }


  async getCover(serie: Serie) {
    try {
      const data = await lastValueFrom(this.serieService.getCover(serie.name));

      let objectURL = 'data:image/jpeg;base64,' + data.image;
      serie.image = objectURL;

    } catch (error) {
      console.log(error);
    }
  }


  getBooksBySerie(serie: string) {
    this.reset();

    let search: Search = new Search();
    search.serie = serie;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  private reset() {
    this.series.length = 0;
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;

    this.selectedSort = sessionStorage.getItem('series_order');
    if (!this.selectedSort) {
      this.sort = "_id";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    }
    else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }
  }

}
