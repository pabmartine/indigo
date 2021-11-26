import { Location } from '@angular/common';
import { Component, HostListener, OnInit, } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { SelectItem } from 'primeng/api/selectitem';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';
import { MetadataService } from 'src/app/services/metadata.service';



@Component({
  selector: 'app-books',
  templateUrl: './books.component.html',
  styleUrls: ['./books.component.css'],
  providers: [MessageService]
})
export class BooksComponent implements OnInit {

  books: Book[] = [];
  favorites: Book[] = [];
  authorInfo: Author;
  title: string;
  private adv_search: Search;
  favoriteAuthor: boolean;
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

  navigationSubscription: any;

  mobHeight: any;
  mobWidth: any;

  searched: boolean;

  user = JSON.parse(sessionStorage.user);


  constructor(
    private bookService: BookService,
    private router: Router,
    private route: ActivatedRoute,
    private authorService: AuthorService,
    private messageService: MessageService,
    private metadataService: MetadataService,
    public translate: TranslateService,
    private location: Location) {

    console.log("init -->" + this.searched);

    //defines the number of elements to retrieve according to the width of the screen
    if (window.screen.width < 640) {
      this.size = 10;
    } else if (window.screen.width < 1024) {
      this.size = 20;
    } else {
      this.size = 60;
    }



    this.sorts.push(
      { label: this.translate.instant('locale.books.order_by.id.desc'), value: 'id,desc' },
      { label: this.translate.instant('locale.books.order_by.id.asc'), value: 'id,asc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.desc'), value: 'pubDate,desc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.asc'), value: 'pubDate,asc' },
      { label: this.translate.instant('locale.books.order_by.title.asc'), value: 'title,asc' },
      { label: this.translate.instant('locale.books.order_by.title.desc'), value: 'title,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.desc'), value: 'rating,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.asc'), value: 'rating,asc' }

    );

    this.adv_search = null;
    this.showGoUpButton = false;



    // subscribe to the router events. Store the subscription so we can
    // unsubscribe later.
    this.navigationSubscription = this.router.events.subscribe((e: any) => {
      // If it is a NavigationEnd event re-initalise the component
      if (e instanceof NavigationEnd) {
        if (this.router.url == "/books" && !sessionStorage.getItem("position") && !this.searched) {
          this.adv_search = null;
          this.doSearch();
        }
      }
    });

    this.route.queryParams.subscribe(params => {

      if (params['author']) {
        this.authorInfo = JSON.parse(params['author']);
      } else {
        this.authorInfo = null;
      }

      if (params['adv_search']) {
        this.adv_search = JSON.parse(params['adv_search']);
        this.doSearch();
      }


    });


    if (!this.adv_search) {
      this.doSearch();
    }
  }

  ngOnInit(): void {
    console.log("init -->" + this.searched);
  }

  ngAfterViewChecked() {
    if (sessionStorage.getItem("position")) {
      document.documentElement.scrollTop = Number(sessionStorage.getItem("position"));
      sessionStorage.removeItem("position");
    }
  }

  ngOnDestroy() {
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
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



  onChange(event) {

    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('books_order', this.selectedSort);

    this.page = 0;
    this.books.length = 0
    this.favorites.length = 0

    this.getAll();
  }

  onScroll() {
    if (this.books.length > 0 && this.books.length < this.total) {
      this.getAll();
    }
  }

  scrollTop() {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  count() {
    this.bookService.count(this.adv_search).subscribe(
      data => {
        this.total = data;
        this.lastPage = this.total / this.size;

        //modifico aquí el título para que coja correctamente el valor del total
        if (this.isGlobalSearch(this.adv_search)) {
          this.title = this.translate.instant('locale.books.search_results') + this.adv_search.path + "  (" + this.total + ")";
        } else if (this.isAuthorSearch(this.adv_search)) {
          let author = this.adv_search.author;
          if (this.authorInfo)
            author = this.authorInfo.name;
          this.title = this.translate.instant('locale.books.title_of') + author + "  (" + this.total + ")";
        } else if (this.isTagSearch(this.adv_search)) {
          this.title = this.translate.instant('locale.books.title_of') + this.adv_search.selectedTags.join(', ') + "  (" + this.total + ")";
        } else if (this.isSerieSearch(this.adv_search)) {
          this.title = this.translate.instant('locale.books.title_of') + this.adv_search.serie + "  (" + this.total + ")";
          // } else if (this.adv_search) {
          //   this.title = this.translate.instant('locale.books.search_results').slice(0, -2) + " (" + this.total + ")";
        } else {
          this.title = this.translate.instant('locale.books.title') + " (" + this.total + ")";
        }
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
      }
    );
  }

  getAll() {
    this.bookService.getAll(this.adv_search, this.page, this.size, this.sort, this.order).subscribe(
      data => {

        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.books, data);
        this.page++;
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
      }
    );
  }





  showDetails(book: Book) {
    //save current data in session
    sessionStorage.setItem("position", document.documentElement.scrollTop.toString());
    this.router.navigate(["detail"], { queryParams: { book: JSON.stringify(book) }, skipLocationChange: true });
  }



  private doSearch() {
    this.reset();
    this.searchAuthorInfo();

    if (!this.adv_search) {
      this.adv_search = new Search();
      this.getFavoritesBooks();
    } else {
      this.favorites.length = 0;
    }

    this.adv_search.languages = this.user.languageBooks;

    this.count();
    this.getAll();

    this.searched = true;

  }

  private searchAuthorInfo() {
    if (this.isAuthorSearch(this.adv_search)) {
      this.authorService.getByName(this.adv_search.author).subscribe(
        data => {
          if (data) {
            this.authorInfo = data;
            if (this.authorInfo.image) {
              let objectURL = 'data:image/jpeg;base64,' + data.image;
              this.authorInfo.image = objectURL;
            }
            this.getFavoriteAuthor();
          }
        },
        error => {
          console.log(error);
        }
      );


    }
  }

  getFavoriteAuthor() {
    this.authorService.getFavorite(this.authorInfo.sort, this.user.username).subscribe(
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

  getFavoritesBooks() {
    this.bookService.getFavorites(this.user.username).subscribe(
      data => {
        this.favorites.length = 0
        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.favorites, data);
      },
      error => {
        console.log(error);
      }
    );
  }

  addFavoriteAuthor() {
    this.authorService.addFavorite(this.authorInfo.sort, this.user.username).subscribe(
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
    this.authorService.deleteFavorite(this.authorInfo.sort, this.user.username).subscribe(
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

  getBooksByAuthor(author: string) {
    this.adv_search = new Search();
    this.adv_search.author = author;
    this.doSearch();
  }

  isAdmin() {
    return JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  refreshAuthor() {
    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.authors.refresh.process'), closable: false, life: 5000 });
    this.metadataService.findAuthor("es", this.authorInfo.sort).subscribe(
      data => {
        this.authorInfo = data;

        if (data.image) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          this.authorInfo.image = objectURL;
        }
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

  private reset() {
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;


    this.selectedSort = sessionStorage.getItem('books_order');
    if (!this.selectedSort) {
      this.sort = "id";
      this.order = "desc";
      this.selectedSort = this.sort + "," + this.order;
    }
    else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }

    this.books.length = 0;
    this.favorites.length = 0;
  }

  public isGlobalSearch(search: Search) {
    return search && search.path && !search.title && !search.ini && !search.end && !search.min && !search.max && !search.selectedTags && !search.author && !search.serie;
  }

  public isAuthorSearch(search: Search) {
    return search && search.author && !search.title && !search.ini && !search.end && !search.min && !search.max && !search.selectedTags && !search.serie && !search.path;
  }

  public isTagSearch(search: Search) {
    return search && search.selectedTags && !search.title && !search.ini && !search.end && !search.min && !search.max && !search.author && !search.serie && !search.path;
  }

  public isSerieSearch(search: Search) {
    return search && search.serie && !search.title && !search.ini && !search.end && !search.min && !search.max && !search.selectedTags && !search.author && !search.path;
  }

  close() {
    //TODO actualizar la lista de favoritos en la vista pricipal
    this.location.back();
  }

}
