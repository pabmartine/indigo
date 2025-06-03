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
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
  }

  isAdmin() {
    return this.user.role == 'ADMIN';
  }

  showDetails(author: Author) {
    this.close();
    this.selected = author;
    this.user = JSON.parse(sessionStorage.user);
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
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          this.selected.image = objectURL;
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
    this.authorService.addFavorite(this.selected.sort, this.user.username).subscribe({
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
    this.authorService.deleteFavorite(this.selected.sort, this.user.username).subscribe({
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
    this.authorService.getFavorite(this.selected.sort, this.user.username).subscribe({
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
    this.books = []; // Usar asignación directa en lugar de .length = 0

    this.adv_search = new Search();
    this.adv_search.author = this.selected.name;
    this.adv_search.languages = this.user.languageBooks;

    this.count();
  }

  count(): void {
    this.bookService.count(this.adv_search).subscribe({
      next: (data) => {
        this.total = data;
        let author = this.adv_search.author;
        this.title = this.translate.instant('locale.books.title_published') + "  (" + this.total + ")";

        // Forzar detección de cambios después de actualizar el título
        this.cdr.detectChanges();

        this.getAll();
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

  getAll(): void {
    this.bookService.getAll(this.adv_search, 0, this.total, "pubDate", "desc").subscribe({
      next: (data) => {
        // Procesar las imágenes de los libros
        const processedBooks = data.map(book => ({
          ...book,
          image: 'data:image/jpeg;base64,' + book.image
        }));

        // Agregar los libros procesados al array existente
        this.books = [...this.books, ...processedBooks];

        // Forzar detección de cambios inmediatamente después de actualizar los libros
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
