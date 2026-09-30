import { fakeAsync, tick } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { HeaderComponent } from './header.component';

describe('Header search scheduling', () => {
  let component: HeaderComponent;
  let navigate: jasmine.Spy;
  beforeEach(() => {
    navigate = jasmine.createSpy('navigate');
    const translate: any = { instant: (value: string) => value, onTranslationChange: new Subject() };
    component = new HeaderComponent({ navigate } as any, translate, {} as any, {} as any);
    spyOn<any>(component, 'loadMessagesDeferred').and.stub();
    component.ngOnInit();
  });
  afterEach(() => component.ngOnDestroy());

  it('cancels a queued search when the field is cleared', fakeAsync(() => {
    component.search = 'old author'; component.onSearchInput();
    tick(100);
    component.clearSearch(); tick(400);
    expect(navigate).toHaveBeenCalledTimes(1);
    expect(navigate).toHaveBeenCalledWith(['books']);
  }));

  it('runs Enter immediately without repeating the delayed search', fakeAsync(() => {
    component.search = 'new title'; component.onSearchInput();
    tick(100); component.doSearch();
    expect(navigate).toHaveBeenCalledTimes(1);
    tick(400); expect(navigate).toHaveBeenCalledTimes(1);
    expect(JSON.parse(navigate.calls.mostRecent().args[1].queryParams.adv_search).path).toBe('new title');
  }));

  it('only searches the latest input after the typing pause', fakeAsync(() => {
    component.search = 'gar'; component.onSearchInput(); tick(100);
    component.search = 'garcia'; component.onSearchInput(); tick(299);
    expect(navigate).not.toHaveBeenCalled(); tick(1);
    expect(navigate).toHaveBeenCalledTimes(1);
    expect(JSON.parse(navigate.calls.mostRecent().args[1].queryParams.adv_search).path).toBe('garcia');
  }));
});
