import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api/menuitem';
import { Book } from 'src/app/domain/book';
import { Notification } from 'src/app/domain/notification';
import { Search } from 'src/app/domain/search';
import { User } from 'src/app/domain/user';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { BookService } from 'src/app/services/book.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UserService } from 'src/app/services/user.service';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {



  public showSideBar: boolean = false;
  public showTopBar: boolean = false;

  @Output() emitterSideBar = new EventEmitter<boolean>();
  @Output() emitterTopBar = new EventEmitter<boolean>();


  items: MenuItem[];
  messages: Notification[] = [];

  search: string;


  constructor(
    private router: Router,
    public translate: TranslateService,
    private notificationService: NotificationService,
    private bookService: BookService,
    private userService: UserService,) {
  }

  ngOnInit(): void {

    this.translate.onTranslationChange.subscribe(
      (event: TranslationChangeEvent) => {
        this.items = this.buildMenu();
      },
    );

    this.items = this.buildMenu();
    this.getMessages();

  }

  buildMenu(): MenuItem[] {
    const menu: MenuItem[] = [
      {
        label: this.translate.instant('locale.header.menu.profile'),
        icon: 'menu-icon pi pi-user',
        routerLink: ['/profile'],
        routerLinkActiveOptions: { exact: true },
      },
      { separator: true },
      {
        label: this.translate.instant('locale.header.menu.logout'), icon: 'menu-icon pi pi-sign-out', command: () => this.logout()
      }
    ];
    return menu;
  }

  getMessages() {
    const user = JSON.parse(sessionStorage.user);

    const successCallback = (data) => {
      console.log(data);
      this.fillMessages(data, user);
    };

    const errorCallback = (error) => {
      console.log(error);
    };

    if (this.isAdmin()) {
      this.notificationService.findAllNotRead().subscribe({
        next: successCallback,
        error: errorCallback
      });
    } else {
      this.notificationService.findAllByUser(user.id).subscribe({
        next: successCallback,
        error: errorCallback
      });
    }
  }


  fillMessages(data: Notification[], user: User) {
    this.messages = data;
    this.messages.forEach((message) => {
      console.log(message.type);
      console.log(NotificationEnum.KINDLE)
      if (message.type === NotificationEnum.KINDLE) {        

        this.bookService.getBookByPath(message.kindle.book).subscribe(data => {
          const book: Book = data;
          let username: string;
          if (user.username == message.user) {
            username = user.username;
            if (message.kindle.error)
              message.message = this.translate.instant('locale.messages.kindle.error', { book: book.title, user: username });
            else
              message.message = this.translate.instant('locale.messages.kindle.ok', { book: book.title, user: username });
          } else {
            this.userService.get(message.user).subscribe(
              data => {
                const user: User = data;
                if (message.kindle.error)
                  message.message = this.translate.instant('locale.messages.kindle.error', { book: book.title, user: user.username });
                else (message.kindle.error)
                message.message = this.translate.instant('locale.messages.kindle.ok', { book: book.title, user: user.username });
              });
          }
        });


      } else {
        console.log(message);
        message.message = this.translate.instant('locale.messages.upload', 
        {total: message.upload.total,
        extractError: message.upload.extractError,
        moveError: message.upload.moveError,
        deleteError: message.upload.deleteError,
        newBooks: message.upload.newBooks,
        updatedBooks: message.upload.updatedBooks,
        newAuthors: message.upload.newAuthors,
        newTags: message.upload.newTags}
        );

      }
    });
  }

  doSearch() {

    let search: Search = new Search();
    search.path = this.search;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
    this.search = "";
  }


  toggleSideBar(event: Event) {
    this.showSideBar = !this.showSideBar;

    if (this.showTopBar) {
      this.showTopBar = false;
      this.emitterTopBar.emit(this.showTopBar);
    }

    this.emitterSideBar.emit(this.showSideBar);
    event.preventDefault();
  }

  toggleTopBar(event: Event) {
    this.showTopBar = !this.showTopBar;

    if (this.showSideBar) {
      this.showSideBar = false;
      this.emitterSideBar.emit(this.showSideBar);
    }

    this.emitterTopBar.emit(this.showTopBar);
    event.preventDefault();
  }


  logout() {
    sessionStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  isAdmin() {
    return JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  markMessageAsRead(id: string) {
    const user = JSON.parse(sessionStorage.user);
    this.notificationService.read(id, user.id).subscribe(
      data => {
        this.messages = this.messages.filter(obj => obj.id !== id);
      }
    );
  }

  showRow(valor: number): boolean {
    console.log(valor);
    return valor > 0;
  }

}
