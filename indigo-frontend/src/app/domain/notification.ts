import { NotificationEnum } from '../enums/notification.enum.';
import { NotificationKindle } from '../domain/notification.kindle';
import { NotificationUpload } from '../domain/notification.upload';
import { StatusEnum } from '../enums/status.enum';

export class Notification {

    constructor(
        public id?:string,
        public type?:NotificationEnum,
        public user?: string,
        public date?:string,
        public readUser?:boolean,
        public readAdmin?:boolean,
        public status?:StatusEnum,
        
        public kindle?:NotificationKindle,
        public upload?:NotificationUpload,

        public message?:string
        ) {
    }
}
