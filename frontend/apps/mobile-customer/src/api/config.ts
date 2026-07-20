// Android emulator: dùng 10.0.2.2 để trỏ về localhost máy dev.
// Thiết bị thật: đổi thành IP LAN của máy chạy docker-compose (ví dụ http://192.168.1.10:8080).
export const API_BASE_URL = 'http://10.0.2.2:8080';
export const WS_BASE_URL = 'ws://10.0.2.2:8080';
