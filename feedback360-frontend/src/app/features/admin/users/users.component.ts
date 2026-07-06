import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { User, UserRequest, Role } from '../../../core/models/user.model';

@Component({ selector: 'app-users', standalone: true, imports: [CommonModule, ReactiveFormsModule], templateUrl: './users.component.html' })
export class UsersComponent implements OnInit {
  users: User[] = []; roles: Role[] = ['ADMIN','MANAGER','EMPLOYEE'];
  showForm = false; editingId: number | null = null; form: FormGroup;
  formError: string | null = null; success: string | null = null;

  constructor(private admin: AdminService, private fb: FormBuilder) {
    this.form = this.fb.group({ firstName:['',Validators.required], lastName:['',Validators.required], email:['', [Validators.required,Validators.email]], password:[''], role:['EMPLOYEE',Validators.required], department:[''] });
  }

  ngOnInit(): void { this.load(); }
  load(): void { this.admin.getUsers().subscribe(d => this.users = d); }

  openCreate(): void {
    this.editingId = null; this.form.reset({role:'EMPLOYEE'}); this.form.get('email')!.enable();
    this.form.get('password')!.setValidators(Validators.required); this.form.get('password')!.updateValueAndValidity();
    this.showForm = true; this.formError = null;
  }

  openEdit(u: User): void {
    this.editingId = u.id; this.form.patchValue({...u, password:''});
    this.form.get('email')!.disable();
    this.form.get('password')!.clearValidators(); this.form.get('password')!.updateValueAndValidity();
    this.showForm = true; this.formError = null;
  }

  save(): void {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as UserRequest;
    const action = this.editingId ? this.admin.updateUser(this.editingId, body) : this.admin.createUser(body);
    action.subscribe({ next: () => { this.showForm = false; this.success = this.editingId ? 'User updated.' : 'User created.'; this.load(); setTimeout(()=>this.success=null,3000); }, error: err => this.formError = err.error?.message||'An error occurred.' });
  }

  deactivate(id: number): void { if (!confirm('Deactivate this user?')) return; this.admin.deactivateUser(id).subscribe(()=>this.load()); }
  activate(id: number): void { this.admin.activateUser(id).subscribe(()=>this.load()); }
  cancel(): void { this.showForm = false; }
}
