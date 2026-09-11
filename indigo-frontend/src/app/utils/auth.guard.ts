import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard  {
    constructor(
        private router: Router,
        private authState: AuthStateService
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {


        const routePath = route.routeConfig?.path;
        if (routePath !== 'books' && routePath !== 'detail') { //TODO hacer un interceptor específico para los borrados
            sessionStorage.removeItem("position");
        }


        if (this.authState.isAuthenticated()) {
            return true;
        }



        this.router.navigate(['/login'], {});
        return false;
    }
}
