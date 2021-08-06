import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { Search } from 'src/app/domain/search';

@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.css']
})
export class TopbarComponent implements OnInit {

  @Output() emitterTopBar = new EventEmitter<boolean>();

  @Input() show:boolean;

  search:string;


  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  doSearch() {
    
    let search:Search = new Search();
    search.path = this.search;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
    this.search = "";

    this.emitterTopBar.emit(false);
  }

}
