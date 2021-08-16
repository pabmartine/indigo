import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { BookService } from 'src/app/services/book.service';
import { Book } from 'src/app/domain/book';
import { CommentService } from 'src/app/services/comment.services';
import { PageService } from 'src/app/services/page.services';
import { SerieService } from 'src/app/services/serie.services';
import { TagService } from 'src/app/services/tag.services';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { UtilService } from 'src/app/services/util.service';
import { ConfigService } from 'src/app/services/config.service';
import { NotificationService } from 'src/app/services/notification.service';
import { Notif } from 'src/app/domain/notif';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { Search } from 'src/app/domain/search';
import { Tag } from 'src/app/domain/tag';
import { DatePipe } from '@angular/common';

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

  private adv_search: Search;

  constructor(
    private bookService: BookService,
    private commentService: CommentService,
    private pageService: PageService,
    private serieService: SerieService,
    private tagService: TagService,
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
        this.showDetails(params['book']);
      }
    });


  }

  ngOnInit(): void {
  }



  getInfo(book: Book, local: boolean) {
    this.bookService.getBookInfo(book.id, local).subscribe(
      data => {
        if (data) {
          book.rating = data.rating;

          if (!local && data.similar) {
            this.getSimilar(data.similar);
          }

          if (!local) {
            this.getRecommendations(data.id);
          }
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  getSimilar(similar: string) {
    this.bookService.getSimilar(similar).subscribe(
      data => {
        data.forEach((book) => {
          this.getCover(book);
          this.getInfo(book, true);
          book.authors = book.authorSort.split("&").map(function (item) {
            return item.trim();
          });

        });
        Array.prototype.push.apply(this.similar, data);

      },
      error => {
        console.log(error);
      }
    );
  }

  getRecommendations(id: number) {
    this.bookService.getRecommendations(id).subscribe(
      data => {
        data.forEach((book) => {
          this.getCover(book);
          this.getInfo(book, true);
          book.authors = book.authorSort.split("&").map(function (item) {
            return item.trim();
          });

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

  getComment(id:number) {
    this.commentService.getComment(id).subscribe(
      data => {
        this.selected.description = data.comment;
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.comment'), closable: false, life: 5000 });
      }
    );
  }

  getPages(id:number) {
    this.pageService.getPages(id).subscribe(
      data => {
        this.selected.pages = data.pages;
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.pages'), closable: false, life: 5000 });
      }
    );
  }

  getSerie(id:number) {
    this.serieService.getSerie(id).subscribe(
      data => {
        this.selected.seriesName = data.serie;
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.serie'), closable: false, life: 5000 });
      }
    );
  }

  getTags(id:number) {
    this.tagService.getTags(id).subscribe(
      data => {
        this.selected.tags = data;
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.error.tags'), closable: false, life: 5000 });
      }
    );
  }

  showDetails(id: number) {
    this.kindle = false;
    this.favoriteBook = false;
    this.similar.length = 0;
    this.recommendations.length = 0;
    this.getData(id);
    this.getComment(id);
    this.getPages(id);
    this.getSerie(id);
    this.getTags(id);
    this.getKindle();
    this.getFavoriteBook(id);
  }

  getData(id: number) {
    this.bookService.getBookTitle(id).subscribe(
      data => {
        this.selected = data;
        this.getCover(data);
        this.getInfo(data, false);
        data.authors = data.authorSort.split("&").map(function (item) {
          return item.trim();
        });
      },
      error => {
        console.log(error);
      }
    );
  }

  getBooksByAuthor(author: string) {
    this.selected = null;
    this.adv_search = new Search();
    this.adv_search.author = author;
    this.doSearch();
  }

  getBooksByTag(tag: string) {
    this.selected = null;

    this.tagService.getTag(tag).subscribe(
      data => {
        this.adv_search = new Search();
        this.adv_search.selectedTags = [];
        this.adv_search.selectedTags.push(new Tag(data.id, data.name));
        this.doSearch();
      },
      error => {
        console.log(error);
      }
    );
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


  addNotification(book: number, type: NotificationEnum, status: StatusEnum, error: string) {
    const user = JSON.parse(sessionStorage.user);

    const notification = new Notif(null, book, user.id, type, status, error, this.datepipe.transform(new Date(), 'dd/MM/yyyy HH:mm:ss'));
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
    let book = this.selected.id;
    const user = JSON.parse(sessionStorage.user);

    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.todo'), closable: false, life: 5000 });

    this.utilService.sendMail(this.selected.path, user.id).subscribe(
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
        this.addNotification(book, NotificationEnum.KINDLE, StatusEnum.NOT_SEND, error.error);
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

  getFavoriteBook(id:number) {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.getFavorite(id, user.id).subscribe(
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




  addFavoriteBook() {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.addFavorite(this.selected.id, user.id).subscribe(
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
    this.bookService.deleteFavorite(this.selected.id, user.id).subscribe(
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

  close() {
    this.location.back();
  }



}

