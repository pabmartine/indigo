import { Component, OnInit, ViewChild, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';
import { MessageService, SelectItem } from 'primeng/api';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Book } from 'src/app/domain/book';
import { Search } from 'src/app/domain/search';
import { User } from 'src/app/domain/user';
import { Author } from 'src/app/domain/author';
import { BookService } from 'src/app/services/book.service';
import { UserService } from 'src/app/services/user.service';
import { AuthorService } from 'src/app/services/author.service';
import { DetailComponent } from 'src/app/pages/detail/detail.component';
import { AuthorComponent } from '../author/author.component';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { ImageService } from 'src/app/utils/image.service';

// Interfaz para libros con imagen temporal
interface BookWithTempImage extends Book {
  originalImage?: string;
}

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
    styleUrls: ["./profile.component.css"],
  providers: [MessageService]
})
export class ProfileComponent implements OnInit, OnDestroy {
  @ViewChild(DetailComponent) detailComponent: DetailComponent;
  @ViewChild(AuthorComponent) authorComponent: AuthorComponent;

  param: any;
  user: User;
  languages: SelectItem[];
  languageBooks: SelectItem[] = [];
  changedLang: boolean;
  books: BookWithTempImage[] = [];

  chooseLanguageBooks = '';

  // Variables para modales
  showDetail = false;
  showAuthorDetail = false;

  private destroy$ = new Subject<void>();

  constructor(
    private messageService: MessageService,
    public translate: TranslateService,
    public userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private authorService: AuthorService,
    private cdr: ChangeDetectorRef,
    private authState: AuthStateService,
    private imageService: ImageService
  ) {

  }

  ngOnInit(): void {
    this.setLanguages();
    this.setBookLanguages();
    this.chooseLanguageBooks = 'Select';

    this.user = new User();

    this.route.queryParams.subscribe(params => {
      if (params['type']) {
        if (params['type'] == 'new') {
        } else if (params['type'] == 'update') {
          this.getUser(params['user']);
        }
      } else {
        const currentUser = this.authState.getCurrentUser();
        if (currentUser) {
          this.param = { username: currentUser.username };
          this.user = currentUser;
        }
      }
    });
    this.getBooks();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  readOnly() {
    const currentUser = this.authState.getCurrentUser();
    if (!currentUser) return false;
    return currentUser.role === 'USER' || (currentUser.role === 'ADMIN' && currentUser.username === this.user?.username);
  }

  isUser() {
    return this.authState.getCurrentUser()?.role === 'USER';
  }

  isValid() {
    let valid = this.user.username && this.user.password && this.user.language;
    return valid;
  }

  getUser(username: string): void {
    this.userService.get(username).subscribe({
      next: (data) => {
        this.param = { username: data.username };
        this.user = data;
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.get'), closable: false, life: 5000 });
      }
    });
  }

  setLanguages() {
    this.languages = [
      { label: this.translate.instant('locale.languages.es'), value: 'es-ES' },
      { label: this.translate.instant('locale.languages.ca'), value: 'ca-ES' },
      { label: this.translate.instant('locale.languages.en'), value: 'en-GB' },
      { label: this.translate.instant('locale.languages.fr'), value: 'fr-FR' },
      { label: this.translate.instant('locale.languages.pt'), value: 'pt-PT' },
      { label: this.translate.instant('locale.languages.de'), value: 'de-DE' },
      { label: this.translate.instant('locale.languages.it'), value: 'it-IT' },
      { label: this.translate.instant('locale.languages.sv'), value: 'sv-SE' }
    ];
  }

  setBookLanguages() {
    this.languageBooks.length = 0;
    this.bookService.getLanguages().subscribe(
      data => {
        const uniqueLangs = [...new Set(data)];
        const labels = new Set<string>();
        this.translate.get('locale.languages').subscribe(translations => {
          const supportedLanguages = Object.keys(translations);
          uniqueLangs.forEach((lang: string) => {
            if (lang) {
              let normalizedLang = lang.toLowerCase();
              if (normalizedLang.includes('-')) {
                normalizedLang = normalizedLang.split('-')[0];
              }
              if (normalizedLang.includes('_')) {
                normalizedLang = normalizedLang.split('_')[0];
              }

              if (supportedLanguages.includes(normalizedLang)) {
                const label = translations[normalizedLang];
                if (label && !labels.has(label)) {
                  this.languageBooks.push({ label: label, value: lang });
                  labels.add(label);
                }
              }
            }
          });
        });
      }
    );
  }

  update(): void {
    this.setLanguages();
    this.messageService.clear();

    this.userService.update(this.user).subscribe({
      next: (data) => {
        const currentUser = this.authState.getCurrentUser();
        if (currentUser && currentUser.id !== this.user.id) {
          this.router.navigate(["settings"]);
        } else {
          if (this.changedLang) {
            let translations: any = (<any>this.translate).translations[this.translate.currentLang];

            this.translate.onTranslationChange.emit(<TranslationChangeEvent>{
              translations: translations,
              lang: this.translate.currentLang
            });
          }

          // Store user in session via AuthStateService
          this.authState.setUser(this.user);

          this.messageService.add({ severity: 'success', detail: this.translate.instant('locale.profile.ok.update'), closable: false, life: 5000 });
        }
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.update'), closable: false, life: 5000 });
      }
    });
  }

  save(): void {
    this.messageService.clear();

    this.userService.get(this.user.username).subscribe({
      next: (data) => {
        if (data) {
          this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.name'), closable: false, life: 5000 });
        } else {
          this.userService.save(this.user).subscribe({
            next: (data) => {
              this.router.navigate(["settings"]);
            },
            error: (error) => {
              console.log(error);
              this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.save'), closable: false, life: 5000 });
            }
          });
        }
      },
      error: (error) => {
        console.log(error);
        this.messageService.add({ severity: 'error', detail: this.translate.instant('locale.profile.error.get'), closable: false, life: 5000 });
      }
    });
  }

  doTranslate() {
    if (this.translate.currentLang != this.user.language) {
      this.translate.use(this.user.language);
      this.changedLang = true;
    } else {
      this.changedLang = false;
    }
  }

  getBooks(): void {
    const currentUser = this.authState.getCurrentUser();
    if (!currentUser?.username) return;

    this.bookService.getSent(currentUser.username).subscribe({
      next: (data) => {
        const booksWithTempData: BookWithTempImage[] = data.map((book) => {
          const processedBook: BookWithTempImage = { ...book };

          if (book.image) {
            processedBook.image = this.imageService.toDataUrlSafe(book.image);
            processedBook.originalImage = book.image;
          }

          if (book.rating) {
            processedBook.rating = Math.round(book.rating);
          }

          if (!processedBook.authors || !Array.isArray(processedBook.authors)) {
            processedBook.authors = [];
          }

          return processedBook;
        });

        this.books = booksWithTempData;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  searchBookByAuthor(author: string) {
    let search: Search = new Search();
    search.author = author;
    this.router.navigate(["books"], { queryParams: { adv_search: JSON.stringify(search) } });
  }

  // MÉTODO CORREGIDO: Subir al inicio y luego mostrar modal
  showBookDetails(book: BookWithTempImage): void {
    // Primero subir al inicio de la página
    this.scrollToTop();

    // Esperar un poco para que se complete el scroll y luego abrir modal
    setTimeout(() => {
      if (this.detailComponent) {
        this.detailComponent.showDetails(book);
        this.showDetail = true;
        this.cdr.detectChanges();
      }
    }, 100);
  }

  // MÉTODO PARA SUBIR AL INICIO DE LA PÁGINA
  private scrollToTop(): void {
    // Usar múltiples métodos para asegurar compatibilidad
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      document.body.scrollTop = 0;
      document.documentElement.scrollTop = 0;
    }
  }

  // MÉTODO OBSOLETO: Reemplazado por showBookDetails
  showDetails(book: BookWithTempImage) {
    this.showBookDetails(book);
  }

  // MÉTODOS PARA MANEJAR MODALES DE LIBROS
  closeDetails(): void {
    this.showDetail = false;
    this.cdr.detectChanges();
  }

  openDetails(): void {
    this.showDetail = true;
    this.cdr.detectChanges();
  }

  // MÉTODOS PARA MANEJAR MODALES DE AUTORES
  showAuthorDetails(author: Author): void {
    // Primero subir al inicio de la página
    this.scrollToTop();

    // Esperar un poco para que se complete el scroll y luego abrir modal
    setTimeout(() => {
      if (this.authorComponent) {
        this.authorComponent.showDetails(author);
        this.showAuthorDetail = true;
        this.cdr.detectChanges();
      }
    }, 100);
  }

  closeAuthorDetails(): void {
    this.showAuthorDetail = false;
    this.cdr.detectChanges();
  }

  openAuthorDetails(): void {
    this.showAuthorDetail = true;
    this.cdr.detectChanges();
  }

  openBook(book: BookWithTempImage): void {
    this.closeAuthorDetails();

    // Esperar un poco para que se cierre el modal de autor
    setTimeout(() => {
      this.showBookDetails(book);
    }, 150);
  }

  openAuthor(authorName: string): void {
    if (!authorName || authorName === 'Unknown Author') {
      this.messageService.add({
        severity: "warn",
        summary: "Author not available",
        detail: "Author information is not available for this book.",
      });
      return;
    }

    this.closeDetails();

    // Primero subir al inicio de la página
    this.scrollToTop();

    // Buscar autor y mostrar modal
    this.authorService
      .getByName(authorName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            const authorData = { ...data };
            if (authorData.image) {
              authorData.image = this.imageService.toDataUrlSafe(authorData.image);
            }

            // Esperar un poco para que se complete el scroll
            setTimeout(() => {
              this.showAuthorDetails(authorData);
            }, 100);
          }
        },
        error: (error) => {
          console.error("Error fetching author:", error);
        },
      });
  }

  refreshBook(book: Book): void {
    const index = this.books.findIndex((b) => b.id === book.id);
    if (index !== -1) {
      const originalBookData = this.books[index];
      this.books[index] = {
        ...book,
        image: book.image
          ? this.imageService.toDataUrlSafe(book.image)
          : originalBookData.image,
        originalImage: book.image || originalBookData.originalImage,
        authors: book.authors || []
      };
      this.cdr.detectChanges();
    }
  }

  deleteBook(bookId: string): void {
    const index = this.books.findIndex((b) => b.id === bookId);
    if (index !== -1) {
      this.books.splice(index, 1);
      this.cdr.detectChanges();
    }
  }

  getFirstAuthor(book: BookWithTempImage): string {
    return book.authors && book.authors.length > 0 ? book.authors[0] : 'Unknown Author';
  }
}
