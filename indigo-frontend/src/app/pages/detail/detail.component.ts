import { DatePipe, Location } from '@angular/common';
import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Dialog } from 'primeng/dialog';
import { Book } from 'src/app/domain/book';
import { Notif } from 'src/app/domain/notif';
import { Search } from 'src/app/domain/search';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { ConfigService } from 'src/app/services/config.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UtilService } from 'src/app/services/util.service';
import * as epub from 'node_modules/epubjs/dist/epub.js';

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

  // public isSpread: boolean;
  // public isEnd: boolean = false;
  // public isStart: boolean = false;
  public chapterList = [];

  // public currentProgress: number = 0;
  // public currentCfi: string;
  // public savedcfi;


  public book;
  public rendition;
  public displayed;

  @ViewChild('viewer') viewer: ElementRef;


  // @HostListener('document:keydown', ['$event'])
  // handleKeyboardEvent(event: KeyboardEvent) {
  //   this.handleKeypress(event);
  // }

  constructor(
    private bookService: BookService,
    private utilService: UtilService,
    private configService: ConfigService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    public translate: TranslateService,
    public notificationService: NotificationService,
    private location: Location,
    public datepipe: DatePipe) {

    this.route.queryParams.subscribe(params => {

      if (params['book']) {
        this.showDetails(JSON.parse(params['book']));
      }
    });

    // this.savedcfi = localStorage.getItem('cfi');
    // var content = new epub.hooks(this);
    // content.register(function () { })

  }

  ngOnInit(): void {
  }

  getSimilar(similar: string[]) {
    if (similar)
      this.bookService.getSimilar(similar).subscribe(
        data => {
          data.forEach((book) => {
            this.getCover(book);
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
            this.getCover(book);
          });
          Array.prototype.push.apply(this.recommendations, data);

        },
        error => {
          console.log(error);
        }
      );
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
        console.log("*********************** FAVORITE")
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
    this.bookService.addFavorite(this.selected.path, user.id).subscribe(
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
    this.bookService.deleteFavorite(this.selected.path, user.id).subscribe(
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
          // const b64 = data.epub.replace('data:application/epub+zip;base64,', '');
          // this.book.open(b64);

          // this.book = new epub("../assets/file.epub");
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
      
            this.book.locations.generate(64); // Generates CFI for every X characters (Characters per/page
          })

        }
      },
      error => {
        console.log(error);
      }
    );



    



    // this.loadEpub();
  }

  showDialogMaximized(dialog: Dialog) {
    dialog.maximize();
  }

  public prev() {
    this.rendition.prev().then(() => {
      // if (this.rendition.location) {
      //   this.currentCfi = this.rendition.location.start.cfi;
      //   localStorage.setItem('cfi', this.currentCfi);
      // }
    })
  }
  public next() {
    this.rendition.next().then(() => {
      // if (this.rendition.location) {
      //   this.currentCfi = this.rendition.location.start.cfi;
      //   localStorage.setItem('cfi', this.currentCfi);
      // }
    })
  }

  // public brighten() { };
  // public dim() { };

  // public handleKeypress(event) {
  //   switch (event.keyCode) {
  //     case 37: this.prev();
  //       break;

  //     case 39: this.next();
  //       break;

  //     case 38: this.brighten();
  //       break;

  //     case 40: this.dim();
  //       break;

  //     default: break;
  //   }
  // }

  public changeChapter(url) {
    this.rendition.display(url);
    return false;
  }

  // public gotocfi(cfi) {
  //   this.rendition.display(cfi);
  // }

  // public go() {
  //   this.gotocfi(this.savedcfi);
  // }

  // loadEpub() {
  //   this.displayed.then((renderer) => {
  //     console.log(this.rendition);
  //   });

  //   this.book.ready.then(() => {
  //     this.book.loaded.navigation.then((toc) => {
  //       toc.forEach((chapter) => {
  //         var ch = chapter;
  //         this.chapterList.push(ch);
  //       })
  //     })

  //     this.book.locations.generate(64); // Generates CFI for every X characters (Characters per/page

  //   })


  //   this.rendition.on("layout", function (layout) {
  //     if (layout.divisor == 2) {
  //       this.isSpread = true;
  //     } else {
  //       this.isSpread = false;
  //     }
  //   });

  //   this.rendition.themes.default({
  //     h2: {
  //       'font-size': '42px',
  //     },
  //     p: {
  //       "margin": '10px'
  //     }
  //   });


  //   this.rendition.on("relocated", function (location) {
  //     this.currentCfi = location.start.cfi;
  //     localStorage.setItem('cfi', this.currentCfi);

  //     if (location.atEnd) {
  //       this.isEnd = true;
  //     } else {
  //       this.isEnd = false;
  //     }

  //     if (location.atStart) {
  //       this.isStart = true;
  //     } else {
  //       this.isStart = false;
  //     }

  //     this.currentProgress = this.book.locations.percentageFromCfi(location.start.cfi);
  //   })
  // }

  close() {
    //TODO actualizar la lista de favoritos en la vista pricipal
    this.location.back();
  }

}

