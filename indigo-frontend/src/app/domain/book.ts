import { SerieBook } from './serieBook';
import { ReviewBook } from './reviewBook';

export class Book {

    constructor(
        public id?: string,
        public title?: string,
        public path?: string,
        public comment?: string,
        public provider?: string,
        public serie?: SerieBook,
        public pubDate?: string,
        public lastModified?: string,
        public pages?: number,
        public rating?: number,
        public authors?:string[],
        public tags?:string[],
        public similar?:string[],
        public recommendations?:string[],
        public languages?:string[],
        public image?: string,
        public reviews?: ReviewBook[],
        ) {
    }
}
