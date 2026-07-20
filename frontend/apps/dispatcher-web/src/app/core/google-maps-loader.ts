import { GOOGLE_MAPS_API_KEY } from './api.config';

let loadPromise: Promise<void> | null = null;

/** Chèn script Google Maps JS API động, chỉ 1 lần, đọc key từ api.config.ts (không hardcode trong index.html). */
export function loadGoogleMaps(): Promise<void> {
  if (loadPromise) {
    return loadPromise;
  }

  loadPromise = new Promise((resolve, reject) => {
    if (!GOOGLE_MAPS_API_KEY) {
      reject(new Error('Chưa cấu hình GOOGLE_MAPS_API_KEY trong core/api.config.ts'));
      return;
    }
    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_API_KEY}`;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Không tải được Google Maps JS API'));
    document.head.appendChild(script);
  });

  return loadPromise;
}
