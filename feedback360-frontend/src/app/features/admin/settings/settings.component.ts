import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector: 'app-settings', standalone: true, imports: [CommonModule, ReactiveFormsModule, TranslateModule], templateUrl: './settings.component.html' })
export class SettingsComponent implements OnInit {
  form: FormGroup; success = false;
  constructor(private admin: AdminService, private fb: FormBuilder) {
    this.form = this.fb.group({ reminderDelayDays:[5,[Validators.required,Validators.min(1),Validators.max(30)]], reminderTemplate:['',Validators.required] });
  }
  ngOnInit(): void { this.admin.getSettings().subscribe(s => this.form.patchValue({ reminderDelayDays: Number(s['reminder.delay.days']??5), reminderTemplate: s['email.template.reminder']??'' })); }
  save(): void {
    if (this.form.invalid) return;
    const {reminderDelayDays, reminderTemplate} = this.form.value;
    this.admin.saveSetting('reminder.delay.days', String(reminderDelayDays)).subscribe();
    this.admin.saveSetting('email.template.reminder', reminderTemplate).subscribe(()=>{ this.success=true; setTimeout(()=>this.success=false,3000); });
  }
  syncNow(): void { this.admin.syncNow().subscribe(); }
}
