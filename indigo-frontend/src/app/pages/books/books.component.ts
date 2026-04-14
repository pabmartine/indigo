import { Location } from "@angular/common"
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  OnInit,
  ViewChild,
} from "@angular/core"
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router"
import { TranslateService } from "@ngx-translate/core"
import { MessageService } from "primeng/api"
import { SelectItem } from "primeng/api/selectitem"
import { combineLatest, Observable, Subject } from "rxjs"
import { catchError, debounceTime, filter, takeUntil, tap } from "rxjs/operators"
import { of } from "rxjs"
import { Author } from "src/app/domain/author"
import { Book } from "src/app/domain/book"
import { Search } from "src/app/domain/search"
import { BookService } from "src/app/services/book.service"
import { DetailComponent } from "src/app/pages/detail/detail.component"
import { AuthorComponent } from "../author/author.component"
import { AuthorService } from "src/app/services/author.service"
import { AuthStateService } from "src/app/services/auth-state.service"
import { ImageService } from 'src/app/utils/image.service'

// Interfaz para libros con imagen temporal
interface BookWithTempImage extends Book {
  originalImage?: string
}

@Component({
  selector: "app-books",
  templateUrl: "./books.component.html",
  styleUrls: ["./books.component.css"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService],
})
export class BooksComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(DetailComponent) detailComponent: DetailComponent
  @ViewChild(AuthorComponent) authorComponent: AuthorComponent
  @ViewChild("scrollSentinel") scrollSentinel?: ElementRef<HTMLDivElement>

  books: BookWithTempImage[] = []
  favorites: BookWithTempImage[] = []

  authorInfo: Author | null = null
  title: string = 'Books'
  private adv_search: Search | null = null

  total = 0

  private page: number = 0
  private size: number = 20
  private sort: string = 'id'
  private order: string = 'desc'

  sorts: SelectItem[] = [
    { label: 'ID (Desc)', value: 'id,desc' },
    { label: 'ID (Asc)', value: 'id,asc' },
    { label: 'Pub Date (Desc)', value: 'pubDate,desc' },
    { label: 'Pub Date (Asc)', value: 'pubDate,asc' },
    { label: 'Title (Asc)', value: 'title,asc' },
    { label: 'Title (Desc)', value: 'title,desc' },
    { label: 'Rating (Desc)', value: 'rating,desc' },
    { label: 'Rating (Asc)', value: 'rating,asc' },
  ]
  selectedSort: string = 'id,desc'

  searched = false
  private isInitialized = false

  user: any = {}

  private bookCache = new Map<string, BookWithTempImage[]>()
  private cacheOrder: string[] = []
  private readonly maxCacheEntries = 5
  private favoritesCache: BookWithTempImage[] | null = null
  private filtersKey: string = "default"
  private scrollObserver?: IntersectionObserver
  private ignoreNextRouteSearch = false

  private destroy$ = new Subject<void>()

  showDetail = false
  showAuthorDetail = false
  isListView = false
  isScrolling = false
  listItemSize = 210

  constructor(
    private bookService: BookService,
    private authorService: AuthorService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    public translate: TranslateService,
    private location: Location,
    private cdr: ChangeDetectorRef,
    private authState: AuthStateService,
    private imageService: ImageService,
    private ngZone: NgZone,
  ) {
    this.initializeUser()
    this.initializeScreenSize()
    this.initializeDefaults()
  }

  ngOnInit(): void {
    // Cargar traducciones de forma segura después de la inicialización
    setTimeout(() => {
      this.loadTranslations()
    }, 100)

    if (!this.isInitialized) {
      // IMPORTANTE: Procesar parámetros de URL ANTES de las suscripciones
      this.initializeSearch()
      this.initializeSubscriptions()
      this.isInitialized = true
    }
  }

  ngAfterViewInit(): void {
    this.setupScrollObserver()
  }

  ngOnDestroy() {
    this.destroy$.next()
    this.destroy$.complete()
    this.resetCache()
    if (this.favoritesCache) {
      this.favoritesCache = null
    }
    this.scrollObserver?.disconnect()
  }

  private initializeUser(): void {
    const currentUser = this.authState.getCurrentUser()
    if (currentUser) {
      this.user = currentUser
      // Ensure languageBooks is always initialized
      if (!this.user.languageBooks || this.user.languageBooks.length === 0) {
        this.user.languageBooks = ['en']
      }
    } else {
      this.user = { languageBooks: ['en'], role: 'USER', username: '' }
    }
  }

  private initializeScreenSize(): void {
    if (typeof window !== 'undefined') {
      if (window.screen.width < 640) {
        this.size = 10
        this.listItemSize = 190
      } else if (window.screen.width < 1024) {
        this.size = 20
        this.listItemSize = 200
      } else {
        this.size = 60
        this.listItemSize = 210
      }
    }
  }

  private initializeDefaults(): void {
    this.adv_search = null
    this.authorInfo = null
    this.title = 'Books'
    this.page = 0
    this.sort = 'id'
    this.order = 'desc'
    this.selectedSort = 'id,desc'
    this.total = 0
    this.searched = false
    this.showDetail = false
    this.showAuthorDetail = false
    this.books = []
    this.favorites = []
  }

  private loadTranslations(): void {
    if (!this.translate) {
      return
    }

    const translationKeys = [
      "locale.books.order_by.id.desc",
      "locale.books.order_by.id.asc",
      "locale.books.order_by.pubdate.desc",
      "locale.books.order_by.pubdate.asc",
      "locale.books.order_by.title.asc",
      "locale.books.order_by.title.desc",
      "locale.books.order_by.rating.desc",
      "locale.books.order_by.rating.asc",
    ]

    this.translate.get(translationKeys)
      .pipe(
        takeUntil(this.destroy$),
        catchError(error => {
          console.error('Error loading translations:', error)
          return of({}) // Retornar objeto vacío en caso de error
        })
      )
      .subscribe({
        next: (translations) => {
          if (translations && Object.keys(translations).length > 0) {
            this.sorts = [
              { label: translations["locale.books.order_by.id.desc"] || 'ID (Desc)', value: "id,desc" },
              { label: translations["locale.books.order_by.id.asc"] || 'ID (Asc)', value: "id,asc" },
              { label: translations["locale.books.order_by.pubdate.desc"] || 'Pub Date (Desc)', value: "pubDate,desc" },
              { label: translations["locale.books.order_by.pubdate.asc"] || 'Pub Date (Asc)', value: "pubDate,asc" },
              { label: translations["locale.books.order_by.title.asc"] || 'Title (Asc)', value: "title,asc" },
              { label: translations["locale.books.order_by.title.desc"] || 'Title (Desc)', value: "title,desc" },
              { label: translations["locale.books.order_by.rating.desc"] || 'Rating (Desc)', value: "rating,desc" },
              { label: translations["locale.books.order_by.rating.asc"] || 'Rating (Asc)', value: "rating,asc" },
            ]
            this.cdr.detectChanges()
          }
        }
      })
  }

  private initializeSubscriptions(): void {
    combineLatest([
      this.router.events.pipe(filter((e) => e instanceof NavigationEnd)),
      this.route.queryParams
    ])
      .pipe(debounceTime(50), takeUntil(this.destroy$))
      .subscribe(([navigationEvent, params]) => {
        this.handleRouteChange(navigationEvent as NavigationEnd, params)
      })
  }

  private handleRouteChange(navigationEvent: NavigationEnd, params: any): void {
    if (this.ignoreNextRouteSearch) {
      this.ignoreNextRouteSearch = false
      return
    }

    let shouldSearch = false
    let paramsChanged = false

    if (params["author"]) {
      try {
        const newAuthorInfo = JSON.parse(params["author"])
        if (JSON.stringify(this.authorInfo) !== JSON.stringify(newAuthorInfo)) {
          this.authorInfo = newAuthorInfo
          paramsChanged = true
        }
      } catch (error) {
        console.error('Error parsing author params:', error)
      }
    } else if (this.authorInfo !== null) {
      this.authorInfo = null
      paramsChanged = true
    }

    if (params["adv_search"]) {
      try {
        const newAdvSearch = JSON.parse(params["adv_search"])
        if (JSON.stringify(this.adv_search) !== JSON.stringify(newAdvSearch)) {
          this.adv_search = newAdvSearch
          paramsChanged = true
        }
      } catch (error) {
        console.error('Error parsing adv_search params:', error)
      }
    } else if (navigationEvent.url === "/books" && this.adv_search !== null && !params["author"]) {
      if (!this.isAuthorSearch(this.adv_search)) {
        this.adv_search = null
        paramsChanged = true
      }
    }

    if (paramsChanged) {
      shouldSearch = true
    }

    if (
      navigationEvent.url === "/books" &&
      !sessionStorage.getItem("position") &&
      !this.searched &&
      !params["adv_search"] &&
      !params["author"]
    ) {
      this.adv_search = null
      shouldSearch = true
    }

    if (shouldSearch) {
      this.doSearch()
    }
  }

  private initializeSearch(): void {
    const queryParams = this.route.snapshot.queryParams

    if (queryParams["adv_search"]) {
      try {
        this.adv_search = JSON.parse(queryParams["adv_search"])
      } catch (error) {
        console.error('Error parsing initial adv_search:', error)
        this.adv_search = null
      }
    }

    // Procesar autor si existe
    if (queryParams["author"]) {
      try {
        this.authorInfo = JSON.parse(queryParams["author"])
      } catch (error) {
        console.error('Error parsing initial author:', error)
        this.authorInfo = null
      }
    }

    const hasQueryParams = queryParams["author"] || queryParams["adv_search"]
    if (!hasQueryParams && !this.searched) {
      this.doSearch()
    } else if (hasQueryParams && !this.searched) {
      this.ignoreNextRouteSearch = true
      this.doSearch()
    }
  }

  private setupScrollObserver(): void {
    if (!this.scrollSentinel || typeof window === "undefined") {
      return
    }

    this.ngZone.runOutsideAngular(() => {
      this.scrollObserver?.disconnect()
      this.scrollObserver = new IntersectionObserver(
        (entries) => {
          if (entries.some((entry) => entry.isIntersecting)) {
            this.ngZone.run(() => this.onScroll())
          }
        },
        { rootMargin: "400px 0px" }
      )

      this.scrollObserver.observe(this.scrollSentinel.nativeElement)
    })
  }

  private restoreScrollPosition(): void {
    const storedPosition = sessionStorage.getItem("position")
    if (!storedPosition) {
      return
    }

    setTimeout(() => {
      document.documentElement.scrollTop = Number(storedPosition)
      sessionStorage.removeItem("position")
    })
  }

  onChange(event: any): void {
    if (this.selectedSort) {
      const index = this.selectedSort.indexOf(",")
      this.sort = this.selectedSort.slice(0, index)
      this.order = this.selectedSort.slice(index + 1)
      sessionStorage.setItem("books_order", this.selectedSort)
    }

    this.page = 0
    this.books.length = 0
    this.resetCache()
    this.getAll()
    this.fetchCountAndUpdateTitle().subscribe()
  }

  onScroll(): void {
    if (this.total > 0 && this.books.length < this.total && this.page > 0 && !this.isScrolling) {
      this.getAll()
    }
  }

  onVirtualScrollIndexChange(index: number): void {
    if (!this.isListView) {
      return
    }

    const preloadThreshold = 8
    if (index + preloadThreshold >= this.books.length) {
      this.onScroll()
    }
  }

  private fetchCountAndUpdateTitle(): Observable<any> {
    if (!this.adv_search) {
      this.adv_search = new Search()
    }
    this.filtersKey = this.createFiltersKey(this.adv_search)

    // CRUCIAL: Asegurar que los idiomas estén configurados - use fallback chain
    const languages = this.user?.languageBooks || this.authState.getLanguageBooks()
    this.adv_search.languages = languages.length > 0 ? languages : ['en']

    return this.bookService.count(this.adv_search).pipe(
      tap((data) => {
        this.total = data
        this.updateTitle()
        this.cdr.detectChanges()
      }),
      catchError((error) => {
        console.error('Error fetching count:', error)
        this.messageService.clear()
        this.messageService.add({
          severity: "error",
          summary: "Error",
          detail: "Error loading book count",
          closable: true,
          life: 5000,
        })
        return of(null) // Retornar null en caso de error
      }),
      takeUntil(this.destroy$)
    )
  }

  private updateTitle(): void {
    let searchContext = ""
    try {
      if (this.isGlobalSearch(this.adv_search)) {
        searchContext = this.adv_search?.path || ''
        this.title = `Search results ${searchContext} (${this.total})`
      } else if (this.isAuthorSearch(this.adv_search)) {
        searchContext = this.authorInfo ? this.authorInfo.name : (this.adv_search?.author || '')
        this.title = `Books by ${searchContext} (${this.total})`
      } else if (this.isTagSearch(this.adv_search)) {
        searchContext = this.adv_search?.selectedTags?.join(", ") || ''
        this.title = `Books in ${searchContext} (${this.total})`
      } else if (this.isSerieSearch(this.adv_search)) {
        searchContext = this.adv_search?.serie || ''
        this.title = `Books in series ${searchContext} (${this.total})`
      } else {
        this.title = `Books (${this.total})`
      }
    } catch (error) {
      console.error('Error updating title:', error)
      this.title = `Books (${this.total})`
    }
    this.cdr.detectChanges()
  }

  getAll(): void {
    if (!this.adv_search) {
      this.adv_search = new Search()
    }

    // CRUCIAL: Asegurar que los idiomas estén configurados - use fallback chain
    const languages = this.user?.languageBooks || this.authState.getLanguageBooks()
    this.adv_search.languages = languages.length > 0 ? languages : ['en']
    this.filtersKey = this.createFiltersKey(this.adv_search)

    const cacheKey = this.createPageCacheKey(this.page)

    if (this.bookCache.has(cacheKey)) {
      const cachedData = this.bookCache.get(cacheKey)!
      Array.prototype.push.apply(this.books, cachedData)
      this.page++
      this.cdr.detectChanges()
      this.restoreScrollPosition()
      return
    }

    this.isScrolling = true
    this.bookService
      .getAllSummary(this.adv_search, this.page, this.size, this.sort, this.order)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Book[]) => {
          if (!data || (data.length === 0 && this.page === 0)) {
            if (this.page === 0) this.books = []
            this.cdr.detectChanges()
            this.isScrolling = false
            return
          }

          const booksWithTempData: BookWithTempImage[] = data.map((book) => {
            const processedBook: BookWithTempImage = { ...book }
            const coverUrl = this.bookService.buildCoverImageUrl(book.id)

            processedBook.coverUrl = coverUrl || undefined
            processedBook.image = coverUrl || undefined
            processedBook.originalImage = coverUrl || undefined

            if (book.rating) {
              processedBook.rating = Math.round(book.rating)
            }

            if (!processedBook.authors || !Array.isArray(processedBook.authors)) {
              processedBook.authors = []
            }

            return processedBook
          })

          Array.prototype.push.apply(this.books, booksWithTempData)
          this.page++
          this.cdr.detectChanges()
          this.restoreScrollPosition()

          this.storeInCache(cacheKey, booksWithTempData)
          this.isScrolling = false
        },
        error: (error) => {
          console.error("Error fetching books:", error)
          this.messageService.clear()
          this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Error loading books",
            closable: true,
            life: 5000,
          })
          this.isScrolling = false
        },
      })
  }

  trackByBookId(index: number, book: BookWithTempImage): string {
    return book.id || `book-${index}`
  }

  getFirstAuthor(book: BookWithTempImage): string {
    return book.authors && book.authors.length > 0 ? book.authors[0] : 'Unknown Author'
  }

  showDetails(book: BookWithTempImage): void {
    if (this.detailComponent) {
      this.detailComponent.showDetails(book)
      this.showDetail = true
      this.cdr.detectChanges()
    }
  }

  closeDetails(): void {
    this.showDetail = false
    this.cdr.detectChanges()
  }

  openDetails(): void {
    this.showDetail = true
    this.cdr.detectChanges()
  }

  showAuthorDetails(author: Author): void {
    if (this.authorComponent) {
      this.authorComponent.showDetails(author)
      this.showAuthorDetail = true
      this.cdr.detectChanges()
    }
  }

  closeAuthorDetails(): void {
    this.showAuthorDetail = false
    this.cdr.detectChanges()
  }

  openAuthorDetails(): void {
    this.showAuthorDetail = true
    this.cdr.detectChanges()
  }

  openBook(book: BookWithTempImage): void {
    this.closeAuthorDetails()
    this.showDetails(book)
  }

  openAuthor(authorName: string): void {
    if (!authorName || authorName === 'Unknown Author') {
      this.messageService.add({
        severity: "warn",
        summary: "Author not available",
        detail: "Author information is not available for this book.",
      })
      return
    }

    this.closeDetails()
    this.authorService
      .getByName(authorName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          if (data) {
            const authorData = { ...data }
            if (authorData.image) {
              authorData.image = this.imageService.toDataUrlSafe(authorData.image)
            }
            this.showAuthorDetails(authorData)
          }
        },
        error: (error) => {
          console.error("Error fetching author:", error)
        },
      })
  }

  refreshBook(book: Book): void {
    const index = this.books.findIndex((b) => b.id === book.id)
    if (index !== -1) {
      const originalBookData = this.books[index]
      const coverUrl = this.bookService.buildCoverImageUrl(book.id) || originalBookData.image
      this.books[index] = {
        ...book,
        image: coverUrl || originalBookData.image,
        originalImage: coverUrl || originalBookData.originalImage,
        authors: book.authors || []
      }

      this.bookCache.forEach((cachedBooks, key) => {
        const cachedIndex = cachedBooks.findIndex((cb) => cb.id === book.id)
        if (cachedIndex !== -1) {
          cachedBooks[cachedIndex] = { ...this.books[index] }
        }
      })
      this.cdr.detectChanges()
    }

    const favIndex = this.favorites.findIndex((b) => b.id === book.id)
    if (favIndex !== -1) {
      const originalFavData = this.favorites[favIndex]
      const coverUrl = this.bookService.buildCoverImageUrl(book.id) || originalFavData.image
      this.favorites[favIndex] = {
        ...book,
        image: coverUrl || originalFavData.image,
        originalImage: coverUrl || originalFavData.originalImage,
        authors: book.authors || []
      }
      if (this.favoritesCache) {
        const favCacheIndex = this.favoritesCache.findIndex((fb) => fb.id === book.id)
        if (favCacheIndex !== -1) {
          this.favoritesCache[favCacheIndex] = { ...this.favorites[favIndex] }
        }
      }
      this.cdr.detectChanges()
    }
  }

  deleteBook(bookId: string): void {
    let foundAndDeleted = false
    const index = this.books.findIndex((b) => b.id === bookId)
    if (index !== -1) {
      this.books.splice(index, 1)
      this.total = Math.max(0, this.total - 1)
      foundAndDeleted = true
    }
    this.bookCache.forEach((cachedBooks) => {
      const cachedIndex = cachedBooks.findIndex((cb) => cb.id === bookId)
      if (cachedIndex !== -1) cachedBooks.splice(cachedIndex, 1)
    })

    const favIndex = this.favorites.findIndex((b) => b.id === bookId)
    if (favIndex !== -1) {
      this.favorites.splice(favIndex, 1)
      if (this.favoritesCache) {
        const favCacheIndex = this.favoritesCache.findIndex((fb) => fb.id === bookId)
        if (favCacheIndex !== -1) this.favoritesCache.splice(favCacheIndex, 1)
      }
      foundAndDeleted = true
    }

    if (foundAndDeleted) {
      this.updateTitle()
      this.cdr.detectChanges()
    }
  }

  private doSearch(): void {
    // Ensure user is properly initialized BEFORE reset
    if (!this.user || !this.user.languageBooks || this.user.languageBooks.length === 0) {
      console.warn('User languageBooks not initialized, re-initializing user')
      this.initializeUser()
    }

    this.reset()
    this.searched = true

    if (!this.adv_search) {
      this.adv_search = new Search()
      if (this.shouldLoadFavorites()) {
        this.getFavoritesBooks()
      }
    } else {
      this.favorites.length = 0
      this.favoritesCache = null
    }

    // CRUCIAL: Configurar idiomas - ALWAYS ensure languages are set
    const languages = this.user?.languageBooks || this.authState.getLanguageBooks()
    this.adv_search.languages = languages.length > 0 ? languages : ['en']
    this.getAll()
    this.fetchCountAndUpdateTitle().subscribe()
  }

  private shouldLoadFavorites(): boolean {
    return this.router.url === "/books" && !this.hasSpecificSearchCriteria(this.adv_search)
  }

  private hasSpecificSearchCriteria(search: Search | null): boolean {
    if (!search) return false
    return !!(
      (search.author && search.author.length > 0) ||
      (search.path && search.path.length > 0) ||
      (search.selectedTags && search.selectedTags.length > 0) ||
      (search.serie && search.serie.length > 0) ||
      (search.title && search.title.length > 0) ||
      search.ini ||
      search.end ||
      search.min ||
      search.max
    )
  }

  getFavoritesBooks(): void {
    if (this.favoritesCache) {
      this.favorites = [...this.favoritesCache]
      this.cdr.detectChanges()
      return
    }

    if (!this.user.username) {
      return
    }

    this.bookService
      .getFavorites(this.user.username)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Book[]) => {
          this.favorites.length = 0

          const favoritesWithTempData: BookWithTempImage[] = data.map((book) => {
            const processedBook: BookWithTempImage = { ...book }
            const coverUrl = this.bookService.buildCoverImageUrl(book.id)

            processedBook.coverUrl = coverUrl || undefined
            processedBook.image = coverUrl || undefined
            processedBook.originalImage = coverUrl || undefined

            if (book.rating) {
              processedBook.rating = Math.round(book.rating)
            }

            if (!processedBook.authors || !Array.isArray(processedBook.authors)) {
              processedBook.authors = []
            }

            return processedBook
          })

          Array.prototype.push.apply(this.favorites, favoritesWithTempData)
          this.favoritesCache = [...favoritesWithTempData]
          this.cdr.detectChanges()
        },
        error: (error) => {
          console.error("Error fetching favorites:", error)
        },
      })
  }

  isAdmin(): boolean {
    return !!this.user && this.user.role === "ADMIN"
  }

  private reset(): void {
    this.total = 0
    this.page = 0

    const storedSort = sessionStorage.getItem("books_order")
    if (storedSort && this.sorts && this.sorts.length > 0 && this.sorts.some((s) => s && s.value === storedSort)) {
      this.selectedSort = storedSort
    } else if (this.sorts && this.sorts.length > 0 && this.sorts[0] && this.sorts[0].value) {
      this.selectedSort = this.sorts[0].value
    } else {
      this.selectedSort = "id,desc"
    }

    if (this.selectedSort) {
      const index = this.selectedSort.indexOf(",")
      if (index > -1) {
        this.sort = this.selectedSort.slice(0, index)
        this.order = this.selectedSort.slice(index + 1)
      } else {
        this.sort = "id"
        this.order = "desc"
      }
    }

    this.books.length = 0
    this.resetCache()
    this.cdr.detectChanges()
  }

  public isGlobalSearch(search: Search | null): boolean {
    return (
      !!search &&
      !!search.path &&
      search.path.length > 0 &&
      !this.hasValue(search.title) &&
      !this.hasValue(search.ini) &&
      !this.hasValue(search.end) &&
      !this.hasValue(search.min) &&
      !this.hasValue(search.max) &&
      !this.hasArrayValue(search.selectedTags) &&
      !this.hasValue(search.author) &&
      !this.hasValue(search.serie)
    )
  }

  public isAuthorSearch(search: Search | null): boolean {
    return (
      !!search &&
      !!search.author &&
      search.author.length > 0 &&
      !this.hasValue(search.title) &&
      !this.hasValue(search.ini) &&
      !this.hasValue(search.end) &&
      !this.hasValue(search.min) &&
      !this.hasValue(search.max) &&
      !this.hasArrayValue(search.selectedTags) &&
      !this.hasValue(search.serie) &&
      !this.hasValue(search.path)
    )
  }

  public isTagSearch(search: Search | null): boolean {
    return (
      !!search &&
      this.hasArrayValue(search.selectedTags) &&
      !this.hasValue(search.title) &&
      !this.hasValue(search.ini) &&
      !this.hasValue(search.end) &&
      !this.hasValue(search.min) &&
      !this.hasValue(search.max) &&
      !this.hasValue(search.author) &&
      !this.hasValue(search.serie) &&
      !this.hasValue(search.path)
    )
  }

  public isSerieSearch(search: Search | null): boolean {
    return (
      !!search &&
      !!search.serie &&
      search.serie.length > 0 &&
      !this.hasValue(search.title) &&
      !this.hasValue(search.ini) &&
      !this.hasValue(search.end) &&
      !this.hasValue(search.min) &&
      !this.hasValue(search.max) &&
      !this.hasArrayValue(search.selectedTags) &&
      !this.hasValue(search.author) &&
      !this.hasValue(search.path)
    )
  }

  private hasValue(value: any): boolean {
    return value !== null && value !== undefined && value !== ""
  }

  private hasArrayValue(value: any[] | null | undefined): boolean {
    return !!value && Array.isArray(value) && value.length > 0
  }

  private resetCache(): void {
    this.bookCache.clear()
    this.cacheOrder = []
  }

  private createFiltersKey(search: Search | null): string {
    if (!search) {
      return "default"
    }

    const {
      path = "",
      title = "",
      author = "",
      serie = "",
      selectedTags = [],
      languages = [],
      ini = "",
      end = "",
      min = "",
      max = "",
    } = search as any

    const normalizedLanguages = Array.isArray(languages) ? [...languages].sort().join(",") : ""
    const normalizedTags = Array.isArray(selectedTags) ? [...selectedTags].sort().join(",") : ""

    return [path, title, author, serie, normalizedTags, normalizedLanguages, ini ?? "", end ?? "", min ?? "", max ?? ""].join("|")
  }

  private createPageCacheKey(page: number): string {
    return `${this.filtersKey}-${page}-${this.size}-${this.sort}-${this.order}`
  }

  private storeInCache(key: string, data: BookWithTempImage[]): void {
    this.bookCache.set(key, data)
    this.cacheOrder.push(key)
    if (this.cacheOrder.length > this.maxCacheEntries) {
      const oldest = this.cacheOrder.shift()
      if (oldest) {
        this.bookCache.delete(oldest)
      }
    }
  }

  close(): void {
    this.location.back()
  }

  setView(isList: boolean): void {
    this.isListView = isList
    this.cdr.detectChanges()
  }
}
