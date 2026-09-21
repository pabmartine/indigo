import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';
import { AuthorComponent } from '../author/author.component';
import { DetailComponent } from '../detail/detail.component';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { User } from 'src/app/domain/user';
import { ImageService } from 'src/app/utils/image.service';

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class RecommendationsComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild(DetailComponent) detailComponent: DetailComponent;
  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;
  @ViewChild('scrollSentinel') scrollSentinel?: ElementRef<HTMLDivElement>;

  books: Book[] = [];

  title: string;
  total: number;

  private page: number;
  private size: number;
  private sort: string;
  private order: string;

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
  private resetPages$ = new Subject<void>();
  private hasMore = true;
  private scrollObserver?: IntersectionObserver;

  private user: User;

  constructor(
    private bookService: BookService,
    private router: Router,
    private authorService: AuthorService,
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
    this.initializeScreenSize();
  }

  ngOnInit(): void {
    this.initializeSortOptions();
    // Ejecutar búsqueda inicial
    this.doSearch();
  }

  ngAfterViewInit(): void {
    this.setupScrollObserver();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.resetPages$.complete();
    this.scrollObserver?.disconnect();
  }

  private initializeScreenSize(): void {
    // Define el número de elementos según el ancho de pantalla
    if (window.screen.width < 640) {
      this.size = 10;
    } else if (window.screen.width < 1024) {
      this.size = 20;
    } else {
      this.size = 20;
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

  }

  // Método para trackBy en ngFor
  trackByBookId(index: number, book: Book): string {
    return book.id ? book.id.toString() : index.toString();
  }

  private restoreScrollPosition(): void {
    const storedPosition = sessionStorage.getItem("position");
    if (!storedPosition) {
      return;
    }

    setTimeout(() => {
      document.documentElement.scrollTop = Number(storedPosition);
      sessionStorage.removeItem("position");
    });
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

  onChange(event): void {
    sessionStorage.setItem('recommendations_order', this.selectedSort);
    this.doSearch();
  }

  onScroll(): void {
    if (this.hasMore && !this.isLoading && this.books.length > 0) {
      this.getAll();
    }
  }

  getAll(): void {
    if (this.isLoading || !this.hasMore || !this.user.username) {
      return;
    }
    this.isLoading = true;
    this.bookService.getRecommendationSummaryPage(this.user.username, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$), takeUntil(this.resetPages$))
      .subscribe({
        next: (response) => {
          const items = response.items || [];
          const seen = new Set(this.books.map(book => book.id));
          this.books.push(...items.filter(book => !seen.has(book.id)).map(book => ({
            ...book,
            image: null,
            originalImage: this.bookService.buildCoverImageUrl(book.id),
            rating: book.rating ? Math.round(book.rating) : book.rating
          })));
          this.total = response.total;
          this.title = this.translate.instant('locale.books.recommendations.title2') + " (" + this.total + ")";
          this.page++;
          this.hasMore = items.length === this.size && this.page * this.size < this.total;
          this.isLoading = false;
          this.cdr.detectChanges();
          this.restoreScrollPosition();
        },
        error: () => {
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
            const objectURL = this.imageService.toDataUrlSafe(data.image);
            data.image = objectURL;
          }
          this.authorComponent.showDetails(data);
          this.showAuthorDetail = true;
          this.cdr.detectChanges();
        },
        error: (error) => {
        }
      });
  }

  private doSearch(): void {
    this.reset();
    this.getAll();
  }

  getBooksByAuthor(author: string): void {
    const search: Search = new Search();
    search.author = author;
    this.router.navigate(["books"], {
      queryParams: {
        adv_search: JSON.stringify(search),
        author: JSON.stringify({ name: author })
      }
    });
  }

  private reset(): void {
    this.resetPages$.next();
    this.isLoading = false;
    this.hasMore = true;
    this.total = 0;
    this.page = 0;
    this.books = [];
    const storedSort = sessionStorage.getItem('recommendations_order');
    this.selectedSort = this.sorts.some(option => option.value === storedSort) ? storedSort : 'count,desc';
    [this.sort, this.order] = this.selectedSort.split(',');
    this.title = this.translate.instant('locale.books.recommendations.title2');
    this.cdr.detectChanges();
  }
}
