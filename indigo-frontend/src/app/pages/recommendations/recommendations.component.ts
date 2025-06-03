import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';
import { AuthorComponent } from '../author/author.component';
import { DetailComponent } from '../detail/detail.component';

// Interfaz para libros con imagen temporal
interface BookWithTempImage extends Book {
  originalImage?: string;
}

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class RecommendationsComponent implements OnInit, OnDestroy {

  @ViewChild(DetailComponent) detailComponent: DetailComponent;
  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;

  books: Book[] = [];

  title: string;
  total: number;

  private page: number;
  private lastPage: number;
  private size: number;
  private sort: string;
  private order: string;

  showGoUpButton: boolean;
  private showScrollHeight = 400;
  private hideScrollHeight = 200;

  sorts: SelectItem[] = [];
  selectedSort: string;

  // Estados de diálogos
  showDetail: boolean = false;
  showAuthorDetail: boolean = false;

  // Estado de carga
  isLoading: boolean = false;

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();

  // Cache para optimizar rendimiento
  private booksCache = new Map<string, Book[]>();

  private user = JSON.parse(sessionStorage.user);

  constructor(
    private bookService: BookService,
    private router: Router,
    private route: ActivatedRoute,
    private authorService: AuthorService,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef
  ) {
    this.initializeScreenSize();
    this.initializeSortOptions();
    this.initializeNavigation();
  }

  ngOnInit(): void {
    // La inicialización se maneja en el constructor para evitar múltiples llamadas
  }

  ngAfterViewChecked(): void {
    if (sessionStorage.getItem("position")) {
      document.documentElement.scrollTop = Number(sessionStorage.getItem("position"));
      sessionStorage.removeItem("position");
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.booksCache.clear();
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
      { label: this.translate.instant('locale.books.order_by.count.desc'), value: 'count,desc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.desc'), value: 'pubDate,desc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.asc'), value: 'pubDate,asc' },
      { label: this.translate.instant('locale.books.order_by.title.asc'), value: 'title,asc' },
      { label: this.translate.instant('locale.books.order_by.title.desc'), value: 'title,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.desc'), value: 'rating,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.asc'), value: 'rating,asc' }
    ];

    this.showGoUpButton = false;
  }

  private initializeNavigation(): void {
    // Suscribirse a eventos de navegación de forma más limpia
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((e: NavigationEnd) => {
      if (e.url === "/recommendations" && !sessionStorage.getItem("position")) {
        this.doSearch();
      }
    });
  }

  // Método para trackBy en ngFor
  trackByBookId(index: number, book: Book): string {
    return book.id ? book.id.toString() : index.toString();
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

  onChange(event): void {
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('books_order', this.selectedSort);

    this.page = 0;
    this.books.length = 0;
    this.booksCache.clear(); // Limpiar cache cuando cambia el orden

    this.getAll();
  }

  onScroll(): void {
    if (this.books.length > 0 && this.books.length < this.total) {
      this.getAll();
    }
  }

  scrollTop(): void {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  count(): void {
    // Este método se mantiene para compatibilidad, pero la lógica principal está en doSearch()
    this.bookService.countRecommendationsByUser(this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          // Validar que data existe y es un número válido
          this.total = (data && typeof data === 'number') ? data : 0;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.books.recommendations.title2') + " (" + this.total + ")";
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.log('Error in count():', error);
          this.total = 0;
          this.title = this.translate.instant('locale.books.recommendations.title2') + " (0)";
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.books.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  getAll(): void {
    // No intentar obtener datos si no hay total
    if (this.total === 0) {
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.username}`;

    // Verificar cache
    if (this.booksCache.has(cacheKey)) {
      const cachedData = this.booksCache.get(cacheKey);
      if (cachedData && Array.isArray(cachedData)) {
        Array.prototype.push.apply(this.books, cachedData);
        this.page++;
        this.cdr.detectChanges();
      }
      return;
    }

    this.bookService.getRecommendationsByUser(this.user.username, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          console.log('Received recommendations data:', data, 'Type:', typeof data, 'Is Array:', Array.isArray(data));

          // Validar que data existe y es un array
          if (!data || !Array.isArray(data)) {
            console.warn('Received invalid data from getRecommendationsByUser:', data);
            // Si no hay datos válidos, marcar como completado
            this.isLoading = false;
            this.cdr.detectChanges();
            return;
          }

          // Si el array está vacío, no hay más datos
          if (data.length === 0) {
            console.log('No more recommendations available');
            this.isLoading = false;
            this.cdr.detectChanges();
            return;
          }

          // INMEDIATAMENTE añadir los datos SIN procesar imágenes
          const booksWithoutImages: BookWithTempImage[] = data.map(book => ({
            ...book,
            image: null, // Temporalmente sin imagen
            originalImage: book.image, // Guardar imagen original
            rating: book.rating ? Math.round(book.rating) : book.rating
          }));

          // Mostrar datos inmediatamente
          Array.prototype.push.apply(this.books, booksWithoutImages);
          this.page++;
          this.isLoading = false;
          this.cdr.detectChanges();

          // Procesar imágenes de forma asíncrona SIN bloquear la UI
          this.processImagesAsync(booksWithoutImages, this.books.length - booksWithoutImages.length);

          // Guardar en cache (con imágenes procesadas para futuras cargas)
          this.processBooks(data).then(processedData => {
            this.booksCache.set(cacheKey, processedData);
          });
        },
        error: (error) => {
          console.error('Error getting recommendations:', error);
          this.isLoading = false;
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.books.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  private processBooks(data: Book[]): Promise<Book[]> {
    return new Promise((resolve) => {
      // Validar que data existe y es un array
      if (!data || !Array.isArray(data)) {
        console.warn('Invalid data in processBooks:', data);
        resolve([]);
        return;
      }

      const processedBooks = data.map(book => {
        if (!book) return null; // Saltar libros nulos/undefined

        const processedBook = { ...book };

        // Procesar imagen de forma síncrona para cache
        if (book.image) {
          processedBook.image = 'data:image/jpeg;base64,' + book.image;
        }

        if (book.rating) {
          processedBook.rating = Math.round(book.rating);
        }

        return processedBook;
      }).filter(book => book !== null); // Filtrar libros nulos

      resolve(processedBooks);
    });
  }

  private processImagesAsync(books: BookWithTempImage[], startIndex: number): void {
    // Validar que books existe y es un array
    if (!books || !Array.isArray(books) || books.length === 0) {
      console.warn('Invalid books array in processImagesAsync:', books);
      return;
    }

    // Procesar imágenes en pequeños lotes para no bloquear la UI
    const batchSize = 5;
    let currentIndex = 0;

    const processBatch = () => {
      const endIndex = Math.min(currentIndex + batchSize, books.length);

      for (let i = currentIndex; i < endIndex; i++) {
        const book = books[i];
        const targetIndex = startIndex + i;

        if (book && book.originalImage && targetIndex < this.books.length) {
          // Procesar imagen de forma asíncrona
          setTimeout(() => {
            if (targetIndex < this.books.length && this.books[targetIndex]) {
              this.books[targetIndex].image = 'data:image/jpeg;base64,' + book.originalImage;
              this.cdr.detectChanges();
            }
          }, i * 10); // Pequeño delay entre imágenes para suavizar la carga
        }
      }

      currentIndex = endIndex;

      // Continuar con el siguiente lote si hay más imágenes
      if (currentIndex < books.length) {
        setTimeout(processBatch, 50); // Pausa entre lotes
      }
    };

    // Iniciar procesamiento
    setTimeout(processBatch, 100);
  }

  showDetails(book: Book): void {
    this.detailComponent.showDetails(book);
    this.showDetail = true;
  }

  closeDetails(): void {
    this.showDetail = false;
  }

  openDetails(): void {
    this.showDetail = true;
  }

  showAuthorDetails(author: Author): void {
    this.authorComponent.showDetails(author);
    this.showAuthorDetail = true;
  }

  closeAuthorDetails(): void {
    this.showAuthorDetail = false;
  }

  openAuthorDetails(): void {
    this.showAuthorDetail = true;
  }

  openBook(book: Book): void {
    this.showAuthorDetail = false;
    this.detailComponent.showDetails(book);
    this.showDetail = true;
  }

  openAuthor(sort: string): void {
    this.showDetail = false;
    this.authorService.getByName(sort)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data && data.image) {
            const objectURL = 'data:image/jpeg;base64,' + data.image;
            data.image = objectURL;
          }
          this.authorComponent.showDetails(data);
          this.showAuthorDetail = true;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.log(error);
        }
      });
  }

  private doSearch(): void {
    this.reset();
    this.isLoading = true;
    this.cdr.detectChanges();

    // Primero hacer count, luego getAll solo si hay datos
    this.bookService.countRecommendationsByUser(this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (count) => {
          this.total = (count && typeof count === 'number') ? count : 0;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.books.recommendations.title2') + " (" + this.total + ")";

          // Solo llamar getAll si hay recomendaciones
          if (this.total > 0) {
            this.getAll();
          } else {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        },
        error: (error) => {
          console.log('Error counting recommendations:', error);
          this.total = 0;
          this.title = this.translate.instant('locale.books.recommendations.title2') + " (0)";
          this.isLoading = false;
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.books.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  getBooksByAuthor(author: string): void {
    const search: Search = new Search();
    search.author = author;
    this.router.navigate(["books"], {
      queryParams: {
        adv_search: JSON.stringify(search),
        author: JSON.stringify(author)
      }
    });
  }

  private reset(): void {
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;
    this.selectedSort = sessionStorage.getItem('books_order');

    if (!this.selectedSort) {
      this.sort = "count";
      this.order = "desc";
      this.selectedSort = this.sort + "," + this.order;
    } else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }

    this.books.length = 0;
  }
}
