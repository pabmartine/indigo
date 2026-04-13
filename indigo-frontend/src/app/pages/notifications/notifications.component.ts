import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Notification } from 'src/app/domain/notification';
import { NotificationUpload } from 'src/app/domain/notification.upload';
import { NotificationEnum } from 'src/app/enums/notification.enum.';
import { StatusEnum } from 'src/app/enums/status.enum';
import { BookService } from 'src/app/services/book.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UserService } from 'src/app/services/user.service';
import { ImageService } from 'src/app/utils/image.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css'],
  providers: [MessageService]

})
export class NotificationsComponent implements OnInit, OnDestroy {

  notifications: Notification[];
  types: any[];
  users: any[] = [];
  statuses: any[];
  read: any[];
  data: string;
  upload: NotificationUpload

  private destroy$ = new Subject<void>();

  constructor(private messageService: MessageService,
    public translate: TranslateService,
    private notificationService: NotificationService,
    private userService: UserService,
    private bookService: BookService,
    private imageService: ImageService,
  ) { }

  ngOnInit(): void {
    this.getNotifications();

    this.types = [
      { label: this.translate.instant('locale.notifications.types.KINDLE'), value: NotificationEnum[NotificationEnum.KINDLE] },
      { label: this.translate.instant('locale.notifications.types.UPLOAD'), value: NotificationEnum[NotificationEnum.UPLOAD] }
    ]

    this.statuses = [
      { label: this.translate.instant('locale.notifications.statuses.SEND'), value: StatusEnum[StatusEnum.SEND] },
      { label: this.translate.instant('locale.notifications.statuses.NOT_SEND'), value: StatusEnum[StatusEnum.NOT_SEND]},
      { label: this.translate.instant('locale.notifications.statuses.FINISHED'), value: StatusEnum[StatusEnum.FINISHED]}
    ]

    this.read = [
      { label: this.translate.instant('locale.notifications.read.true'), value: true },
      { label: this.translate.instant('locale.notifications.read.false'), value: false }
    ]

    this.getUsers();
  }

  getUsers(): void {
    this.users.length = 0;
    this.userService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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

    this.notificationService.findAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            this.notifications = data;
            this.notifications.forEach((notif) => {

              if (notif.type===NotificationEnum.KINDLE){
                this.getBook(notif);
              }
              if (notif.type===NotificationEnum.UPLOAD){
                ;
              }
            });
          }
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.notifications.actions.get.error'), closable: false, life: 5000 });
        }
      });
  }

  getBook(notif: Notification): void {
    this.bookService.getBookByPath(notif.kindle.book)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            notif.kindle.title = data.title;
            notif.kindle.image = this.imageService.toDataUrlSafe(data.image);
            console.log(notif.kindle.image);
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }


  deleteNotification(id: string): void {
    this.notificationService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.getNotifications();
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.notifications.actions.delete.error'), closable: false, life: 5000 });
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}

