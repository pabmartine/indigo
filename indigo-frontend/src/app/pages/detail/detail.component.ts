import { DatePipe, Location } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, OnInit, Output, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { saveAs } from 'file-saver';
import { MenuItem, MessageService } from 'primeng/api';
import { Dialog } from 'primeng/dialog';
import { Book } from 'src/app/domain/book';
import { Notif } from 'src/app/domain/notif';
import { Search } from 'src/app/domain/search';
import { Serie } from 'src/app/domain/serie';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { ConfigService } from 'src/app/services/config.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { NotificationService } from 'src/app/services/notification.service';
import { MailService } from 'src/app/services/mail.service';
import { lastValueFrom } from 'rxjs';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  providers: [MessageService, DatePipe]

})
export class DetailComponent implements OnInit {

  @Output() eventAuthor: EventEmitter<String> = new EventEmitter<String>();
  @Output() eventBook: EventEmitter<Book> = new EventEmitter<Book>();
  @Output() deleteBookEvent: EventEmitter<String> = new EventEmitter<String>();

  serie: Book[] = [];
  similar: Book[] = [];
  recommendations: Book[] = [];
  selected: Book;
  selectedImage: string;
  title: string;
  kindle: boolean;
  favoriteBook: boolean;
  showEpub: boolean;

  expandRecommendations: boolean;
  showExpandRecommendations: boolean;

  expandSerie: boolean;
  showExpandSerie: boolean;

  expandReview: boolean;
  showExpandReview: boolean;

  expandSimilar: boolean;
  showExpandSimilar: boolean;

  private adv_search: Search;

  public chapterList = [];

  public book;
  public rendition;
  public displayed;

  items: MenuItem[];
  editDialog: boolean = false;
  editedBook: Book;

  user = JSON.parse(sessionStorage.user);

  @ViewChild('viewer') viewer: ElementRef;

  constructor(
    private bookService: BookService,
    private mailService: MailService,
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
    this.items = [
      {
        id: 'deleteBook',
        label: this.translate.instant('locale.buttons.delete'),
        icon: 'pi pi-trash',
        command: () => this.deleteBook()
      },
      {
        id: 'editBook',
        label: this.translate.instant('locale.buttons.edit'),
        icon: 'pi pi-pencil',
        command: () => this.editBook()
      },
      {
        id: 'refreshBook',
        label: this.translate.instant('locale.buttons.refresh'),
        icon: 'pi pi-refresh',
        command: () => this.refreshBook()
      }
    ];
  }

  async getImage(path: string) {
    try {
      this.selectedImage = this.selected.image;

      if (path) {
        const data = await lastValueFrom(this.bookService.getImage(path));

        if (data) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          this.selectedImage = objectURL;
        }
      }

    } catch (error) {
      console.log(error);
    }
  }

  async getSerie(serie: Serie) {
    try {
      if (serie) {
        const data = await lastValueFrom(this.bookService.getSerie(serie.name, this.user.languageBooks));

        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.serie, data);
      }

    } catch (error) {
      console.log(error);
    }
  }

  async getSimilar(similar: string[]) {
    try {
      if (similar) {
        const data = await lastValueFrom(this.bookService.getSimilar(similar, this.user.languageBooks));

        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.similar, data);
      }

    } catch (error) {
      console.log(error);
    }
  }


  async getRecommendations(recommendations: string[]) {
    try {
      if (recommendations) {
        const data = await lastValueFrom(this.bookService.getRecommendationsByBook(recommendations, this.user.languageBooks));

        data.forEach((book) => {
          let objectURL = 'data:image/jpeg;base64,' + book.image;
          book.image = objectURL;
        });

        Array.prototype.push.apply(this.recommendations, data);
      }

    } catch (error) {
      console.log(error);
    }
  }



  showDetails(book: Book) {
    this.close();

    this.selected = book;
    this.editedBook = new Book();
    this.kindle = false;
    this.favoriteBook = false;
    this.serie.length = 0;
    this.similar.length = 0;
    this.recommendations.length = 0;
    //this.getReviews(book.path);
    this.getImage(book.path);
    this.getSerie(book.serie);
    this.getSimilar(book.similar);
    this.getRecommendations(book.recommendations);
    this.getKindle();
    this.getFavoriteBook(book.path);
    this.view(book.path);

    setTimeout(() => {
      this.open();
      this.checkOverflowSerie();
      this.checkOverflowReview();
      this.checkOverflowSimilar();
      this.checkOverflowRecommendations();
    }, 200)

  }

  openAuthor(author: string) {
    this.eventAuthor.emit(author);
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
    this.close();
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(this.adv_search) } });
  }


  async addNotification(book: string, type: NotificationEnum, status: StatusEnum, error: string) {
    try {
      const user = JSON.parse(sessionStorage.user);

      const notification = new Notif(
        null,
        book,
        user.username,
        type,
        status,
        error,
        this.datepipe.transform(new Date(), 'dd/MM/yyyy HH:mm:ss')
      );

      const data = await lastValueFrom(this.notificationService.save(notification));

      console.log(data);

    } catch (error) {
      console.log(error);
    }
  }

  sendToKindle() {
    let book = this.selected.path;
    const user = JSON.parse(sessionStorage.user);

    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.kindle.todo'), closable: false, life: 5000 });

    this.mailService.sendMail(book, user.kindle).subscribe(
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
        this.addNotification(book + '', NotificationEnum.KINDLE, StatusEnum.NOT_SEND, error.error.message);
      }
    );


  }


  async getKindle() {
    try {
      const data = await lastValueFrom(this.configService.get("smtp.status"));

      if (data.value === 'ok') {
        this.kindle = true;
      }

    } catch (error) {
      console.log(error);
    }
  }



  async getFavoriteBook(id: string) {
    try {
      const user = JSON.parse(sessionStorage.user);
      const data = await lastValueFrom(this.bookService.getFavorite(id, user.username));

      if (data) {
        this.favoriteBook = true;
      }

    } catch (error) {
      console.log(error);
    }
  }


  view(id: string) {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.view(id, user.username).subscribe(
      error => {
        console.log(error);
      }
    );
  }

  async addFavoriteBook() {
    const user = JSON.parse(sessionStorage.user);

    try {
      const data = await lastValueFrom(this.bookService.addFavorite(this.selected.path, user.username));

      this.favoriteBook = true;
      this.messageService.clear();
      this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.add.ok'), closable: false, life: 5000 });

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.favorite.add.error'), closable: false, life: 5000 });
    }
  }




  async deleteFavoriteBook() {
    const user = JSON.parse(sessionStorage.user);

    try {
      const data = await lastValueFrom(this.bookService.deleteFavorite(this.selected.path, user.username));

      this.favoriteBook = false;
      this.messageService.clear();
      this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.favorite.delete.ok'), closable: false, life: 5000 });

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.favorite.delete.error'), closable: false, life: 5000 });
    }
  }


  viewEpub() {

    /*
    this.showEpub = true;

    this.bookService.getEpub(this.selected.path).subscribe(
      data => {
        if (data) {
          var file = new File([data], "name");
          this.book = new ePub(file);


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
*/
  }

  async downloadEpub() {
    try {
      const data = await lastValueFrom(this.bookService.getEpub(this.selected.path));

      saveAs(data, this.selected.title);

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.download.error'), closable: false, life: 5000 });
    }
  }


  isAdmin() {
    return JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  async refreshBook() {
    this.messageService.clear();
    this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.process'), closable: false, life: 5000 });

    try {
      const data = await lastValueFrom(this.metadataService.findBook(this.selected.path, "es"));

      this.selected = data;

      if (data.image) {
        let objectURL = 'data:image/jpeg;base64,' + data.image;
        this.selected.image = objectURL;
      }

      this.eventBook.emit(this.selected);

      this.messageService.clear();
      this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.refresh.result.ok'), closable: false, life: 5000 });

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.refresh.result.error'), closable: false, life: 5000 });
    }
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


  @Output() eventClose: EventEmitter<void> = new EventEmitter<void>();

  close() {
    this.eventClose.emit();
  }

  @Output() eventOpen: EventEmitter<void> = new EventEmitter<void>();

  open() {
    this.eventOpen.emit();
  }

  async deleteBook() {
    try {
      const data = await lastValueFrom(this.bookService.deleteBook(this.selected.id));

      this.deleteBookEvent.emit(this.selected.id);
      this.messageService.clear();
      this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.delete.ok'), closable: false, life: 5000 });

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.delete.error'), closable: false, life: 5000 });
    }
  }


  editBook() {
    this.editedBook = this.selected;
    this.editDialog = true;
    //this.close();
  }

  async saveBook() {
    try {
      const data = await lastValueFrom(this.bookService.editBook(this.editedBook));

      this.eventBook.emit(this.editedBook);
      this.messageService.clear();
      this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.books.detail.edit.ok'), closable: false, life: 5000 });

    } catch (error) {
      console.log(error);
      this.messageService.clear();
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.books.detail.edit.error'), closable: false, life: 5000 });
    }
  }


  checkOverflowRecommendations() {
    let row = document.getElementById('inlineRecommendations');
    if (row)
      this.showExpandRecommendations = this.isOverFlowed(row);
  }

  checkOverflowSimilar() {
    let row = document.getElementById('inlineSimilar');
    if (row)
      this.showExpandSimilar = this.isOverFlowed(row);
  }

  checkOverflowSerie() {
    let row = document.getElementById('inlineSerie');
    if (row)
      this.showExpandSerie = this.isOverFlowed(row);
  }

  checkOverflowReview() {
    let row = document.getElementById('inlineReview');
    if (row)
      this.showExpandReview = this.isOverFlowed(row);
  }

  isOverFlowed(element) {
    if (element) {
      return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
    }
    return false;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.checkOverflowRecommendations();
    this.checkOverflowSimilar();
    this.checkOverflowSerie();
    this.checkOverflowReview();
  }

  toDate(date: string): Date {
    let d = date.split("/");
    let dat = new Date(d[2] + '/' + d[1] + '/' + d[0]);
    return dat;
  }

}

