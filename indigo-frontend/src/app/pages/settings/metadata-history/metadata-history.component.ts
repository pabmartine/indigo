import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, of, timer } from 'rxjs';
import { catchError, exhaustMap, filter, finalize, map, startWith, switchMap, takeUntil } from 'rxjs/operators';
import { MetadataService } from 'src/app/services/metadata.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-metadata-history',
  templateUrl: './metadata-history.component.html',
  styleUrls: ['./metadata-history.component.scss']
})
export class MetadataHistoryComponent implements OnInit, OnDestroy {
  items: any[] = [];
  total = 0;
  page = 0;
  size = 25;
  type = '';
  status = '';
  search = '';
  entityId = '';
  entityLabel = '';
  autoRefresh = true;
  loading = false;
  refreshing = false;
  tableHovered = false;
  tableFocused = false;
  error = '';
  refreshedAt: Date;
  detail: any;
  detailOpen = false;
  detailLoading = false;
  detailError = '';
  private detailId = '';
  private readonly reload$ = new Subject<boolean>();
  private readonly closeDetail$ = new Subject<void>();
  private readonly destroy$ = new Subject<void>();
  readonly statuses = {RUNNING: 'En curso', WAITING: 'En espera', FOUND: 'Encontrado', NOT_FOUND: 'Sin coincidencia', SKIPPED: 'Omitido', DEFERRED: 'Aplazado', ERROR: 'Error'};
  readonly types = {BOOKS: 'Libro', AUTHORS: 'Autor', REVIEWS: 'Opiniones'};
  readonly typeOptions = [{label: 'Todas', value: ''}, {label: 'Libros', value: 'BOOKS'}, {label: 'Autores', value: 'AUTHORS'}, {label: 'Opiniones', value: 'REVIEWS'}];
  readonly statusOptions = [{label: 'Todos', value: ''}, ...Object.entries(this.statuses).filter(([value]) => value !== 'WAITING').map(([value, label]) => ({value, label}))];
  readonly fields = {description: 'Descripción', image: 'Foto', provider: 'Proveedor', metadataSources: 'Fuentes por campo',
    rating: 'Valoración', ratingAverage: 'Valoración media', ratingsCount: 'Número de valoraciones', ratingDistribution: 'Distribución de valoraciones',
    ratingProvider: 'Fuente de valoración', ratingUpdatedAt: 'Fecha de valoración', openLibraryWorkId: 'Obra en Open Library',
    openLibraryEditionId: 'Edición en Open Library', metadataMatchStatus: 'Coincidencia', metadataMatchConfidence: 'Confianza de coincidencia',
    lastMetadataSync: 'Última sincronización', reviews: 'Reseñas', lastReviewsMetadataSync: 'Última consulta de reseñas',
    reviewsMetadataStatus: 'Estado de reseñas', reviewsMetadataError: 'Error de reseñas'};

  constructor(private readonly metadata: MetadataService, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.reload$.pipe(switchMap(background => {
      this.loading = !background;
      this.refreshing = true;
      this.error = '';
      return this.metadata.historyPage(this.page, this.size, this.type, this.status, this.search, this.entityId).pipe(
        catchError(() => { this.error = 'No se pudo cargar el historial. Puedes volver a intentarlo.'; return of(null); }),
        map(result => ({result, background})),
        finalize(() => { this.loading = false; this.refreshing = false; }));
    }), takeUntil(this.destroy$)).subscribe(({result, background}) => {
      if (!result || (background && this.interacting)) return;
      this.items = result.items;
      this.total = result.total;
      this.refreshedAt = new Date();
    });
    this.route.queryParamMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const type = params.get('type'); const status = params.get('status');
      this.type = type && this.types[type] ? type : '';
      this.status = status && this.statuses[status] ? status : '';
      this.applyFilters();
    });
    timer(0, 5000).pipe(takeUntil(this.destroy$)).subscribe(tick => {
      if (tick > 0 && this.autoRefresh && this.page === 0 && !document.hidden && !this.refreshing && !this.interacting) this.refresh(true);
    });
  }

  get interacting(): boolean { return this.detailOpen || this.tableHovered || this.tableFocused; }
  readonly trackOperation = (_: number, item: any): string => item._id;
  tableFocusOut(event: FocusEvent): void {
    this.tableFocused = (event.currentTarget as HTMLElement).contains(event.relatedTarget as Node);
  }
  refresh(background = false): void { this.reload$.next(background); }
  applyFilters(): void { this.page = 0; this.refresh(); }
  paginate(event: any): void { this.size = event.rows || 25; this.page = Math.floor((event.first || 0) / this.size); this.refresh(); }
  entityHistory(item: any): void {
    this.type = item.type; this.entityId = item.entityId; this.entityLabel = item.label;
    this.status = ''; this.search = ''; this.closeDetail(); this.applyFilters();
  }
  clearEntity(): void { this.entityId = ''; this.entityLabel = ''; this.applyFilters(); }
  openDetail(item: any): void {
    this.closeDetail$.next();
    this.detailId = item._id; this.detail = null; this.detailError = ''; this.detailOpen = true;
    timer(5000, 5000).pipe(
      startWith(0),
      filter((_, index) => index === 0 || (!document.hidden && (!this.detail || ['RUNNING', 'WAITING'].includes(this.detail.status)))),
      exhaustMap(() => {
        this.detailLoading = !this.detail;
        return this.metadata.historyEntry(this.detailId).pipe(
          catchError(() => { this.detailError = 'No se pudo actualizar el detalle. Puedes volver a intentarlo.'; return of(null); }),
          finalize(() => this.detailLoading = false));
      }),
      takeUntil(this.closeDetail$), takeUntil(this.destroy$)
    ).subscribe(entry => {
      if (entry) { this.detail = entry; this.detailError = ''; }
    });
  }
  retryDetail(): void { this.openDetail({_id: this.detailId}); }
  closeDetail(): void { this.closeDetail$.next(); this.detailOpen = false; }
  statusLabel(value: string): string { return this.statuses[value] || value; }
  typeLabel(value: string): string { return this.types[value] || value; }
  fieldLabel(value: string): string { return this.fields[value] || value; }
  fieldStates(value: any): {field: string; present: boolean}[] {
    return Object.entries(value || {}).map(([field, present]) => ({field, present: !!present}));
  }
  textValue(value: any): string {
    if (value === null || value === undefined || value === '') return 'Sin datos';
    return typeof value === 'object' ? JSON.stringify(value, null, 2) : String(value);
  }
  imageValue(value: any): string | null {
    if (typeof value !== 'string' || !value) return null;
    if (/^data:image\/(jpeg|png|webp);base64,/.test(value)) return value;
    return /^[A-Za-z0-9+/=\s]+$/.test(value) ? 'data:image/jpeg;base64,' + value : null;
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); this.closeDetail$.complete(); }
}
