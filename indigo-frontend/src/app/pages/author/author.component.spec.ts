import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AuthorComponent } from './author.component';
import { AuthorService } from 'src/app/services/author.service';
import { BookService } from 'src/app/services/book.service';
import { MetadataService } from 'src/app/services/metadata.service';
import { AuthStateService } from 'src/app/services/auth-state.service';

@Pipe({ name: 'translate' })
class TranslateStub implements PipeTransform {
  transform(value: string): string { return value; }
}

describe('Author detail content', () => {
  let metadata: { findAuthor: jasmine.Spy };
  beforeEach(async () => {
    metadata = { findAuthor: jasmine.createSpy('findAuthor').and.returnValue(of({ name: 'A. A. Attanasio', description: 'Bio' })) };
    await TestBed.configureTestingModule({
      declarations: [AuthorComponent, TranslateStub],
      providers: [
        { provide: AuthorService, useValue: {} },
        { provide: BookService, useValue: {} },
        { provide: MetadataService, useValue: metadata },
        { provide: AuthStateService, useValue: {} },
        { provide: TranslateService, useValue: { instant: (key: string) => key } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  it('renders the stored description and photo in desktop and mobile layouts', () => {
    const fixture = TestBed.createComponent(AuthorComponent);
    fixture.componentInstance.user = { role: 'USER' } as any;
    fixture.componentInstance.selected = { id: '1', name: 'A. A. Attanasio', description: '<p>Descripción almacenada</p>', image: 'data:image/png;base64,aW1hZ2U=' };
    fixture.detectChanges();
    const descriptions = fixture.nativeElement.querySelectorAll('.book-detail-comment');
    expect(descriptions.length).toBe(2);
    descriptions.forEach((node: HTMLElement) => expect(node.textContent).toBe('Descripción almacenada'));
    expect(fixture.nativeElement.querySelectorAll('.book-detail-image').length).toBe(2);
  });

  it('refreshes by name even when sort is different or missing', () => {
    const fixture = TestBed.createComponent(AuthorComponent);
    const component = fixture.componentInstance;
    component.user = { role: 'ADMIN' } as any;
    for (const sort of [undefined, 'Attanasio, A. A.']) {
      component.selected = { id: '1', name: 'A. A. Attanasio', sort };
      component.refreshAuthor();
      expect(metadata.findAuthor.calls.mostRecent().args).toEqual(['es', 'A. A. Attanasio']);
    }
  });
});
