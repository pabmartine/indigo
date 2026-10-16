import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthorService } from './author.service';

describe('Author detail requests', () => {
  let service: AuthorService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(AuthorService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('keeps special characters in the author name as a single query parameter', () => {
    const name = 'A + B & C #1';
    service.getByName(name).subscribe();
    const request = http.expectOne(req => req.url.endsWith('/author/sort'));
    expect(new URL(request.request.urlWithParams, 'http://localhost').searchParams.get('sort')).toBe(name);
    request.flush({ name, description: 'Bio' });
  });
});
