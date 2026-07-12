import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, TranslateModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  form: FormGroup;
  error: string | null = null;
  loading = false;
  private returnUrl: string;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router, private route: ActivatedRoute, private translate: TranslateService) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
    if (this.auth.isAuthenticated()) this.auth.redirectByRole(this.returnUrl);
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true; this.error = null;
    const { email, password } = this.form.value;
    this.auth.login(email, password).subscribe({
      next: () => { this.loading = false; this.auth.redirectByRole(this.returnUrl || undefined); },
      error: () => { this.loading = false; this.error = this.translate.instant('login.invalidCredentials'); }
    });
  }
}
