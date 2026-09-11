import { Component, OnDestroy, OnInit, SimpleChanges, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService, SelectItem } from 'primeng/api';
import { forkJoin, Subject, timer, Subscription } from 'rxjs';
import { takeUntil, switchMap, filter as rxFilter, catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { Config } from 'src/app/domain/config';
import { User } from 'src/app/domain/user';
import { AuthorService } from 'src/app/services/author.service';
import { ConfigService } from 'src/app/services/config.service';
import { MailService } from 'src/app/services/mail.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { UserService } from 'src/app/services/user.service';
import { FileService } from 'src/app/services/file.service';
import { AuthStateService } from 'src/app/services/auth-state.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService, ConfirmationService]
})
export class SettingsComponent implements OnInit, OnDestroy {

  type: string;
  entity: string;

  isSendTestMail: boolean = false;
  current: number = 0;
  total: number = 0;
  message: string;
  progressBar: number = 0;
  metadataRunning: boolean = false;
  metadataItems: any[] = [];
  metadataHistory: any[] = [];
  activityBusy = false;
  reviewQueue: any = {status: 'IDLE'};
  reviewQueueBusy = false;
  reviewQueueLoaded = false;
  reviewAmazonSeconds = 30;
  reviewGoodreadsSeconds = 30;
  get reviewQueueActive(): boolean { return ['RUNNING', 'PAUSED'].includes(this.reviewQueue.status) || !!this.reviewQueue.inFlight; }
  get reviewQueueState(): string {
    return ({IDLE: 'Sin iniciar', RUNNING: 'En curso', PAUSED: 'En pausa', STOPPED: 'Detenido', COMPLETED: 'Completado'} as any)[this.reviewQueue.status] || this.reviewQueue.status;
  }
  private updateReviewQueue(value: any): void {
    this.reviewQueue = value;
    if (!this.reviewQueueLoaded) {
      this.reviewAmazonSeconds = value.settings.amazon;
      this.reviewGoodreadsSeconds = value.settings.goodreads;
      this.reviewQueueLoaded = true;
    }
    this.cdr.markForCheck();
  }
  reviewQueueAction(action: string): void {
    if (this.reviewQueueBusy || !this.reviewQueueLoaded) return;
    if (action === 'missing' && this.reviewQueueActive) return;
    const replace = action === 'all' && this.reviewQueueActive;
    if (replace && !window.confirm('¿Cancelar el recorrido actual y empezar todas las reseñas desde cero? Las reseñas guardadas se conservan.')) return;
    if (action === 'stop' && !window.confirm('¿Parar el proceso? Se conservarán las reseñas obtenidas.')) return;
    const request = action === 'all' || action === 'missing' ? this.metadataService.startReviewQueue(action === 'all', replace)
      : action === 'settings' ? this.metadataService.configureReviewQueue(this.reviewAmazonSeconds, this.reviewGoodreadsSeconds)
      : this.metadataService.controlReviewQueue(action);
    this.reviewQueueBusy = true;
    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: result => {
        this.reviewQueueBusy = false;
        if (result) this.updateReviewQueue(result);
        this.messageService.add({severity: 'success', summary: action === 'settings' ? 'Intervalos guardados' : 'Proceso de reseñas actualizado'});
        this.cdr.markForCheck();
      },
      error: () => { this.reviewQueueBusy = false; this.messageService.add({severity: 'error', summary: 'No se pudo completar. Revisa los intervalos o actualiza el estado del proceso.'}); this.cdr.markForCheck(); }
    });
  }
  pendingImports: any[] = [];
  importsBusy = false;
  importsLoaded = false;
  importTaskLabels: {[key: string]: string} = {fileDone: 'Mover EPUB', authorsDone: 'Registrar autores', tagsDone: 'Registrar categorías'};

  loadPendingImports(): void {
    this.metadataService.pendingImports().pipe(takeUntil(this.destroy$)).subscribe({
      next: items => { this.pendingImports = items; this.importsLoaded = true; this.cdr.markForCheck(); },
      error: () => { this.messageService.add({severity: 'error', summary: 'No se pudieron cargar las importaciones pendientes'}); this.cdr.markForCheck(); }
    });
  }

  retryImport(item: any): void {
    if (this.importsBusy || this.uploadsRunning) return;
    this.importsBusy = true;
    this.metadataService.retryImport(item.bookId).pipe(takeUntil(this.destroy$)).subscribe({
      next: result => {
        this.importsBusy = false;
        const incomplete = result.pendingTasks?.length > 0;
        this.messageService.add({severity: incomplete ? 'warn' : 'success',
          summary: incomplete ? 'La importación sigue pendiente' : 'Importación completada', detail: result.lastError});
        this.loadPendingImports();
        this.cdr.markForCheck();
      },
      error: error => {
        this.importsBusy = false;
        this.messageService.add({severity: 'error', summary: error.status === 409
          ? 'Espera a que termine la importación en curso' : 'No se pudo reintentar la importación'});
        this.cdr.markForCheck();
      }
    });
  }

  loadActivity(): void {
    this.metadataService.activity().subscribe({ next: items => this.metadataItems = items,
      error: () => this.messageService.add({severity: 'error', summary: 'No se pudo cargar la actividad'}) });
    this.metadataService.history().subscribe({ next: items => this.metadataHistory = items,
      error: () => this.messageService.add({severity: 'error', summary: 'No se pudo cargar el historial'}) });
  }

  activityAction(action: 'retry' | 'undo' | 'lock' | 'unlock', item: any): void {
    if (this.activityBusy) return;
    if (action === 'undo' && !window.confirm('¿Deshacer estos cambios de metadatos?')) return;
    this.activityBusy = true;
    const request = action === 'retry' ? this.metadataService.retryItem(item._id)
      : action === 'undo' ? this.metadataService.undoItem(item._id)
      : this.metadataService.lockItem(item.type, item.entityId, action === 'lock');
    request.subscribe({ next: () => { this.activityBusy = false; this.loadActivity();
      this.messageService.add({severity: 'success', summary: 'Operación completada'}); },
      error: () => { this.activityBusy = false;
        this.messageService.add({severity: 'error', summary: 'No se pudo completar. Actualiza la lista; los datos pueden haber cambiado.'}); } });
  }
  metadataCompletedAt: number | null = null;
  metadataRuns: { [key: string]: any } = {};
  private metadataCompletionNotified = false;

  uploads: number = 0;
  uploadsProgress: number = 0;
  uploadsRunning: boolean = false;
  uploadsProcessed: number = 0;
  uploadsFailed: number = 0;
  uploadsSucceeded: number = 0;
  uploadsNewBooks: number = 0;
  uploadsUpdatedBooks: number = 0;
  uploadsMoved: number = 0;
  uploadsDeleted: number = 0;
  private uploadsCompletionNotified = false;

  userList: User[];
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
  booksTotal: number = 0;
  authorsTotal: number = 0;
  reviewsTotal: number = 0;


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

  private destroy$ = new Subject<void>();
  private statusPollingSub: Subscription | null = null;

  constructor(private messageService: MessageService,
    public translate: TranslateService,
    public authorService: AuthorService,
    public metadataService: MetadataService,
    public configService: ConfigService,
    public fileService: FileService,
    public mailService: MailService,
    public userService: UserService,
    private router: Router,
    private confirmationService: ConfirmationService,
    private authState: AuthStateService,
    private cdr: ChangeDetectorRef) {


  }

  getData() {
    this.getUsers();
    this.getGlobal();
    this.getMetadataSummary();
    this.getSmtp();
    this.getUploads();
  }

  ngOnInit(): void {

    this.getData();
    this.loadActivity();
    this.startStatusPolling();
    timer(0, 3000).pipe(rxFilter(() => !document.hidden),
      switchMap(() => this.metadataService.reviewQueue().pipe(catchError(() => EMPTY))),
      takeUntil(this.destroy$)).subscribe(value => this.updateReviewQueue(value));

  }

  ngOnChanges(changes: SimpleChanges) {
    // Removed console.log for production
  }

  private startStatusPolling(): void {
    if (this.statusPollingSub) {
      return;
    }

    this.statusPollingSub = timer(0, 1000)
      .pipe(
        rxFilter(() => !document.hidden),
        switchMap(() => this.metadataService.getDataStatus()),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data) => this.handleMetadataStatus(data),
        error: (error) => {
          console.error('[Settings] Error fetching data status:', error);
        }
      });
  }

  private handleMetadataStatus(data: any): void {
    if (!data) {
      return;
    }

    const wasUploadsRunning = this.uploadsRunning;
    const wasMetadataRunning = this.metadataRunning;
    this.type = data.type;
    this.entity = data.entity;
    this.metadataRunning = !!data.status;
    this.metadataCompletedAt = data.completedAt || null;
    this.current = data.current;
    this.total = data.total;
    this.message = data.message;
    this.metadataRuns = data.runs || {};
    if (this.type && this.entity) {
      this.metadataRuns[`${this.type}:${this.entity}`] = {
        ...(this.metadataRuns[`${this.type}:${this.entity}`] || {}),
        type: this.type, entity: this.entity, status: this.metadataRunning,
        current: this.current, total: this.total, completedAt: this.metadataCompletedAt,
        found: data.found || 0, notFound: data.notFound || 0,
        skipped: data.skipped || 0, errors: data.errors || 0
      };
    }

    this.uploads = data.uploadsTotal;
    this.uploadsProgress = data.uploadsCurrent;
    this.uploadsRunning = !!data.uploadsRunning;
    this.uploadsProcessed = data.uploadsProcessed || 0;
    this.uploadsFailed = data.uploadsFailed || 0;
    this.uploadsSucceeded = data.uploadsSucceeded || 0;
    this.uploadsNewBooks = data.uploadsNewBooks || 0;
    this.uploadsUpdatedBooks = data.uploadsUpdatedBooks || 0;
    this.uploadsMoved = data.uploadsMoved || 0;
    this.uploadsDeleted = data.uploadsDeleted || 0;

    if (this.metadataRunning) {
      this.metadataCompletionNotified = false;
    }
    else if (wasMetadataRunning && this.total > 0 && !this.metadataCompletionNotified) {
      this.metadataCompletionNotified = true;
      this.messageService.add({
        severity: (data.errors || 0) > 0 ? 'warn' : 'success',
        summary: 'Actualización de metadatos',
        detail: `Proceso terminado: ${data.found || 0} encontrados, ${data.notFound || 0} sin coincidencia, ${data.errors || 0} errores.`,
        closable: false,
        life: 10000
      });
    }

    if (this.uploadsRunning) {
      this.uploadsCompletionNotified = false;
    }
    else if (wasUploadsRunning && this.uploadsProcessed > 0 && !this.uploadsCompletionNotified) {
      this.uploadsCompletionNotified = true;
      this.messageService.add({
        severity: this.uploadsFailed > 0 ? 'warn' : 'success',
        summary: 'Importación de libros',
        detail: this.uploadsFailed > 0
          ? `Terminada con ${this.uploadsFailed} error(es). El resumen permanece visible en Subida de nuevos libros.`
          : `Terminada: ${this.uploadsProcessed} libro(s) procesado(s). El resumen permanece visible en Subida de nuevos libros.`,
        closable: false,
        life: 10000
      });
    }

    if (this.message) {
      this.message = this.translate.instant('locale.settings.panel.metadata.' + this.message);
    }

    if (this.total !== 0) {
      this.progressBar = Math.round((this.current * 100) / this.total);
    } else {
      this.progressBar = 0;
    }

    this.cdr.markForCheck();
  }


  getUsers(): void {
    this.userService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            this.userList = data;
            this.cdr.markForCheck();
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }


  getGlobal(): void {
    this.configService.get("books.recommendations")
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            this.booksRecommendations = Number(data.value);
            this.cdr.markForCheck();
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }

  getUploads(): void {
    this.fileService.getUploadsPath()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            this.uploadsPath = data.path;
            this.cdr.markForCheck();
          }
        },
        error: (error) => {
          console.log(error);
        }
      });
  }


  getMetadataSummary(): void {
    this.metadataService.getSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.booksTotal = data?.books || 0;
          this.authorsTotal = data?.authors || 0;
          this.reviewsTotal = data?.reviews || 0;
          this.cdr.markForCheck();
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

    forkJoin(observables)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ([provider, host, port, encryption, username, password, status]) => {
          this.smtpProvider = provider?.value || 'other';
          this.smtpHost = host?.value || '';
          this.smtpPort = port?.value || '';
          this.smtpEncryption = encryption?.value || '';
          this.smtpUsername = username?.value || '';
          this.smtpPassword = password?.value || '';
          this.smtpStatus = status?.value || 'unknown';
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.log(error);
        }
      });

  }

  save(): void {
    const configs: Config[] = [
      new Config("smtp.provider", this.smtpProvider),
      new Config("smtp.host", this.smtpHost),
      new Config("smtp.port", this.smtpPort),
      new Config("smtp.encryption", this.smtpEncryption),
      new Config("smtp.username", this.smtpUsername),
      new Config("smtp.password", this.smtpPassword),
      new Config("books.recommendations", String(this.booksRecommendations))
    ];

    this.configService.save(configs)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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
    if (this.uploadsRunning) {
      return;
    }

    this.fileService.upload(data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.uploads = data;
          this.uploadsProgress = 0;
          this.uploadsProcessed = 0;
          this.uploadsFailed = 0;
          this.uploadsSucceeded = 0;
          this.uploadsNewBooks = 0;
          this.uploadsUpdatedBooks = 0;
          this.uploadsMoved = 0;
          this.uploadsDeleted = 0;
          this.uploadsRunning = true;
          this.uploadsCompletionNotified = false;
          this.messageService.clear();
          this.messageService.add({ severity: 'info', detail: 'Importación iniciada. El progreso se mostrará en esta sección.', closable: false, life: 5000 });
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.log(error);
          this.messageService.add({ severity: 'error', detail: 'No se pudo iniciar la importacion de libros.', closable: false, life: 5000 });
        }
      });
  }

  getUploadsStatusText(): string {
    if (this.uploadsRunning) {
      return `Procesando ${this.uploadsProcessed} de ${this.uploads} libros`;
    }

    if (this.uploadsProcessed === 0) {
      return '';
    }

    return this.uploadsFailed > 0
      ? `Ultima importacion: ${this.uploadsProcessed} procesados, ${this.uploadsFailed} con error`
      : `Ultima importacion: ${this.uploadsProcessed} procesados`;
  }

  hasUploadsResult(): boolean {
    return !this.uploadsRunning && this.uploadsProcessed > 0;
  }

  openImportedBooks(): void {
    this.router.navigate(['/books']);
  }

  isBooksFull() {
    return this.metadataRunning && this.type === 'FULL' && this.entity === 'BOOKS';
  }

  isBooksPartial() {
    return this.metadataRunning && this.type === 'PARTIAL' && this.entity === 'BOOKS';
  }

  isAuthorsFull() {
    return this.metadataRunning && this.type === 'FULL' && this.entity === 'AUTHORS';
  }

  isAuthorsPartial() {
    return this.metadataRunning && this.type === 'PARTIAL' && this.entity === 'AUTHORS';
  }

  isReviewsFull() {
    return this.metadataRunning && this.type === 'FULL' && this.entity === 'REVIEWS';
  }

  isReviewsPartial() {
    return this.metadataRunning && this.type === 'PARTIAL' && this.entity === 'REVIEWS';
  }

  isMetadataRunning(type: string, entity: string): boolean {
    return this.type === type && this.entity === entity;
  }

  getMetadataProgress(type: string, entity: string): number {
    const run = this.getMetadataRun(type, entity);
    return run?.total ? Math.round((run.current * 100) / run.total) : 0;
  }

  getMetadataCounter(type: string, entity: string): string {
    const run = this.getMetadataRun(type, entity);
    if (!run || run.total === 0) {
      return '0 / 0 elementos';
    }

    return `${run.current} / ${run.total} elementos`;
  }

  getMetadataStatusLabel(type: string, entity: string): string {
    const run = this.getMetadataRun(type, entity);
    if (!run) {
      return 'Listo';
    }
    return run.status ? 'En curso' : (run.errors > 0 ? 'Con errores' : 'Finalizado');
  }

  getMetadataLastExecution(type: string, entity: string): string {
    const run = this.getMetadataRun(type, entity);
    if (!run?.completedAt) {
      return '-';
    }
    return new Date(run.completedAt).toLocaleString();
  }

  getMetadataResult(type: string, entity: string): string {
    const run = this.getMetadataRun(type, entity);
    if (!run || run.current === 0) {
      return '';
    }
    return `${run.found || 0} encontrados · ${run.notFound || 0} sin coincidencia · ${run.skipped || 0} omitidos · ${run.errors || 0} errores`;
  }

  private getMetadataRun(type: string, entity: string): any {
    return this.metadataRuns[`${type}:${entity}`];
  }



  doExecuteMetadata(type: string, entity: string): void {
    if (entity === 'REVIEWS') { this.reviewQueueAction(type === 'FULL' ? 'all' : 'missing'); return; }
    const startMetadataService = () => {
      this.metadataService.start("es", type, entity)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.type = type;
            this.entity = entity;
            this.current = 0;
            this.total = 0;
            this.progressBar = 0;
            this.metadataRunning = true;
            this.metadataCompletedAt = null;
            this.metadataCompletionNotified = false;
            this.messageService.add({ severity: 'info', detail: 'Actualización de metadatos iniciada.', closable: false, life: 5000 });
            this.cdr.markForCheck();
          },
          error: (error) => {
            console.log(error);
            this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.actions.start.error'), closable: false, life: 5000 });
          }
        });
    };

    const stopAndStartMetadataService = () => {
      this.metadataService.stop()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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
      this.metadataService.stop()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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

    const user = this.authState.getCurrentUser();
    if (!user?.kindle) {
      this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.settings.panel.smtp.error.kindle'), closable: false, life: 5000 });
      this.isSendTestMail = false;
      return;
    }

    this.mailService.sendTestMail(user.kindle)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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
    this.userService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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
    if (this.uploadsRunning) {
      return;
    }

    this.fileService.count()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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

  ngOnDestroy(): void {
    this.statusPollingSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

}
