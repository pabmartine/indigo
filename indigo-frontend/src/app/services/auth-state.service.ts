import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../domain/user';

/**
 * Centralized authentication state management service.
 * Replaces direct sessionStorage access with reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthStateService {
  private readonly USER_STORAGE_KEY = 'user';

  private userSubject: BehaviorSubject<User | null>;
  public user$: Observable<User | null>;

  constructor() {
    // Initialize with current session user or null
    const initialUser = this.loadUserFromStorage();
    this.userSubject = new BehaviorSubject<User | null>(initialUser);
    this.user$ = this.userSubject.asObservable();
  }

  /**
   * Get current user value (synchronous)
   */
  public getCurrentUser(): User | null {
    return this.userSubject.value;
  }

  /**
   * Get user observable for reactive components
   */
  public getUserObservable(): Observable<User | null> {
    return this.user$;
  }

  /**
   * Set user and persist to sessionStorage
   */
  public setUser(user: User | null): Promise<void> {
    return new Promise((resolve) => {
      if (user) {
        try {
          sessionStorage.setItem(this.USER_STORAGE_KEY, JSON.stringify(user));
          this.userSubject.next(user);
        } catch (error) {
          console.error('Failed to save user to sessionStorage:', error);
        }
      } else {
        this.clearUser();
      }
      resolve();
    });
  }

  /**
   * Clear user from state and sessionStorage
   */
  public clearUser(): void {
    sessionStorage.removeItem(this.USER_STORAGE_KEY);
    this.userSubject.next(null);
  }

  /**
   * Check if user is authenticated
   */
  public isAuthenticated(): boolean {
    const user = this.getCurrentUser();
    return user !== null && user !== undefined;
  }

  /**
   * Check if user is admin
   */
  public isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ADMIN';
  }

  /**
   * Get user's language preferences for books
   */
  public getLanguageBooks(): string[] {
    const user = this.getCurrentUser();
    return user?.languageBooks || ['en'];
  }

  /**
   * Get username
   */
  public getUsername(): string | undefined {
    return this.getCurrentUser()?.username;
  }

  /**
   * Update specific user properties without replacing entire object
   */
  public updateUser(updates: Partial<User>): void {
    const currentUser = this.getCurrentUser();
    if (currentUser) {
      const updatedUser = { ...currentUser, ...updates };
      this.setUser(updatedUser);
    }
  }

  /**
   * Load user from sessionStorage with error handling
   */
  private loadUserFromStorage(): User | null {
    try {
      const userJson = sessionStorage.getItem(this.USER_STORAGE_KEY);
      if (userJson) {
        const user = JSON.parse(userJson);
        // Validate that parsed object looks like a User
        if (user && typeof user === 'object') {
          return user as User;
        }
      }
      return null;
    } catch (error) {
      console.error('Failed to load user from sessionStorage:', error);
      // Clear corrupted data
      sessionStorage.removeItem(this.USER_STORAGE_KEY);
      return null;
    }
  }
}
