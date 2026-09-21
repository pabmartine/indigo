import { fakeAsync, tick } from '@angular/core/testing';
import { CatalogViewport } from './catalog-viewport';

describe('CatalogViewport', () => {
  let grid: HTMLDivElement;
  let sentinel: HTMLDivElement;
  let viewport: CatalogViewport;
  let callback: IntersectionObserverCallback;
  let observer: any;
  const original = window.IntersectionObserver;

  beforeEach(() => {
    grid = document.createElement('div');
    grid.style.cssText = 'display:grid; grid-template-columns:repeat(7, 200px);';
    sentinel = document.createElement('div');
    document.body.append(grid, sentinel);
    observer = { observe: jasmine.createSpy(), unobserve: jasmine.createSpy(), disconnect: jasmine.createSpy() };
    (window as any).IntersectionObserver = function(cb: IntersectionObserverCallback) { callback = cb; return observer; };
  });
  afterEach(() => {
    viewport?.destroy();
    grid.remove();
    sentinel.remove();
    window.IntersectionObserver = original;
  });

  it('fills the actual grid height with complete rows plus a spare row', () => {
    spyOn(grid, 'getBoundingClientRect').and.returnValue({ top: window.innerHeight - 600 } as DOMRect);
    viewport = new CatalogViewport(grid, sentinel, () => {});
    expect(viewport.pageSize(185)).toBe(35);
    grid.style.gridTemplateColumns = 'repeat(2, 140px)';
    expect(viewport.pageSize(185)).toBe(10);
  });

  it('rechecks a sentinel that stayed visible during a request and stops on destruction', fakeAsync(() => {
    const more = jasmine.createSpy();
    viewport = new CatalogViewport(grid, sentinel, more);
    viewport.start();
    callback([{ isIntersecting: true } as IntersectionObserverEntry], observer);
    expect(more).toHaveBeenCalledTimes(1);
    viewport.refresh();
    tick(20);
    expect(observer.unobserve).toHaveBeenCalledWith(sentinel);
    expect(observer.observe).toHaveBeenCalledTimes(2);
    viewport.refresh();
    viewport.destroy();
    tick(20);
    expect(observer.observe).toHaveBeenCalledTimes(2);
  }));
});
