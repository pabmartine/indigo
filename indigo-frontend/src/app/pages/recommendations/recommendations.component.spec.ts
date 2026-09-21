import { Subject } from 'rxjs';
import { RecommendationsComponent } from './recommendations.component';

describe('RecommendationsComponent pagination', () => {
  let component: RecommendationsComponent;
  let requests: Subject<any>[];
  let service: any;

  beforeEach(() => {
    sessionStorage.removeItem('recommendations_order');
    requests = [];
    service = {
      getRecommendationSummaryPage: jasmine.createSpy().and.callFake(() => {
        const response = new Subject<any>();
        requests.push(response);
        return response;
      }),
      buildCoverImageUrl: (id: string) => '/cover/' + id
    };
    component = new RecommendationsComponent(
      service, {} as any, {} as any, { clear() {}, add() {} } as any,
      { instant: (key: string) => key } as any, { detectChanges() {} } as any,
      {} as any, { getCurrentUser: () => ({ username: 'reader', languageBooks: ['en'] }) } as any,
      {} as any
    );
    (component as any).size = 2;
  });

  afterEach(() => {
    component.ngOnDestroy();
    sessionStorage.removeItem('recommendations_order');
    sessionStorage.removeItem('books_order');
  });

  it('makes one initial request and prevents duplicate requests while loading', () => {
    component.ngOnInit();
    component.getAll();
    component.onScroll();
    expect(service.getRecommendationSummaryPage).toHaveBeenCalledTimes(1);
    requests[0].next({ items: [{ id: 'a' }, { id: 'b' }], total: 3 });
    component.onScroll();
    component.onScroll();
    expect(service.getRecommendationSummaryPage).toHaveBeenCalledTimes(2);
    requests[1].next({ items: [{ id: 'c' }], total: 3 });
    component.onScroll();
    expect(component.books.map(book => book.id)).toEqual(['a', 'b', 'c']);
    expect(service.getRecommendationSummaryPage).toHaveBeenCalledTimes(2);
  });

  it('cancels old pages on sort change and keeps its sort separate from the book list', () => {
    sessionStorage.setItem('books_order', 'rating,asc');
    component.ngOnInit();
    expect(service.getRecommendationSummaryPage).toHaveBeenCalledWith('reader', 0, 2, 'count', 'desc');
    component.selectedSort = 'title,asc';
    component.onChange(null);
    requests[0].next({ items: [{ id: 'stale' }], total: 1 });
    expect(component.books.length).toBe(0);
    requests[1].next({ items: [{ id: 'fresh' }], total: 1 });
    expect(component.books.map(book => book.id)).toEqual(['fresh']);
    expect(sessionStorage.getItem('books_order')).toBe('rating,asc');
  });
});
