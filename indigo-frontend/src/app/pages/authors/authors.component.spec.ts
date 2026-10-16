import { Subject } from 'rxjs';
import { AuthorsComponent } from './authors.component';
import { ImageService } from 'src/app/utils/image.service';

describe('Author details from summary cards', () => {
  let component: AuthorsComponent;
  let requests: Subject<any>[];
  let getByName: jasmine.Spy;
  let showDetails: jasmine.Spy;

  beforeEach(() => {
    requests = [];
    getByName = jasmine.createSpy('getByName').and.callFake(() => {
      const response = new Subject<any>();
      requests.push(response);
      return response;
    });
    showDetails = jasmine.createSpy('showDetails');
    component = new AuthorsComponent(
      { getByName, buildCoverImageUrl: (id: string) => '/cover/' + id } as any,
      {} as any, {} as any, {} as any, { detectChanges() {} } as any, {} as any,
      { getCurrentUser: () => ({ languageBooks: ['es'] }) } as any, new ImageService());
    component.authorComponent = { showDetails } as any;
  });

  afterEach(() => component.ngOnDestroy());

  it('loads the stored description for A. A. Attanasio even without sort', () => {
    const summary = { id: 'attanasio', name: 'A. A. Attanasio' };
    component.showDetails(summary);
    expect(getByName).toHaveBeenCalledWith(summary.name);
    expect(showDetails).not.toHaveBeenCalled();
    requests[0].next({ ...summary, description: 'Descripción almacenada', image: 'aW1hZ2U=' });
    expect(showDetails).toHaveBeenCalledWith(jasmine.objectContaining({
      description: 'Descripción almacenada', image: 'data:image/jpeg;base64,aW1hZ2U='
    }));
    expect((summary as any).description).toBeUndefined();
  });

  it('falls back to name for blank sort and preserves image URLs', () => {
    const author = { id: '1', name: 'Author', sort: '   ' };
    component.showDetails(author);
    expect(getByName).toHaveBeenCalledWith('Author');
    requests[0].next({ ...author, description: 'Bio', image: 'https://example.org/photo.jpg' });
    expect(showDetails.calls.mostRecent().args[0].image).toBe('https://example.org/photo.jpg');
  });

  it('still loads full details by sort when present', () => {
    component.showDetails({ id: '1', name: 'Author Name', sort: 'Name, Author' });
    expect(getByName).toHaveBeenCalledWith('Name, Author');
    requests[0].next({ id: '1', name: 'Author Name', description: 'Bio' });
    expect(showDetails.calls.mostRecent().args[0].image).toBe('/cover/1');
  });

  it('does not let a delayed response replace the latest selected author', () => {
    component.showDetails({ id: '1', name: 'First' });
    component.showDetails({ id: '2', name: 'Second' });
    expect(requests[0].observers.length).toBe(0);
    requests[0].next({ name: 'First', description: 'Old' });
    requests[1].next({ name: 'Second', description: 'Current' });
    expect(showDetails).toHaveBeenCalledTimes(1);
    expect(showDetails.calls.mostRecent().args[0].name).toBe('Second');
  });

  it('cancels detail requests when leaving the component', () => {
    component.showDetails({ id: '1', name: 'First' });
    component.ngOnDestroy();
    expect(requests[0].observers.length).toBe(0);
  });
});
