export class ReviewBook {
    constructor(public name?:string, public title?:string, public comment?:string, public rating?:number, public date?:string,
                public lastMetadataSync?:string, public provider?:string, public sourceUrl?:string,
                public originalLanguage?:string, public language?:string){
    }
}
