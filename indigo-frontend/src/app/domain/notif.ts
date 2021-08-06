import { NotificationEnum } from '../enums/notification.enum.';
import { StatusEnum } from '../enums/status.enum';

export class Notif {

    constructor(
        public id?:number,
        public book?: number,
        public user?: number,
        public type?:NotificationEnum,
        public status?:StatusEnum,
        public error?:string,
        public readByUser?:boolean,
        public readByAdmin?:boolean,
        public message?:string,
        public username?:string,
        public title?:string,
        public image?:string,
        ) {
    }
}
