import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { FeedbackService } from '../../../core/services/feedback.service';
import { FeedbackSummary } from '../../../core/models/feedback.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({ selector: 'app-my-modules', standalone: true, imports: [CommonModule, FormsModule, RouterLink, TranslateModule], templateUrl: './my-modules.component.html' })
export class MyModulesComponent implements OnInit {
  allModules: FeedbackSummary[] = []; modules: FeedbackSummary[] = []; loading = true;
  search = ''; category = ''; status = '';

  constructor(private svc: FeedbackService, public auth: AuthService) {}

  ngOnInit(): void {
    this.svc.getMyModules().subscribe({
      next: d => { this.allModules = d; this.modules = d; this.loading = false; },
      error: () => this.loading = false
    });
  }

  get categories(): string[] {
    return Array.from(new Set(this.allModules.map(m => m.moduleCategory).filter(c => !!c)));
  }

  applyFilter(): void {
    this.modules = this.allModules.filter(m =>
      (!this.search || m.moduleTitle.toLowerCase().includes(this.search.toLowerCase())) &&
      (!this.category || m.moduleCategory === this.category) &&
      (!this.status || m.status === this.status)
    );
  }

  clearFilter(): void { this.search = ''; this.category = ''; this.status = ''; this.modules = this.allModules; }
}
