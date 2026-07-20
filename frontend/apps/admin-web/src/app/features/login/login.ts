import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth-service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  template: `
    <div style="max-width: 320px; margin: 80px auto; padding: 24px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,.1);">
      <h2>Đăng nhập Admin</h2>
      <form (ngSubmit)="submit()">
        <div style="margin-bottom: 12px;">
          <label>Tên đăng nhập</label><br />
          <input name="username" [(ngModel)]="username" style="width: 100%; padding: 8px;" />
        </div>
        <div style="margin-bottom: 12px;">
          <label>Mật khẩu</label><br />
          <input name="password" type="password" [(ngModel)]="password" style="width: 100%; padding: 8px;" />
        </div>
        <button type="submit" style="width: 100%; padding: 10px; background: #1a73e8; color: #fff; border: none; border-radius: 4px;">
          Đăng nhập
        </button>
        @if (error()) {
          <p style="color: #d33; margin-top: 12px;">{{ error() }}</p>
        }
      </form>
    </div>
  `,
})
export class Login {
  username = '';
  password = '';
  error = signal('');

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error.set('');
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/drivers']),
      error: () => this.error.set('Sai tài khoản hoặc mật khẩu'),
    });
  }
}
