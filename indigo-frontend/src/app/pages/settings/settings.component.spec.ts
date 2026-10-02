import { of } from 'rxjs';
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
});
