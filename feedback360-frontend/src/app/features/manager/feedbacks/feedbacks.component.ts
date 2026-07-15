import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FeedbackService } from '../../../core/services/feedback.service';
import { FeedbackBrowse } from '../../../core/models/feedback.model';
import { Page } from '../../../core/models/integration-log.model';

@Component({ selector: 'app-feedbacks', standalone: true, imports: [CommonModule, FormsModule, RouterLink, TranslateModule], templateUrl: './feedbacks.component.html' })
export class FeedbacksComponent implements OnInit {
  page: Page<FeedbackBrowse> | null = null; currentPage = 0; loading = false;
  module=''; department=''; dateFrom=''; dateTo=''; minRating=''; status='';
  pageSize = 20; pageSizeOptions = [10, 20, 50];
  reminderMessage: string | null = null;

  constructor(private svc: FeedbackService, private translate: TranslateService) {}
  ngOnInit(): void {
    const saved = localStorage.getItem('feedbacks_pageSize');
    if (saved) this.pageSize = Number(saved);
    this.load();
  }

  load(): void {
    this.loading = true;
    this.svc.browseManager({ module:this.module, department:this.department, dateFrom:this.dateFrom, dateTo:this.dateTo, minRating:this.minRating, status:this.status, page:this.currentPage, size:this.pageSize })
      .subscribe({ next: d => { this.page = d; this.loading = false; }, error: () => this.loading = false });
  }

  onPageSizeChange(): void { localStorage.setItem('feedbacks_pageSize', String(this.pageSize)); this.currentPage = 0; this.load(); }
  applyFilter(): void { this.currentPage=0; this.load(); }
  clearFilter(): void { this.module=''; this.department=''; this.dateFrom=''; this.dateTo=''; this.minRating=''; this.status=''; this.currentPage=0; this.load(); }
  goToPage(p: number): void { this.currentPage=p; this.load(); }
  exportCsv(): void { this.svc.exportCsv({ module:this.module, department:this.department, dateFrom:this.dateFrom, dateTo:this.dateTo, minRating:this.minRating, status:this.status }); }
  get pages(): number[] { return Array.from({length: this.page?.totalPages ?? 0}, (_,i)=>i); }

  sendRemindersNow(): void {
    this.svc.sendRemindersNow().subscribe(() => {
      this.reminderMessage = this.translate.instant('managerFeedbacks.reminderSuccess');
      setTimeout(() => this.reminderMessage = null, 4000);
    });
  }
}
