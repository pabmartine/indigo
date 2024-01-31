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
import { lastValueFrom } from 'rxjs';

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

  async getDataStatus() {
    try {
      const data = await lastValueFrom(this.metadataService.getDataStatus());

      this.type = data.type;
      this.entity = data.entity;

      this.current = data.current;
      this.total = data.total;
      this.message = data.message;

      if (this.message) {
        this.message = this.translate.instant('locale.settings.panel.metadata.' + this.message);
      }

      if (this.total !== 0) {
        this.progressBar = Math.round((this.current * 100) / this.total);
      }

    } catch (error) {
      console.log(error);
    }
  }


  async getUsers() {
    try {
      const data = await lastValueFrom(this.userService.getAll());

      if (data) {
        this.userList = data;
      }

    } catch (error) {
      console.log(error);
    }
  }


  async getGlobal() {
    try {
      const data = await lastValueFrom(this.configService.get("books.recommendations"));

      if (data) {
        this.booksRecommendations = Number(data.value);
      }

    } catch (error) {
      console.log(error);
    }
  }


  async getMetadata() {
    try {
      const data = await lastValueFrom(this.configService.get("goodreads.key"));
      if (data) {
        this.goodReadsKey = data.value;
      }
    } catch (error) {
      console.error(error);
    }
  }

  async getSmtp(init: boolean) {
    try {
      if (init) {
        const providerData = await lastValueFrom(this.configService.get("smtp.provider"));
        this.smtpProvider = providerData?.value || 'other';
      }

      const hostData = await lastValueFrom(this.configService.get("smtp.host"));
      this.smtpHost = hostData?.value || '';

      const portData = await lastValueFrom(this.configService.get("smtp.port"));
      this.smtpPort = portData?.value || '';

      const encryptionData = await lastValueFrom(this.configService.get("smtp.encryption"));
      this.smtpEncryption = encryptionData?.value || '';

      const usernameData = await lastValueFrom(this.configService.get("smtp.username"));
      this.smtpUsername = usernameData?.value || '';

      const passwordData = await lastValueFrom(this.configService.get("smtp.password"));
      this.smtpPassword = passwordData?.value || '';

      const statusData = await lastValueFrom(this.configService.get("smtp.status"));
      this.smtpStatus = statusData?.value || 'unknown';

    } catch (error) {
      console.error(error);
    }
  }

  async save() {
    try {
      const configs: Config[] = [
        new Config("goodreads.key", this.goodReadsKey),
        new Config("smtp.provider", this.smtpProvider),
        new Config("smtp.host", this.smtpHost),
        new Config("smtp.port", this.smtpPort),
        new Config("smtp.encryption", this.smtpEncryption),
        new Config("smtp.username", this.smtpUsername),
        new Config("smtp.password", this.smtpPassword),
        new Config("books.recommendations", String(this.booksRecommendations))
      ];

      await lastValueFrom(this.configService.save(configs));

      this.messageService.add({
        severity: 'success',
        detail: this.translate.instant('locale.settings.actions.save.ok'),
        closable: false,
        life: 5000
      });

      await this.getData(); // Si getData() devuelve un observable, usa await lastValueFrom(this.getData());

    } catch (error) {
      this.messageService.add({
        severity: 'error',
        detail: this.translate.instant('locale.settings.actions.save.error'),
        closable: false,
        life: 5000
      });

      await this.getData(); // Si getData() devuelve un observable, usa await lastValueFrom(this.getData());
    }
  }

  isBooksFull() {
    return this.type === 'FULL' && this.entity === 'BOOKS';
  }

  isBooksPartial() {
    return this.type === 'PARTIAL' && this.entity === 'BOOKS';
  }

  isAuthorsFull() {
    return this.type === 'FULL' && this.entity === 'AUTHORS';
  }

  isAuthorsPartial() {
    return this.type === 'PARTIAL' && this.entity === 'AUTHORS';
  }

  isReviewsFull() {
    return this.type === 'FULL' && this.entity === 'REVIEWS';
  }

  isReviewsPartial() {
    return this.type === 'PARTIAL' && this.entity === 'REVIEWS';
  }

  isAllFull() {
    return this.type === 'FULL' && this.entity === 'LOAD';
  }

  isAllPartial() {
    return this.type === 'PARTIAL' && this.entity === 'LOAD';
  }



  async doExecuteMetadata(type: string, entity: string) {
    try {
      if (
        (type === 'FULL' && entity === 'REVIEWS' && this.isReviewsFull()) ||
        (type === 'PARTIAL' && entity === 'REVIEWS' && this.isReviewsPartial()) ||
        (type === 'FULL' && entity === 'AUTHORS' && this.isAuthorsFull()) ||
        (type === 'PARTIAL' && entity === 'AUTHORS' && this.isAuthorsPartial()) ||
        (type === 'FULL' && entity === 'BOOKS' && this.isBooksFull()) ||
        (type === 'PARTIAL' && entity === 'BOOKS' && this.isBooksPartial()) ||
        (type === 'FULL' && entity === 'LOAD' && this.isAllFull()) ||
        (type === 'PARTIAL' && entity === 'LOAD' && this.isAllPartial())
      ) {
        await lastValueFrom(this.metadataService.stop());
        this.type = null;
        this.entity = null;
      } else {
        await lastValueFrom(this.metadataService.stop());

        await lastValueFrom(this.metadataService.start("es", type, entity));
        console.log("Arrancado servicio data");
      }
    } catch (error) {
      console.log(error);
      const errorMessageKey = error?.error?.message || 'locale.settings.actions.error';
      this.messageService.add({ severity: 'error', detail: this.translate.instant(errorMessageKey), closable: false, life: 5000 });
    }
  }

  async doSendTestMail() {
    this.isSendTestMail = true;

    try {
      const user = JSON.parse(sessionStorage.user);
      await lastValueFrom(this.mailService.sendTestMail(user.kindle));

      // Refresh SMTP configuration after sending the test mail
      this.getSmtp(true);
    } catch (error) {
      console.log(error);
    } finally {
      this.isSendTestMail = false;
    }
  }


  newUser() {
    this.router.navigate(["profile"], { queryParams: { type: "new" } });
  }

  updateUser(user: User) {
    this.router.navigate(["profile"], { queryParams: { type: "update", user: user.username } });
  }


  async deleteUser(id: string) {
    try {
      await lastValueFrom(this.userService.delete(id));
      this.getUsers();
    } catch (error) {
      console.log(error);
      this.messageService.add({
        severity: 'error',
        detail: this.translate.instant('locale.settings.actions.delete.error'),
        closable: false,
        life: 5000
      });
    }
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