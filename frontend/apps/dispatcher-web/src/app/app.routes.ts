import { Route } from '@angular/router';
import { Login } from './features/login/login';
import { DispatchBoard } from './features/dispatch-board/dispatch-board';
import { authGuard } from './core/auth-guard';

export const appRoutes: Route[] = [
  { path: 'login', component: Login },
  { path: 'dispatch', component: DispatchBoard, canActivate: [authGuard] },
  { path: '', redirectTo: 'dispatch', pathMatch: 'full' },
  { path: '**', redirectTo: 'dispatch' },
];
