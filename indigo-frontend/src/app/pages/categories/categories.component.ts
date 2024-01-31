import { Component, OnInit } from '@angular/core';
import { TagService } from 'src/app/services/tag.service';
import { Router } from '@angular/router';
import { Tag } from 'src/app/domain/tag';
import { SelectItem } from 'primeng/api/selectitem';
import { MenuItem } from 'primeng/api/menuitem';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { Search } from 'src/app/domain/search';


@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css'],
  providers: [MessageService]

})
export class CategoriesComponent implements OnInit {

  items: MenuItem[];

  tags: Tag[] = [];
  sortedTags: Tag[];
  sourceTag: Tag = new Tag();
  targetTag: Tag;
  newTag: string;
  background_image: string;


  title: string;


  total: number;
  private sort: string;
  private order: string;

  sorts: SelectItem[] = [];
  selectedSort: string;

  rename: boolean;
  merge: boolean;
  image: boolean;

  renameValid: boolean = false;
  mergeValid: boolean = false;
  imageValid: boolean = false;

  user = JSON.parse(sessionStorage.user);


  constructor(private tagService: TagService,
    private router: Router,
    private messageService: MessageService,
    public translate: TranslateService) {

    this.sorts.push(
      { label: this.translate.instant('locale.tags.order_by.total.desc'), value: 'numBooks,desc' },
      { label: this.translate.instant('locale.tags.order_by.total.asc'), value: 'numBooks,asc' },
      { label: this.translate.instant('locale.tags.order_by.name.asc'), value: 'name,asc' },
      { label: this.translate.instant('locale.tags.order_by.name.desc'), value: 'name,desc' }
    );

  }

  ngOnInit(): void {

    this.items = [
      {
        label: this.translate.instant('locale.tags.actions.rename.title'), icon: 'menu-icon fa fa-font', command: () => this.showRename()
      },
      {
        label: this.translate.instant('locale.tags.actions.merge.title'), icon: 'menu-icon fa fa-compress', command: () => this.showMerge()
      },
      {
        label: this.translate.instant('locale.tags.actions.image.modify.title'), icon: 'menu-icon fa fa-image', command: () => this.showImage()
      },
      {
        label: this.translate.instant('locale.tags.actions.image.update.title'), icon: 'menu-icon fa fa-image', command: () => this.updateImage()
      }
    ];

    this.reset();
    this.getAll();
  }

  onChange(event) {

    const index = this.selectedSort.indexOf(",");
    this.sort = this.selectedSort.slice(0, index);
    this.order = this.selectedSort.slice(index + 1);

    sessionStorage.setItem('tags_order', this.selectedSort);


    this.tags.length = 0;

    this.getAll();
  }

  getAll() {
    this.tagService.getAll(this.user.languageBooks, this.sort, this.order).subscribe(
      data => {
        this.tags = data;
        this.title = this.translate.instant('locale.tags.title') + " (" + this.tags.length + ")";      

      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.tags.error.data'), closable: false, life: 5000 });
      }
    );
  }

  getBooksByTag(tag: Tag) {
    this.reset();
    
    let search:Search = new Search();
    search.selectedTags = [];
    search.selectedTags.push(tag.name);
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  showMenu() {
    return this.title && JSON.parse(sessionStorage.user).role == 'ADMIN';
  }

  showRename() {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.rename = true;
  }

  showMerge() {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.targetTag = this.sortedTags[0];
    this.merge = true;
  }

  showImage() {
    this.messageService.clear();
    this.sortedTags = Object.assign([], this.tags);
    this.sortedTags.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.image = true;
  }

  updateImage() {
    this.messageService.clear();
    
    this.tagService.updateImage(this.sourceTag.id).subscribe(
      data => {
        this.sourceTag.image = data.image;
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ key: "rename", severity: 'error', detail: this.translate.instant('locale.tags.error.image'), closable: false, life: 5000 });
      }
    );
    
  }

  validateRename(event) {
    var found = this.sortedTags.filter(tag => {
      return tag.name === this.newTag;
    })[0];

    this.messageService.clear();

    if (found) {
      this.renameValid = false;
      this.messageService.add({ key: "rename", severity: 'error', detail: this.translate.instant('locale.tags.actions.rename.error'), closable: false, life: 5000 });
    } else {
      this.renameValid = true;
      this.messageService.add({ key: "rename", severity: 'info', detail: this.translate.instant('locale.tags.actions.rename.info', { source: this.sourceTag.name, target: this.newTag }), closable: false, life: 5000 });
    }
  }

  validateMerge(event) {

    this.messageService.clear();

    if (this.sourceTag == this.targetTag) {
      this.mergeValid = false;
      this.messageService.add({ key: "merge", severity: 'error', detail: this.translate.instant('locale.tags.actions.merge.error'), closable: false, life: 5000 });
    } else {
      this.mergeValid = true;
      this.messageService.add({ key: "merge", severity: 'info', detail: this.translate.instant('locale.tags.actions.merge.info', { source: this.sourceTag.name, target: this.targetTag.name }), closable: false, life: 5000 });
    }
  }

  validateImage(event) {

    this.messageService.clear();

    if (!this.background_image.startsWith("http") || !this.background_image.includes(".")) {
      this.imageValid = false;
      this.messageService.add({ key: "image", severity: 'error', detail: this.translate.instant('locale.tags.actions.image.error'), closable: false, life: 5000 });
    } else {
      this.imageValid = true;
      this.messageService.add({ key: "image", severity: 'info', detail: this.translate.instant('locale.tags.actions.image.info'), closable: false, life: 5000 });
    }


  }

  doRename() {

    this.tagService.rename(this.sourceTag.id, this.newTag).subscribe(
      data => {
        this.reset();
        this.getAll();
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ key: "rename", severity: 'error', detail: this.translate.instant('locale.tags.error.rename'), closable: false, life: 5000 });
      }
    );

    this.tags.length = 0;
    this.newTag = null;
  }

  doMerge() {

    this.tagService.merge(this.sourceTag.id, this.targetTag.id).subscribe(
      data => {
        this.reset();
        this.getAll();
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ key: "merge", severity: 'error', detail: this.translate.instant('locale.tags.error.merge'), closable: false, life: 5000 });
      }
    );

    this.tags.length = 0;
    this.targetTag = null;
  }

  doImage() {

    this.tagService.saveImage(this.sourceTag.id, this.background_image).subscribe(
      data => {
        this.reset();
        this.getAll();
      },
      error => {
        console.log(error);
        this.messageService.clear();
        this.messageService.add({ key: "image", severity: 'error', detail: this.translate.instant('locale.tags.error.image'), closable: false, life: 5000 });
      }
    );

    this.tags.length = 0;
    this.background_image = null;
  }

   private reset() {
    this.tags.length = 0;
    this.total = 0;


    this.selectedSort = sessionStorage.getItem('tags_order');
    if (!this.selectedSort) {
      this.sort = "name";
      this.order = "asc";
      this.selectedSort = this.sort + "," + this.order;
    }
    else {
      const index = this.selectedSort.indexOf(",");
      this.sort = this.selectedSort.slice(0, index);
      this.order = this.selectedSort.slice(index + 1);
    }

  }


}
