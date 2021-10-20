import { NotificationEnum } from '../enums/notification.enum.';
import { StatusEnum } from '../enums/status.enum';

export class Notif {

    constructor(
        public id?:string,
        public book?: string,
        public user?: string,
        public type?:NotificationEnum,
        public status?:StatusEnum,
        public error?:string,
        public sendDate?:string,
        public readUser?:boolean,
        public readAdmin?:boolean,
        public message?:string,
        public title?:string,
        public image?:string,
        ) {
    }
}
