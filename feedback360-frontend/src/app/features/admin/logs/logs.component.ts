import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { IntegrationLog, Page } from '../../../core/models/integration-log.model';

@Component({ selector: 'app-logs', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './logs.component.html' })
export class LogsComponent implements OnInit {
  filterType = ''; filterStatus = ''; filterSince = '';
  currentPage = 0; pageSize = 20; pageSizeOptions = [10, 20, 50];
  page: Page<IntegrationLog> | null = null; loading = false; syncMessage: string | null = null;

  constructor(private admin: AdminService) {}

  ngOnInit(): void {
    const saved = localStorage.getItem('logs_pageSize');
    if (saved) this.pageSize = Number(saved);
    this.load();
  }

  load(): void {
    this.loading = true;
    this.admin.getLogs({ type: this.filterType||undefined, status: this.filterStatus||undefined, since: this.filterSince||undefined, page: this.currentPage, pageSize: this.pageSize })
      .subscribe({ next: d => { this.page = d; this.loading = false; }, error: () => this.loading = false });
  }

  onPageSizeChange(): void { localStorage.setItem('logs_pageSize', String(this.pageSize)); this.currentPage = 0; this.load(); }
  applyFilter(): void { this.currentPage = 0; this.load(); }
  clearFilter(): void { this.filterType=''; this.filterStatus=''; this.filterSince=''; this.load(); }
  goToPage(p: number): void { this.currentPage = p; this.load(); }

  syncNow(): void {
    this.admin.syncNow().subscribe(() => { this.syncMessage = 'Sync triggered — check new entries below.'; setTimeout(()=>this.syncMessage=null,4000); this.currentPage=0; this.load(); });
  }

  get pages(): number[] { return Array.from({length: this.page?.totalPages ?? 0}, (_,i)=>i); }
}
