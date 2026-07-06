import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeedbackSummary, FeedbackDetail, FeedbackSubmitRequest, FeedbackBrowse } from '../models/feedback.model';
import { Page } from '../models/integration-log.model';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  constructor(private http: HttpClient) {}

  getMyModules(): Observable<FeedbackSummary[]> {
    return this.http.get<FeedbackSummary[]>('/api/feedback/my-modules');
  }

  getDetail(id: number): Observable<FeedbackDetail> {
    return this.http.get<FeedbackDetail>(`/api/feedback/${id}`);
  }

  submit(moduleId: number, body: FeedbackSubmitRequest): Observable<{ feedbackId: number }> {
    return this.http.post<{ feedbackId: number }>(`/api/feedback/submit?moduleId=${moduleId}`, body);
  }

  browseManager(params: Record<string, string | number>): Observable<Page<FeedbackBrowse>> {
    let p = new HttpParams();
    Object.entries(params).forEach(([k, v]) => { if (v !== null && v !== '' && v !== undefined) p = p.set(k, String(v)); });
    return this.http.get<Page<FeedbackBrowse>>('/api/manager/feedbacks', { params: p });
  }

  exportCsv(params: Record<string, string>): void {
    let p = new HttpParams();
    Object.entries(params).forEach(([k, v]) => { if (v) p = p.set(k, v); });
    window.open('/api/manager/feedbacks/export?' + p.toString(), '_blank');
  }
}
