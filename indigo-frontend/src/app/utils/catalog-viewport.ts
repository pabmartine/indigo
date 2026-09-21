/** Measures the actual grid and rechecks pagination after layout changes. */
export class CatalogViewport {
  private intersection?: IntersectionObserver;
  private resize?: ResizeObserver;
  private frame?: number;
  private stopped = false;

  constructor(private grid: HTMLElement, private sentinel: HTMLElement,
              private loadMore: () => void) {}

  pageSize(rowHeight: number): number {
    const style = getComputedStyle(this.grid);
    const columns = Math.max(1, style.gridTemplateColumns.split(' ').filter(Boolean).length);
    const visibleHeight = Math.max(rowHeight, window.innerHeight - this.grid.getBoundingClientRect().top);
    // One spare row avoids an empty bottom edge. Keep this size for the whole pagination session.
    return Math.min(200, Math.max(10, columns * (Math.ceil(visibleHeight / rowHeight) + 1)));
  }

  start(): void {
    this.intersection = new IntersectionObserver(entries => {
      if (entries.some(entry => entry.isIntersecting)) this.loadMore();
    }, { rootMargin: '200px 0px' });
    this.intersection.observe(this.sentinel);
    this.resize = new ResizeObserver(() => this.refresh());
    this.resize.observe(this.grid);
    window.addEventListener('resize', this.refresh);
  }

  refresh = (): void => {
    if (this.stopped || this.frame !== undefined) return;
    this.frame = requestAnimationFrame(() => {
      this.frame = undefined;
      // Re-observe even when the sentinel never left the viewport during the request.
      this.intersection?.unobserve(this.sentinel);
      this.intersection?.observe(this.sentinel);
    });
  };

  destroy(): void {
    this.stopped = true;
    if (this.frame !== undefined) cancelAnimationFrame(this.frame);
    this.intersection?.disconnect();
    this.resize?.disconnect();
    window.removeEventListener('resize', this.refresh);
  }
}
