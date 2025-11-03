import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService, TranslationChangeEvent } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api/menuitem';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthStateService } from 'src/app/services/auth-state.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit, OnDestroy {

  @Input() show: boolean;

  items: MenuItem[];
  others: MenuItem[];

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    public translate: TranslateService,
    private authState: AuthStateService
  ) { }

  ngOnInit(): void {
    this.translate.onTranslationChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        (event: TranslationChangeEvent) => {
          this.items = this.buildMenu();
          this.others = this.buildOthers();
        },
      );

    this.items = this.buildMenu();
    this.others = this.buildOthers();

  }



  buildMenu(): MenuItem[] {
    const menu: MenuItem[] = [

      {
        label: this.translate.instant('locale.sidebar.menu.recommendations'),
        icon: 'menu-item-icon pi pi-star',
        routerLink: ['/recommendations'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },{
        label: this.translate.instant('locale.sidebar.menu.books'),
        icon: 'menu-item-icon pi pi-book',
        routerLink: ['/books'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {
        label: this.translate.instant('locale.sidebar.menu.authors'),
        icon: 'menu-item-icon pi pi-user-edit',
        routerLink: ['/authors'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {
        label: this.translate.instant('locale.sidebar.menu.tags'),
        icon: 'menu-item-icon pi pi-tags',
        routerLink: ['/categories'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {
        label: this.translate.instant('locale.sidebar.menu.series'),
        icon: 'menu-item-icon pi pi-th-large',
        routerLink: ['/series'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {separator:true},
      {
        label: this.translate.instant('locale.sidebar.menu.search'),
        icon: 'menu-item-icon pi pi-search-plus',
        routerLink: ['/search'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {
        label: this.translate.instant('locale.sidebar.menu.notifications'),
        icon: 'menu-icon pi pi-bell',
        routerLink: ['/notifications'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false,
        visible: this.isAdmin()
      },
      {
        label: this.translate.instant('locale.sidebar.menu.settings'),
        icon: 'menu-icon pi pi-cog',
        routerLink: ['/settings'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false,
        visible: this.isAdmin()
      },
    ];
    return menu;
  }

  buildOthers(): MenuItem[] {
    const menu: MenuItem[] = [
      {
        label: this.translate.instant('locale.header.menu.profile'),
        icon: 'menu-icon pi pi-user',
        routerLink: ['/profile'],
        routerLinkActiveOptions: { exact: true },
        command: () => this.show = false
      },
      {
        label: this.translate.instant('locale.header.menu.logout'), icon: 'menu-icon pi pi-sign-out', command: () => this.logout()
      }    ];
    return menu;
  }

  logout(){
    this.authState.clearUser();
    this.router.navigate(['/login']);
  }

  isAdmin(){
    return this.authState.isAdmin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
