import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { SelectItem } from 'primeng/api/selectitem';
import { Serie } from 'src/app/domain/serie';
import { SerieService } from 'src/app/services/serie.service';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { User } from 'src/app/domain/user';
@Component({
  selector: 'app-series',
  templateUrl: './series.component.html',
  styleUrls: ['./series.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class SeriesComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('scrollSentinel') scrollSentinel?: ElementRef<HTMLDivElement>;

  series: Serie[] = [];
  seriesRows: Serie[][] = [];

  title: string;
  total: number;

  private page: number;
  private lastPage: number;
  private size: number;
  private sort: string;
  private order: string;

  sorts: SelectItem[] = [
    { label: 'Total (Desc)', value: 'numBooks,desc' },
    { label: 'Total (Asc)', value: 'numBooks,asc' },
    { label: 'Name (Asc)', value: '_id,asc' },
    { label: 'Name (Desc)', value: '_id,desc' }
  ];
  selectedSort: string;

  // Estado de carga
  isLoading: boolean = false;
  isScrolling: boolean = false;
  seriesColumns = 6;
  seriesRowHeight = 260;

  user: User;

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();

  // Cache para optimizar rendimiento
  private seriesCache = new Map<string, Serie[]>();
  private scrollObserver?: IntersectionObserver;

  constructor(
    private serieService: SerieService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private authState: AuthStateService
  ) {
    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;
    // Ensure languageBooks is always initialized
    if (!this.user.languageBooks || this.user.languageBooks.length === 0) {
      this.user.languageBooks = this.authState.getLanguageBooks();
    }
    this.initializeScreenSize();
  }

  ngOnInit(): void {
    this.initializeSortOptions();
    this.reset();
    this.loadInitialDataInParallel();
  }

  ngAfterViewInit(): void {
    this.setupScrollObserver();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.seriesCache.clear();
    this.scrollObserver?.disconnect();
  }

  private initializeScreenSize(): void {
    // Define el número de elementos según el ancho de pantalla
    if (window.screen.width < 640) {
      this.size = 10;
      this.seriesColumns = 2;
      this.seriesRowHeight = 230;
    } else if (window.screen.width < 1024) {
      this.size = 20;
      this.seriesColumns = 4;
      this.seriesRowHeight = 245;
    } else {
      this.size = 60;
      this.seriesColumns = 6;
      this.seriesRowHeight = 260;
    }
  }

  private updateSeriesRows(): void {
    const rows: Serie[][] = [];
    for (let index = 0; index < this.series.length; index += this.seriesColumns) {
      rows.push(this.series.slice(index, index + this.seriesColumns));
    }
    this.seriesRows = rows;
  }

  private initializeSortOptions(): void {
    const translationKeys = [
      'locale.series.order_by.total.desc',
      'locale.series.order_by.total.asc',
      'locale.series.order_by.sort.asc',
      'locale.series.order_by.sort.desc'
    ];

    this.translate.get(translationKeys)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (translations) => {
          if (translations && Object.keys(translations).length > 0) {
            this.sorts = [
              { label: translations['locale.series.order_by.total.desc'] || 'Total (Desc)', value: 'numBooks,desc' },
              { label: translations['locale.series.order_by.total.asc'] || 'Total (Asc)', value: 'numBooks,asc' },
              { label: translations['locale.series.order_by.sort.asc'] || 'Name (Asc)', value: '_id,asc' },
              { label: translations['locale.series.order_by.sort.desc'] || 'Name (Desc)', value: '_id,desc' }
            ];
            this.cdr.detectChanges();
          }
        },
        error: (error) => {
          console.error('Error loading translations for sorts:', error);
        }
      });
  }

  private loadInitialDataInParallel(): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.serieService.getPage(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.total = response.total;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.series.title') + " (" + this.total + ")";

          const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;
          const processedSeries = this.mapSeriesWithCover(response.items || []);
          Array.prototype.push.apply(this.series, processedSeries);
          this.updateSeriesRows();
          this.page++;
          this.cdr.detectChanges();
          this.seriesCache.set(cacheKey, processedSeries);

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading initial series data:', error);
          this.isLoading = false;
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.series.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  // Método para trackBy en ngFor
  trackBySerieId(index: number, serie: Serie): string {
    return serie.name ? serie.name.toString() : index.toString();
  }

  onChange(event): void {
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('series_order', this.selectedSort);

    this.page = 0;
    this.series.length = 0;
    this.seriesCache.clear(); // Limpiar cache cuando cambia el orden

    this.getAll();
  }

  private setupScrollObserver(): void {
    if (!this.scrollSentinel || typeof window === 'undefined') {
      return;
    }

    this.ngZone.runOutsideAngular(() => {
      this.scrollObserver?.disconnect();
      this.scrollObserver = new IntersectionObserver(
        (entries) => {
          if (entries.some((entry) => entry.isIntersecting)) {
            this.ngZone.run(() => this.onScroll());
          }
        },
        { rootMargin: '400px 0px' }
      );

      this.scrollObserver.observe(this.scrollSentinel.nativeElement);
    });
  }

  onScroll(): void {
    if (this.series.length < this.total && !this.isScrolling) {
      this.getAll();
    }
  }

  onVirtualScrollIndexChange(index: number): void {
    const preloadThreshold = 3;
    if (index + preloadThreshold >= this.seriesRows.length) {
      this.onScroll();
    }
  }

  getAll(): void {
    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;

    // Verificar cache
    if (this.seriesCache.has(cacheKey)) {
      const cachedData = this.seriesCache.get(cacheKey);
      Array.prototype.push.apply(this.series, cachedData);
      this.updateSeriesRows();
      this.page++;
      this.cdr.detectChanges();
      return;
    }

    this.isScrolling = true;
    this.serieService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          const processedSeries = this.mapSeriesWithCover(data);
          Array.prototype.push.apply(this.series, processedSeries);
          this.updateSeriesRows();
          this.page++;
          this.cdr.detectChanges();
          this.isScrolling = false;
          this.seriesCache.set(cacheKey, processedSeries);
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.series.error.data'),
            closable: false,
            life: 5000
          });
          this.isScrolling = false;
        }
      });
  }

  private mapSeriesWithCover(series: Serie[]): Serie[] {
    return series.map(serie => ({
      ...serie,
      image: undefined,
      originalImage: this.serieService.buildCoverUrl(serie.name)
    }));
  }

  getBooksBySerie(serie: string): void {
    this.reset();

    const search: Search = new Search();
    search.serie = serie;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  private reset(): void {
    this.series.length = 0;
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;

    this.selectedSort = sessionStorage.getItem('series_order');
    if (!this.selectedSort) {
      this.sort = "_id";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    } else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }
  }
}
