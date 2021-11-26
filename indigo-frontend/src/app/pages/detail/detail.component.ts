import { DatePipe, Location } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import * as epub from 'node_modules/epubjs/dist/epub.js';
import { MessageService } from 'primeng/api';
import { Dialog } from 'primeng/dialog';
import { Book } from 'src/app/domain/book';
import { Notif } from 'src/app/domain/notif';
import { Search } from 'src/app/domain/search';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { ConfigService } from 'src/app/services/config.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UtilService } from 'src/app/services/util.service';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  providers: [MessageService]

})
export class DetailComponent implements OnInit {

  similar: Book[] = [];
  recommendations: Book[] = [];
  selected: Book;
  title: string;
  kindle: boolean;
  favoriteBook: boolean;
  showEpub: boolean;

  private adv_search: Search;

  public chapterList = [];



  public book;
  public rendition;
  public displayed;

  @ViewChild('viewer') viewer: ElementRef;


  constructor(
    private bookService: BookService,
    private utilService: UtilService,
    private configService: ConfigService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private metadataService: MetadataService,
    public translate: TranslateService,
    public notificationService: NotificationService,
    private location: Location,
    public datepipe: DatePipe) {

    this.route.queryParams.subscribe(params => {

      if (params['book']) {
        this.showDetails(JSON.parse(params['book']));
      }
    });

  }

  ngOnInit(): void {
  }

  getSimilar(similar: string[]) {
    if (similar)
      this.bookService.getSimilar(similar).subscribe(
        data => {
          data.forEach((book) => {
            let objectURL = 'data:image/jpeg;base64,' + book.image;
            book.image = objectURL;
          });
          Array.prototype.push.apply(this.similar, data);

        },
        error => {
          console.log(error);
        }
      );
  }

  getRecommendations(recommendations: string[]) {
    if (recommendations)
      this.bookService.getRecommendationsByBook(recommendations).subscribe(
        data => {
          data.forEach((book) => {
            let objectURL = 'data:image/jpeg;base64,' + book.image;
            book.image = objectURL;
          });
          Array.prototype.push.apply(this.recommendations, data);

        },
        error => {
          console.log(error);
        }
      );
  }

  

  showDetails(book: Book) {
    this.selected = book;
    this.kindle = false;
    this.favoriteBook = false;
    this.similar.length = 0;
    this.recommendations.length = 0;
    this.getSimilar(book.similar);
    this.getRecommendations(book.recommendations);
    this.getKindle();
    this.getFavoriteBook(book.path);
    this.view(book.path);
  }

  getBooksByAuthor(author: string) {
    this.selected = null;
    this.adv_search = new Search();
    this.adv_search.author = author;
    this.doSearch();
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
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(this.adv_search) } });
  }


  addNotification(book: string, type: NotificationEnum, status: StatusEnum, error: string) {
    const user = JSON.parse(sessionStorage.user);

    const notification = new Notif(null, book, user.username, type, status, error, this.datepipe.transform(new Date(), 'dd/MM/yyyy HH:mm:ss'));
    this.notificationService.save(notification).subscribe(
      data => {
        console.log(data);
      },
      error => {
        console.log(error);
      }
    );
  }

  sendToKindle() {
    let book = this.selected.path;
    const user = JSON.parse(sessionStorage.user);

    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.todo'), closable: false, life: 5000 });

    this.utilService.sendMail(book, user.kindle).subscribe(
      data => {
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.ok'), closable: false, life: 5000 });

        //Add to notifications table
        this.addNotification(book, NotificationEnum.KINDLE, StatusEnum.SEND, null);

      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.kindle.error'), closable: false, life: 5000 });

        //Add to notifications table
        this.addNotification(book + '', NotificationEnum.KINDLE, StatusEnum.NOT_SEND, error.error);
      }
    );


  }


  getKindle() {
    this.configService.get("kindlegen.path").subscribe(
      data => {
        if (data.value) {
          this.configService.get("smtp.status").subscribe(
            data => {
              if (data.value == 'ok')
                this.kindle = true;
            },
            error => {
              console.log(error);
            }
          );
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  getFavoriteBook(id: string) {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.getFavorite(id, user.username).subscribe(
      data => {
        if (data) {
          this.favoriteBook = true;
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  view(id: string) {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.view(id, user.username).subscribe(
      error => {
        console.log(error);
      }
    );
  }

  addFavoriteBook() {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.addFavorite(this.selected.path, user.username).subscribe(
      data => {
        this.favoriteBook = true;
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.add.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.favorite.add.error'), closable: false, life: 5000 });
      }
    );
  }



  deleteFavoriteBook() {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.deleteFavorite(this.selected.path, user.username).subscribe(
      data => {
        this.favoriteBook = false;
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.delete.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.favorite.delete.error'), closable: false, life: 5000 });
      }
    );
  }

  viewEpub() {
    this.showEpub = true;

    this.bookService.getEpub(this.selected.path).subscribe(
      data => {
        if (data) {
          var file = new File([data], "name");
          this.book = new epub(file);

          
          this.rendition = this.book.renderTo("viewer", { flow: "paginated", method: "continuous", width: "100%", height: "97%" });
          this.displayed = this.rendition.display();

          this.displayed.then((renderer) => {
            console.log(this.rendition);
          });

          this.book.ready.then(() => {
            this.book.loaded.navigation.then((toc) => {
              toc.forEach((chapter) => {
                var ch = chapter;
                this.chapterList.push(ch);
              })
            })
      
            this.book.locations.generate(64);
          })

        }
      },
      error => {
        console.log(error);
      }
    );

  }

  isAdmin() {
    return JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  refreshBook() {
    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.process'), closable: false, life: 5000 });
    this.metadataService.findBook(this.selected.path).subscribe(
      data => {
        this.showDetails(data);

        if (data.image) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          this.selected.image = objectURL;
        }
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.result.ok'), closable: false, life: 5000 });
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.refresh.result.error'), closable: false, life: 5000 });
      }
    );
  }

  showDialogMaximized(dialog: Dialog) {
    dialog.maximize();
  }

  public prev() {
    this.rendition.prev().then(() => {
    })
  }
  public next() {
    this.rendition.next().then(() => {
    })
  }


  public changeChapter(url) {
    this.rendition.display(url);
    return false;
  }


  close() {
    this.router.navigate(["books"]);
  }

}

