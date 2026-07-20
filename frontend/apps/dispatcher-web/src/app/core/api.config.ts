// Trong dev, Angular chạy ở :4300 và gọi thẳng tới api-gateway ở :8080.
export const API_BASE_URL = 'http://localhost:8080';
// SockJS đứng sau gateway, xem backend/api-gateway route "realtime-service-sockjs".
export const WS_SOCKJS_URL = `${API_BASE_URL}/ws-sockjs`;

/**
 * Google Maps JavaScript API key — KHÔNG hardcode giá trị thật ở đây.
 * Anh tự tạo key ở Google Cloud Console (bật "Maps JavaScript API") rồi điền vào,
 * hoặc set biến môi trường build-time NG_APP_GOOGLE_MAPS_API_KEY (xem README).
 */
export const GOOGLE_MAPS_API_KEY = '';
