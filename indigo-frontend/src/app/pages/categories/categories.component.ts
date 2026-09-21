import { CatalogViewport } from 'src/app/utils/catalog-viewport';
import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
import { AuthStateService } from 'src/app/services/auth-state.service';
import { User } from 'src/app/domain/user';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class CategoriesComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('catalogGrid') catalogGrid?: ElementRef<HTMLDivElement>;
  private viewport?: CatalogViewport;
  private initialFrame?: number;

  @ViewChild('scrollSentinel') scrollSentinel?: ElementRef<HTMLDivElement>;

  items: MenuItem[];

  tags: Tag[] = [];
  sortedTags: Tag[];
  sourceTag: Tag = new Tag();
  targetTag: Tag;
  newTag: string;
  background_image: string;

  title: string;
  total: number = 0;
  private exhausted = false;
  private page: number = 0;
  private size: number = 60;
  private sort: string;
  private order: string;

  sorts: SelectItem[] = [
    { label: 'Total (Desc)', value: 'numBooks,desc' },
    { label: 'Total (Asc)', value: 'numBooks,asc' },
    { label: 'Name (Asc)', value: 'name,asc' },
    { label: 'Name (Desc)', value: 'name,desc' }
  ];
  selectedSort: string;

  rename: boolean;
  merge: boolean;
  image: boolean;

  renameValid: boolean = false;
  mergeValid: boolean = false;
  imageValid: boolean = false;

  // Estado de carga
  isLoading: boolean = false;
  isScrolling: boolean = false;

  user: User;

  // Subject para manejar la destrucción del componente
  private destroy$ = new Subject<void>();
  private resetPages$ = new Subject<void>();

  // Cache para optimizar rendimiento
  private tagsCache = new Map<string, Tag[]>();

  constructor(
    private tagService: TagService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private authState: AuthStateService
  ) {
    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;
    if (!this.user.languageBooks || this.user.languageBooks.length === 0) {
      this.user.languageBooks = this.authState.getLanguageBooks();
    }
  }

  ngOnInit(): void {
    this.initializeSortOptions();
    this.initializeMenuItems();
    this.reset();
  }

  ngAfterViewInit(): void {
    this.initialFrame = requestAnimationFrame(() => {
      this.viewport = new CatalogViewport(this.catalogGrid!.nativeElement,
        this.scrollSentinel!.nativeElement, () => this.ngZone.run(() => this.onScroll()));
      this.size = this.viewport.pageSize(216);
      this.ngZone.runOutsideAngular(() => this.viewport!.start());
      this.loadInitialData();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.resetPages$.complete();
    this.tagsCache.clear();
    this.viewport?.destroy();
    if (this.initialFrame !== undefined) cancelAnimationFrame(this.initialFrame);
  }

  private initializeSortOptions(): void {
    const translationKeys = [
      'locale.tags.order_by.total.desc',
      'locale.tags.order_by.total.asc',
      'locale.tags.order_by.name.asc',
      'locale.tags.order_by.name.desc'
    ];

    this.translate.get(translationKeys)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (translations) => {
          if (translations && Object.keys(translations).length > 0) {
            this.sorts = [
              { label: translations['locale.tags.order_by.total.desc'] || 'Total (Desc)', value: 'numBooks,desc' },
              { label: translations['locale.tags.order_by.total.asc'] || 'Total (Asc)', value: 'numBooks,asc' },
              { label: translations['locale.tags.order_by.name.asc'] || 'Name (Asc)', value: 'name,asc' },
              { label: translations['locale.tags.order_by.name.desc'] || 'Name (Desc)', value: 'name,desc' }
            ];
            this.cdr.detectChanges();
          }
        },
        error: (error) => {
          console.error('Error loading translations for sorts:', error);
        }
      });
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

  private loadInitialData(): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.tagService.getSummaryPage(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$), takeUntil(this.resetPages$))
      .subscribe({
        next: (response) => {
          this.exhausted = (response.items || []).length < this.size;
          this.total = response.total;
          this.title = this.translate.instant('locale.tags.title') + " (" + this.total + ")";

          const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;
          const processedTags = this.mapTagsWithCover(response.items || []);
          Array.prototype.push.apply(this.tags, processedTags);
          this.page++;
          this.viewport?.refresh();
          this.tagsCache.set(cacheKey, processedTags);

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading initial tags data:', error);
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

  onScroll(): void {
    if (!this.exhausted && this.tags.length < this.total && !this.isLoading && !this.isScrolling) {
      this.getAll();
    }
  }

  getAll(): void {
    if (this.isLoading || this.isScrolling) {
      return;
    }
    const cacheKey = `${this.page}-${this.size}-${this.sort}-${this.order}-${this.user.languageBooks?.join(',')}`;

    if (this.tagsCache.has(cacheKey)) {
      const cachedData = this.tagsCache.get(cacheKey);
      Array.prototype.push.apply(this.tags, cachedData);
      this.page++;
      this.viewport?.refresh();
      this.cdr.detectChanges();
      return;
    }

    this.isScrolling = true;
    this.tagService.getSummaryPage(this.user.languageBooks, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$), takeUntil(this.resetPages$))
      .subscribe({
        next: (response) => {
          this.exhausted = (response.items || []).length < this.size;
          this.total = response.total;
          this.title = this.translate.instant('locale.tags.title') + ' (' + this.total + ')';
          const processedTags = this.mapTagsWithCover(response.items || []);
          Array.prototype.push.apply(this.tags, processedTags);
          this.page++;
          this.viewport?.refresh();
          this.tagsCache.set(cacheKey, processedTags);
          this.isScrolling = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.log(error);
          this.messageService.clear();
          this.messageService.add({
            severity: 'error',
            detail: this.translate.instant('locale.tags.error.data'),
            closable: false,
            life: 5000
          });
          this.isScrolling = false;
          this.cdr.detectChanges();
        }
      });
  }

  private mapTagsWithCover(tags: Tag[]): Tag[] {
    return tags.map(tag => ({
      ...tag,
      image: undefined,
      originalImage: this.tagService.buildCoverUrl(tag.id)
    }));
  }

  trackByTagId(index: number, tag: Tag): string {
    return tag.id ? tag.id.toString() : index.toString();
  }

  onChange(event): void {
    this.exhausted = false;
    this.resetPages$.next();
    this.isLoading = false;
    this.isScrolling = false;
    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('tags_order', this.selectedSort);

    this.page = 0;
    this.tags.length = 0;
    this.tagsCache.clear();

    this.loadInitialData();
  }

  getBooksByTag(tag: Tag): void {
    this.reset();

    const search: Search = new Search();
    search.selectedTags = [];
    search.selectedTags.push(tag.name);
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  showMenu(): boolean {
    return this.title && this.user?.role === 'ADMIN';
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
          this.loadInitialData();
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
          this.loadInitialData();
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
          this.loadInitialData();
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
    this.exhausted = false;
    this.resetPages$.next();
    this.isLoading = false;
    this.isScrolling = false;
    this.tags.length = 0;
    this.total = 0;
    this.page = 0;
    this.tagsCache.clear();

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
