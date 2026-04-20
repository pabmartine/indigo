import { Serie } from './serie';

export interface SeriePage {
  items: Serie[];
  total: number;
  page: number;
  size: number;
}
