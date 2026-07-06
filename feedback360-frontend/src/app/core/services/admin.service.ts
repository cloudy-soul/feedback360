import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserRequest } from '../models/user.model';
import { IntegrationLog, Page } from '../models/integration-log.model';

interface LogFilter { type?: string; status?: string; since?: string; page?: number; pageSize?: number; }

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> { return this.http.get<User[]>('/api/admin/users'); }
  createUser(req: UserRequest): Observable<User> { return this.http.post<User>('/api/admin/users', req); }
  updateUser(id: number, req: UserRequest): Observable<User> { return this.http.put<User>(`/api/admin/users/${id}`, req); }
  deactivateUser(id: number): Observable<void> { return this.http.patch<void>(`/api/admin/users/${id}/deactivate`, {}); }
  activateUser(id: number): Observable<void> { return this.http.patch<void>(`/api/admin/users/${id}/activate`, {}); }

  getLogs(filter: LogFilter): Observable<Page<IntegrationLog>> {
    let params = new HttpParams();
    if (filter.type)     params = params.set('type', filter.type);
    if (filter.status)   params = params.set('status', filter.status);
    if (filter.since)    params = params.set('since', filter.since);
    if (filter.page != null)     params = params.set('page', String(filter.page));
    if (filter.pageSize != null) params = params.set('pageSize', String(filter.pageSize));
    return this.http.get<Page<IntegrationLog>>('/api/admin/logs', { params });
  }

  syncNow(): Observable<{ result: string }> { return this.http.post<{ result: string }>('/api/admin/integration/sync-now', {}); }

  getSettings(): Observable<Record<string, string>> { return this.http.get<Record<string, string>>('/api/admin/settings'); }
  saveSetting(key: string, value: string): Observable<void> { return this.http.put<void>('/api/admin/settings', { key, value }); }
}
