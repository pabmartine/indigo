import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LazyImageDirective } from './lazy-image.directive';

@Component({ template: `<img [appLazyImage]="source" [lazySource]="lazySource" [lazyPlaceholder]="placeholder" (lazyLoaded)="source = $event">` })
class ImageHost {
  source = '/missing-cover';
  lazySource?: string;
  placeholder = './assets/images/avatar3.jpg';
}

describe('LazyImageDirective fallback', () => {
  let fixture: ComponentFixture<ImageHost>;
  beforeEach(() => {
    TestBed.configureTestingModule({ declarations: [ImageHost, LazyImageDirective] });
    fixture = TestBed.createComponent(ImageHost);
    fixture.detectChanges();
  });
  afterEach(() => fixture.destroy());

  it('replaces a broken cover immediately, without waiting for change detection', () => {
    const img: HTMLImageElement = fixture.nativeElement.querySelector('img');
    img.dispatchEvent(new Event('error'));
    expect(img.getAttribute('src')).toBe(fixture.componentInstance.placeholder);
    expect(fixture.componentInstance.source).toBe(fixture.componentInstance.placeholder);
    fixture.detectChanges();
    img.dispatchEvent(new Event('error'));
    expect(img.getAttribute('src')).toBe(fixture.componentInstance.placeholder);
  });

  it('allows a fresh cover after displaying the fallback', () => {
    const img: HTMLImageElement = fixture.nativeElement.querySelector('img');
    img.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    fixture.componentInstance.source = '/updated-cover';
    fixture.detectChanges();
    expect(img.getAttribute('src')).toBe('/updated-cover');
    img.dispatchEvent(new Event('error'));
    expect(img.getAttribute('src')).toBe(fixture.componentInstance.placeholder);
  });
  it('loads a changed lazy URL after the previous URL failed', () => {
    let intersect: IntersectionObserverCallback;
    const original = window.IntersectionObserver;
    (window as any).IntersectionObserver = function(callback: IntersectionObserverCallback) {
      intersect = callback;
      return { observe() {}, disconnect() {} };
    };
    try {
      const img: HTMLImageElement = fixture.nativeElement.querySelector('img');
      img.dispatchEvent(new Event('error'));
      fixture.detectChanges();
      fixture.componentInstance.lazySource = '/new-lazy-cover';
      fixture.detectChanges();
      intersect!([{ isIntersecting: true } as IntersectionObserverEntry], {} as IntersectionObserver);
      expect(img.getAttribute('src')).toBe('/new-lazy-cover');
    } finally {
      window.IntersectionObserver = original;
    }
  });

});
