# Indigo Frontend Analysis Report

## I. Introduction

This report details the analysis of the frontend Angular application "Indigo Frontend." The purpose is to document the functionality of its core services and components, focusing on their interaction with backend APIs, data management, user interactions, and inter-component relationships. This provides an overview of the frontend architecture and behavior.

## II. Services Analysis

### A. AuthorService

*   **Base Endpoint:** `environment.endpoint + "author"`
*   **Functions:**
    1.  **`count(languages: string[]): Observable<any>`**
        *   **Purpose:** Retrieves the total count of authors, potentially filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/count?languages=[languages_list]`
        *   **Key Request Parameters:** `languages` (array of strings, joined by comma).
    2.  **`getAll(languages: string[], page: number, size: number, sort: string, order: string): Observable<any>`**
        *   **Purpose:** Fetches a paginated and sorted list of authors, potentially filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/all?languages=[languages_list]&page=[page]&size=[size]&sort=[sort]&order=[order]`
        *   **Key Request Parameters:** `languages` (array of strings, comma-separated), `page`, `size`, `sort`, `order`.
    3.  **`getByName(author: string): Observable<any>`**
        *   **Purpose:** Retrieves author information by name (name used in a `sort` parameter).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/sort?sort=[author]`
        *   **Key Request Parameters:** `author` (string, used as `sort` query param).
    4.  **`getFavorite(author: string, user: string): Observable<any>`**
        *   **Purpose:** Checks if a specific author is a favorite for a given user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&author=[author]`
        *   **Key Request Parameters:** `user`, `author`.
    5.  **`getFavorites(user: string): Observable<any>`**
        *   **Purpose:** Retrieves a list of all favorite authors for a given user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/favorites?user=[user]`
        *   **Key Request Parameters:** `user`.
    6.  **`addFavorite(author: string, user: string): Observable<any>`**
        *   **Purpose:** Adds an author to a user's list of favorites.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&author=[author]`
        *   **Key Request Parameters:** `user`, `author`.
        *   **Request Body:** `null`.
    7.  **`deleteFavorite(author: string, user: string): Observable<any>`**
        *   **Purpose:** Removes an author from a user's list of favorites.
        *   **HTTP Method:** `DELETE`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&author=[author]`
        *   **Key Request Parameters:** `user`, `author`.

### B. BookService

*   **Base Endpoint:** `environment.endpoint + "book"`
*   **Functions:**
    1.  **`count(adv_search: Search): Observable<any>`**
        *   **Purpose:** Retrieves the total count of books based on advanced search criteria.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/count/search/advance`
        *   **Request Body:** `adv_search` (type `Search`).
    2.  **`getAll(adv_search: Search, page: number, size: number, sort: string, order: string): Observable<any>`**
        *   **Purpose:** Fetches a paginated and sorted list of books based on advanced search criteria.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/all/advance?page=[page]&size=[size]&sort=[sort]&order=[order]`
        *   **Request Body:** `adv_search` (type `Search`).
    3.  **`getEpub(path: string): Observable<Blob>`**
        *   **Purpose:** Downloads the EPUB file for a book.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/epub?path=[encoded_path]`
        *   **Key Request Parameters:** `path` (string).
        *   **Notes:** `path` parameter undergoes custom character encoding (`&` to `@_@`, `[` to `@-@`, `]` to `@¡@`, `` ` `` to `@!@`). Response type is `blob`.
    4.  **`getSerie(serie: string, languages: string[]): Observable<any>`**
        *   **Purpose:** Retrieves books belonging to a specific series, filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/serie?serie=[serie]&languages=[languages_list]`
        *   **Key Request Parameters:** `serie`, `languages` (comma-separated).
    5.  **`getSimilar(similar: string[], languages: string[]): Observable<any>`**
        *   **Purpose:** Fetches books similar to a given list, filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/similar?similar=[similar_list]&languages=[languages_list]`
        *   **Key Request Parameters:** `similar` (array, comma-separated), `languages` (comma-separated).
    6.  **`getRecommendationsByBook(recommendations: string[], languages: string[]): Observable<any>`**
        *   **Purpose:** Gets book recommendations based on a list of specified books, filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/recommendations/book?recommendations=[recommendations_list]&languages=[languages_list]`
        *   **Key Request Parameters:** `recommendations` (array, comma-separated), `languages` (comma-separated).
    7.  **`countRecommendationsByUser(user: string): Observable<any>`**
        *   **Purpose:** Counts the number of book recommendations for a specific user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/recommendations/user/count?user=[user]`
        *   **Key Request Parameters:** `user`.
    8.  **`getRecommendationsByUser(user: string, page: number, size: number, sort: string, order: string): Observable<any>`**
        *   **Purpose:** Retrieves paginated/sorted book recommendations for a user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/recommendations/user?user=[user]&page=[page]&size=[size]&sort=[sort]&order=[order]`
        *   **Key Request Parameters:** `user`, `page`, `size`, `sort`, `order`.
    9.  **`getFavorite(book: string, user: string): Observable<any>`**
        *   **Purpose:** Checks if a specific book is a favorite for a given user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&book=[book]`
        *   **Key Request Parameters:** `user`, `book`.
    10. **`getFavorites(user: string): Observable<any>`**
        *   **Purpose:** Retrieves a list of all favorite books for a given user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/favorites?user=[user]`
        *   **Key Request Parameters:** `user`.
    11. **`addFavorite(book: string, user: string): Observable<any>`**
        *   **Purpose:** Adds a book to a user's list of favorites.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&book=[book]`
        *   **Key Request Parameters:** `user`, `book`.
        *   **Request Body:** `null`.
    12. **`deleteFavorite(book: string, user: string): Observable<any>`**
        *   **Purpose:** Removes a book from a user's list of favorites.
        *   **HTTP Method:** `DELETE`
        *   **API Endpoint Called:** `[base_endpoint]/favorite?user=[user]&book=[book]`
        *   **Key Request Parameters:** `user`, `book`.
    13. **`getBookId(id: string): Observable<Book>`**
        *   **Purpose:** Retrieves a specific book by its ID.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/id?id=[id]`
        *   **Key Request Parameters:** `id`.
    14. **`getBookByPath(path: string): Observable<Book>`**
        *   **Purpose:** Retrieves a specific book by its file path.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/path?path=[path]`
        *   **Key Request Parameters:** `path`.
    15. **`getSent(user: string): Observable<any>`**
        *   **Purpose:** Retrieves a list of books "sent" by/to a user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/sent?user=[user]`
        *   **Key Request Parameters:** `user`.
    16. **`view(book: string, user: string): Observable<any>`**
        *   **Purpose:** Marks a book as "viewed" by a user.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/view?user=[user]&book=[book]`
        *   **Key Request Parameters:** `user`, `book`.
        *   **Request Body:** `null`.
    17. **`getLanguages(): Observable<any>`**
        *   **Purpose:** Retrieves a list of available languages for books.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/languages`.
    18. **`getImage(path: string): Observable<any>`**
        *   **Purpose:** Retrieves the cover image for a book given its path.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/image?path=[path]`
        *   **Key Request Parameters:** `path`.
    19. **`deleteBook(id: string): Observable<any>`**
        *   **Purpose:** Deletes a book by its ID.
        *   **HTTP Method:** `DELETE`
        *   **API Endpoint Called:** `[base_endpoint]/delete?id=[id]`
        *   **Key Request Parameters:** `id`.
    20. **`editBook(book: Book): Observable<any>`**
        *   **Purpose:** Updates the details of an existing book.
        *   **HTTP Method:** `PUT`
        *   **API Endpoint Called:** `[base_endpoint]/edit`
        *   **Request Body:** `book` (type `Book`).

### C. ConfigService

*   **Base Endpoint:** `environment.endpoint + "config"`
*   **Functions:**
    1.  **`get(key: string): Observable<Config>`**
        *   **Purpose:** Retrieves a configuration value associated with a specific key.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/get?key=[key]`
        *   **Key Request Parameters:** `key` (string).
    2.  **`save(configs: Config[]): Observable<any>`**
        *   **Purpose:** Saves an array of configuration objects.
        *   **HTTP Method:** `PUT`
        *   **API Endpoint Called:** `[base_endpoint]/save`
        *   **Request Body:** `configs` (an array of `Config` objects).

### D. LoginService

*   **Base Endpoint:** `environment.endpoint + "login"`
*   **Functions:**
    1.  **`login(user: User)`**
        *   **Purpose:** Attempts to log in a user by sending user credentials to the backend.
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]`
        *   **Request Body:** `user` (type `User`).
        *   **Notes:** Sets `Content-Type` header to `application/json`. Uses `observe: 'response'` to get the full HTTP response, likely to extract authorization token from headers.

### E. MailService

*   **Base Endpoint:** `environment.endpoint + "mail"`
*   **Functions:**
    1.  **`sendTestMail(address: string): Observable<any>`**
        *   **Purpose:** Sends a test email to the specified email address.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/test?address=[address]`
        *   **Key Request Parameters:** `address` (string).
    2.  **`sendMail(path: string, address: string): Observable<any>`**
        *   **Purpose:** Sends an email, likely with content related to the provided `path`, to the specified address.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/send?path=[encoded_path]&address=[address]`
        *   **Key Request Parameters:** `path` (string), `address` (string).
        *   **Notes:** `path` parameter undergoes custom character encoding (`&` to `@_@`, `[` to `@-@`, `]` to `@¡@`, `` ` `` to `@!@`).

### F. MetadataService

*   **Base Endpoint:** `environment.endpoint + "metadata"`
*   **Functions:**
    1.  **`start(lang: string, type: string, entity: string): Observable<any>`**
        *   **Purpose:** Initiates a metadata-related process or task on the backend.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/start?lang=[lang]&type=[type]&entity=[entity]`
        *   **Key Request Parameters:** `lang`, `type`, `entity`.
    2.  **`getDataStatus(): Observable<any>`**
        *   **Purpose:** Retrieves the status of a data processing task.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/status`
        *   **Notes:** Sends `ignoreLoadingBar: ''` header, likely to prevent a global loading bar for polling requests.
    3.  **`stop(): Observable<any>`**
        *   **Purpose:** Stops an ongoing metadata-related process or task.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/stop`.
    4.  **`findAuthor(lang: string, author: string): Observable<any>`**
        *   **Purpose:** Searches for or retrieves metadata for a specific author.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/author?lang=[lang]&author=[author]`
        *   **Key Request Parameters:** `lang`, `author`.
    5.  **`findBook(book: string, lang: string): Observable<any>`**
        *   **Purpose:** Searches for or retrieves metadata for a specific book.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/book?book=[book]&lang=[lang]`
        *   **Key Request Parameters:** `book`, `lang`.

### G. NotificationService

*   **Base Endpoint:** `environment.endpoint + "notification"`
*   **Functions:**
    1.  **`findAll(): Observable<any>`**
        *   **Purpose:** Retrieves all notifications.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/all`.
    2.  **`findAllNotRead(): Observable<any>`**
        *   **Purpose:** Retrieves all notifications that have not been read.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/not_read`.
    3.  **`findAllByUser(user: string): Observable<any>`**
        *   **Purpose:** Retrieves all notifications for a specific user.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/user?user=[user]`
        *   **Key Request Parameters:** `user`.
    4.  **`save(notification: Notif): Observable<any>`**
        *   **Purpose:** Saves a notification (create or update).
        *   **HTTP Method:** `PUT`
        *   **API Endpoint Called:** `[base_endpoint]/save`
        *   **Request Body:** `notification` (type `Notif`).
    5.  **`read(id: string, user: string): Observable<any>`**
        *   **Purpose:** Marks a specific notification as read for a particular user.
        *   **HTTP Method:** `GET` (Note: Modifies state but uses GET).
        *   **API Endpoint Called:** `[base_endpoint]/read?id=[id]&user=[user]`
        *   **Key Request Parameters:** `id`, `user`.
    6.  **`delete(id: string): Observable<any>`**
        *   **Purpose:** Deletes a specific notification by its ID.
        *   **HTTP Method:** `DELETE`
        *   **API Endpoint Called:** `[base_endpoint]/delete?id=[id]`
        *   **Key Request Parameters:** `id`.

### H. SerieService

*   **Base Endpoint:** `environment.endpoint + "serie"`
*   **Functions:**
    1.  **`count(languages: string[]): Observable<any>`**
        *   **Purpose:** Retrieves the total count of series, potentially filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/count?languages=[languages_list]`
        *   **Key Request Parameters:** `languages` (array, comma-separated).
    2.  **`getAll(languages: string[], page: number, size: number, sort: string, order: string): Observable<any>`**
        *   **Purpose:** Fetches a paginated and sorted list of series, filtered by language(s).
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/all?languages=[languages_list]&page=[page]&size=[size]&sort=[sort]&order=[order]`
        *   **Key Request Parameters:** `languages` (array, comma-separated), `page`, `size`, `sort`, `order`.
    3.  **`getCover(serie: string): Observable<any>`**
        *   **Purpose:** Retrieves the cover image for a specific series.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/cover?serie=[encoded_serie]`
        *   **Key Request Parameters:** `serie` (string).
        *   **Notes:** `serie` parameter undergoes custom character encoding (`&` to `@_@`, `[` to `@-@`, `]` to `@¡@`, `` ` `` to `@!@`).

### I. TagService

*   **Base Endpoint:** `environment.endpoint + "tag"`
*   **Functions:**
    1.  **`getAll(languages: string[], sort: string, order: string): Observable<any>`**
        *   **Purpose:** Retrieves all tags, potentially filtered by language(s) and sorted.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/all?languages=[languages_list]&sort=[sort]&order=[order]`
        *   **Key Request Parameters:** `languages` (array, comma-separated), `sort`, `order`.
    2.  **`rename(source: number, target: string): Observable<any>`**
        *   **Purpose:** Renames a tag.
        *   **HTTP Method:** `GET` (Note: Modifies data but uses GET).
        *   **API Endpoint Called:** `[base_endpoint]/rename?source=[source]&target=[target]`
        *   **Key Request Parameters:** `source` (tag ID), `target` (new tag name).
    3.  **`merge(source: number, target: number): Observable<any>`**
        *   **Purpose:** Merges two tags.
        *   **HTTP Method:** `GET` (Note: Modifies data but uses GET).
        *   **API Endpoint Called:** `[base_endpoint]/merge?source=[source]&target=[target]`
        *   **Key Request Parameters:** `source` (ID of tag to merge), `target` (ID of tag to merge into).
    4.  **`saveImage(source: number, image: string): Observable<any>`**
        *   **Purpose:** Associates an image URL with a tag.
        *   **HTTP Method:** `GET` (Note: Modifies data but uses GET).
        *   **API Endpoint Called:** `[base_endpoint]/image?source=[source]&image=[image]`
        *   **Key Request Parameters:** `source` (tag ID), `image` (image URL/path).
    5.  **`updateImage(source: number): Observable<any>`**
        *   **Purpose:** Updates the image associated with a tag (likely triggers a backend fetch based on tag info).
        *   **HTTP Method:** `GET` (Note: Modifies data but uses GET).
        *   **API Endpoint Called:** `[base_endpoint]/image/update?source=[source]`
        *   **Key Request Parameters:** `source` (tag ID).

### J. UserService

*   **Base Endpoint:** `environment.endpoint + "user"`
*   **Functions:**
    1.  **`getAll(): Observable<any>`**
        *   **Purpose:** Retrieves all users.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/getAll`.
    2.  **`get(username: string): Observable<User>`**
        *   **Purpose:** Retrieves a specific user by their username.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/get?username=[username]`
        *   **Key Request Parameters:** `username`.
    3.  **`update(user: User): Observable<any>`**
        *   **Purpose:** Updates an existing user's information.
        *   **HTTP Method:** `PUT`
        *   **API Endpoint Called:** `[base_endpoint]/update`
        *   **Request Body:** `user` (type `User`).
    4.  **`save(user: User): Observable<any>`**
        *   **Purpose:** Creates a new user or saves changes (typically for creation with POST).
        *   **HTTP Method:** `POST`
        *   **API Endpoint Called:** `[base_endpoint]/save`
        *   **Request Body:** `user` (type `User`).
    5.  **`delete(id: string): Observable<any>`**
        *   **Purpose:** Deletes a user by their ID.
        *   **HTTP Method:** `DELETE`
        *   **API Endpoint Called:** `[base_endpoint]/delete?id=[id]`
        *   **Key Request Parameters:** `id`.
    6.  **`getById(id: string): Observable<User>`**
        *   **Purpose:** Retrieves a specific user by their ID.
        *   **HTTP Method:** `GET`
        *   **API Endpoint Called:** `[base_endpoint]/getById?id=[id]`
        *   **Key Request Parameters:** `id`.

## III. Components Analysis

### Components Analyzed in Detail

#### A. BooksComponent

*   **Main Data Displayed/Managed:** List of favorite books, main list of books (search results or all), view title, author information (if applicable), total book count.
*   **Data Fetching & Service Usage:**
    *   `BookService`: `count()` for total, `getAll()` for paginated list, `getFavorites()` for user's favorites.
    *   `AuthorService`: `getByName()` for opening author details.
    *   User data from `sessionStorage`. Route query parameters for `adv_search`.
*   **User Interactions Handled:** Infinite scrolling (loads more books), sorting, opening book details (dialog), opening author details (dialog), scroll-to-top.
*   **Child Components Used:**
    *   `app-detail` (via `DetailComponent` ViewChild): Shows book details in a dialog. Handles close, open, author navigation, book data refresh, and book deletion events.
    *   `app-author` (via `AuthorComponent` ViewChild): Shows author details in a dialog. Handles close, open, and book selection events.
*   **Input/Output:** Receives search parameters via route. Outputs actions to child dialogs.

#### B. DetailComponent

*   **Main Data Displayed/Managed:** Selected book's full details (title, author, description, cover, series, tags, pubDate, language, pages, rating), lists of related books (series, similar, recommendations), reviews, Kindle send status, favorite status, EPUB viewer, edit form.
*   **Data Fetching & Service Usage:**
    *   `BookService`: `getImage`, `getSerie`, `getSimilar`, `getRecommendationsByBook`, `getFavorite`, `addFavorite`, `deleteFavorite`, `view`, `getEpub`, `deleteBook`, `editBook`.
    *   `MailService`: `sendMail` (for Kindle).
    *   `ConfigService`: `get("smtp.status")` (for Kindle availability).
    *   `MetadataService`: `findBook` (to refresh metadata).
    *   `NotificationService`: `save` (for Kindle send status notifications).
    *   Receives initial book data via `showDetails(book)` method (from parent or route params).
*   **User Interactions Handled:** Closing detail view, downloading EPUB, viewing EPUB (with controls), sending to Kindle, add/remove favorite, opening author page, navigating to other books (series, similar, recommendations, tags), editing book details, deleting book, refreshing metadata, expanding/collapsing sections.
*   **Child Components Used:** None directly, but it is used as a child by `BooksComponent`, `AuthorsComponent`, `RecommendationsComponent`.
*   **Input/Output:**
    *   **Input:** Effectively `selected: Book`, populated by `showDetails(book)`.
    *   **Output Events:** `eventAuthor` (string - author name), `eventBook` (Book - updated book), `deleteBookEvent` (string - bookId), `eventClose` (void), `eventOpen` (void).

#### C. SearchComponent

*   **Main Data Displayed/Managed:** Search form with fields for title, author, publication date range, page range, tags, and series.
*   **Data Fetching & Service Usage:**
    *   `TagService`: `getAll()` to populate the tags multi-select dropdown.
    *   User data from `sessionStorage` (for language preferences for tags).
*   **User Interactions Handled:** Filling search fields.
*   **Action Taken on Search:** `doSearch()` method navigates to `BooksComponent`, passing the `Search` object (all criteria) as a JSON string in the `adv_search` query parameter.
*   **Child Components Used:** None.
*   **Input/Output:** Outputs search criteria via route navigation.

#### D. AuthorsComponent

*   **Main Data Displayed/Managed:** List of favorite authors, main list of all authors (image, name, book count), view title with total author count.
*   **Data Fetching & Service Usage:**
    *   `AuthorService`: `count()`, `getAll()`, `getFavorites()`, `getByName()`.
    *   User data from `sessionStorage`.
*   **User Interactions Handled:** Infinite scrolling, sorting authors, opening author details (dialog), opening book details from author context (dialog).
*   **Child Components Used:**
    *   `app-author` (via `AuthorComponent` ViewChild): Shows individual author details in a dialog. Manages fetching author's books, favorite status for the author, metadata refresh.
    *   `app-detail` (via `DetailComponent` ViewChild): Shows book details if a book is selected from the `app-author` dialog.
*   **Input/Output:** None directly.

#### E. SeriesComponent

*   **Main Data Displayed/Managed:** List of series (cover image, name, book count), view title with total series count.
*   **Data Fetching & Service Usage:**
    *   `SerieService`: `count()`, `getAll()`, `getCover()` (for each series).
    *   User data from `sessionStorage`.
*   **User Interactions Handled:** Infinite scrolling, sorting series, navigating to `BooksComponent` to show books within a selected series.
*   **Child Components Used:** None.
*   **Input/Output:** None directly.

#### F. ProfileComponent

*   **Main Data Displayed/Managed:** User account form (username, password, app language, Kindle email, preferred book languages), list of "Sent Books".
*   **Data Fetching & Service Usage:**
    *   `UserService`: `get()` (for admin edit), `update()`, `save()` (for new user).
    *   `BookService`: `getLanguages()` (for book language options), `getSent()` (for sent books list).
*   **User Interactions Handled:** Editing profile fields, saving profile (create/update), changing app display language, navigating to book/author details from "Sent Books".
*   **User Context Determination:** Route query parameters (`type`, `user`) for admin actions, or `sessionStorage.user` for self-profile.
*   **Child Components Used:** None.
*   **Input/Output:** None directly.

### Components Briefly Reviewed

#### A. CategoriesComponent

*   **Primary Service Dependencies:** `TagService`, `Router`, `MessageService`, `TranslateService`.
*   **Main Data Displayed or Managed:** List of tags/categories (`tags: Tag[]`) with name, book count, image. Admin dialogs for rename/merge/image URL.
*   **Key Actions/Purpose:** Displays tags. Allows navigation to books filtered by tag. Admin functions: rename tag, merge tags, set/update tag image URL.

#### B. LoginComponent

*   **Primary Service Dependencies:** `LoginService`, `UserService`, `Router`, `MessageService`, `TranslateService`.
*   **Main Data Displayed or Managed:** Login form (username, password), "remember me" checkbox.
*   **Key Actions/Purpose:** Authenticates users. On success, fetches user details, stores session, sets language, and navigates. Handles "remember me" with `localStorage`.

#### C. NotificationsComponent

*   **Primary Service Dependencies:** `NotificationService`, `BookService`, `UserService`, `MessageService`, `TranslateService`.
*   **Main Data Displayed or Managed:** Table of notifications (`notifications: Notif[]`) with details (type, user, book title/image, dates, status, error). Filters for the table.
*   **Key Actions/Purpose:** Displays system notifications. Allows filtering and deletion of notifications. Fetches associated book details for context.

#### D. RecommendationsComponent

*   **Primary Service Dependencies:** `BookService`, `AuthorService`, `Router`, `MessageService`, `TranslateService`.
*   **Main Data Displayed or Managed:** List of recommended books (`books: Book[]`) with image, title, author, rating.
*   **Key Actions/Purpose:** Fetches and displays personalized book recommendations. Supports sorting and infinite scrolling. Allows opening book/author details via child dialogs.
*   **Child Components Used:** `app-detail` (ViewChild `DetailComponent`), `app-author` (ViewChild `AuthorComponent`).

#### E. SettingsComponent

*   **Primary Service Dependencies:** `MetadataService`, `ConfigService`, `UserService`, `MailService`, `Router`, `MessageService`, `TranslateService`.
*   **Main Data Displayed or Managed:** Panels for user management (list, add/edit/delete users), global settings (e.g., recommendations count), metadata tasks (Goodreads key, start/stop various metadata processes with progress), SMTP server configuration (for Kindle emails).
*   **Key Actions/Purpose:** Centralized administration panel. Manages users, application configurations, triggers backend metadata jobs, and configures email sending.

## IV. Conclusion

The Indigo Frontend application exhibits a modular architecture typical of Angular applications, with clear separation of concerns between services (handling API interactions and business logic) and components (handling presentation and user interaction).

Key observations include:
*   **Service-Oriented:** Components heavily rely on injectable services to fetch and manipulate data, promoting reusability and testability.
*   **Comprehensive Functionality:** The application covers a wide range of features for managing and exploring a book library, including search, recommendations, user profiles, favorites, series, authors, tags, and administrative settings.
*   **Backend Driven:** Most data is fetched dynamically from a backend API, with services acting as intermediaries.
*   **User Roles & Personalization:** Features like user-specific favorites, recommendations, Kindle integration, and language preferences indicate support for personalized user experiences. Admin roles have extended capabilities for data management and system configuration.
*   **Rich UI Components:** Use of PrimeNG components (dialogs, tables, dropdowns, etc.) provides a rich and interactive user interface.
*   **Modularity in Display:** Components like `DetailComponent` and `AuthorComponent` are reused as dialogs within multiple parent components (`BooksComponent`, `AuthorsComponent`, `RecommendationsComponent`), promoting consistency.
*   **API Design Notes:** Some services use GET requests for operations that modify state (e.g., `TagService` for rename/merge, `NotificationService` for marking as read), which is not strictly RESTful but might be a design choice for simplicity. Several services also use custom encoding for certain parameters.

Overall, the frontend appears well-structured to support its diverse feature set, with a clear pattern of component-service interaction for managing application state and backend communication.
