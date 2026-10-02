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
});
