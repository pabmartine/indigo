import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LayoutRoutes } from './layout.routing';
import { RouterModule } from '@angular/router';
import { DatePipe } from '@angular/common';

/**
 * Layout Module
 *
 * This module now serves as a routing shell that lazy loads feature modules.
 * All page components have been moved to their respective feature modules.
 */
@NgModule({
  declarations: [
    // All components moved to feature modules
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(LayoutRoutes)
  ],
  providers: [
    DatePipe
  ]
})
export class LayoutModule { }
