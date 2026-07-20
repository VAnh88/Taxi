import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Driver } from '@org/shared-types';
import { API_BASE_URL } from '../../core/api.config';

@Component({
  selector: 'app-driver-list',
  imports: [RouterLink],
  template: `
    <div style="padding: 24px;">
      <nav style="margin-bottom: 16px;">
        <a routerLink="/drivers">Tài xế</a> | <a routerLink="/trips">Chuyến đi</a>
      </nav>
      <h2>Danh sách tài xế ({{ drivers().length }})</h2>
      <table>
        <thead>
          <tr>
            <th>Họ tên</th><th>SĐT</th><th>Đội xe</th><th>Trạng thái</th><th>Ca</th><th>Rating</th>
          </tr>
        </thead>
        <tbody>
          @for (d of drivers(); track d.id) {
            <tr>
              <td>{{ d.fullName }}</td>
              <td>{{ d.phone }}</td>
              <td>{{ d.teamName ?? '-' }}</td>
              <td>{{ d.status }}</td>
              <td>{{ d.shiftStatus }}</td>
              <td>{{ d.rating }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class DriverList implements OnInit {
  drivers = signal<Driver[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Driver[]>(`${API_BASE_URL}/api/drivers`).subscribe((data) => this.drivers.set(data));
  }
}
