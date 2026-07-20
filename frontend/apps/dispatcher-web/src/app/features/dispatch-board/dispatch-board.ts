import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { GoogleMapsModule } from '@angular/google-maps';
import {
  CreateTripRequest,
  Driver,
  Trip,
  TRIP_STATUS_LABEL_VI,
} from '@org/shared-types';
import { API_BASE_URL } from '../../core/api.config';
import { RealtimeService } from '../../core/realtime-service';
import { loadGoogleMaps } from '../../core/google-maps-loader';

const DEFAULT_CENTER: google.maps.LatLngLiteral = { lat: 21.3187, lng: 105.5908 };

@Component({
  selector: 'app-dispatch-board',
  imports: [CommonModule, FormsModule, GoogleMapsModule],
  templateUrl: './dispatch-board.html',
})
export class DispatchBoard implements OnInit {
  mapReady = signal(false);
  mapCenter = DEFAULT_CENTER;
  mapZoom = 13;
  mapOptions: google.maps.MapOptions = { disableDefaultUI: false };

  drivers = signal<Driver[]>([]);
  trips = signal<Trip[]>([]);
  selectedTripId = signal<string | null>(null);
  formError = signal('');
  submitting = signal(false);

  callerPhone = '';
  callerName = '';
  pickupAddress = '';
  pickupLat = '21.3187';
  pickupLng = '105.5908';
  dropoffAddress = '';
  dropoffLat = '21.33';
  dropoffLng = '105.60';

  /** Vị trí xe hiển thị = danh sách ban đầu, ghi đè lat/lng/trạng thái theo sự kiện realtime mới nhất. */
  markers = computed(() => {
    const fleet = this.realtime.fleetLocations();
    return this.drivers()
      .map((d) => {
        const live = fleet.get(d.id);
        const lat = live?.lat ?? d.currentLat;
        const lng = live?.lng ?? d.currentLng;
        if (lat == null || lng == null) return null;
        return {
          driver: d,
          position: { lat, lng } as google.maps.LatLngLiteral,
          shiftStatus: live?.shiftStatus ?? d.shiftStatus,
        };
      })
      .filter((m): m is NonNullable<typeof m> => m !== null);
  });

  pendingTrips = computed(() =>
    this.trips().filter((t) => t.status === 'REQUESTED' || t.status === 'NO_DRIVER_AVAILABLE' || t.status === 'DRIVER_ASSIGNED')
  );

  constructor(private http: HttpClient, private realtime: RealtimeService) {}

  ngOnInit(): void {
    loadGoogleMaps()
      .then(() => this.mapReady.set(true))
      .catch((e) => this.formError.set(e.message));

    this.realtime.connect();
    this.loadDrivers();
    this.loadTrips();

    // Cuốc mới/đổi trạng thái -> làm mới bảng theo dõi.
    setInterval(() => this.loadTrips(), 5000);
  }

  loadDrivers(): void {
    this.http.get<Driver[]>(`${API_BASE_URL}/api/drivers`).subscribe((data) => this.drivers.set(data));
  }

  loadTrips(): void {
    this.http.get<Trip[]>(`${API_BASE_URL}/api/trips`).subscribe((data) => this.trips.set(data));
  }

  statusLabel(status: string): string {
    return TRIP_STATUS_LABEL_VI[status as keyof typeof TRIP_STATUS_LABEL_VI] ?? status;
  }

  submitDispatch(): void {
    this.formError.set('');
    if (!this.callerPhone.trim()) {
      this.formError.set('Cần số điện thoại khách');
      return;
    }
    const body: CreateTripRequest = {
      pickupAddress: this.pickupAddress,
      pickupLat: parseFloat(this.pickupLat),
      pickupLng: parseFloat(this.pickupLng),
      dropoffAddress: this.dropoffAddress,
      dropoffLat: parseFloat(this.dropoffLat),
      dropoffLng: parseFloat(this.dropoffLng),
      sourceChannel: 'DISPATCHER',
      callerPhone: this.callerPhone,
      callerName: this.callerName,
    };
    this.submitting.set(true);
    this.http.post<Trip>(`${API_BASE_URL}/api/trips`, body).subscribe({
      next: () => {
        this.submitting.set(false);
        this.callerPhone = '';
        this.callerName = '';
        this.pickupAddress = '';
        this.dropoffAddress = '';
        this.loadTrips();
      },
      error: () => {
        this.submitting.set(false);
        this.formError.set('Không tạo được lệnh điều xe');
      },
    });
  }

  selectTripForAssign(tripId: string): void {
    this.selectedTripId.set(this.selectedTripId() === tripId ? null : tripId);
  }

  assignDriver(driverId: string): void {
    const tripId = this.selectedTripId();
    if (!tripId) {
      this.formError.set('Chọn 1 cuốc đang chờ trong bảng trước khi bấm vào xe trên bản đồ');
      return;
    }
    this.http.post<Trip>(`${API_BASE_URL}/api/trips/${tripId}/assign`, { driverId }).subscribe({
      next: () => {
        this.selectedTripId.set(null);
        this.loadTrips();
      },
      error: () => this.formError.set('Không gán được tài xế (kiểm tra trạng thái cuốc)'),
    });
  }
}
