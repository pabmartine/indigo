import { Component, HostListener, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { SelectItem } from 'primeng/api/selectitem';
import { Author } from 'src/app/domain/author';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';



@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css'],
  providers: [MessageService]
})
export class RecommendationsComponent implements OnInit {

  books: Book[] = [];

  title: string;

  showGoUpButton: boolean;
  private showScrollHeight = 400;
  private hideScrollHeight = 200;

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


  getCover(book: Book) {
    this.bookService.getCover(book.path).subscribe(
      data => {
        if (data) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          book.image = objectURL;
        }
      },
      error => {
        console.log(error);
      }
    );
  }


  showDetails(book: Book) {
    //save current data in session
    sessionStorage.setItem("position", document.documentElement.scrollTop.toString());
    this.router.navigate(["detail"], { queryParams: { book: JSON.stringify(book) } });
  }



  private doSearch() {
    this.reset();
    this.getAll();
  }



  getAll() {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.getRecommendationsByUser(user.username).subscribe(
      data => {
        this.books.length = 0
        data.forEach((book) => {
          this.getCover(book);
        });

        Array.prototype.push.apply(this.books, data);
      },
      error => {
        console.log(error);
      }
    );
  }


  getBooksByAuthor(author: string) {
    let search: Search = new Search();
    search.author = author;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search), author: JSON.stringify(author) } });
  }

  private reset() {
    this.books.length = 0;
  }


}
