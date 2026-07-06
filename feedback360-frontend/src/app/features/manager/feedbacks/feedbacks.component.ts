import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FeedbackService } from '../../../core/services/feedback.service';
import { FeedbackBrowse } from '../../../core/models/feedback.model';
import { Page } from '../../../core/models/integration-log.model';

@Component({ selector: 'app-feedbacks', standalone: true, imports: [CommonModule, FormsModule, RouterLink], templateUrl: './feedbacks.component.html' })
export class FeedbacksComponent implements OnInit {
  page: Page<FeedbackBrowse> | null = null; currentPage = 0; loading = false;
  module=''; department=''; dateFrom=''; dateTo=''; minRating=''; status='';

  constructor(private svc: FeedbackService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.browseManager({ module:this.module, department:this.department, dateFrom:this.dateFrom, dateTo:this.dateTo, minRating:this.minRating, status:this.status, page:this.currentPage })
      .subscribe({ next: d => { this.page = d; this.loading = false; }, error: () => this.loading = false });
  }

  applyFilter(): void { this.currentPage=0; this.load(); }
  clearFilter(): void { this.module=''; this.department=''; this.dateFrom=''; this.dateTo=''; this.minRating=''; this.status=''; this.currentPage=0; this.load(); }
  goToPage(p: number): void { this.currentPage=p; this.load(); }
  exportCsv(): void { this.svc.exportCsv({ module:this.module, department:this.department, dateFrom:this.dateFrom, dateTo:this.dateTo, minRating:this.minRating, status:this.status }); }
  get pages(): number[] { return Array.from({length: this.page?.totalPages ?? 0}, (_,i)=>i); }
}
