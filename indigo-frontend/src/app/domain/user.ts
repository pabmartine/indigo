export class User {

    constructor(
        public id?:string ,
        public username?:string ,
        public password?:string, 
        public role?:string, 
        public language?:string,
        public languageBooks?:string[],
        public kindle?:string,
        public token?:string,
        ) {
    }

}
