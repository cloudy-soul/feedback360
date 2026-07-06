import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Role } from '../models/user.model';

interface LoginResponse { role: Role; userId: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _role = signal<Role | null>(null);
  private _userId = signal<number | null>(null);
  readonly role = this._role.asReadonly();
  readonly userId = this._userId.asReadonly();

  constructor(private http: HttpClient, private router: Router) {
    const savedRole = sessionStorage.getItem('role') as Role | null;
    const savedUserId = sessionStorage.getItem('userId');
    if (savedRole) this._role.set(savedRole);
    if (savedUserId) this._userId.set(Number(savedUserId));
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      tap(res => {
        this._role.set(res.role);
        this._userId.set(Number(res.userId));
        sessionStorage.setItem('role', res.role);
        sessionStorage.setItem('userId', res.userId);
      })
    );
  }

  logout(): void {
    this.http.post('/api/auth/logout', {}).subscribe();
    this._role.set(null);
    this._userId.set(null);
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean { return this._role() !== null; }
  isAdmin(): boolean { return this._role() === 'ADMIN'; }
  isManager(): boolean { return this._role() === 'MANAGER' || this._role() === 'ADMIN'; }

  redirectByRole(returnUrl?: string): void {
    if (returnUrl) { this.router.navigateByUrl(returnUrl); return; }
    switch (this._role()) {
      case 'ADMIN':    this.router.navigate(['/admin/users']); break;
      case 'MANAGER':  this.router.navigate(['/manager/dashboard']); break;
      case 'EMPLOYEE': this.router.navigate(['/my-modules']); break;
    }
  }
}
