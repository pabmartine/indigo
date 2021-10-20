import { Component, OnInit } from '@angular/core';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { User } from 'src/app/domain/user';
import { UserService } from 'src/app/services/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Book } from 'src/app/domain/book';
import { BookService } from 'src/app/services/book.service';
import { Search } from 'src/app/domain/search';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  providers: [MessageService]
})
export class ProfileComponent implements OnInit {

  param: any;
  user: User;
  languages: SelectItem[];
  permissions: String[];
  changedLang:boolean;
  books: Book[] = [];

  constructor(
    private messageService: MessageService, 
    public translate: TranslateService, 
    public userService: UserService,     
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService
    ) {
   
    this.permissions = [
      'show_random_books'
    ];
  }

  ngOnInit(): void {

    this.setLanguages();

    this.user = new User();

    this.route.queryParams.subscribe(params => {
      if (params['type']) {
        if (params['type']=='new') {
        } else if (params['type']=='update') {
          this.getUser(params['user']);
        }
      } else if (sessionStorage.user) {
          const user = JSON.parse(sessionStorage.user);
          this.param = { username: user.username };
          this.user = user;
        } 
    });

    this.getBooks();
  }

  readOnly(){
    return JSON.parse(sessionStorage.user).role == 'USER' || (JSON.parse(sessionStorage.user).role == 'ADMIN' && JSON.parse(sessionStorage.user).username == this.user.username);
  }

  isValid(){
    let valid = this.user.username && this.user.password && this.user.language;
    return valid;
  }

  getUser(username:string){
    this.userService.get(username).subscribe(
      data => {
       this.param = { username: data.username };
       this.user = data;
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.get'), closable: false, life: 5000 });

      }
    );
  }

  setLanguages(){
    this.languages = [
      { label: this.translate.instant('locale.languages.es'), value: 'es' },
      { label: this.translate.instant('locale.languages.en'), value: 'en' },
      { label: this.translate.instant('locale.languages.fr'), value: 'fr' }
    ];
  }

  update() {

    this.setLanguages();

    this.messageService.clear();
    
    this.userService.update(this.user).subscribe(
      data => {

        if (JSON.parse(sessionStorage.user).id != this.user.id){
          this.router.navigate(["settings"]);
        } else {
          if (this.changedLang){
            let translations: any = (<any>this.translate).translations[this.translate.currentLang];
  
            this.translate.onTranslationChange.emit(<TranslationChangeEvent>{
              translations: translations,
              lang: this.translate.currentLang
            });
          }
          //Store user in session        
          sessionStorage.setItem('user', JSON.stringify(this.user));
  
          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.profile.ok.update'), closable: false, life: 5000 });
        }

       
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.update'), closable: false, life: 5000 });

      }
    );
  }

  save() {
    this.messageService.clear();

    this.userService.get(this.user.username).subscribe(
      data => {
        if (data){
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.name'), closable: false, life: 5000 });
        } else {
          this.userService.save(this.user).subscribe(
            data => {
              this.router.navigate(["settings"]);
            },
            error => {
              console.log(error);
              this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.save'), closable: false, life: 5000 });
      
            }
          );
        }
      },
      error => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.get'), closable: false, life: 5000 });

      }
    );    
  }

  doTranslate(){
    if (this.translate.currentLang != this.user.language) {
      this.translate.use(this.user.language);
      
      this.changedLang = true;
    } else {
      this.changedLang = false;
    }
  }

  getCover(book: Book) {
    this.bookService.getCover(book.path).subscribe(
      data => {
        if (data) {
          let objectURL = 'data:image/jpeg;base64,' + data.image;
          book.image = objectURL;
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  getBooks() {
    const user = JSON.parse(sessionStorage.user);
    this.bookService.getSent(user.username).subscribe(
      data => {

        data.forEach((book) => {
          this.getCover(book);


        });

        Array.prototype.push.apply(this.books, data);
      },
      error => {
        console.log(error);
      }
    );
  }

  searchBookByAuthor(author: string) {
    let search:Search = new Search();
    search.author = author;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  showDetails(book: Book) {
    this.router.navigate(["detail"], { queryParams: { book: JSON.stringify(book) } });
  }

}
