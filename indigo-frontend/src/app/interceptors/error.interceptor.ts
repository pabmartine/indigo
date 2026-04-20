import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { AuthStateService } from '../services/auth-state.service';

/**
 * HTTP Error Interceptor
 *
 * Centralizes error handling for all HTTP requests in the application.
 * Automatically handles common HTTP errors and provides user-friendly feedback.
 *
 * Features:
 * - 401 Unauthorized: Clears user session and redirects to login
 * - 403 Forbidden: Shows permission denied message
 * - 404 Not Found: Shows resource not found message
 * - 500+ Server Errors: Shows generic server error message
 * - Network Errors: Shows connectivity issue message
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(
    private injector: Injector,
    private router: Router,
    private authState: AuthStateService
  ) {}

  private get translate(): TranslateService {
    return this.injector.get(TranslateService);
  }

  private get messageService(): MessageService {
    return this.injector.get(MessageService);
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.handleError(error);
      })
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = '';
    let severity: 'error' | 'warn' | 'info' = 'error';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = this.translate.instant('locale.error.network') ||
                     'A network error occurred. Please check your connection.';
      console.error('Client-side error:', error.error.message);
    } else {
      if (this.isRecoverableCurrentUserLookup(error)) {
        return throwError(() => error);
      }

      // Backend returned an unsuccessful response code
      switch (error.status) {
        case 0:
          // Network error - no response from server
          errorMessage = this.translate.instant('locale.error.no_connection') ||
                        'Cannot connect to server. Please check your internet connection.';
          console.error('Network error: No connection to server');
          break;

        case 401:
          // Unauthorized - clear session and redirect to login
          errorMessage = this.translate.instant('locale.error.unauthorized') ||
                        'Your session has expired. Please login again.';
          console.warn('401 Unauthorized - Clearing session');
          this.authState.clearUser();
          this.router.navigate(['/login']);
          severity = 'warn';
          break;

        case 403:
          // Forbidden - user doesn't have permission
          errorMessage = this.translate.instant('locale.error.forbidden') ||
                        'You do not have permission to perform this action.';
          console.warn('403 Forbidden:', error.url);
          severity = 'warn';
          break;

        case 404:
          // Not Found
          errorMessage = this.translate.instant('locale.error.not_found') ||
                        'The requested resource was not found.';
          console.warn('404 Not Found:', error.url);
          severity = 'warn';
          break;

        case 408:
          // Request Timeout
          errorMessage = this.translate.instant('locale.error.timeout') ||
                        'The request took too long. Please try again.';
          console.warn('408 Request Timeout:', error.url);
          break;

        case 409:
          // Conflict
          errorMessage = this.translate.instant('locale.error.conflict') ||
                        'The request conflicts with the current state of the resource.';
          console.warn('409 Conflict:', error.url);
          severity = 'warn';
          break;

        case 422:
          // Unprocessable Entity - validation error
          errorMessage = this.translate.instant('locale.error.validation') ||
                        'The data provided is invalid. Please check and try again.';
          console.warn('422 Unprocessable Entity:', error.error);
          severity = 'warn';
          break;

        case 429:
          // Too Many Requests
          errorMessage = this.translate.instant('locale.error.rate_limit') ||
                        'Too many requests. Please wait a moment and try again.';
          console.warn('429 Too Many Requests:', error.url);
          severity = 'warn';
          break;

        case 500:
          // Internal Server Error
          errorMessage = this.translate.instant('locale.error.server') ||
                        'An internal server error occurred. Please try again later.';
          console.error('500 Internal Server Error:', error.url);
          break;

        case 502:
          // Bad Gateway
          errorMessage = this.translate.instant('locale.error.bad_gateway') ||
                        'The server is temporarily unavailable. Please try again later.';
          console.error('502 Bad Gateway:', error.url);
          break;

        case 503:
          // Service Unavailable
          errorMessage = this.translate.instant('locale.error.service_unavailable') ||
                        'The service is temporarily unavailable. Please try again later.';
          console.error('503 Service Unavailable:', error.url);
          break;

        case 504:
          // Gateway Timeout
          errorMessage = this.translate.instant('locale.error.gateway_timeout') ||
                        'The server took too long to respond. Please try again.';
          console.error('504 Gateway Timeout:', error.url);
          break;

        default:
          // Generic error
          if (error.status >= 500) {
            errorMessage = this.translate.instant('locale.error.server') ||
                          'A server error occurred. Please try again later.';
            console.error(`Server error ${error.status}:`, error.url, error.message);
          } else if (error.status >= 400) {
            errorMessage = this.translate.instant('locale.error.client') ||
                          'An error occurred processing your request.';
            console.warn(`Client error ${error.status}:`, error.url, error.message);
          } else {
            errorMessage = this.translate.instant('locale.error.unknown') ||
                          'An unexpected error occurred.';
            console.error('Unknown error:', error);
          }
      }
    }

    // Show error message to user (skip for 401 as we're redirecting)
    if (error.status !== 401) {
      this.messageService.clear();
      this.messageService.add({
        severity: severity,
        summary: severity === 'error' ? 'Error' : 'Warning',
        detail: errorMessage,
        life: 5000,
        closable: true
      });
    }

    // Log full error details in development
    if (!this.isProduction()) {
      console.error('HTTP Error Details:', {
        status: error.status,
        statusText: error.statusText,
        url: error.url,
        message: error.message,
        error: error.error
      });
    }

    // Return error to allow component-level handling if needed
    return throwError(() => error);
  }

  private isRecoverableCurrentUserLookup(error: HttpErrorResponse): boolean {
    return !!error.url &&
           /\/api\/user\/me(?:\?|$)/.test(error.url) &&
           (error.status === 404 || error.status === 500);
  }

  private isProduction(): boolean {
    // Check if we're in production mode
    return typeof window !== 'undefined' &&
           (window.location.hostname !== 'localhost' &&
            window.location.hostname !== '127.0.0.1');
  }
}
