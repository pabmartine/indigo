import { Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';
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
    public translate: TranslateService) { }

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
    }, 200)

  }


  close() {
    this.eventClose.emit();
  }

  open() {
    this.eventOpen.emit();
  }

  openBook(book: Book){
    this.eventBook.emit(book);
  }

  refreshAuthor() {
    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.authors.refresh.process'), closable: false, life: 5000 });
    this.metadataService.findAuthor("es", this.selected.sort).subscribe(
      data => {

        this.selected = data;

        console.log(data.image);

        if (data.image && !data.image.startsWith('http')) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          this.selected.image = objectURL;
        }
        if (data.image && data.image.startsWith('http')) {
          this.selected.image = "./assets/images/avatar3.jpg";
        }

        this.eventAuthor.emit(this.selected);

        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.authors.refresh.result.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.refresh.result.error'), closable: false, life: 5000 });
      }
    );
  }

  addFavoriteAuthor() {
    this.authorService.addFavorite(this.selected.sort, this.user.username).subscribe(
      data => {
        this.favoriteAuthor = true;
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.authors.favorites.add.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.favorites.add.error'), closable: false, life: 5000 });
      }
    );
  }


  deleteFavoriteAuthor() {
    this.authorService.deleteFavorite(this.selected.sort, this.user.username).subscribe(
      data => {
        this.favoriteAuthor = false;
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.authors.favorites.delete.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.favorites.delete.error'), closable: false, life: 5000 });
      }
    );
  }

  getFavoriteAuthor() {
    this.authorService.getFavorite(this.selected.sort, this.user.username).subscribe(
      data => {
        if (data) {
          this.favoriteAuthor = true;
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  checkOverflowBooks() {
    let row = document.getElementById('inlineBooks');
    this.showExpandBooks = this.isOverFlowed(row);
  }

  isOverFlowed(element) {
    return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.checkOverflowBooks();
  }


  private doSearch() {
    this.total = 0;
    this.books.length = 0;

    this.adv_search = new Search();
    this.adv_search.author = this.selected.name;
    this.adv_search.languages = this.user.languageBooks;

    this.count();

  }

  count() {
    this.bookService.count(this.adv_search).subscribe(
      data => {
        this.total = data;
        let author = this.adv_search.author;
        this.title = this.translate.instant('locale.books.title_published') + "  (" + this.total + ")";

        this.getAll();
        
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
      }
    );
  }

  getAll() {
    this.bookService.getAll(this.adv_search, 0, this.total, "pubDate", "desc").subscribe(
      data => {

        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.books, data);
       
        setTimeout(() => {
          this.checkOverflowBooks();
        }, 200)
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
      }
    );
  }

}
