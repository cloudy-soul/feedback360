import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FeedbackService } from '../../../core/services/feedback.service';
import { FeedbackSummary } from '../../../core/models/feedback.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({ selector: 'app-my-modules', standalone: true, imports: [CommonModule, RouterLink], templateUrl: './my-modules.component.html' })
export class MyModulesComponent implements OnInit {
  modules: FeedbackSummary[] = []; loading = true;
  constructor(private svc: FeedbackService, public auth: AuthService) {}
  ngOnInit(): void { this.svc.getMyModules().subscribe({ next: d => { this.modules = d; this.loading = false; }, error: () => this.loading = false }); }
}
