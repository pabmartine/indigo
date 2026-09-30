import { Subject } from 'rxjs';
import { BooksComponent } from './books.component';

describe('Book search requests', () => {
  let component: any;
  let requests: Subject<any>[];
  let page: jasmine.Spy;
  beforeEach(() => {
    sessionStorage.removeItem('books_order');
    requests = [];
    page = jasmine.createSpy('page').and.callFake(() => {
      const response = new Subject<any>(); requests.push(response); return response;
    });
    const service: any = { getAllSummaryPage: page, buildCoverImageUrl: (id: string) => '/cover/' + id };
    const auth: any = { getCurrentUser: () => ({ username: '', languageBooks: ['es'] }) };
    component = new BooksComponent(service, {} as any, {} as any, {} as any,
      { clear() {}, add() {} } as any, {} as any, {} as any,
      { detectChanges() {} } as any, auth, {} as any, {} as any);
    component.adv_search = {title: 'old'};
  });
  afterEach(() => { component.ngOnDestroy(); sessionStorage.removeItem('books_order'); });

  it('unsubscribes the old request and only displays the current search', () => {
    component.doSearch();
    component.adv_search = {author: 'new'}; component.doSearch();
    expect(requests[0].observers.length).toBe(0);
    requests[0].next({items: [{id: 'stale'}], total: 1});
    expect(component.books).toEqual([]);
    requests[1].next({items: [{id: 'fresh'}], total: 1});
    expect(component.books.map((book: any) => book.id)).toEqual(['fresh']);
    expect(component.total).toBe(1);
  });

  it('prevents overlapping pages and cancels a request when sorting changes', () => {
    component.getAll(); component.getAll();
    expect(page).toHaveBeenCalledTimes(1);
    component.selectedSort = 'title,asc'; component.onChange(null);
    expect(page).toHaveBeenCalledTimes(2);
    expect(requests[0].observers.length).toBe(0);
    requests[1].next({items: [{id: 'new-sort'}], total: 1});
    expect(component.books.map((book: any) => book.id)).toEqual(['new-sort']);
  });
});
