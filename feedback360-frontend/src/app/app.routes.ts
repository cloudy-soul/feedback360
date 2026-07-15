import { Routes } from '@angular/router';
import { authGuard, adminGuard, managerGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'my-modules',
    canActivate: [authGuard],
    loadComponent: () => import('./features/employee/my-modules/my-modules.component').then(m => m.MyModulesComponent)
  },
  {
    path: 'feedback/submit',
    canActivate: [authGuard],
    loadComponent: () => import('./features/employee/feedback-form/feedback-form.component').then(m => m.FeedbackFormComponent)
  },
  {
    path: 'feedback/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/employee/feedback-detail/feedback-detail.component').then(m => m.FeedbackDetailComponent)
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'users', loadComponent: () => import('./features/admin/users/users.component').then(m => m.UsersComponent) },
      { path: 'logs', loadComponent: () => import('./features/admin/logs/logs.component').then(m => m.LogsComponent) },
      { path: 'settings', loadComponent: () => import('./features/admin/settings/settings.component').then(m => m.SettingsComponent) },
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  },
  {
    path: 'manager',
    canActivate: [authGuard, managerGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/manager/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'feedbacks', loadComponent: () => import('./features/manager/feedbacks/feedbacks.component').then(m => m.FeedbacksComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
