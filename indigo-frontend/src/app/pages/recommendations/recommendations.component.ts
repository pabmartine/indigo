import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';
import { DetailComponent } from '../detail/detail.component';
import { AuthorComponent } from '../author/author.component';
import { Author } from 'src/app/domain/author';
import { lastValueFrom } from 'rxjs';



@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css'],
  providers: [MessageService]
})
export class RecommendationsComponent implements OnInit {

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

  navigationSubscription: any;

  mobHeight: any;
  mobWidth: any;


  constructor(
    private bookService: BookService,
    private router: Router,
    private route: ActivatedRoute,
    private authorService: AuthorService,
    private messageService: MessageService,
    public translate: TranslateService) {

    //defines the number of elements to retrieve according to the width of the screen
    if (window.screen.width < 640) {
      this.size = 10;
    } else if (window.screen.width < 1024) {
      this.size = 20;
    } else {
      this.size = 60;
    }

    this.sorts.push(
      { label: this.translate.instant('locale.books.order_by.count.desc'), value: 'count,desc' },
      // { label: this.translate.instant('locale.books.order_by.count.asc'), value: 'count,asc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.desc'), value: 'pubDate,desc' },
      { label: this.translate.instant('locale.books.order_by.pubdate.asc'), value: 'pubDate,asc' },
      { label: this.translate.instant('locale.books.order_by.title.asc'), value: 'title,asc' },
      { label: this.translate.instant('locale.books.order_by.title.desc'), value: 'title,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.desc'), value: 'rating,desc' },
      { label: this.translate.instant('locale.books.order_by.rating.asc'), value: 'rating,asc' }

    );

    this.showGoUpButton = false;



    // subscribe to the router events. Store the subscription so we can
    // unsubscribe later.
    this.navigationSubscription = this.router.events.subscribe((e: any) => {
      // If it is a NavigationEnd event re-initalise the component
      if (e instanceof NavigationEnd) {
        if (this.router.url == "/recommendations" && !sessionStorage.getItem("position")) {
          this.doSearch();
        }
      }
    });


  }

  ngOnInit(): void {
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

  async count() {
    try {
      const user = JSON.parse(sessionStorage.user);
      const data = await lastValueFrom(this.bookService.countRecommendationsByUser(user.username));

      this.total = data;
      this.lastPage = this.total / this.size;
      this.title = this.translate.instant('locale.books.recommendations.title2') + " (" + this.total + ")";

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
    }
  }


  async getAll() {
    try {
      const user = JSON.parse(sessionStorage.user);
      const data = await lastValueFrom(this.bookService.getRecommendationsByUser(user.username, this.page, this.size, this.sort, this.order));

      data.forEach((book) => {
        let objectURL = 'data:image/jpeg;base64,' + book.image;
        book.image = objectURL;
      });

      Array.prototype.push.apply(this.books, data);
      this.page++;

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.data'), closable: false, life: 5000 });
    }
  }



  showDetail: boolean;

  showDetails(book: Book) {
    //save current data in session
    //sessionStorage.setItem("position", document.documentElement.scrollTop.toString());
    //this.router.navigate(["detail"], { queryParams: { book: JSON.stringify(book) } });

    this.detailComponent.showDetails(book);
    this.showDetail = true;
  }

  closeDetails() {
    this.showDetail = false;
  }
  openDetails() {
    this.showDetail = true;
  }

  showAuthorDetail: boolean;

  showAuthorDetails(author: Author) {
    this.authorComponent.showDetails(author);
  }

  closeAuthorDetails() {
    this.showAuthorDetail = false;
  }
  openAuthorDetails() {
    this.showAuthorDetail = true;
  }

  openBook(book: Book) {
    this.showAuthorDetail = false;
    this.detailComponent.showDetails(book);
  }

  async openAuthor(sort: string) {
    try {
      this.showDetail = false;
      const data = await lastValueFrom(this.authorService.getByName(sort));

      if (data && data.image) {
        let objectURL = 'data:image/jpeg;base64,' + data.image;
        data.image = objectURL;
      }

      this.authorComponent.showDetails(data);

    } catch (error) {
      console.log(error);
    }
  }



  private doSearch() {
    this.reset();
    this.count();
    this.getAll();
  }





  getBooksByAuthor(author: string) {
    let search: Search = new Search();
    search.author = author;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search), author: JSON.stringify(author) } });
  }

  private reset() {
    this.total = 0;
    this.page = 0;
    this.lastPage = 0;
    this.selectedSort = sessionStorage.getItem('books_order');
    if (!this.selectedSort) {
      this.sort = "count";
      this.order = "desc";
      this.selectedSort = this.sort + "," + this.order;
    }
    else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }

    this.books.length = 0;
  }


}
