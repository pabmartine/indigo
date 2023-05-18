import { Component, OnInit, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { Config } from 'src/app/domain/config';
import { User } from 'src/app/domain/user';
import { AuthorService } from 'src/app/services/author.service';
import { ConfigService } from 'src/app/services/config.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { UserService } from 'src/app/services/user.service';
import { MailService } from 'src/app/services/mail.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  providers: [MessageService]
})
export class SettingsComponent implements OnInit {

  type: string;
  entity: string;

  isSendTestMail: boolean = false;
  current: number = 0;
  total: number = 0;
  message: string;
  progressBar: number = 0;

  userList: User[];
  goodReadsKey: string;
  metadataPull: number;
  booksRecommendations: number;

  smtpHost: string;
  smtpPort: string;
  smtpEncryption: string = "starttls";
  smtpUsername: string;
  smtpPassword: string;
  smtpStatus: string;
  smtpProvider: string = "other";

  encryptions: SelectItem[] = [
    // { label: '', value: 'none' },
    { label: 'STARTTLS', value: 'starttls' },
    { label: 'SSL/TLS', value: 'ssl/tls' }
  ];

  providers: SelectItem[] = [
    { label: this.translate.instant('locale.settings.panel.smtp.providers.gmail'), value: 'gmail' },
    { label: this.translate.instant('locale.settings.panel.smtp.providers.outlook'), value: 'outlook' },
    { label: this.translate.instant('locale.settings.panel.smtp.providers.other'), value: 'other' }
  ];

  panelStates = new Map();


  constructor(private messageService: MessageService,
    public translate: TranslateService,
    public authorService: AuthorService,
    public metadataService: MetadataService,
    public configService: ConfigService,
    public mailService: MailService,
    public userService: UserService,
    private router: Router) {


  }

  getData() {
    this.getDataStatus();
    this.getUsers();
    this.getGlobal();
    this.getMetadata();
    this.getSmtp(true);
  }

  ngOnInit(): void {

    this.getData();

    let interval = setInterval(() => {
      this.getDataStatus();
    }, 5000);

  }

  ngOnChanges(changes: SimpleChanges) {
    console.log(changes)
  }

  getDataStatus() {
    this.metadataService.getDataStatus().subscribe(
      data => {
        this.type = data.type;
        this.entity = data.entity;

        this.current = data.current;
        this.total = data.total;
        this.message = data.message;
        if (this.message) {
          this.message = this.translate.instant('locale.settings.panel.metadata.' + this.message);
        }

        if (this.total != 0)
          this.progressBar = Math.round((this.current * 100) / this.total);
      },
      error => {
        console.log(error);
      }
    );
  }

  getUsers() {
    this.userService.getAll().subscribe(
      data => {
        if (data)
          this.userList = data;
      },
      error => {
        console.log(error);
      }
    );
  }

  getGlobal() {
    this.configService.get("books.recommendations").subscribe(
      data => {
        if (data)
          this.booksRecommendations = Number(data.value);
      },
      error => {
        console.log(error);
      }
    );

  }

  getMetadata() {
    this.configService.get("metadata.pull").subscribe(
      data => {
        if (data)
          this.metadataPull = Number(data.value) / 1000;
      },
      error => {
        console.log(error);
      }
    );


    this.configService.get("goodreads.key").subscribe(
      data => {
        if (data)
          this.goodReadsKey = data.value;
      },
      error => {
        console.log(error);
      }
    );
  }

  getSmtp(init: boolean) {

    if (init)
      this.configService.get("smtp.provider").subscribe(
        data => {
          if (data)
            this.smtpProvider = data.value;
          else this.smtpProvider = 'other';
        },
        error => {
          console.log(error);
        }
      );

    this.configService.get("smtp.host").subscribe(
      data => {
        if (data)
          this.smtpHost = data.value;
        else this.smtpHost = '';
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.port").subscribe(
      data => {
        if (data)
          this.smtpPort = data.value;
        else this.smtpPort = '';
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.encryption").subscribe(
      data => {
        if (data)
          this.smtpEncryption = data.value;
        else this.smtpEncryption = '';
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.username").subscribe(
      data => {
        if (data)
          this.smtpUsername = data.value;
        else this.smtpUsername = '';
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.password").subscribe(
      data => {
        if (data)
          this.smtpPassword = data.value;
        else this.smtpPassword = '';
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.status").subscribe(
      data => {
        if (!data) this.smtpStatus = "unknown";
        else this.smtpStatus = data.value;
      },
      error => {
        console.log(error);
      }
    );

  }

  save() {

    let configs: Config[] = [];
    configs.push(new Config("goodreads.key", this.goodReadsKey));
    configs.push(new Config("metadata.pull", String(this.metadataPull * 1000)));
    configs.push(new Config("smtp.provider", this.smtpProvider));
    configs.push(new Config("smtp.host", this.smtpHost));
    configs.push(new Config("smtp.port", this.smtpPort));
    configs.push(new Config("smtp.encryption", this.smtpEncryption));
    configs.push(new Config("smtp.username", this.smtpUsername));
    configs.push(new Config("smtp.password", this.smtpPassword));
    configs.push(new Config("books.recommendations", String(this.booksRecommendations)));


    this.configService.save(configs).subscribe(
      data => {
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.settings.actions.save.ok'), closable: false, life: 5000 });
        this.getData();
      },
      error => {
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.save.error'), closable: false, life: 5000 });
        this.getData();
      }
    );


  }

  isBooksFull() {
    return this.type === 'full' && this.entity === 'books';
  }

  isBooksPartial() {
    return this.type === 'partial' && this.entity === 'books';
  }

  isAuthorsFull() {
    return this.type === 'full' && this.entity === 'authors';
  }

  isAuthorsPartial() {
    return this.type === 'partial' && this.entity === 'authors';
  }

  isReviewsFull() {
    return this.type === 'full' && this.entity === 'reviews';
  }

  isReviewsPartial() {
    return this.type === 'partial' && this.entity === 'reviews';
  }

  isAllFull() {
    return this.type === 'full' && this.entity === 'all';
  }

  isAllPartial() {
    return this.type === 'partial' && this.entity === 'all';
  }



  doExecuteMetadata(type: string, entity: string) {

    if (
      type === 'full' && entity === 'reviews' && this.isReviewsFull() ||
      type === 'partial' && entity === 'reviews' && this.isReviewsPartial() ||
      type === 'full' && entity === 'authors' && this.isAuthorsFull() ||
      type === 'partial' && entity === 'authors' && this.isAuthorsPartial() ||
      type === 'full' && entity === 'books' && this.isBooksFull() ||
      type === 'partial' && entity === 'books' && this.isBooksPartial() ||
      type === 'full' && entity === 'all' && this.isAllFull() ||
      type === 'partial' && entity === 'all' && this.isAllPartial()
    ) {
      this.metadataService.stop().subscribe(
        data => {
          this.type = null;
          this.entity = null;
        },
        error => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.stop.error'), closable: false, life: 5000 });
        });
    }
    else
      this.metadataService.stop().subscribe(
        data => {
          this.metadataService.start("es", type, entity).subscribe(
            data => {
              console.log("Arrancado servicio data");
            },
            error => {
              console.log(error);
              this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
            }
          );
        },
        error => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.stop.error'), closable: false, life: 5000 });
        }
      );
  }



  doSendTestMail() {
    this.isSendTestMail = true;

    const user = JSON.parse(sessionStorage.user);
    this.mailService.sendTestMail(user.kindle).subscribe(
      data => {
        this.getSmtp(true);
        this.isSendTestMail = false;
      },
      error => {
        console.log(error);
        this.isSendTestMail = false;
      }
    );

  }

  newUser() {
    this.router.navigate(["profile"], { queryParams: { type: "new" } });
  }

  updateUser(user: User) {
    this.router.navigate(["profile"], { queryParams: { type: "update", user: user.username } });
  }


  deleteUser(id: string) {
    this.userService.delete(id).subscribe(
      data => {
        this.getUsers();

      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.delete.error'), closable: false, life: 5000 });
      }
    );
  }

  onChange(event) {
    switch (event.value) {
      case 'gmail': {
        this.smtpHost = 'smtp.gmail.com';
        this.smtpPort = '587';
        this.smtpEncryption = 'starttls';
        break;
      }
      case 'outlook': {
        this.smtpHost = 'smtp-mail.outlook.com';
        this.smtpPort = '587';
        this.smtpEncryption = 'starttls';
        break;
      }
      default: {
        this.getSmtp(false);
        break;
      }
    }
  }

}