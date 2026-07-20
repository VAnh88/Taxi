import { Route } from '@angular/router';
import { Login } from './features/login/login';
import { DriverList } from './features/drivers/driver-list';
import { TripList } from './features/trips/trip-list';
import { authGuard } from './core/auth-guard';

export const appRoutes: Route[] = [
  { path: 'login', component: Login },
  { path: 'drivers', component: DriverList, canActivate: [authGuard] },
  { path: 'trips', component: TripList, canActivate: [authGuard] },
  { path: '', redirectTo: 'drivers', pathMatch: 'full' },
  { path: '**', redirectTo: 'drivers' },
];
