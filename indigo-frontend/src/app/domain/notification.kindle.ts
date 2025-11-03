import { StatusEnum } from '../enums/status.enum';

export class NotificationKindle {

    constructor(
        public book?: string,
        public error?:string,
        public title?:string,
        public image?:string,
        ) {
    }
}
