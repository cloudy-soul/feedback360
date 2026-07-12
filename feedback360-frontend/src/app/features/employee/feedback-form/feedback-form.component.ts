import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FeedbackService } from '../../../core/services/feedback.service';

@Component({ selector: 'app-feedback-form', standalone: true, imports: [ReactiveFormsModule, CommonModule, RouterLink, TranslateModule], templateUrl: './feedback-form.component.html' })
export class FeedbackFormComponent implements OnInit {
  form: FormGroup; moduleId!: number; moduleTitle: string | null = null; error: string | null = null; submitting = false; ratingDisplay = 5;
  constructor(private fb: FormBuilder, private route: ActivatedRoute, private router: Router, private svc: FeedbackService, private translate: TranslateService) {
    this.form = this.fb.group({ rating: [5, [Validators.required, Validators.min(0), Validators.max(10)]], comment: ['', Validators.required] });
    this.form.get('rating')!.valueChanges.subscribe(v => this.ratingDisplay = v);
  }
  ngOnInit(): void {
    this.moduleId = Number(this.route.snapshot.queryParams['moduleId']);
    this.moduleTitle = this.route.snapshot.queryParams['moduleTitle'] ?? null;
    if (!this.moduleTitle) {
      this.svc.getMyModules().subscribe(modules => {
        const match = modules.find(m => m.moduleId === this.moduleId);
        if (match) this.moduleTitle = match.moduleTitle;
      });
    }
  }
  submit(): void {
    if (this.form.invalid || this.submitting) return;
    this.submitting = true; this.error = null;
    this.svc.submit(this.moduleId, this.form.value).subscribe({
      next: () => this.router.navigate(['/my-modules']),
      error: err => { this.submitting = false; this.error = err.error?.error || this.translate.instant('common.somethingWentWrong'); }
    });
  }
}
