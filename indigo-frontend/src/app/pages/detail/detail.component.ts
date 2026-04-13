import { DatePipe, Location } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem, MessageService } from 'primeng/api';
import { Dialog } from 'primeng/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Book } from 'src/app/domain/book';
import { Notification } from 'src/app/domain/notification';
import { Search } from 'src/app/domain/search';
import { Serie } from 'src/app/domain/serie';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { ConfigService } from 'src/app/services/config.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { NotificationService } from 'src/app/services/notification.service';
import { MailService } from 'src/app/services/mail.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { ImageService } from 'src/app/utils/image.service';
import { User } from 'src/app/domain/user';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  providers: [MessageService, DatePipe]

})
export class DetailComponent implements OnInit, OnDestroy {

  @Output() eventAuthor: EventEmitter<String> = new EventEmitter<String>();
  @Output() eventBook: EventEmitter<Book> = new EventEmitter<Book>();
  @Output() deleteBookEvent: EventEmitter<String> = new EventEmitter<String>();

  serie: Book[] = [];
  similar: Book[] = [];
  recommendations: Book[] = [];
  selected: Book;
  selectedImage: string;
  title: string;
  kindle: boolean;
  favoriteBook: boolean;
  showEpub: boolean;

  expandRecommendations: boolean;
  showExpandRecommendations: boolean;

  expandSerie: boolean;
  showExpandSerie: boolean;

  expandReview: boolean;
  showExpandReview: boolean;

  expandSimilar: boolean;
  showExpandSimilar: boolean;

  private adv_search: Search;

  public chapterList = [];

  public book;
  public rendition;
  public displayed;

  items: MenuItem[];
  editDialog: boolean = false;
  editedBook: Book;

  user: User;

  private destroy$ = new Subject<void>();

  @ViewChild('viewer') viewer: ElementRef;

  private epubFactory: any | null = null;

  constructor(
    private bookService: BookService,
    private mailService: MailService,
    private configService: ConfigService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private metadataService: MetadataService,
    public translate: TranslateService,
    public notificationService: NotificationService,
    private location: Location,
    public datepipe: DatePipe,
    private authState: AuthStateService,
    private imageService: ImageService) {

    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;

    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        if (params['book']) {
          this.showDetails(JSON.parse(params['book']));
        }
      });

  }

  ngOnInit(): void {
    this.items = [
      {
        id: 'deleteBook',
        label: this.translate.instant('locale.buttons.delete'),
        icon: 'pi pi-trash',
        command: () => this.deleteBook()
      },
      {
        id: 'editBook',
        label: this.translate.instant('locale.buttons.edit'),
        icon: 'pi pi-pencil',
        command: () => this.editBook()
      },
      {
        id: 'refreshBook',
        label: this.translate.instant('locale.buttons.refresh'),
        icon: 'pi pi-refresh',
        command: () => this.refreshBook()
      }
    ];
  }

  getSerie(serie: Serie): void {
    if (serie) {
      this.bookService.getSerie(serie.name, this.user.languageBooks)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          error: (error) => {
          }
        });
    }
  }


  getSimilar(similar: string[]): void {
    if (similar) {
      this.bookService.getSimilar(similar, this.user.languageBooks)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            data.forEach((book) => {
              const coverUrl = this.bookService.buildCoverImageUrl(book.id)
              book.image = coverUrl || book.image
            });
            Array.prototype.push.apply(this.similar, data);
          },
          error: (error) => {
          }
        });
    }
  }



  getRecommendations(recommendations: string[]): void {
    if (recommendations) {
      this.bookService.getRecommendationsByBook(recommendations, this.user.languageBooks)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            data.forEach((book) => {
              const coverUrl = this.bookService.buildCoverImageUrl(book.id)
              book.image = coverUrl || book.image
            });
            Array.prototype.push.apply(this.recommendations, data);
          },
          error: (error) => {
          }
        });
    }
  }




  showDetails(book: Book) {
    this.close();

    this.selected = book;
    this.selectedImage = this.bookService.buildCoverImageUrl(book.id);
    this.selected.image = this.selectedImage;
    this.editedBook = new Book();
    this.kindle = false;
    this.favoriteBook = false;
    this.serie.length = 0;
    this.similar.length = 0;
    this.recommendations.length = 0;
    this.getSerie(book.serie);
    this.getSimilar(book.similar);
    this.getRecommendations(book.recommendations);
    this.getKindle();
    this.getFavoriteBook(book.path);
    this.view(book.path);

    setTimeout(() => {
      this.open();
      this.checkOverflowSerie();
      this.checkOverflowReview();
      this.checkOverflowSimilar();
      this.checkOverflowRecommendations();
    }, 200)

  }

  openAuthor(author: string) {
    this.eventAuthor.emit(author);
  }

  getBooksByTag(tag: string) {
    this.selected = null;

    this.adv_search = new Search();
    this.adv_search.selectedTags = [];
    this.adv_search.selectedTags.push(tag);
    this.doSearch();

  }

  getBooksBySerie(serie: string) {
    this.selected = null;
    this.adv_search = new Search();
    this.adv_search.serie = serie;
    this.doSearch();
  }

  private doSearch() {
    this.close();
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(this.adv_search) } });
  }




  sendToKindle(): void {
    const book = this.selected.path;

    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.todo'), closable: false, life: 5000 });

    this.mailService.sendMail(book, this.user.kindle)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.ok'), closable: false, life: 5000 });


        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.kindle.error'), closable: false, life: 5000 });


        }
      });
  }



  getKindle(): void {
    this.configService.get("smtp.status")
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data.value == 'ok') {
            this.kindle = true;
          }
        },
        error: (error) => {
        }
      });
  }


  getFavoriteBook(id: string): void {
    this.bookService.getFavorite(id, this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            this.favoriteBook = true;
          }
        },
        error: (error) => {
        }
      });
  }


  view(id: string) {
    this.bookService.view(id, this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        error => {
        }
      );
  }

  addFavoriteBook(): void {
    this.bookService.addFavorite(this.selected.path, this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.favoriteBook = true;
          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.add.ok'), closable: false, life: 5000 });
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.favorite.add.error'), closable: false, life: 5000 });
        }
      });
  }




  deleteFavoriteBook(): void {
    this.bookService.deleteFavorite(this.selected.path, this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.favoriteBook = false;
          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.delete.ok'), closable: false, life: 5000 });
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.favorite.delete.error'), closable: false, life: 5000 });
        }
      });
  }


  viewEpub() {
    this.showEpub = true;

    this.bookService.getEpub(this.selected.path)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: async (data) => {
          if (data) {
            try {
              const Epub = await this.loadEpubFactory();
              var file = new File([data], "name");
              this.book = Epub(file);

              this.rendition = this.book.renderTo("viewer", { flow: "paginated", method: "continuous", width: "100%", height: "97%" });
              this.displayed = this.rendition.display();

              this.displayed.then((renderer) => {
              }).catch((error) => {
                console.error('Error displaying epub:', error);
                this.handleEpubError();
              });

              this.book.ready.then(() => {
                this.book.loaded.navigation.then((toc) => {
                  toc.forEach((chapter) => {
                    var ch = chapter;
                    this.chapterList.push(ch);
                  })
                }).catch((error) => {
                  console.error('Error loading navigation:', error);
                });

                this.book.locations.generate(64);
              }).catch((error) => {
                console.error('Error in book.ready:', error);
                this.handleEpubError();
              });

            } catch (error) {
              console.error('Error creating epub:', error);
              this.handleEpubError();
            }
          } else {
            this.handleEpubError();
          }
        },
        error: (error) => {
          console.error('Error fetching epub from server:', error);
          this.handleEpubError();
        }
      });
  }

  private async loadEpubFactory(): Promise<any> {
    if (this.epubFactory) {
      return this.epubFactory;
    }

    const epubModule = await import('epubjs');
    this.epubFactory = epubModule.default ?? epubModule;
    return this.epubFactory;
  }

  private handleEpubError(): void {
    this.showEpub = false;
    this.messageService.clear();
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: this.translate.instant('locale.books.detail.view.error') || 'Error loading EPUB file',
      closable: true,
      life: 5000
    });
    // Reset epub-related properties
    this.book = null;
    this.rendition = null;
    this.displayed = null;
    this.chapterList = [];
  }

  downloadEpub(): void {
    this.bookService.getEpub(this.selected.path)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          const filename = `${this.selected.title || 'book'}.epub`;
          const downloadUrl = URL.createObjectURL(data);
          const anchor = document.createElement('a');
          anchor.href = downloadUrl;
          anchor.download = filename;
          anchor.click();
          URL.revokeObjectURL(downloadUrl);
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.download.error'), closable: false, life: 5000 });
        }
      });
  }


  isAdmin() {
    return this.authState.isAdmin();
  }

  refreshBook(): void {
    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.process'), closable: false, life: 5000 });

    this.metadataService.findBook(this.selected.path, "es")
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.selected = data;

          const coverUrl = this.bookService.buildCoverImageUrl(this.selected.id) || this.selected.image;
          if (data.image) {
            this.selected.image = data.image.startsWith('http')
              ? data.image
              : this.imageService.toDataUrlSafe(data.image, coverUrl || '')
          } else {
            this.selected.image = coverUrl || this.selected.image;
          }
          this.selectedImage = this.selected.image;

          this.eventBook.emit(this.selected);

          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.result.ok'), closable: false, life: 5000 });
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.refresh.result.error'), closable: false, life: 5000 });
        }
      });
  }


  showDialogMaximized(dialog: Dialog) {
    dialog.maximize();
  }

  public prev() {
    if (this.rendition) {
      this.rendition.prev().then(() => {
      }).catch((error) => {
        console.error('Error navigating to previous page:', error);
      });
    }
  }
  public next() {
    if (this.rendition) {
      this.rendition.next().then(() => {
      }).catch((error) => {
        console.error('Error navigating to next page:', error);
      });
    }
  }


  public changeChapter(url) {
    if (this.rendition) {
      this.rendition.display(url);
    }
    return false;
  }


  @Output() eventClose: EventEmitter<void> = new EventEmitter<void>();

  close() {
    this.eventClose.emit();
  }

  @Output() eventOpen: EventEmitter<void> = new EventEmitter<void>();

  open() {
    this.eventOpen.emit();
  }

  deleteBook(): void {
    this.bookService.deleteBook(this.selected.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.deleteBookEvent.emit(this.selected.id);
          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.delete.ok'), closable: false, life: 5000 });
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.delete.error'), closable: false, life: 5000 });
        }
      });
  }


  editBook() {
    this.editedBook = this.selected;
    this.editDialog = true;
    //this.close();
  }

  saveBook(): void {
    this.bookService.editBook(this.editedBook)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.eventBook.emit(this.editedBook);
          this.messageService.clear();
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.edit.ok'), closable: false, life: 5000 });
        },
        error: (error) => {
          this.messageService.clear();
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.edit.error'), closable: false, life: 5000 });
        }
      });
  }


  checkOverflowRecommendations() {
    let row = document.getElementById('inlineRecommendations');
    if (row)
      this.showExpandRecommendations = this.isOverFlowed(row);
  }

  checkOverflowSimilar() {
    let row = document.getElementById('inlineSimilar');
    if (row)
      this.showExpandSimilar = this.isOverFlowed(row);
  }

  checkOverflowSerie() {
    let row = document.getElementById('inlineSerie');
    if (row)
      this.showExpandSerie = this.isOverFlowed(row);
  }

  checkOverflowReview() {
    let row = document.getElementById('inlineReview');
    if (row)
      this.showExpandReview = this.isOverFlowed(row);
  }

  isOverFlowed(element) {
    if (element) {
      return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
    }
    return false;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.checkOverflowRecommendations();
    this.checkOverflowSimilar();
    this.checkOverflowSerie();
    this.checkOverflowReview();
  }

  toDate(date: string): Date {
    let d = date.split("/");
    let dat = new Date(d[2] + '/' + d[1] + '/' + d[0]);
    return dat;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
