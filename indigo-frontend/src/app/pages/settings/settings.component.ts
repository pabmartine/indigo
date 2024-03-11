import { Component, OnInit, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService, SelectItem } from 'primeng/api';
import { forkJoin } from 'rxjs';
import { Config } from 'src/app/domain/config';
import { User } from 'src/app/domain/user';
import { AuthorService } from 'src/app/services/author.service';
import { ConfigService } from 'src/app/services/config.service';
import { MailService } from 'src/app/services/mail.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { UserService } from 'src/app/services/user.service';
import { FileService } from 'src/app/services/file.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class SettingsComponent implements OnInit {

  type: string;
  entity: string;

  isSendTestMail: boolean = false;
  current: number = 0;
  total: number = 0;
  message: string;
  progressBar: number = 0;

  uploads: number = 0;
  uploadsProgress: number = 0;

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

  uploadsPath: string;
  badge: number = 0;


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
    public fileService: FileService,
    public mailService: MailService,
    public userService: UserService,
    private router: Router,
    private confirmationService: ConfirmationService) {


  }

  getData() {
    this.getDataStatus();
    this.getUsers();
    this.getGlobal();
    this.getMetadata();
    this.getSmtp();
    this.getUploads();
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

  getDataStatus(): void {
    this.metadataService.getDataStatus().subscribe({
      next: (data) => {
        this.type = data.type;
        this.entity = data.entity;
        this.current = data.current;
        this.total = data.total;
        this.message = data.message;

        this.uploads = data.uploadsTotal;
        this.uploadsProgress = data.uploadsCurrent;
  
        if (this.message) {
          this.message = this.translate.instant('locale.settings.panel.metadata.' + this.message);
        }
  
        if (this.total !== 0) {
          this.progressBar = Math.round((this.current * 100) / this.total);
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }
  

  getUsers(): void {
    this.userService.getAll().subscribe({
      next: (data) => {
        if (data) {
          this.userList = data;
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }
  

  getGlobal(): void {
    this.configService.get("books.recommendations").subscribe({
      next: (data) => {
        if (data) {
          this.booksRecommendations = Number(data.value);
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  getUploads(): void {
    this.fileService.getUploadsPath().subscribe({
      next: (data) => {
        if (data) {
          this.uploadsPath = data.path;
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }
  

  getMetadata(): void {
    this.configService.get("goodreads.key").subscribe({
      next: (data) => {
        if (data) {
          this.goodReadsKey = data.value;
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }
  

  getSmtp(): void {
    const observables = [
      this.configService.get("smtp.provider"),
      this.configService.get("smtp.host"),
      this.configService.get("smtp.port"),
      this.configService.get("smtp.encryption"),
      this.configService.get("smtp.username"),
      this.configService.get("smtp.password"),
      this.configService.get("smtp.status")
    ];
  
    forkJoin(observables).subscribe({
      next: ([provider, host, port, encryption, username, password, status]) => {
        this.smtpProvider = provider?.value || 'other';
        this.smtpHost = host?.value || '';
        this.smtpPort = port?.value || '';
        this.smtpEncryption = encryption?.value || '';
        this.smtpUsername = username?.value || '';
        this.smtpPassword = password?.value || '';
        this.smtpStatus = status?.value || 'unknown';
      },
      error: (error) => {
        console.log(error);
      }
    });
  
  }

  save(): void {
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
  
    this.configService.save(configs).subscribe({
      next: () => {
        this.messageService.clear();
        this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.settings.actions.save.ok'), closable: false, life: 5000 });
      },
      error: (error) => {
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.save.error'), closable: false, life: 5000 });
        console.error(error);
      }
    });
  }
  


  upload(data:number): void {
    this.fileService.upload(data).subscribe({
      next: (data) => {
        if (data) {
          console.log(data);
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
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



  doExecuteMetadata(type: string, entity: string): void {
    const startMetadataService = () => {
      this.metadataService.start("es", type, entity).subscribe({
        next: () => {
          console.log("Arrancado servicio data");
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
        }
      });
    };
  
    const stopAndStartMetadataService = () => {
      this.metadataService.stop().subscribe({
        next: () => {
          startMetadataService();
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.stop.error'), closable: false, life: 5000 });
        }
      });
    };
  
    if (
      (type === 'FULL' && entity === 'REVIEWS' && this.isReviewsFull()) ||
      (type === 'PARTIAL' && entity === 'REVIEWS' && this.isReviewsPartial()) ||
      (type === 'FULL' && entity === 'AUTHORS' && this.isAuthorsFull()) ||
      (type === 'PARTIAL' && entity === 'AUTHORS' && this.isAuthorsPartial()) ||
      (type === 'FULL' && entity === 'BOOKS' && this.isBooksFull()) ||
      (type === 'PARTIAL' && entity === 'BOOKS' && this.isBooksPartial())
    ) {
      this.metadataService.stop().subscribe({
        next: () => {
          this.type = null;
          this.entity = null;
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.stop.error'), closable: false, life: 5000 });
        }
      });
    } else {
      stopAndStartMetadataService();
    }
  }
  



  doSendTestMail() {
    this.isSendTestMail = true;
  
    const user = JSON.parse(sessionStorage.user);
    this.mailService.sendTestMail(user.kindle).subscribe({
      next: (data) => {
        this.getSmtp();
        this.isSendTestMail = false;
      },
      error: (error) => {
        console.log(error);
        this.isSendTestMail = false;
      }
    });
  }
  

  newUser() {
    this.router.navigate(["profile"], { queryParams: { type: "new" } });
  }

  updateUser(user: User) {
    this.router.navigate(["profile"], { queryParams: { type: "update", user: user.username } });
  }


  deleteUser(id: string) {
    this.userService.delete(id).subscribe({
      next: (data) => {
        this.getUsers();
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.delete.error'), closable: false, life: 5000 });
      }
    });
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
        this.getSmtp();
        break;
      }
    }
  }

  detect(){

    this.fileService.count().subscribe({
      next: (data) => {
        console.log(data);
        if (data>0) {
          this.confirmationService.confirm({ 
            message: 'Se han detectado ' + data + ' libros nuevos. ¿Desea añadirlos a su biblioteca?', 
            header: 'Añadir libros',
            acceptLabel: 'Aceptar',
            rejectLabel: 'Cancelar',
            accept: () => {
              this.upload(data);
            },
          }); 
         } else {
          this.confirmationService.confirm({ 
            message: 'No se han detectado libros nuevos en ' + this.uploadsPath, 
            header: 'Añadir libros',
            acceptLabel: 'Cerrar',
            rejectVisible: false
          });
         }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }


}