import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FeedbackService } from '../../../core/services/feedback.service';
import { FeedbackDetail } from '../../../core/models/feedback.model';

@Component({ selector: 'app-feedback-detail', standalone: true, imports: [CommonModule], templateUrl: './feedback-detail.component.html' })
export class FeedbackDetailComponent implements OnInit {
  feedback: FeedbackDetail | null = null; loading = true;
  constructor(private route: ActivatedRoute, private svc: FeedbackService) {}
  ngOnInit(): void { this.svc.getDetail(Number(this.route.snapshot.params['id'])).subscribe({ next: d => { this.feedback = d; this.loading = false; }, error: () => this.loading = false }); }
}
