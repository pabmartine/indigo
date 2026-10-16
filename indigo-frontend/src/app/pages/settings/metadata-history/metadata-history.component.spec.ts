import { fakeAsync, tick } from '@angular/core/testing';
import { convertToParamMap } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import { MetadataHistoryComponent } from './metadata-history.component';

describe('Metadata history', () => {
  let component: MetadataHistoryComponent;
  let service: any;
  let params: BehaviorSubject<any>;

  beforeEach(() => {
    service = {historyPage: jasmine.createSpy().and.returnValue(of({items: [], total: 0})),
      historyEntry: jasmine.createSpy()};
    spyOnProperty(document, 'hidden', 'get').and.returnValue(false);
    params = new BehaviorSubject(convertToParamMap({type: 'AUTHORS', status: 'ERROR'}));
    component = new MetadataHistoryComponent(service, {queryParamMap: params} as any);
  });
  afterEach(() => component.ngOnDestroy());

  it('opens error links with their filters and resets pagination when searching', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    expect(service.historyPage).toHaveBeenCalledWith(0, 25, 'AUTHORS', 'ERROR', '', '');
    component.paginate({first: 50, rows: 25});
    expect(service.historyPage).toHaveBeenCalledWith(2, 25, 'AUTHORS', 'ERROR', '', '');
    component.search = 'Tolkien'; component.applyFilters();
    expect(service.historyPage).toHaveBeenCalledWith(0, 25, 'AUTHORS', 'ERROR', 'Tolkien', '');
    component.ngOnDestroy();
  }));

  it('ignores stale page responses after changing filters', fakeAsync(() => {
    const oldPage = new Subject<any>(); const newPage = new Subject<any>();
    service.historyPage.and.returnValues(oldPage, newPage);
    component.ngOnInit(); tick(0);
    component.status = 'FOUND'; component.applyFilters();
    oldPage.next({items: [{label: 'Old'}], total: 1});
    expect(component.items).toEqual([]);
    newPage.next({items: [{label: 'New'}], total: 1});
    expect(component.items[0].label).toBe('New');
    component.ngOnDestroy();
  }));

  it('can recover from a loading error without losing future refreshes', fakeAsync(() => {
    service.historyPage.and.returnValue(throwError(() => new Error('Failed')));
    component.ngOnInit(); tick(0);
    expect(component.error).toContain('No se pudo cargar');
    expect(component.loading).toBeFalse();
    service.historyPage.and.returnValue(of({items: [{label: 'Recovered'}], total: 1}));
    component.refresh();
    expect(component.error).toBe('');
    expect(component.items[0].label).toBe('Recovered');
    component.ngOnDestroy();
  }));

  it('filters all attempts for one entity and can return to the full history', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    component.entityHistory({_id: 'operation', entityId: 'author-1', type: 'AUTHORS', label: 'Author'});
    expect(service.historyPage).toHaveBeenCalledWith(0, 25, 'AUTHORS', '', '', 'author-1');
    component.clearEntity();
    expect(service.historyPage).toHaveBeenCalledWith(0, 25, 'AUTHORS', '', '', '');
    component.ngOnDestroy();
  }));

  it('does not replace a newer detail with an older response', () => {
    const first = new Subject<any>(); const second = new Subject<any>();
    service.historyEntry.and.returnValues(first, second);
    component.openDetail({_id: 'first'}); component.openDetail({_id: 'second'});
    second.next({_id: 'second'}); first.next({_id: 'first'});
    expect(component.detail._id).toBe('second');
  });

  it('stops refreshing when the screen is destroyed', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    component.ngOnDestroy(); service.historyPage.calls.reset(); tick(10000);
    expect(service.historyPage).not.toHaveBeenCalled();
  }));
  it('opens deferred books from settings and exposes the filter', fakeAsync(() => {
    params.next(convertToParamMap({type: 'BOOKS', status: 'DEFERRED'}));
    component.ngOnInit(); tick(0);
    expect(service.historyPage).toHaveBeenCalledWith(0, 25, 'BOOKS', 'DEFERRED', '', '');
    expect(component.statusOptions).toContain(jasmine.objectContaining({value: 'DEFERRED', label: 'Aplazado'}));
    component.ngOnDestroy();
  }));

  it('refreshes automatically without blocking the table buttons', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    const response = new Subject<any>();
    service.historyPage.and.returnValue(response);
    tick(5000);
    expect(component.refreshing).toBeTrue();
    expect(component.loading).toBeFalse();
    response.next({items: [{_id: 'new'}], total: 1}); response.complete();
    expect(component.refreshing).toBeFalse();
    component.ngOnDestroy();
  }));

  it('keeps the clicked row stable if a background response arrives during interaction', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    component.items = [{_id: 'selected'}];
    const response = new Subject<any>();
    service.historyPage.and.returnValue(response);
    tick(5000);
    component.tableHovered = true;
    response.next({items: [{_id: 'newer'}], total: 1}); response.complete();
    expect(component.items[0]._id).toBe('selected');
    service.historyPage.calls.reset(); tick(5000);
    expect(service.historyPage).not.toHaveBeenCalled();
    component.tableHovered = false; tick(5000);
    expect(service.historyPage).toHaveBeenCalledTimes(1);
    component.ngOnDestroy();
  }));

  it('pauses list refresh while the detail follows a running operation to completion', fakeAsync(() => {
    component.ngOnInit(); tick(0);
    service.historyPage.calls.reset();
    service.historyEntry.and.returnValues(of({_id: 'job', status: 'RUNNING'}), of({_id: 'job', status: 'FOUND'}));
    component.openDetail({_id: 'job'});
    expect(component.detailOpen).toBeTrue();
    tick(5000);
    expect(component.detail.status).toBe('FOUND');
    expect(service.historyPage).not.toHaveBeenCalled();
    tick(5000);
    expect(service.historyEntry).toHaveBeenCalledTimes(2);
    component.closeDetail(); tick(5000);
    expect(service.historyPage).toHaveBeenCalledTimes(1);
    component.ngOnDestroy();
  }));

  it('cancels requests when reopening the same operation and when closing the dialog', fakeAsync(() => {
    const first = new Subject<any>(); const second = new Subject<any>();
    service.historyEntry.and.returnValues(first, second);
    component.openDetail({_id: 'same'});
    component.openDetail({_id: 'same'});
    expect(first.observers.length).toBe(0);
    first.next({_id: 'same', summary: 'stale'});
    expect(component.detailLoading).toBeTrue();
    second.next({_id: 'same', summary: 'current'});
    expect(component.detail.summary).toBe('current');
    component.closeDetail();
    expect(second.observers.length).toBe(0);
    tick(10000);
    expect(service.historyEntry).toHaveBeenCalledTimes(2);
  }));

  it('does not cancel slow detail requests and recovers after a polling error', fakeAsync(() => {
    const response = new Subject<any>();
    service.historyEntry.and.returnValues(response, throwError(() => new Error('Offline')), of({_id: 'job', status: 'FOUND'}));
    component.openDetail({_id: 'job'}); tick(10000);
    expect(service.historyEntry).toHaveBeenCalledTimes(1);
    expect(response.observers.length).toBe(1);
    response.next({_id: 'job', status: 'RUNNING'}); response.complete();
    tick(5000);
    expect(component.detail.status).toBe('RUNNING');
    expect(component.detailError).not.toBe('');
    tick(5000);
    expect(component.detail.status).toBe('FOUND');
    expect(component.detailError).toBe('');
    component.ngOnDestroy();
  }));

});
