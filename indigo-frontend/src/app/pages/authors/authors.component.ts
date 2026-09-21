import { CatalogViewport } from 'src/app/utils/catalog-viewport';
import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { SelectItem } from 'primeng/api/selectitem';
import { Subject } from 'rxjs';
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
export class AuthorsComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;
  @ViewChild(DetailComponent) detailComponent: DetailComponent;
  @ViewChild('catalogGrid') catalogGrid?: ElementRef<HTMLDivElement>;
  private viewport?: CatalogViewport;
  private initialFrame?: number;

  @ViewChild('scrollSentinel') scrollSentinel?: ElementRef<HTMLDivElement>;

  authors: Author[] = [];
  authorRows: Author[][] = [];
  favorites: Author[] = [];

  title: string;

  total: number = 0;

  private exhausted = false;
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
  private resetPages$ = new Subject<void>();

  // Estados de diálogos
  showDetail: boolean = false;
  showBookDetail: boolean = false;

  // Estado de carga
  isLoading: boolean = false;
  isScrolling: boolean = false;
  authorColumns = 5;
  authorRowHeight = 190;

  constructor(
    private authorService: AuthorService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private authState: AuthStateService,
    private imageService: ImageService
  ) {
    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;
    // Ensure languageBooks is always initialized
    if (!this.user.languageBooks || this.user.languageBooks.length === 0) {
      this.user.languageBooks = this.authState.getLanguageBooks();
    }
  }

  ngOnInit(): void {
    this.initializeSortOptions();
    this.reset();
  }

  ngAfterViewInit(): void {
    this.initialFrame = requestAnimationFrame(() => {
      this.viewport = new CatalogViewport(this.catalogGrid!.nativeElement,
        this.scrollSentinel!.nativeElement, () => this.ngZone.run(() => this.onScroll()));
      this.size = this.viewport.pageSize(185);
      this.ngZone.runOutsideAngular(() => this.viewport!.start());
      this.loadInitialData();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.resetPages$.complete();
    this.authorsCache.clear();
    this.viewport?.destroy();
    if (this.initialFrame !== undefined) cancelAnimationFrame(this.initialFrame);
  }

  private updateAuthorRows(): void {
    const rows: Author[][] = [];
    for (let index = 0; index < this.authors.length; index += this.authorColumns) {
      rows.push(this.authors.slice(index, index + this.authorColumns));
    }
    this.authorRows = rows;
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

  private loadInitialData(): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    if (this.user && this.user.username) {
      this.getFavorites();
    }

    this.authorService.getSummaryPage(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$), takeUntil(this.resetPages$))
      .subscribe({
        next: (response) => {
          this.exhausted = (response.items || []).length < this.size;
          this.total = response.total || 0;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";

          const processedAuthors = this.mapAuthorsWithCover(response.items || []);
          Array.prototype.push.apply(this.authors, processedAuthors);
          this.updateAuthorRows();
          this.page++;
          this.viewport?.refresh();

          const cacheKey = `${this.page - 1}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;
          this.authorsCache.set(cacheKey, processedAuthors);

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading initial authors data:', error);
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
    this.exhausted = false;
    this.resetPages$.next();
    this.isLoading = false;
    this.isScrolling = false;
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('authors_order', this.selectedSort);

    this.page = 0;
    this.authors.length = 0;
    this.authorRows = [];
    this.authorsCache.clear(); // Limpiar cache cuando cambia el orden

    this.getAll();
  }

  onScroll(): void {
    if (!this.exhausted && this.authors.length < this.total && !this.isLoading && !this.isScrolling) {
      this.getAll();
    }
  }

  onVirtualScrollIndexChange(index: number): void {
    const preloadThreshold = 3;
    if (index + preloadThreshold >= this.authorRows.length) {
      this.onScroll();
    }
  }

  getAll(): void {
    if (this.isLoading || this.isScrolling) {
      return;
    }
    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;

    // Verificar cache
    if (this.authorsCache.has(cacheKey)) {
      const cachedData = this.authorsCache.get(cacheKey);
      Array.prototype.push.apply(this.authors, cachedData);
      this.updateAuthorRows();
      this.page++;
      this.viewport?.refresh();
      this.cdr.detectChanges();
      return;
    }

    this.isScrolling = true;
    this.authorService.getSummaryPage(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$), takeUntil(this.resetPages$))
      .subscribe({
        next: (response) => {
          this.exhausted = (response.items || []).length < this.size;
          this.total = response.total || 0;
          this.lastPage = this.total / this.size;
          this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";

          const processedAuthors = this.mapAuthorsWithCover(response.items || []);
          Array.prototype.push.apply(this.authors, processedAuthors);
          this.updateAuthorRows();
          this.page++;
          this.viewport?.refresh();

          this.authorsCache.set(cacheKey, processedAuthors);
          this.isScrolling = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading authors page:', error);
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

  private mapAuthorsWithCover(authors: Author[]): Author[] {
    return (authors || []).map(author => ({
      ...author,
      image: undefined,
      originalImage: this.authorService.buildCoverImageUrl(author.id) || undefined
    }));
  }

  getBooksByAuthor(author: Author): void {
    this.reset();

    const search: Search = new Search();
    search.author = author.name;
    this.router.navigate(["books"], {
      queryParams: {
        adv_search: JSON.stringify(search),
        author: JSON.stringify(author)
      }
    });
  }

  private getFavoritesAsync(): Promise<void> {
    return new Promise((resolve) => {
      // Si el usuario no está logueado, no hay favoritos que cargar
      if (!this.user || !this.user.username) {
        this.favorites = [];
        this.cdr.detectChanges();
        resolve();
        return;
      }

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
            const favoritesWithCovers = this.mapAuthorsWithCover(data || []);
            this.favorites = favoritesWithCovers;
            this.favoritesCache = [...favoritesWithCovers];
            this.cdr.detectChanges();
            resolve();
          },
          error: (error) => {
            console.error('Error fetching favorites:', error);
            resolve(); // No rechazar, solo continuar
          }
        });
    });
  }

  getFavorites(): void {
    this.getFavoritesAsync();
  }

  private reset(): void {
    this.exhausted = false;
    this.resetPages$.next();
    this.isLoading = false;
    this.isScrolling = false;
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

    if (!author.description && author.sort) {
      this.authorService.getByName(author.sort)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            const authorData = data ? { ...data } : { ...author };
            if (!authorData.image && author.id) {
              authorData.image = this.authorService.buildCoverImageUrl(author.id);
            } else if (authorData.image) {
              authorData.image = this.imageService.toDataUrlSafe(authorData.image);
            }
            this.authorComponent.showDetails(authorData);
            this.showDetail = true;
            this.cdr.detectChanges();
          },
          error: () => {
            const fallback = { ...author };
            fallback.image = fallback.image || this.authorService.buildCoverImageUrl(fallback.id);
            this.authorComponent.showDetails(fallback);
            this.showDetail = true;
            this.cdr.detectChanges();
          }
        });
    } else {
      const authorCopy = { ...author };
      authorCopy.image = authorCopy.image || this.authorService.buildCoverImageUrl(authorCopy.id);
      this.authorComponent.showDetails(authorCopy);
      this.showDetail = true;
      this.cdr.detectChanges();
    }
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
            const authorData = { ...data };
            if (!authorData.image && authorData.id) {
              authorData.image = this.authorService.buildCoverImageUrl(authorData.id);
            } else if (authorData.image) {
              authorData.image = this.imageService.toDataUrlSafe(authorData.image);
            }
            this.authorComponent.showDetails(authorData);
            this.showDetail = true;
            this.cdr.detectChanges();
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }

  refreshAuthor(author: Author): void {
    if (!author) return;

    const coverUrl = this.authorService.buildCoverImageUrl(author.id);
    const updatedAuthor = {
      ...author,
      image: coverUrl || author.image,
      originalImage: coverUrl || author.originalImage
    };

    const index = this.authors.findIndex((a) => a.id === author.id);
    if (index !== -1) {
      this.authors[index] = updatedAuthor;
      this.updateAuthorRows();
      this.cdr.detectChanges();
    }

    // También actualizar en favoritos si existe
    const favIndex = this.favorites.findIndex((a) => a.id === author.id);
    if (favIndex !== -1) {
      this.favorites[favIndex] = updatedAuthor;
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
