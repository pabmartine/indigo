import {ActivatedRouteSnapshot, DetachedRouteHandle, RouteReuseStrategy} from '@angular/router';

export class CustomReuseStrategy implements RouteReuseStrategy {

   storedRouteHandles = new Map<string, DetachedRouteHandle>();
   
    // Decides if the route should be stored
    shouldDetach(route: ActivatedRouteSnapshot): boolean {

      //If user logout/login force delete all caches
      if (route.routeConfig.path != 'books' && route.routeConfig.path != 'detail') { //TODO esto debería ir aquí?? puedo acceder a esta clases desde el nuevo interceptor d borrado??
         this.storedRouteHandles.clear();
      }

      return route.data.reuseRoute === true;
    }
   
    //Store the information for the route we're destructing
    store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle): void {
       this.storedRouteHandles.set(route.routeConfig.path, handle);
    }
   
   //Return true if we have a stored route object for the next route
    shouldAttach(route: ActivatedRouteSnapshot): boolean {
       return this.storedRouteHandles.has(route.routeConfig.path);
    }
   
    //If we returned true in shouldAttach(), now return the actual route data for restoration
    retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle {
       return this.storedRouteHandles.get(route.routeConfig.path);
    }
   
    //Reuse the route if we're going to and from the same route
    shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
       return future.routeConfig === curr.routeConfig;
    }
   }