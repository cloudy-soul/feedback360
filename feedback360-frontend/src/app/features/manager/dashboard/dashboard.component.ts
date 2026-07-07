import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard.service';
import { Dashboard } from '../../../core/models/dashboard.model';

@Component({ selector: 'app-dashboard', standalone: true, imports: [CommonModule], templateUrl: './dashboard.component.html' })
export class DashboardComponent implements OnInit {
  data: Dashboard | null = null;
  loading = true;

  constructor(private svc: DashboardService) {}

  ngOnInit(): void {
    this.svc.get().subscribe({
      next: d => {
        this.data = d;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  get pieValues(): number[] {
    return this.data ? [this.data.totalSubmitted, this.data.pendingCount] : [];
  }

  get departmentLabels(): string[] {
    return this.data ? Object.keys(this.data.byDepartment) : [];
  }

  get departmentValues(): number[] {
    return this.data ? Object.values(this.data.byDepartment) : [];
  }

  get trendLabels(): string[] {
    return this.data ? this.data.ratingTrend.map(t => t.period) : [];
  }

  get trendValues(): number[] {
    return this.data ? this.data.ratingTrend.map(t => t.avgRating) : [];
  }

  get moduleLabels(): string[] {
    return this.data ? this.data.topModules.map(m => m.title) : [];
  }

  get moduleValues(): number[] {
    return this.data ? this.data.topModules.map(m => m.avgRating) : [];
  }
}
