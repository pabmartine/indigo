import {
  AfterViewInit,
  Directive,
  ElementRef,
  EventEmitter,
  Input,
  HostListener,
  OnChanges,
  OnDestroy,
  Output,
  Renderer2,
  SimpleChanges,
} from '@angular/core';
import { ImageService } from 'src/app/utils/image.service';

@Directive({
  selector: '[appLazyImage]'
})
export class LazyImageDirective implements AfterViewInit, OnDestroy, OnChanges {

  @Input('appLazyImage') processedSource?: string | null;
  @Input() lazySource?: string | null;
  @Input() lazyPlaceholder: string = './assets/images/unknown.jpg';

  @Output() lazyLoaded = new EventEmitter<string>();

  private observer?: IntersectionObserver;
  private hasLoaded = false;
  private showingPlaceholder = false;
  private initialized = false;

  constructor(
    private elementRef: ElementRef<HTMLImageElement>,
    private renderer: Renderer2,
    private imageService: ImageService
  ) {}

  ngAfterViewInit(): void {
    this.initialized = true;
    this.setInitialSource();
    if (!this.hasLoaded) {
      this.observe();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.initialized && changes['lazySource']) {
      this.observer?.disconnect();
      this.hasLoaded = false;
      this.setPlaceholder();
      this.observe();
      return;
    }
    if (changes['processedSource'] && this.processedSource) {
      this.setImage(this.processedSource);
      this.hasLoaded = true;
    }
  }

  private setInitialSource(): void {
    if (this.processedSource) {
      this.setImage(this.processedSource);
      this.hasLoaded = true;
    } else {
      this.setPlaceholder();
    }
  }

  private observe(): void {
    if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
      this.loadImage();
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            this.observer?.disconnect();
            this.loadImage();
          }
        });
      },
      { rootMargin: '100px' }
    );

    this.observer.observe(this.elementRef.nativeElement);
  }

  private loadImage(): void {
    if (this.hasLoaded) {
      return;
    }

    const source = this.resolveSource();
    if (source) {
      this.setImage(source);
      this.hasLoaded = true;
    } else {
      this.setPlaceholder();
    }
  }

  private resolveSource(): string | null {
    if (!this.lazySource) {
      return this.processedSource || null;
    }
    if (/^(https?:|data:|blob:|\.?\.?\/)/.test(this.lazySource)) {
      return this.lazySource;
    }
    return this.imageService.toDataUrlSafe(this.lazySource, this.lazyPlaceholder);
  }

  private setImage(src: string): void {
    this.showingPlaceholder = src === this.lazyPlaceholder;
    if (this.elementRef.nativeElement.getAttribute('src') !== src) {
      this.renderer.setAttribute(this.elementRef.nativeElement, 'src', src);
    }
    this.renderer.setAttribute(this.elementRef.nativeElement, 'loading', 'lazy');
  }

  private setPlaceholder(): void {
    this.showingPlaceholder = true;
    this.renderer.setAttribute(this.elementRef.nativeElement, 'src', this.lazyPlaceholder);
  }

  @HostListener('error')
  onImageError(): void {
    // Updating the DOM directly also works in OnPush views and outside Angular's zone.
    // Do not retry if the placeholder itself is unavailable.
    if (this.showingPlaceholder) return;
    this.hasLoaded = true;
    this.setPlaceholder();
    this.lazyLoaded.emit(this.lazyPlaceholder);
  }

  @HostListener('load')
  onImageLoad(): void {
    if (!this.showingPlaceholder) {
      this.lazyLoaded.emit(this.elementRef.nativeElement.getAttribute('src')!);
    }
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
