import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { SelectItem } from 'primeng/api/selectitem';
import { Serie } from 'src/app/domain/serie';
import { SerieService } from 'src/app/services/serie.service';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Interfaz para series con imagen temporal
interface SerieWithTempImage extends Serie {
  originalImageRequested?: boolean;
}

@Component({
  selector: 'app-series',
  templateUrl: './series.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class SeriesComponent implements OnInit, OnDestroy {

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

  // Estado de carga
  isLoading: boolean = false;
  isScrolling: boolean = false;

  user = JSON.parse(sessionStorage.user);

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();

  // Cache para optimizar rendimiento
  private seriesCache = new Map<string, Serie[]>();

  constructor(
    private serieService: SerieService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef
  ) {
    this.initializeScreenSize();
    this.initializeSortOptions();
  }

  ngOnInit(): void {
    this.showGoUpButton = false;
    this.reset();
    this.loadInitialDataInParallel();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.seriesCache.clear();
  }

  private initializeScreenSize(): void {
    // Define el número de elementos según el ancho de pantalla
    if (window.screen.width < 640) {
      this.size = 10;
    } else if (window.screen.width < 1024) {
      this.size = 20;
    } else {
      this.size = 60;
    }
  }

  private initializeSortOptions(): void {
    this.sorts = [
      { label: this.translate.instant('locale.series.order_by.total.desc'), value: 'numBooks,desc' },
      { label: this.translate.instant('locale.series.order_by.total.asc'), value: 'numBooks,asc' },
      { label: this.translate.instant('locale.series.order_by.sort.asc'), value: '_id,asc' },
      { label: this.translate.instant('locale.series.order_by.sort.desc'), value: '_id,desc' }
    ];
  }

  private loadInitialDataInParallel(): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    const count$ = this.serieService.count(this.user.languageBooks);
    const series$ = this.serieService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order);

    forkJoin({ count: count$, series: series$ })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ count, series }) => {
          // Process Count
          this.total = count;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.series.title') + " (" + this.total + ")";

          // Process Series
          const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;
          const seriesWithoutImages: SerieWithTempImage[] = series.map(serie => ({
            ...serie,
            image: null,
            originalImageRequested: false
          }));
          Array.prototype.push.apply(this.series, seriesWithoutImages);
          this.page++;
          this.cdr.detectChanges();
          this.requestCoversAsync(seriesWithoutImages, 0);
          this.seriesCache.set(cacheKey, series);

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading initial series data in parallel:', error);
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

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
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

  onScroll(): void {
    if (this.series.length < this.total && !this.isScrolling) {
      this.getAll();
    }
  }

  scrollTop(): void {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  getAll(): void {
    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;

    // Verificar cache
    if (this.seriesCache.has(cacheKey)) {
      const cachedData = this.seriesCache.get(cacheKey);
      Array.prototype.push.apply(this.series, cachedData);
      this.page++;
      this.cdr.detectChanges();
      return;
    }

    this.isScrolling = true;
    this.serieService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          // INMEDIATAMENTE mostrar series sin imágenes
          const seriesWithoutImages: SerieWithTempImage[] = data.map(serie => ({
            ...serie,
            image: null, // Temporalmente sin imagen
            originalImageRequested: false // Flag para saber si ya se pidió la imagen
          }));

          // Mostrar datos inmediatamente
          Array.prototype.push.apply(this.series, seriesWithoutImages);
          this.page++;
          this.cdr.detectChanges();

          // Solicitar covers de forma asíncrona
          this.requestCoversAsync(seriesWithoutImages, this.series.length - seriesWithoutImages.length);

          this.isScrolling = false;
          // Las imágenes se guardarán en cache cuando se reciban
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

  private requestCoversAsync(series: SerieWithTempImage[], startIndex: number): void {
    // Solicitar covers en pequeños lotes para no sobrecargar el servidor
    const batchSize = 3;
    let currentIndex = 0;

    const processBatch = () => {
      const endIndex = Math.min(currentIndex + batchSize, series.length);

      for (let i = currentIndex; i < endIndex; i++) {
        const serie = series[i];
        const targetIndex = startIndex + i;

        if (!serie.originalImageRequested && targetIndex < this.series.length) {
          serie.originalImageRequested = true;

          // Solicitar cover de forma asíncrona
          setTimeout(() => {
            this.getCoverAsync(serie.name, targetIndex);
          }, i * 100); // Delay entre solicitudes para evitar sobrecarga
        }
      }

      currentIndex = endIndex;

      // Continuar con el siguiente lote
      if (currentIndex < series.length) {
        setTimeout(processBatch, 200); // Pausa entre lotes
      }
    };

    // Iniciar procesamiento
    setTimeout(processBatch, 150);
  }

  private getCoverAsync(serieName: string, serieIndex: number): void {
    this.serieService.getCover(serieName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (serieIndex < this.series.length && this.series[serieIndex].name === serieName) {
            const objectURL = 'data:image/jpeg;base64,' + data.image;
            this.series[serieIndex].image = objectURL;
            this.cdr.detectChanges();

            // Actualizar cache con la imagen procesada
            this.updateCacheWithImage(serieName, objectURL);
          }
        },
        error: (error) => {
          console.log(`Error loading cover for serie: ${serieName}`, error);
          // No mostrar error al usuario, simplemente la serie quedará sin imagen
        }
      });
  }

  private updateCacheWithImage(serieName: string, imageUrl: string): void {
    // Actualizar todas las entradas de cache que contengan esta serie
    this.seriesCache.forEach((series, key) => {
      const serieIndex = series.findIndex(s => s.name === serieName);
      if (serieIndex !== -1) {
        series[serieIndex].image = imageUrl;
      }
    });
  }

  // Método original mantenido para compatibilidad, pero optimizado
  getCover(serie: Serie): void {
    this.getCoverAsync(serie.name, this.series.findIndex(s => s.name === serie.name));
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
