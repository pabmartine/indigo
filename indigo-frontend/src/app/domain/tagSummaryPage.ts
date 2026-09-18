import { Tag } from './tag';

export interface TagSummaryPage {
  items: Tag[];
  total: number;
  page: number;
  size: number;
}
