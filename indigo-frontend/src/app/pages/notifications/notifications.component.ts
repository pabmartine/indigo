import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Notif } from 'src/app/domain/notif';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css'],
  providers: [MessageService]

})
export class NotificationsComponent implements OnInit {

  notifications: Notif[];
  types: any[];
  users: any[] = [];
  statuses: any[];
  read: any[];
  cover: string;


  constructor(private messageService: MessageService,
    public translate: TranslateService,
    private notificationService: NotificationService,
    private userService: UserService,
    private bookService: BookService,
  ) { }

  ngOnInit(): void {
    this.getNotifications();

    this.types = [
      { label: this.translate.instant('locale.notifications.types.KINDLE'), value: NotificationEnum[NotificationEnum.KINDLE] }
    ]

    this.statuses = [
      { label: this.translate.instant('locale.notifications.statuses.SEND'), value: StatusEnum[StatusEnum.SEND] },
      { label: this.translate.instant('locale.notifications.statuses.NOT_SEND'), value: StatusEnum[StatusEnum.NOT_SEND] }
    ]

    this.read = [
      { label: this.translate.instant('locale.notifications.read.true'), value: true },
      { label: this.translate.instant('locale.notifications.read.false'), value: false }
    ]

    this.getUsers();
  }

  getUsers(): void {
    this.users.length = 0;
    this.userService.getAll().subscribe({
      next: (data) => {
        data.forEach((user) => {
          this.users.push({ label: user.username, value: user.username });
        });
      },
      error: (error) => {
        console.log(error);
      }
    });
  }


  getNotifications(): void {
    this.notificationService.findAll().subscribe({
      next: (data) => {
        if (data) {
          this.notifications = data;
          this.notifications.forEach((notif) => {
            this.getBook(notif);
          });
        }
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.notifications.actions.get.error'), closable: false, life: 5000 });
      }
    });
  }


  getBook(notif: Notif): void {
    this.bookService.getBookByPath(notif.book).subscribe({
      next: (data) => {
        if (data) {
          notif.title = data.title;
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          notif.image = objectURL;
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }


  deleteNotification(id: string): void {
    this.notificationService.delete(id).subscribe({
      next: (data) => {
        this.getNotifications();
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.notifications.actions.delete.error'), closable: false, life: 5000 });
      }
    });
  }


}

