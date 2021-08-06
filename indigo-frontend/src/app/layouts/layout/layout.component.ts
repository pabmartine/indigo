import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css']

})
export class LayoutComponent implements OnInit {

  public sideBar: boolean;
  public topBar: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  showSideBar(show:boolean){
    this.sideBar = show;
  }

  showTopBar(show:boolean){
    this.topBar = show;
  }

}
