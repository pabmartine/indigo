import { Component, OnInit, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { AuthorService } from 'src/app/services/author.services';
import { ConfigService } from 'src/app/services/config.service';
import { Config } from 'src/app/domain/config';
import { UtilService } from 'src/app/services/util.service';
import { User } from 'src/app/domain/user';
import { UserService } from 'src/app/services/user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  providers: [MessageService]
})
export class SettingsComponent implements OnInit {


  isData: boolean = false;
  isNoData: boolean = false;
  isSendTestMail: boolean = false;
  current: number = 0;
  total: number = 0;
  progressBar: number = 0;

  userList: User[];
  goodReadsKey: string;
  metadataPull: number;
  kindlegenPath: string;
  booksRecommendations: number;
  booksRecommendations2: number;

  smtpHost: string;
  smtpPort: string;
  smtpEncryption: string;
  smtpUsername: string;
  smtpPassword: string;
  smtpStatus: string;
  encryptions: SelectItem[] = [
    { label: '', value: 'none' },
    { label: 'STARTTLS', value: 'starttls' },
    { label: 'SSL/TLS', value: 'ssl/tls' }
  ];

  panelStates = new Map();


  constructor(private messageService: MessageService,
    public translate: TranslateService,
    public authorService: AuthorService,
    public configService: ConfigService,
    public utilService: UtilService,
    public userService: UserService,
    private router: Router) {


  }

  getData(){
    this.getDataStatus();
    this.getUsers();
    this.getGlobal();
    this.getMetadata();
    this.getSmtp();
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
    this.authorService.getDataStatus().subscribe(
      data => {
        if (data.type == 'data') {
          this.isData = data.status;
          this.isNoData = null;
        } else {
          this.isData = null;
          this.isNoData = data.status;
        }
        this.current = data.current;
        this.total = data.total;

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
    this.configService.get("kindlegen.path").subscribe(
      data => {
        if (data)
          this.kindlegenPath = data.value;
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("books.recommendations").subscribe(
      data => {
        if (data)
          this.booksRecommendations = Number(data.value);
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("books.recommendations2").subscribe(
      data => {
        if (data)
          this.booksRecommendations2 = Number(data.value);
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

  getSmtp() {
    this.configService.get("smtp.host").subscribe(
      data => {
        if (data)
          this.smtpHost = data.value;
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.port").subscribe(
      data => {
        if (data)
          this.smtpPort = data.value;
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.encryption").subscribe(
      data => {
        if (data)
          this.smtpEncryption = data.value;
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.username").subscribe(
      data => {
        if (data)
          this.smtpUsername = data.value;
      },
      error => {
        console.log(error);
      }
    );

    this.configService.get("smtp.password").subscribe(
      data => {
        if (data)
          this.smtpPassword = data.value;
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

    let configs: Config[] =  [];
    configs.push(new Config("goodreads.key", this.goodReadsKey));
    configs.push(new Config("metadata.pull", String(this.metadataPull * 1000)));
    configs.push(new Config("smtp.host", this.smtpHost));
    configs.push(new Config("smtp.port", this.smtpPort));
    configs.push(new Config("smtp.encryption", this.smtpEncryption));
    configs.push(new Config("smtp.username", this.smtpUsername));
    configs.push(new Config("smtp.password", this.smtpPassword));
    configs.push(new Config("kindlegen.path", this.kindlegenPath));
    configs.push(new Config("books.recommendations", String(this.booksRecommendations)));
    configs.push(new Config("books.recommendations2", String(this.booksRecommendations2)));

    
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

  doExecuteNoData() {
    this.isNoData = !this.isNoData;
    this.isData = false;

    this.authorService.stopData().subscribe(
      data => {
        if (this.isNoData) {

          this.authorService.startNoData("es").subscribe(
            data => {
            },
            error => {
              console.log(error);
              this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
            }
          );
        }

      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
      }
    );

  }

  doExecuteData() {
    this.isData = !this.isData;
    this.isNoData = false;



    this.authorService.stopData().subscribe(
      data => {
        if (this.isData) {
          this.authorService.startData("es").subscribe(
            data => {
              console.log("Arrancado servicio data");
            },
            error => {
              console.log(error);
              this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
            }
          );

        }
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
    this.utilService.sendTestMail(user.id).subscribe(
      data => {
        this.getSmtp();
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

  
  deleteUser(id: number) {
    this.userService.delete(id).subscribe(
      data => {
        this.getUsers();

      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.delete.error'), closable: false, life: 5000 });
      }
    );  }

}