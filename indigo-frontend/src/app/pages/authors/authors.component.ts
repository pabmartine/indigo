import { Component, OnInit, HostListener, ChangeDetectorRef } from '@angular/core';
import { SelectItem } from 'primeng/api/selectitem';
import { Author } from 'src/app/domain/author';
import { AuthorService } from 'src/app/services/author.service';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';


@Component({
  selector: 'app-authors',
  templateUrl: './authors.component.html',
  styleUrls: ['./authors.component.css'],
  providers: [MessageService]
})
export class AuthorsComponent implements OnInit {

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


  constructor(private authorService: AuthorService,
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
    this.count();
    this.getAll();
    this.getFavorites();
  }

  onChange(event) {

    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('authors_order', this.selectedSort);

    this.page = 0;
    this.authors.length = 0;
    this.favorites.length = 0

    this.getAll();
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
      this.getAll();
    }
  }

  scrollTop() {
    document.body.scrollTop = 0; // Safari
    document.documentElement.scrollTop = 0; // Other
  }

  count() {
    this.authorService.count(this.user.languageBooks).subscribe(
      data => {
        this.total = data;
        this.lastPage = this.total / this.size;
        this.title = this.translate.instant('locale.authors.title') + " (" + this.total + ")";
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.authors.error.data'), closable: false, life: 5000 });
      }
    );
  }

  getAll() {
    this.authorService.getAll(this.user.languageBooks, this.page, this.size, this.sort, this.order).subscribe(
      data => {

        data.forEach(author => {
          if (author.image){
            let objectURL = 'data:image/jpeg;base64,' + author.image;
            author.image = objectURL;
          }
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
    this.reset();

    let search: Search = new Search();
    search.author = author.sort;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search), author: JSON.stringify(author) } });
  }



  getFavorites() {
    const user = JSON.parse(sessionStorage.user);
    this.authorService.getFavorites(user.username).subscribe(
      data => {

        Array.prototype.push.apply(this.favorites, data);
        this.page++;
      },
      error => {
        console.log(error);
      }
    );
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
}
