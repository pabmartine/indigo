import { ChangeDetectorRef, Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from 'primeng/api';
import { SelectItem } from 'primeng/api/selectitem';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorComponent } from 'src/app/pages/author/author.component';
import { DetailComponent } from 'src/app/pages/detail/detail.component';
import { AuthorService } from 'src/app/services/author.service';


@Component({
  selector: 'app-authors',
  templateUrl: './authors.component.html',
  styleUrls: ['./authors.component.css'],
  providers: [MessageService]
})
export class AuthorsComponent implements OnInit {

  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;
  @ViewChild(DetailComponent) detailComponent: DetailComponent;

  authors: Author[] = [];
  favorites: Author[] = [];

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

  user = JSON.parse(sessionStorage.user);


  constructor(
    private authorService: AuthorService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private changeDetectorRef: ChangeDetectorRef) {

    //defines the number of elements to retrieve according to the width of the screen
    if (window.screen.width <= 640) {
      this.size = 10;
    } else if (window.screen.width <= 1024) {
      this.size = 20;
    } else {
      this.size = 80;
    }

    this.sorts.push(
      { label: this.translate.instant('locale.authors.order_by.total.desc'), value: 'numBooks.total,desc' },
      { label: this.translate.instant('locale.authors.order_by.total.asc'), value: 'numBooks.total,asc' },
      { label: this.translate.instant('locale.authors.order_by.sort.asc'), value: 'sort,asc' },
      { label: this.translate.instant('locale.authors.order_by.sort.desc'), value: 'sort,desc' }
    );
  }

  ngOnInit(): void {
    this.showGoUpButton = false;
    this.reset();

    const observables = [
      this.authorService.count(this.user.languageBooks).pipe(
        catchError(error => {
          console.error('Error fetching count:', error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
          return of(0);
        })
      ),
      this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order).pipe(
        catchError(error => {
          console.error('Error fetching initial authors:', error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
          return of([]);
        })
      ),
      this.authorService.getFavorites(this.user.username).pipe(
        catchError(error => {
          console.error('Error fetching favorites:', error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.favorites'), closable: false, life: 5000 });
          return of([]);
        })
      )
    ];

    forkJoin(observables).subscribe(
      ([countData, authorsData, favoritesData]: [number, Author[], Author[]]) => {
        this.total = countData;
        this.lastPage = this.total / this.size;
        this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";

        this.authors = authorsData.map(author => {
          // author.image remains base64 string or path
          return author;
        });
        if (authorsData.length > 0) {
          this.page++;
        }

        this.favorites = favoritesData.map(author => {
          // author.image remains base64 string or path
          return author;
        });

        this.changeDetectorRef.detectChanges();
      },
      error => {
        // Generic error for forkJoin if needed, though individual catches are preferred
        console.error('Error in forkJoin for initial author data load:', error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.generic'), closable: false, life: 5000 });
      }
    );
  }

  private fetchSortedInitialAuthors() {
    const observables = [
      this.authorService.count(this.user.languageBooks).pipe(
        catchError(error => {
          console.error('Error fetching count:', error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
          return of(0);
        })
      ),
      this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order).pipe(
        catchError(error => {
          console.error('Error fetching sorted initial authors:', error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
          return of([]);
        })
      )
    ];

    forkJoin(observables).subscribe(
      ([countData, authorsData]: [number, Author[]]) => {
        this.total = countData;
        this.lastPage = this.total / this.size;
        // Title update might not be strictly necessary here if only sort order changes, not the total count logic.
        // However, if count could change due to some backend logic with sorting, it's safer.
        this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";


        this.authors = authorsData.map(author => {
          // author.image remains base64 string or path
          return author;
        });

        if (authorsData.length > 0) {
          this.page++;
        }
        this.changeDetectorRef.detectChanges();
      },
      error => {
        console.error('Error in forkJoin for sorted initial authors:', error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.generic'), closable: false, life: 5000 });
      }
    );
  }


  onChange(event) {
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('authors_order', this.selectedSort);

    this.page = 0;
    this.authors.length = 0;
    // Favorites are not reloaded on sort, so this.favorites.length = 0; is removed.
    this.fetchSortedInitialAuthors();
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

  onScroll() {
    if (this.authors.length < this.total) {
      this.getAllForScroll();
    }
  }

  scrollTop() {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  getAllForScroll() {
    this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order).subscribe(
      data => {
        data.forEach(author => {
          // author.image remains base64 string or path
        });

        Array.prototype.push.apply(this.authors, data);
        this.changeDetectorRef.detectChanges();
        this.page++;
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
      }
    );
  }

  getBooksByAuthor(author: Author) {
    // this.reset(); // Reset might not be needed here if navigating away
    let search: Search = new Search();
    search.author = author.sort;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search), author: JSON.stringify(author) } });
  }

  private reset() {
    this.authors.length = 0;
    this.favorites.length = 0;
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;

    this.selectedSort = sessionStorage.getItem('authors_order');
    if (!this.selectedSort) {
      this.sort = "sort";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    }
    else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }

  }



  showDetail: boolean;

  showDetails(author: Author) {
    this.authorComponent.showDetails(author);
  }

  closeDetails() {
    this.showDetail = false;
  }
  openDetails() {
    this.showDetail = true;
  }


  showBookDetail: boolean;

  openBook(book: Book) {
    this.showDetail = false;
    this.detailComponent.showDetails(book);
  }

  openAuthor(sort: string) {
    this.showBookDetail = false;
    this.authorService.getByName(sort).subscribe(
      data => {
        if (data)
          if (data.image) {
            let objectURL = 'data:image/jpeg;base64,' + data.image;
            data.image = objectURL;
          }
        this.authorComponent.showDetails(data);
      },
      error => {
        console.log(error);
      }
    );


  }

  refreshAuthor(author: Author) {
    const index = this.authors.findIndex((b) => b.id === author.id);
    if (index !== -1) {
      this.authors[index] = author;
    } 
  }

  closeBookDetails() {
    this.showDetail = false;
    this.showBookDetail = false;
  }
  openBookDetails() {
    this.showDetail = false;
    this.showBookDetail = true;
  }
}
