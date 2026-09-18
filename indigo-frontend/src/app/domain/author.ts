export class Author {

    constructor(
        public id:string,
        public name:string,
        public sort?: string,
        public description?: any,
        public provider?: string,
        public image?:string,
        public numBooks?: number,
        public coverUrl?: string,
        public originalImage?: string
        ) {
    }
}
