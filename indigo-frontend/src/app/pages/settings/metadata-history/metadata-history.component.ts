import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, of, timer } from 'rxjs';
import { catchError, finalize, switchMap, takeUntil } from 'rxjs/operators';
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
  error = '';
  refreshedAt: Date;
  detail: any;
  detailOpen = false;
  detailLoading = false;
  detailError = '';
  private detailId = '';
  private readonly reload$ = new Subject<void>();
  private readonly destroy$ = new Subject<void>();
  readonly statuses = {RUNNING: 'En curso', FOUND: 'Encontrado', NOT_FOUND: 'Sin coincidencia', SKIPPED: 'Omitido', ERROR: 'Error'};
  readonly types = {BOOKS: 'Libro', AUTHORS: 'Autor', REVIEWS: 'Reseñas'};
  readonly typeOptions = [{label: 'Todas', value: ''}, {label: 'Libros', value: 'BOOKS'}, {label: 'Autores', value: 'AUTHORS'}, {label: 'Reseñas', value: 'REVIEWS'}];
  readonly statusOptions = [{label: 'Todos', value: ''}, ...Object.entries(this.statuses).map(([value, label]) => ({value, label}))];
  readonly fields = {description: 'Descripción', image: 'Foto', provider: 'Proveedor', metadataSources: 'Fuentes por campo',
    rating: 'Valoración', ratingAverage: 'Valoración media', ratingsCount: 'Número de valoraciones', ratingDistribution: 'Distribución de valoraciones',
    ratingProvider: 'Fuente de valoración', ratingUpdatedAt: 'Fecha de valoración', openLibraryWorkId: 'Obra en Open Library',
    openLibraryEditionId: 'Edición en Open Library', metadataMatchStatus: 'Coincidencia', metadataMatchConfidence: 'Confianza de coincidencia',
    lastMetadataSync: 'Última sincronización', reviews: 'Reseñas', lastReviewsMetadataSync: 'Última consulta de reseñas',
    reviewsMetadataStatus: 'Estado de reseñas', reviewsMetadataError: 'Error de reseñas'};

  constructor(private readonly metadata: MetadataService, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.reload$.pipe(switchMap(() => {
      this.loading = true;
      this.error = '';
      return this.metadata.historyPage(this.page, this.size, this.type, this.status, this.search, this.entityId).pipe(
        catchError(() => { this.error = 'No se pudo cargar el historial. Puedes volver a intentarlo.'; return of(null); }),
        finalize(() => this.loading = false));
    }), takeUntil(this.destroy$)).subscribe(result => {
      if (!result) return;
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
      if (tick > 0 && this.autoRefresh && this.page === 0 && !document.hidden && !this.loading) this.refresh();
    });
  }

  refresh(): void { this.reload$.next(); }
  applyFilters(): void { this.page = 0; this.refresh(); }
  paginate(event: any): void { this.size = event.rows || 25; this.page = Math.floor((event.first || 0) / this.size); this.refresh(); }
  entityHistory(item: any): void {
    this.type = item.type; this.entityId = item.entityId; this.entityLabel = item.label;
    this.status = ''; this.search = ''; this.detailOpen = false; this.applyFilters();
  }
  clearEntity(): void { this.entityId = ''; this.entityLabel = ''; this.applyFilters(); }
  openDetail(item: any): void {
    this.detailId = item._id; this.detail = null; this.detailError = ''; this.detailOpen = true; this.detailLoading = true;
    const id = item._id;
    this.metadata.historyEntry(id).pipe(takeUntil(this.destroy$), finalize(() => {
      if (id === this.detailId) this.detailLoading = false;
    })).subscribe({next: entry => { if (id === this.detailId) this.detail = entry; },
      error: () => { if (id === this.detailId) this.detailError = 'No se pudo cargar el detalle. Vuelve a abrir la operación.'; }});
  }
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
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
