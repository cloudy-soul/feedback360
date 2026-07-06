import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="fb-navbar">
      <a routerLink="/" class="fb-navbar-brand">
        <span class="cap">Capgemini</span>
        <span class="fb-brand-divider"></span>
        <span>FeedBack360</span>
      </a>
      @if (auth.isAdmin()) {
        <a routerLink="/admin/users"    routerLinkActive="active" class="fb-nav-link">Users</a>
        <a routerLink="/admin/logs"     routerLinkActive="active" class="fb-nav-link">Logs</a>
        <a routerLink="/admin/settings" routerLinkActive="active" class="fb-nav-link">Settings</a>
      }
      @if (auth.isManager() && !auth.isAdmin()) {
        <a routerLink="/manager/dashboard" routerLinkActive="active" class="fb-nav-link">Dashboard</a>
        <a routerLink="/manager/feedbacks" routerLinkActive="active" class="fb-nav-link">Feedback</a>
      }
      @if (!auth.isAdmin() && !auth.isManager()) {
        <a routerLink="/my-modules" routerLinkActive="active" class="fb-nav-link">My Modules</a>
      }
      <div class="fb-navbar-right">
        <span class="fb-navbar-user">&#128100; {{ auth.role() }}</span>
        <button class="fb-btn fb-btn-outline fb-btn-sm fb-navbar-logout" (click)="auth.logout()">Logout</button>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  constructor(public auth: AuthService) {}
}
