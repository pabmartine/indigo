import { Book } from './book';

export interface BookSummaryPage {
  items: Book[];
  total: number;
  page: number;
  size: number;
}
