import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cdate'
})
export class CdatePipe implements PipeTransform {

  transform(value: string): Date {
    let dateString = value.slice(0,10);
    let newDate = new Date(dateString);
    return newDate;
  }

}
