import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { SelectItem } from 'primeng/api/selectitem';
import { forkJoin, of, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorComponent } from 'src/app/pages/author/author.component';
import { DetailComponent } from 'src/app/pages/detail/detail.component';
import { AuthorService } from 'src/app/services/author.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { User } from 'src/app/domain/user';
import { ImageService } from 'src/app/utils/image.service';

@Component({
  selector: 'app-authors',
  templateUrl: './authors.component.html',
  styleUrls: ['./authors.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class AuthorsComponent implements OnInit, OnDestroy {

  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;
  @ViewChild(DetailComponent) detailComponent: DetailComponent;

  authors: Author[] = [];
  favorites: Author[] = [];

  title: string;

  total: number = 0;

  private page: number = 0;
  private lastPage: number = 0;

  private size: number;
  private sort: string = "name";
  private order: string = "asc";

  sorts: SelectItem[] = [
    { label: 'Total (Desc)', value: 'numBooks.total,desc' },
    { label: 'Total (Asc)', value: 'numBooks.total,asc' },
    { label: 'Name (Asc)', value: 'name,asc' },
    { label: 'Name (Desc)', value: 'name,desc' }
  ];
  selectedSort: string;

  user: User;

  // Cache para optimizar rendimiento
  private authorsCache = new Map<string, Author[]>();
  private favoritesCache: Author[] = null;

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();

  // Estados de diálogos
  showDetail: boolean = false;
  showBookDetail: boolean = false;

  // Estado de carga
  isLoading: boolean = false;
  isScrolling: boolean = false;

  constructor(
    private authorService: AuthorService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef,
    private authState: AuthStateService,
    private imageService: ImageService
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.authorsCache.clear();
  }

  private initializeScreenSize(): void {
    // Define el número de elementos según el ancho de pantalla
    if (window.screen.width <= 640) {
      this.size = 10;
    } else if (window.screen.width <= 1024) {
      this.size = 20;
    } else {
      this.size = 80;
    }
  }

  private initializeSortOptions(): void {
    const translationKeys = [
      'locale.authors.order_by.total.desc',
      'locale.authors.order_by.total.asc',
      'locale.authors.order_by.sort.asc',
      'locale.authors.order_by.sort.desc'
    ];

    this.translate.get(translationKeys)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (translations) => {
          if (translations && Object.keys(translations).length > 0) {
            this.sorts = [
              { label: translations['locale.authors.order_by.total.desc'] || 'Total (Desc)', value: 'numBooks.total,desc' },
              { label: translations['locale.authors.order_by.total.asc'] || 'Total (Asc)', value: 'numBooks.total,asc' },
              { label: translations['locale.authors.order_by.sort.asc'] || 'Name (Asc)', value: 'name,asc' },
              { label: translations['locale.authors.order_by.sort.desc'] || 'Name (Desc)', value: 'name,desc' }
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

    const count$ = this.authorService.count(this.user.languageBooks);
    const authors$ = this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order);
    const favorites$ = (this.user && this.user.username)
      ? this.authorService.getFavorites(this.user.username)
      : of([]);

    forkJoin({ count: count$, authors: authors$, favorites: favorites$ })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ count, authors, favorites }) => {
          // Process Count
          this.total = count;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";

          // Process Authors
          const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;
          const authorsWithoutImages = authors.map(author => ({
            ...author,
            image: null,
            originalImage: author.image
          }));
          Array.prototype.push.apply(this.authors, authorsWithoutImages);
          this.page++;
          this.cdr.detectChanges();
          this.processAuthorsImagesAsync(authorsWithoutImages, 0);
          const processedData = this.processAuthors(authors);
          this.authorsCache.set(cacheKey, processedData);

          // Process Favorites
          if (favorites && favorites.length > 0) {
            const favoritesWithoutImages = favorites.map(author => ({
              ...author,
              image: null,
              originalImage: author.image
            }));
            this.favorites = favoritesWithoutImages;
            this.processFavoritesImagesAsync(favoritesWithoutImages);
            const processedFavorites = this.processAuthors(favorites);
            this.favoritesCache = [...processedFavorites];
          }

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading initial data in parallel:', error);
          this.isLoading = false;
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.authors.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  // Método para trackBy en ngFor
  trackByAuthorId(index: number, author: Author): string {
    return author.id || index.toString();
  }

  onChange(event): void {
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('authors_order', this.selectedSort);

    this.page = 0;
    this.authors.length = 0;
    this.authorsCache.clear(); // Limpiar cache cuando cambia el orden

    this.getAll();
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const scrollPosition = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop

    // Infinite scroll detection - trigger when user is near bottom
    const windowHeight = window.innerHeight
    const documentHeight = document.documentElement.scrollHeight
    const scrollThreshold = 300 // pixels from bottom to trigger load

    if (scrollPosition + windowHeight >= documentHeight - scrollThreshold) {
      this.onScroll()
    }
  }

  onScroll(): void {
    if (this.authors.length < this.total && !this.isScrolling) {
      this.getAll();
    }
  }

  getAll(): void {
    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;

    // Verificar cache
    if (this.authorsCache.has(cacheKey)) {
      const cachedData = this.authorsCache.get(cacheKey);
      Array.prototype.push.apply(this.authors, cachedData);
      this.page++;
      this.cdr.detectChanges();
      return;
    }

    this.isScrolling = true;
    this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          // INMEDIATAMENTE mostrar autores sin procesar imágenes
          const authorsWithoutImages = data.map(author => ({
            ...author,
            image: null, // Temporalmente sin imagen
            originalImage: author.image // Guardar imagen original
          }));

          // Mostrar datos inmediatamente
          Array.prototype.push.apply(this.authors, authorsWithoutImages);
          this.page++;
          this.cdr.detectChanges();

          // Procesar imágenes de forma asíncrona
          this.processAuthorsImagesAsync(authorsWithoutImages, this.authors.length - authorsWithoutImages.length);

          // Guardar en cache con imágenes procesadas para futuras cargas
          const processedData = this.processAuthors(data);
          this.authorsCache.set(cacheKey, processedData);
          this.isScrolling = false;
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.authors.error.data'),
            closable: false,
            life: 5000
          });
          this.isScrolling = false;
        }
      });
  }

  private processAuthors(data: Author[]): Author[] {
    return data.map(author => {
      if (author.image) {
        author.image = this.imageService.toDataUrlSafe(author.image);
      }
      return author;
    });
  }

  private processAuthorsImagesAsync(authors: any[], startIndex: number): void {
    // Procesar imágenes en pequeños lotes para no bloquear la UI
    const batchSize = 3;
    let currentIndex = 0;

    const processBatch = () => {
      const endIndex = Math.min(currentIndex + batchSize, authors.length);

      for (let i = currentIndex; i < endIndex; i++) {
        const author = authors[i];
        const targetIndex = startIndex + i;

        if (author.originalImage && targetIndex < this.authors.length) {
          // Procesar imagen de forma asíncrona
          setTimeout(() => {
            this.authors[targetIndex].image = this.imageService.toDataUrlSafe(author.originalImage);
            this.cdr.detectChanges();
          }, i * 15); // Pequeño delay entre imágenes
        }
      }

      currentIndex = endIndex;

      // Continuar con el siguiente lote si hay más imágenes
      if (currentIndex < authors.length) {
        setTimeout(processBatch, 100); // Pausa entre lotes
      }
    };

    // Iniciar procesamiento
    setTimeout(processBatch, 150);
  }

  private processFavoritesImagesAsync(favorites: any[]): void {
    favorites.forEach((author, index) => {
      if (author.originalImage) {
        setTimeout(() => {
          this.favorites[index].image = this.imageService.toDataUrlSafe(author.originalImage);
          this.cdr.detectChanges();
        }, index * 75); // Delay progresivo para suavizar la carga
      }
    });
  }

  getBooksByAuthor(author: Author): void {
    this.reset();

    const search: Search = new Search();
    search.author = author.sort;
    this.router.navigate(["books"], {
      queryParams: {
        adv_search: JSON.stringify(search),
        author: JSON.stringify(author)
      }
    });
  }

  private getFavoritesAsync(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Si el usuario no está logueado, no hay favoritos que cargar
      if (!this.user || !this.user.username) {
        console.log('User or username not available, not fetching favorites.');
        this.favorites = [];
        this.cdr.detectChanges();
        resolve();
        return;
      }

      console.log('Fetching author favorites for user:', this.user.username);
      // Usar cache si está disponible
      if (this.favoritesCache) {
        this.favorites = [...this.favoritesCache];
        this.cdr.detectChanges();
        resolve();
        return;
      }

      this.authorService.getFavorites(this.user.username)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            // INMEDIATAMENTE mostrar favoritos sin imágenes procesadas
            const favoritesWithoutImages = data.map(author => ({
              ...author,
              image: null,
              originalImage: author.image
            }));

            Array.prototype.push.apply(this.favorites, favoritesWithoutImages);
            this.cdr.detectChanges();

            // Procesar imágenes de favoritos de forma asíncrona
            this.processFavoritesImagesAsync(favoritesWithoutImages);

            // Guardar en cache con imágenes procesadas para futuras cargas
            const processedFavorites = data.map(author => {
              if (author.image) {
                author.image = this.imageService.toDataUrlSafe(author.image);
              }
              return author;
            });
            this.favoritesCache = [...processedFavorites];

            resolve();
          },
          error: (error) => {
            console.log(error);
            resolve(); // No rechazar, solo continuar
          }
        });
    });
  }

  getFavorites(): void {
    this.getFavoritesAsync();
  }

  private reset(): void {
    this.authors.length = 0;
    this.favorites.length = 0;
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;

    this.selectedSort = sessionStorage.getItem('authors_order');
    if (!this.selectedSort) {
      this.sort = "name";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    } else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }
  }

  showDetails(author: Author): void {
    if (!author || !this.authorComponent) return;

    this.authorComponent.showDetails(author);
    this.showDetail = true;
  }

  closeDetails(): void {
    this.showDetail = false;
  }

  openDetails(): void {
    this.showDetail = true;
  }

  openBook(book: Book): void {
    if (!book || !this.detailComponent) return;

    this.showDetail = false;
    this.detailComponent.showDetails(book);
    this.showBookDetail = true;
  }

  openAuthor(sort: string): void {
    if (!sort) return;

    this.showBookDetail = false;
    this.authorService.getByName(sort)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            if (data.image) {
              data.image = this.imageService.toDataUrlSafe(data.image);
            }
            this.authorComponent.showDetails(data);
            this.showDetail = true;
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }

  refreshAuthor(author: Author): void {
    if (!author) return;

    const index = this.authors.findIndex((a) => a.id === author.id);
    if (index !== -1) {
      this.authors[index] = author;
      this.cdr.detectChanges();
    }

    // También actualizar en favoritos si existe
    const favIndex = this.favorites.findIndex((a) => a.id === author.id);
    if (favIndex !== -1) {
      this.favorites[favIndex] = author;
      // Limpiar cache de favoritos para refrescar
      this.favoritesCache = null;
      this.cdr.detectChanges();
    }
  }

  closeBookDetails(): void {
    this.showDetail = false;
    this.showBookDetail = false;
  }

  openBookDetails(): void {
    this.showDetail = false;
    this.showBookDetail = true;
  }
}
