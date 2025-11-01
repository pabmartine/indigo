import { Injectable } from '@angular/core';

/**
 * Image Utility Service
 *
 * Provides helper methods for image processing and manipulation.
 */
@Injectable({
  providedIn: 'root'
})
export class ImageService {

  constructor() { }

  /**
   * Converts a base64 string to a data URL for image display
   *
   * @param base64String - The base64 encoded image string
   * @param mimeType - The MIME type of the image (default: 'image/jpeg')
   * @returns The complete data URL string, or null if input is invalid
   *
   * @example
   * const imageUrl = imageService.toDataUrl(base64String);
   * // Returns: 'data:image/jpeg;base64,/9j/4AAQSkZJRg...'
   */
  toDataUrl(base64String: string | null | undefined, mimeType: string = 'image/jpeg'): string | null {
    if (!base64String) {
      return null;
    }

    // If already a data URL, return as-is
    if (base64String.startsWith('data:')) {
      return base64String;
    }

    // Convert base64 string to data URL
    return `data:${mimeType};base64,${base64String}`;
  }

  /**
   * Safely converts a base64 string to a data URL with fallback
   *
   * @param base64String - The base64 encoded image string
   * @param fallback - Fallback value if conversion fails (default: empty string)
   * @param mimeType - The MIME type of the image (default: 'image/jpeg')
   * @returns The complete data URL string or fallback value
   */
  toDataUrlSafe(base64String: string | null | undefined, fallback: string = '', mimeType: string = 'image/jpeg'): string {
    return this.toDataUrl(base64String, mimeType) ?? fallback;
  }
}
