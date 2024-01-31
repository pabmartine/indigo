import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { MenuItem } from 'primeng/api/menuitem';
import { Router } from '@angular/router';
import { TranslationChangeEvent, TranslateService } from '@ngx-translate/core';
import { NotificationService } from 'src/app/services/notification.service';
import { BookService } from 'src/app/services/book.service';
import { UserService } from 'src/app/services/user.service';
import { Notif } from 'src/app/domain/notif';
import { Book } from 'src/app/domain/book';
import { User } from 'src/app/domain/user';
import { Search } from 'src/app/domain/search';
import { lastValueFrom } from 'rxjs';


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
  messages: Notif[] = [];

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

  async getMessages() {
    const user = JSON.parse(sessionStorage.user);
  
    try {
      let data;
  
      if (this.isAdmin()) {
        data = await lastValueFrom(this.notificationService.findAllNotRead());
      } else {
        data = await lastValueFrom(this.notificationService.findAllByUser(user.id));
      }
  
      this.fillMessages(data, user);
  
    } catch (error) {
      console.log(error);
    }
  }

  async fillMessages(data: Notif[], user: User) {
    this.messages = data;
  
    for (const message of this.messages) {
      try {
        const book: Book = await lastValueFrom(this.bookService.getBookByPath(message.book));
  
        let username: string;
        if (user.username == message.user) {
          username = user.username;
          if (message.error) {
            message.message = this.translate.instant('locale.messages.kindle.error', { book: book.title, user: username });
          } else {
            message.message = this.translate.instant('locale.messages.kindle.ok', { book: book.title, user: username });
          }
        } else {
          const userData: User = await lastValueFrom(this.userService.get(message.user));
          if (message.error) {
            message.message = this.translate.instant('locale.messages.kindle.error', { book: book.title, user: userData.username });
          } else {
            message.message = this.translate.instant('locale.messages.kindle.ok', { book: book.title, user: userData.username });
          }
        }
      } catch (error) {
        console.log(error);
      }
    }
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

  async markMessageAsRead(id: string) {
    try {
      const user = JSON.parse(sessionStorage.user);
      await lastValueFrom(this.notificationService.read(id, user.id));
      this.messages = this.messages.filter(obj => obj.id !== id);
    } catch (error) {
      console.log(error);
    }
  }
  

}
