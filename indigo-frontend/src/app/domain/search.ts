import { Tag } from './tag';

export class Search {

    constructor(
        public title?: string,
        public author?: string,
        public ini?: Date,
        public end?: Date,
        public min?: number,
        public max?: number,
        public serie?: string,
        public selectedTags?: Tag[],
        public path?: string
        ) {
    }


}
