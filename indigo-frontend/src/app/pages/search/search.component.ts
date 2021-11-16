import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { TagService } from 'src/app/services/tag.service';
import { Tag } from 'src/app/domain/tag';
import { SelectItem } from 'primeng/api';
import { Search } from 'src/app/domain/search';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  allTags: Tag[];
  items: SelectItem[];
  tags: SelectItem[] = [];
  search?: Search;

  user = JSON.parse(sessionStorage.user);

  constructor(public translate: TranslateService, private tagService: TagService, private router: Router) {
    this.search = new Search();
    this.search.selectedTags = [];
    this.getAllTags();
  }

  ngOnInit(): void {

  }

  doSearch() {
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(this.search) } });
  }

  getAllTags() {
    this.tags.length = 0;
    this.tagService.getAll(this.user.languageBooks, "name", "asc").subscribe(
      data => {
        data.forEach((tag) => {
          this.tags.push({ label: tag.name, value: tag.name });
        });
      }
    );

  }

}
