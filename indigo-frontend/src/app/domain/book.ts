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
        public isbn10?: string[],
        public isbn13?: string[],
        public identifiers?: Record<string, string[]>,
        public openLibraryWorkId?: string,
        public openLibraryEditionId?: string,
        public ratingAverage?: number,
        public ratingsCount?: number,
        public ratingDistribution?: Record<string, number>,
        public ratingProvider?: string,
        public ratingUpdatedAt?: string,
        public metadataMatchStatus?: string,
        public metadataMatchConfidence?: number,
        public similar?:string[],
        public recommendations?:string[],
        public languages?:string[],
        public image?: string,
        public coverUrl?: string,
        public reviews?: ReviewBook[],
        ) {
    }
}
