import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Trip } from '@org/shared-types';
import { API_BASE_URL } from '../../core/api.config';

@Component({
  selector: 'app-trip-list',
  imports: [RouterLink, CommonModule],
  template: `
    <div style="padding: 24px;">
      <nav style="margin-bottom: 16px;">
        <a routerLink="/drivers">Tài xế</a> | <a routerLink="/trips">Chuyến đi</a>
      </nav>
      <h2>Chuyến đi gần đây ({{ trips().length }})
        <button (click)="reload()" style="margin-left: 8px; padding: 4px 10px;">Làm mới</button>
      </h2>
      <table>
        <thead>
          <tr>
            <th>Điểm đón</th><th>Điểm trả</th><th>Trạng thái</th><th>Kênh</th><th>Giá</th><th>Thời điểm đặt</th>
          </tr>
        </thead>
        <tbody>
          @for (t of trips(); track t.id) {
            <tr>
              <td>{{ t.pickupAddress }}</td>
              <td>{{ t.dropoffAddress }}</td>
              <td>{{ t.status }}</td>
              <td>{{ t.sourceChannel }}</td>
              <td>{{ t.price ?? '-' }}</td>
              <td>{{ t.requestedAt | date: 'short' }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class TripList implements OnInit {
  trips = signal<Trip[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.http.get<Trip[]>(`${API_BASE_URL}/api/trips`).subscribe((data) => this.trips.set(data));
  }
}
