import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { LanguageService } from '../../core/services/language.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslateModule],
  template: `
    <nav class="fb-navbar">
      <a routerLink="/" class="fb-navbar-brand">
        <img src="/assets/capLogo.png" alt="Capgemini logo" class="fb-brand-logo fb-brand-logo-nav" />
        <span class="fb-brand-divider"></span>
        <span>FeedBack360</span>
      </a>
      @if (auth.isAdmin()) {
        <a routerLink="/admin/users"    routerLinkActive="active" class="fb-nav-link">{{ 'nav.users' | translate }}</a>
        <a routerLink="/admin/logs"     routerLinkActive="active" class="fb-nav-link">{{ 'nav.logs' | translate }}</a>
        <a routerLink="/admin/settings" routerLinkActive="active" class="fb-nav-link">{{ 'nav.settings' | translate }}</a>
      }
      @if (auth.isManager() && !auth.isAdmin()) {
        <a routerLink="/manager/dashboard" routerLinkActive="active" class="fb-nav-link">{{ 'nav.dashboard' | translate }}</a>
        <a routerLink="/manager/feedbacks" routerLinkActive="active" class="fb-nav-link">{{ 'nav.feedback' | translate }}</a>
      }
      @if (!auth.isAdmin() && !auth.isManager()) {
        <a routerLink="/my-modules" routerLinkActive="active" class="fb-nav-link">{{ 'nav.myModules' | translate }}</a>
      }
      <div class="fb-navbar-right">
        <div class="fb-lang-switch">
          <button type="button" [class]="'fb-lang-btn' + (lang.current() === 'en' ? ' active' : '')" (click)="lang.use('en')">EN</button>
          <button type="button" [class]="'fb-lang-btn' + (lang.current() === 'fr' ? ' active' : '')" (click)="lang.use('fr')">FR</button>
        </div>
        <span class="fb-navbar-user">&#128100; {{ auth.fullName() }} · {{ auth.role() }}</span>
        <button class="fb-btn fb-btn-outline fb-btn-sm fb-navbar-logout" (click)="auth.logout()">{{ 'nav.logout' | translate }}</button>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  constructor(public auth: AuthService, public lang: LanguageService) {}
}
