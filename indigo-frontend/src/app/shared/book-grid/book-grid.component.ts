import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { Book } from 'src/app/domain/book';

@Component({
  selector: 'app-book-grid',
  templateUrl: './book-grid.component.html',
  styleUrls: ['./book-grid.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookGridComponent {
  @Input() books: Book[] = [];
  @Input() showRating: boolean = true;
  @Output() bookClick = new EventEmitter<Book>();
  @Output() authorClick = new EventEmitter<string>();

  onBookClick(book: Book): void {
    this.bookClick.emit(book);
  }

  onAuthorClick(author: string, event: Event): void {
    event.stopPropagation();
    this.authorClick.emit(author);
  }

  getFirstAuthor(book: Book): string {
    return book.authors && book.authors.length > 0 ? book.authors[0] : 'Unknown Author';
  }

  trackByBookId(index: number, book: Book): string {
    return book.id || index.toString();
  }
}
