import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { TagService } from 'src/app/services/tag.service';
import { Router } from '@angular/router';
import { Tag } from 'src/app/domain/tag';
import { SelectItem } from 'primeng/api/selectitem';
import { MenuItem } from 'primeng/api/menuitem';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Interfaz para tags con imagen temporal
interface TagWithTempImage extends Tag {
  originalImage?: string;
}

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class CategoriesComponent implements OnInit, OnDestroy {

  items: MenuItem[];

  tags: Tag[] = [];
  sortedTags: Tag[];
  sourceTag: Tag = new Tag();
  targetTag: Tag;
  newTag: string;
  background_image: string;

  title: string;
  total: number;
  private sort: string;
  private order: string;

  sorts: SelectItem[] = [];
  selectedSort: string;

  rename: boolean;
  merge: boolean;
  image: boolean;

  renameValid: boolean = false;
  mergeValid: boolean = false;
  imageValid: boolean = false;

  // Estado de carga
  isLoading: boolean = false;

  user = JSON.parse(sessionStorage.user);

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();

  // Cache para optimizar rendimiento
  private tagsCache: Tag[] = null;

  constructor(
    private tagService: TagService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.initializeSortOptions();
    this.initializeMenuItems();
    this.reset();
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeSortOptions(): void {
    this.sorts = [
      { label: this.translate.instant('locale.tags.order_by.total.desc'), value: 'numBooks,desc' },
      { label: this.translate.instant('locale.tags.order_by.total.asc'), value: 'numBooks,asc' },
      { label: this.translate.instant('locale.tags.order_by.name.asc'), value: 'name,asc' },
      { label: this.translate.instant('locale.tags.order_by.name.desc'), value: 'name,desc' }
    ];
  }

  private initializeMenuItems(): void {
    this.items = [
      {
        label: this.translate.instant('locale.tags.actions.rename.title'),
        icon: 'menu-icon fa fa-font',
        command: () => this.showRename()
      },
      {
        label: this.translate.instant('locale.tags.actions.merge.title'),
        icon: 'menu-icon fa fa-compress',
        command: () => this.showMerge()
      },
      {
        label: this.translate.instant('locale.tags.actions.image.modify.title'),
        icon: 'menu-icon fa fa-image',
        command: () => this.showImage()
      },
      {
        label: this.translate.instant('locale.tags.actions.image.update.title'),
        icon: 'menu-icon fa fa-image',
        command: () => this.updateImage()
      }
    ];
  }

  private loadData(): void {
    // Usar cache si está disponible
    if (this.tagsCache) {
      this.tags = [...this.tagsCache];
      this.title = this.translate.instant('locale.tags.title') + " (" + this.tags.length + ")";
      this.cdr.detectChanges();
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    this.getAll();
  }

  // Método para trackBy en ngFor
  trackByTagId(index: number, tag: Tag): string {
    return tag.id ? tag.id.toString() : index.toString();
  }

  onChange(event): void {
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('tags_order', this.selectedSort);

    this.tags.length = 0;
    this.tagsCache = null; // Limpiar cache cuando cambia el orden

    this.getAll();
  }

  getAll(): void {
    this.tagService.getAll(this.user.languageBooks, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          // INMEDIATAMENTE mostrar tags sin procesar imágenes
          const tagsWithoutImages: TagWithTempImage[] = data.map(tag => ({
            ...tag,
            image: tag.image ? null : './assets/images/unknown.jpg', // Placeholder si no hay imagen
            originalImage: tag.image // Guardar imagen original
          }));

          this.tags = tagsWithoutImages;
          this.title = this.translate.instant('locale.tags.title') + " (" + this.tags.length + ")";
          this.isLoading = false;
          this.cdr.detectChanges();

          // Procesar imágenes de forma asíncrona
          this.processTagsImagesAsync(tagsWithoutImages);

          // Guardar en cache con imágenes procesadas para futuras cargas
          const processedTags = this.processTags(data);
          this.tagsCache = [...processedTags];
        },
        error: (error) => {
          console.log(error);
          this.isLoading = false;
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.data'),
            closable: false,
            life: 5000
          });
          this.cdr.detectChanges();
        }
      });
  }

  private processTags(data: Tag[]): Tag[] {
    return data.map(tag => {
      const processedTag = { ...tag };
      // Las tags ya vienen con imagen procesada del servidor o null
      if (!processedTag.image) {
        processedTag.image = './assets/images/unknown.jpg';
      }
      return processedTag;
    });
  }

  private processTagsImagesAsync(tags: TagWithTempImage[]): void {
    // Procesar imágenes en pequeños lotes para no bloquear la UI
    const batchSize = 4;
    let currentIndex = 0;

    const processBatch = () => {
      const endIndex = Math.min(currentIndex + batchSize, tags.length);

      for (let i = currentIndex; i < endIndex; i++) {
        const tag = tags[i];

        if (tag.originalImage && i < this.tags.length) {
          // Procesar imagen de forma asíncrona
          setTimeout(() => {
            if (i < this.tags.length) {
              this.tags[i].image = tag.originalImage;
              this.cdr.detectChanges();
            }
          }, i * 20); // Pequeño delay entre imágenes
        }
      }

      currentIndex = endIndex;

      // Continuar con el siguiente lote si hay más imágenes
      if (currentIndex < tags.length) {
        setTimeout(processBatch, 80); // Pausa entre lotes
      }
    };

    // Iniciar procesamiento
    setTimeout(processBatch, 100);
  }

  getBooksByTag(tag: Tag): void {
    this.reset();

    const search: Search = new Search();
    search.selectedTags = [];
    search.selectedTags.push(tag.name);
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  showMenu(): boolean {
    return this.title && JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  showRename(): void {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.rename = true;
  }

  showMerge(): void {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.targetTag = this.sortedTags[0];
    this.merge = true;
  }

  showImage(): void {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.image = true;
  }

  updateImage(): void {
    this.messageService.clear();

    this.tagService.updateImage(this.sourceTag.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.sourceTag.image = data.image;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            key: "rename",
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.image'),
            closable: false,
            life: 5000
          });
        }
      });
  }

  validateRename(event): void {
    const found = this.sortedTags.filter(tag => {
      return tag.name === this.newTag;
    })[0];

    this.messageService.clear();

    if (found) {
      this.renameValid = false;
      this.messageService.add({
        key: "rename",
        severity: 'error',
        detail: this.translate.instant('locale.tags.actions.rename.error'),
        closable: false,
        life: 5000
      });
    } else {
      this.renameValid = true;
      this.messageService.add({
        key: "rename",
        severity: 'info',
        detail: this.translate.instant('locale.tags.actions.rename.info', {
          source: this.sourceTag.name,
          target: this.newTag
        }),
        closable: false,
        life: 5000
      });
    }
  }

  validateMerge(event): void {
    this.messageService.clear();

    if (this.sourceTag == this.targetTag) {
      this.mergeValid = false;
      this.messageService.add({
        key: "merge",
        severity: 'error',
        detail: this.translate.instant('locale.tags.actions.merge.error'),
        closable: false,
        life: 5000
      });
    } else {
      this.mergeValid = true;
      this.messageService.add({
        key: "merge",
        severity: 'info',
        detail: this.translate.instant('locale.tags.actions.merge.info', {
          source: this.sourceTag.name,
          target: this.targetTag.name
        }),
        closable: false,
        life: 5000
      });
    }
  }

  validateImage(event): void {
    this.messageService.clear();

    if (!this.background_image.startsWith("http") || !this.background_image.includes(".")) {
      this.imageValid = false;
      this.messageService.add({
        key: "image",
        severity: 'error',
        detail: this.translate.instant('locale.tags.actions.image.error'),
        closable: false,
        life: 5000
      });
    } else {
      this.imageValid = true;
      this.messageService.add({
        key: "image",
        severity: 'info',
        detail: this.translate.instant('locale.tags.actions.image.info'),
        closable: false,
        life: 5000
      });
    }
  }

  doRename(): void {
    this.tagService.rename(this.sourceTag.id, this.newTag)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.reset();
          this.tagsCache = null; // Limpiar cache
          this.getAll();
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            key: "rename",
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.rename'),
            closable: false,
            life: 5000
          });
        }
      });

    this.tags.length = 0;
    this.newTag = null;
  }

  doMerge(): void {
    this.tagService.merge(this.sourceTag.id, this.targetTag.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.reset();
          this.tagsCache = null; // Limpiar cache
          this.getAll();
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            key: "merge",
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.merge'),
            closable: false,
            life: 5000
          });
        }
      });

    this.tags.length = 0;
    this.targetTag = null;
  }

  doImage(): void {
    this.tagService.saveImage(this.sourceTag.id, this.background_image)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.reset();
          this.tagsCache = null; // Limpiar cache
          this.getAll();
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            key: "image",
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.image'),
            closable: false,
            life: 5000
          });
        }
      });

    this.tags.length = 0;
    this.background_image = null;
  }

  private reset(): void {
    this.tags.length = 0;
    this.total = 0;

    this.selectedSort = sessionStorage.getItem('tags_order');
    if (!this.selectedSort) {
      this.sort = "name";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    } else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }
  }
}
