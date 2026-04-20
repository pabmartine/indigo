import { Component, EventEmitter, HostListener, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { Author } from 'src/app/domain/author';
import { MessageService } from 'primeng/api';
import { MetadataService } from 'src/app/services/metadata.service';
import { TranslateService } from '@ngx-translate/core';
import { AuthorService } from 'src/app/services/author.service';
import { User } from 'src/app/domain/user';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { BookService } from 'src/app/services/book.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { ImageService } from 'src/app/utils/image.service';

@Component({
  selector: 'app-author',
  templateUrl: './author.component.html',
  styleUrls: ['./author.component.css'],
  providers: [MessageService]
})
export class AuthorComponent implements OnInit {

  @Output() eventClose: EventEmitter<void> = new EventEmitter<void>();
  @Output() eventOpen: EventEmitter<void> = new EventEmitter<void>();
  @Output() eventBook: EventEmitter<Book> = new EventEmitter<Book>();
  @Output() eventAuthor: EventEmitter<Author> = new EventEmitter<Author>();

  selected: Author;
  favoriteAuthor: boolean;
  user: User;

  books: Book[] = [];
  expandBooks: boolean;
  showExpandBooks: boolean;

  title: string;
  private adv_search: Search;

  total: number;

  constructor(
    private bookService: BookService,
    private messageService: MessageService,
    private metadataService: MetadataService,
    private authorService: AuthorService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef,
    private authState: AuthStateService,
    private imageService: ImageService
  ) { }

  ngOnInit(): void {
  }

  isAdmin() {
    return this.user.role == 'ADMIN';
  }

  showDetails(author: Author) {
    this.close();
    this.selected = author;
    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;
    this.getFavoriteAuthor();
    this.doSearch();
    setTimeout(() => {
      this.open();
    }, 200);
  }

  close() {
    this.eventClose.emit();
  }

  open() {
    this.eventOpen.emit();
  }

  openBook(book: Book) {
    this.eventBook.emit(book);
  }

  refreshAuthor(): void {
    this.messageService.clear();
    this.messageService.add({
      severity: 'success',
      detail: this.translate.instant('locale.authors.refresh.process'),
      closable: false,
      life: 5000
    });

    this.metadataService.findAuthor("es", this.selected.sort).subscribe({
      next: (data) => {
        this.selected = data;

        if (data.image && !data.image.startsWith('http')) {
          this.selected.image = this.imageService.toDataUrlSafe(data.image);
        }
        if (data.image && data.image.startsWith('http')) {
          this.selected.image = "./assets/images/avatar3.jpg";
        }

        this.eventAuthor.emit(this.selected);

        this.messageService.clear();
        this.messageService.add({
          severity: 'success',
          detail: this.translate.instant('locale.authors.refresh.result.ok'),
          closable: false,
          life: 5000
        });

        // Forzar detección de cambios después de actualizar el autor
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({
          severity: 'error',
          detail: this.translate.instant('locale.authors.refresh.result.error'),
          closable: false,
          life: 5000
        });
      }
    });
  }

  addFavoriteAuthor(): void {
    this.authorService.addFavorite(this.selected.name, this.user.username).subscribe({
      next: (data) => {
        this.favoriteAuthor = true;
        this.messageService.clear();
        this.messageService.add({
          severity: 'success',
          detail: this.translate.instant('locale.authors.favorites.add.ok'),
          closable: false,
          life: 5000
        });

        // Forzar detección de cambios para actualizar el botón de favorito
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({
          severity: 'error',
          detail: this.translate.instant('locale.authors.favorites.add.error'),
          closable: false,
          life: 5000
        });
      }
    });
  }

  deleteFavoriteAuthor(): void {
    this.authorService.deleteFavorite(this.selected.name, this.user.username).subscribe({
      next: (data) => {
        this.favoriteAuthor = false;
        this.messageService.clear();
        this.messageService.add({
          severity: 'success',
          detail: this.translate.instant('locale.authors.favorites.delete.ok'),
          closable: false,
          life: 5000
        });

        // Forzar detección de cambios para actualizar el botón de favorito
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({
          severity: 'error',
          detail: this.translate.instant('locale.authors.favorites.delete.error'),
          closable: false,
          life: 5000
        });
      }
    });
  }

  getFavoriteAuthor(): void {
    this.authorService.getFavorite(this.selected.name, this.user.username).subscribe({
      next: (data) => {
        if (data) {
          this.favoriteAuthor = true;
        }
        // Forzar detección de cambios después de verificar favorito
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  checkOverflowBooks() {
    let row = document.getElementById('inlineBooks');
    if (row) {
      this.showExpandBooks = this.isOverFlowed(row);
      // Forzar detección de cambios después de verificar overflow
      this.cdr.detectChanges();
    }
  }

  isOverFlowed(element) {
    if (element) {
      return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
    }
    return false;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.checkOverflowBooks();
  }

  private doSearch() {
    this.total = 0;
    this.books = [];

    this.adv_search = new Search();
    this.adv_search.author = this.selected.name;
    this.adv_search.languages = this.user.languageBooks;

    this.getAll();
  }

  getAll(): void {
    this.bookService.getAllSummaryPage(this.adv_search, 0, 500, "pubDate", "desc").subscribe({
      next: (response) => {
        this.total = response.total || 0;
        this.title = this.translate.instant('locale.books.title_published') + "  (" + this.total + ")";

        const processedBooks = (response.items || []).map(book => ({
          ...book,
          image: undefined,
          coverUrl: this.bookService.buildCoverImageUrl(book.id) || book.image
        }));

        this.books = [...processedBooks];
        this.cdr.detectChanges();

        setTimeout(() => {
          this.checkOverflowBooks();
        }, 200);
      },
      error: (error) => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({
          severity: 'error',
          detail: this.translate.instant('locale.books.error.data'),
          closable: false,
          life: 5000
        });
      }
    });
  }
}
