import { Subject } from 'rxjs';
import { AuthorsComponent } from '../pages/authors/authors.component';
import { CategoriesComponent } from '../pages/categories/categories.component';
import { SeriesComponent } from '../pages/series/series.component';

for (const kind of ['authors', 'categories', 'series']) {
  describe(kind + ' pagination', () => {
    let component: any;
    let requests: Subject<any>[];
    let page: jasmine.Spy;
    const items = kind === 'categories' ? 'tags' : kind;
    beforeEach(() => {
      sessionStorage.removeItem(kind + '_order');
      requests = [];
      page = jasmine.createSpy().and.callFake(() => {
        const response = new Subject<any>();
        requests.push(response);
        return response;
      });
      const service: any = { getPage: page, getSummaryPage: page,
        buildCoverUrl: (id: string) => '/cover/' + id, buildCoverImageUrl: (id: string) => '/cover/' + id };
      const router: any = {};
      const messages: any = { clear() {}, add() {} };
      const translate: any = { instant: (key: string) => key };
      const cdr: any = { detectChanges() {} };
      const zone: any = {};
      const auth: any = { getCurrentUser: () => ({ username: '', languageBooks: ['es'] }) };
      component = kind === 'authors'
        ? new AuthorsComponent(service, router, messages, translate, cdr, zone, auth, {} as any)
        : kind === 'categories'
          ? new CategoriesComponent(service, router, messages, translate, cdr, zone, auth)
          : new SeriesComponent(service, router, messages, translate, cdr, zone, auth);
      component.reset();
      component.size = 2;
    });
    afterEach(() => {
      component.ngOnDestroy();
      sessionStorage.removeItem(kind + '_order');
    });

    it('serializes page requests and stops at the last short page even if the count is stale', () => {
      component.getAll();
      component.getAll();
      component.onScroll();
      expect(page).toHaveBeenCalledTimes(1);
      requests[0].next({ items: [{ id: 'a', name: 'A' }, { id: 'b', name: 'B' }], total: 10 });
      component.onScroll();
      component.onScroll();
      expect(page).toHaveBeenCalledTimes(2);
      expect(page.calls.mostRecent().args.slice(1, 3)).toEqual([1, 2]);
      requests[1].next({ items: [{ id: 'c', name: 'C' }], total: 10 });
      component.onScroll();
      expect(page).toHaveBeenCalledTimes(2);
      expect(component[items].map((item: any) => item.id)).toEqual(['a', 'b', 'c']);
    });

    it('cancels an old response when sorting during a request', () => {
      component.getAll();
      component.selectedSort = 'name,desc';
      component.onChange(null);
      expect(page).toHaveBeenCalledTimes(2);
      requests[0].next({ items: [{ id: 'stale' }], total: 1 });
      expect(component[items]).toEqual([]);
      requests[1].next({ items: [{ id: 'fresh', name: 'Fresh' }], total: 1 });
      expect(component[items].map((item: any) => item.id)).toEqual(['fresh']);
    });
  });
}
