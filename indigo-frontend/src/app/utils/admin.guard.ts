import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

@Injectable({ providedIn: 'root' })
export class AdminGuard {
  constructor(
    private router: Router,
    private authState: AuthStateService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (this.authState.isAuthenticated() && this.authState.isAdmin()) {
      return true;
    }

    this.router.navigate(['/books']);
    return false;
  }
}
