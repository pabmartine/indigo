import { Page } from './page';

export class Book {

    constructor(
        public id?: number,
        public title?: string,
        public authorSort?: string,
        public pubDate?: string,
        public lastModified?: string,
        public seriesName?: string,
        public seriesIndex?: number,
        public path?: string,
        public pages?: Page,
        public image?: string,
        public description?: string,
        public tags?:string[],
        public authors?:string[],
        public rating?:number,
        public similar?:string
        ) {
    }
}
