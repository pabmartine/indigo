import { of, Subject, throwError } from 'rxjs';
import { fakeAsync, tick } from '@angular/core/testing';
import { SettingsComponent } from './settings.component';

describe('Settings metadata processes', () => {
  let component: SettingsComponent;
  let service: any;

  beforeEach(() => {
    service = {
      start: jasmine.createSpy().and.returnValue(of({})),
      stop: jasmine.createSpy().and.returnValue(of({}))
    };
    component = new SettingsComponent(
      { add() {} } as any, { instant: (key: string) => key } as any,
      {} as any, service, {} as any, {} as any, {} as any, {} as any,
      {} as any, {} as any, {} as any, { markForCheck() {} } as any
    );
  });

  afterEach(() => component.ngOnDestroy());

  it('starts authors without stopping books', () => {
    component.doExecuteMetadata('FULL', 'BOOKS');
    component.doExecuteMetadata('FULL', 'AUTHORS');
    expect(component.isBooksFull()).toBeTrue();
    expect(component.isAuthorsFull()).toBeTrue();
    expect(service.stop).not.toHaveBeenCalled();
  });

  it('stops only the selected entity', () => {
    component.doExecuteMetadata('FULL', 'BOOKS');
    component.doExecuteMetadata('FULL', 'AUTHORS');
    component.doExecuteMetadata('FULL', 'AUTHORS');
    expect(service.stop).toHaveBeenCalledWith('AUTHORS');
    expect(component.isBooksFull()).toBeTrue();
    expect(component.isAuthorsFull()).toBeFalse();
    expect(component.metadataRunning).toBeTrue();
  });

  it('switches mode only within the selected entity', () => {
    component.doExecuteMetadata('FULL', 'BOOKS');
    component.doExecuteMetadata('FULL', 'AUTHORS');
    component.doExecuteMetadata('PARTIAL', 'AUTHORS');
    expect(component.isBooksFull()).toBeTrue();
    expect(component.isAuthorsFull()).toBeFalse();
    expect(component.isAuthorsPartial()).toBeTrue();
    expect(service.stop).not.toHaveBeenCalled();
  });

  it('keeps per-entity status when the latest process has finished', () => {
    (component as any).handleMetadataStatus({
      type: 'FULL', entity: 'AUTHORS', status: true, current: 1, total: 1,
      runs: {
        'FULL:BOOKS': { status: true, current: 2, total: 10 },
        'FULL:AUTHORS': { status: false, current: 1, total: 1 }
      }
    });
    expect(component.isBooksFull()).toBeTrue();
    expect(component.isAuthorsFull()).toBeFalse();
    expect(component.getMetadataProgress('FULL', 'BOOKS')).toBe(20);
  });

  it('shows editions processing while no authors have been indexed yet', () => {
    component.libraryIndex = { status: 'PROCESSING_EDITIONS', processedRecords: 120000, matchedAuthors: 0 };
    expect(component.indexRunning).toBeTrue();
    expect(component.indexProcessing).toBeTrue();
    expect(component.indexDownloading).toBeFalse();
    expect(component.indexStatusText).toBe('Procesando ediciones');
  });

  it('refreshes index counters and marks the view for checking', () => {
    const state = { status: 'PROCESSING_EDITIONS', processedRecords: 130000, updatedAt: '2026-10-02T08:10:00Z' };
    service.libraryIndex = jasmine.createSpy().and.returnValue(of(state));
    const refresh = spyOn((component as any).cdr, 'markForCheck');
    component.indexAction('status');
    expect(component.libraryIndex).toEqual(state);
    expect(component.indexBusy).toBeFalse();
    expect(refresh).toHaveBeenCalled();
  });

  it('distinguishes download progress from completed processing', () => {
    component.libraryIndex = { status: 'DOWNLOADING_AUTHORS', downloadedBytes: 1048576 };
    expect(component.indexDownloading).toBeTrue();
    expect(component.indexProcessing).toBeFalse();
    expect(component.indexStatusText).toBe('Descargando autores');
    component.libraryIndex = { status: 'COMPLETED', matchedAuthors: 12 };
    expect(component.indexRunning).toBeFalse();
    expect(component.indexStatusText).toBe('Completado');
  });

  it('prevents duplicate start requests until the response arrives', () => {
    const response = new Subject<any>();
    service.start.and.returnValue(response);
    component.doExecuteMetadata('FULL', 'AUTHORS');
    component.doExecuteMetadata('PARTIAL', 'AUTHORS');
    expect(service.start).toHaveBeenCalledTimes(1);
    expect(component.metadataActionBusy.AUTHORS).toBeTrue();
    response.next({});
    response.complete();
    expect(component.metadataActionBusy.AUTHORS).toBeFalse();
  });

  it('refreshes activity with OnPush and releases the loading state', () => {
    service.activity = jasmine.createSpy().and.returnValue(of([{label: 'Autor'}]));
    service.history = jasmine.createSpy().and.returnValue(of([]));
    const refresh = spyOn((component as any).cdr, 'markForCheck');
    component.loadActivity();
    expect(component.metadataItems[0].label).toBe('Autor');
    expect(component.activityBusy).toBeFalse();
    expect(refresh).toHaveBeenCalled();
  });

  it('rejects invalid review intervals before sending a request', () => {
    component.reviewQueueLoaded = true;
    service.configureReviewQueue = jasmine.createSpy();
    for (const value of [null, 0, 14, 3601, 30.5]) {
      component.reviewAmazonSeconds = value;
      expect(component.validReviewIntervals).toBeFalse();
      component.reviewQueueAction('settings');
    }
    expect(service.configureReviewQueue).not.toHaveBeenCalled();
    component.reviewAmazonSeconds = 15;
    component.reviewGoodreadsSeconds = 3600;
    expect(component.validReviewIntervals).toBeTrue();
  });

  it('continues polling after a temporary status failure', fakeAsync(() => {
    spyOnProperty(document, 'hidden', 'get').and.returnValue(false);
    service.getDataStatus = jasmine.createSpy().and.returnValues(throwError(() => new Error('Offline')), of({status: false, runs: {}}));
    (component as any).startStatusPolling();
    tick(1000);
    expect(service.getDataStatus).toHaveBeenCalledTimes(2);
    component.ngOnDestroy();
  }));

  it('distinguishes preparation from a stopped run and shows fractional progress', () => {
    component.metadataRuns['PARTIAL:AUTHORS'] = {status: true, current: 0, total: 0};
    expect(component.getMetadataStatusLabel('PARTIAL', 'AUTHORS')).toBe('Preparando');
    expect(component.getMetadataCounter('PARTIAL', 'AUTHORS')).toContain('Preparando');
    component.metadataRuns['PARTIAL:AUTHORS'] = {status: false, current: 25, total: 40133};
    expect(component.getMetadataStatusLabel('PARTIAL', 'AUTHORS')).toBe('Detenido');
    expect(component.getMetadataProgress('PARTIAL', 'AUTHORS')).toBe(0.1);
  });

  it('shows the provider wait without treating it as completion and clears it on resume', () => {
    component.metadataRuns['PARTIAL:AUTHORS'] = {status: true, total: 40000, current: 25, waitingUntil: 123456, waitingFor: 'Wikipedia · Margaret Rogerson'};
    expect(component.getMetadataStatusLabel('PARTIAL', 'AUTHORS')).toBe('Esperando Wikipedia');
    expect(component.getMetadataWait('PARTIAL', 'AUTHORS').waitingUntil).toBe(123456);
    expect(component.getMetadataWait('FULL', 'BOOKS')).toBeNull();
    component.metadataRuns['PARTIAL:AUTHORS'].waitingUntil = null;
    expect(component.getMetadataWait('PARTIAL', 'AUTHORS')).toBeNull();
    expect(component.getMetadataStatusLabel('PARTIAL', 'AUTHORS')).toBe('En curso');
  });
});
