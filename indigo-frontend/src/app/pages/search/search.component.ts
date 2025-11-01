import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Search } from 'src/app/domain/search';
import { Tag } from 'src/app/domain/tag';
import { TagService } from 'src/app/services/tag.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { User } from 'src/app/domain/user';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html'
})
export class SearchComponent implements OnInit, OnDestroy {

  allTags: Tag[];
  items: SelectItem[];
  tags: SelectItem[] = [];
  search?: Search;

  user: User;

  private destroy$ = new Subject<void>();

  constructor(
    public translate: TranslateService,
    private tagService: TagService,
    private router: Router,
    private authState: AuthStateService
  ) {
    this.user = this.authState.getCurrentUser() || { languageBooks: ['en'], role: 'USER', username: '' } as User;
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
    this.tagService.getAll(this.user.languageBooks, "name", "asc")
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        data => {
          data.forEach((tag) => {
            this.tags.push({ label: tag.name, value: tag.name });
          });
        }
      );

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
