import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard  {
    constructor(private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {


        if (route.routeConfig.path != 'books' && route.routeConfig.path != 'detail') { //TODO hacer un interceptor específico para los borrados
            sessionStorage.removeItem("position");
        }


        if (sessionStorage.user) {
            return true;
        }



        this.router.navigate(['/login'], {});
        return false;
    }
}