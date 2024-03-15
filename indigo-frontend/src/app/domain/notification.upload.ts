

export class NotificationUpload {

    constructor(
        public total?: number,
        public extract?: number,
        public extractError?: number,
        public moveError?: number,
        public deleteError?: number,
        public newBooks?: number,
        public updatedBooks?: number,
        public newAuthors?: number,
        public newTags?: number,
        public deleted?: number
    ) {
    }

 
}
