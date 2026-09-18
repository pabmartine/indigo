import { Author } from './author';

export interface AuthorSummaryPage {
  items: Author[];
  total: number;
  page: number;
  size: number;
}
