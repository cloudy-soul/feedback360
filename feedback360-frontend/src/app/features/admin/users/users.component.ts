import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { User, UserRequest, Role } from '../../../core/models/user.model';
import { Page } from '../../../core/models/integration-log.model';
import { PersonNamePipe } from '../../../core/pipes/person-name.pipe';

@Component({ selector: 'app-users', standalone: true, imports: [CommonModule, ReactiveFormsModule, FormsModule, PersonNamePipe, TranslateModule], templateUrl: './users.component.html' })
export class UsersComponent implements OnInit {
  page: Page<User> | null = null; loading = false;
  roles: Role[] = ['ADMIN','MANAGER','EMPLOYEE'];
  showForm = false; editingId: number | null = null; form: FormGroup;
  formError: string | null = null; success: string | null = null;

  search = ''; roleFilter = '';
  currentPage = 0; pageSize = 10; pageSizeOptions = [10, 20, 50];

  constructor(private admin: AdminService, private fb: FormBuilder, private translate: TranslateService) {
    this.form = this.fb.group({ firstName:['',Validators.required], lastName:['',Validators.required], email:['', [Validators.required,Validators.email]], password:[''], role:['EMPLOYEE',Validators.required], department:[''] });
  }

  ngOnInit(): void {
    const saved = localStorage.getItem('users_pageSize');
    if (saved) this.pageSize = Number(saved);
    this.load();
  }

  load(): void {
    this.loading = true;
    this.admin.getUsers({ search: this.search || undefined, role: this.roleFilter || undefined, page: this.currentPage, pageSize: this.pageSize })
      .subscribe({ next: d => { this.page = d; this.loading = false; }, error: () => this.loading = false });
  }

  onPageSizeChange(): void { localStorage.setItem('users_pageSize', String(this.pageSize)); this.currentPage = 0; this.load(); }
  applyFilter(): void { this.currentPage = 0; this.load(); }
  clearFilter(): void { this.search = ''; this.roleFilter = ''; this.currentPage = 0; this.load(); }
  goToPage(p: number): void { this.currentPage = p; this.load(); }
  get pages(): number[] { return Array.from({length: this.page?.totalPages ?? 0}, (_,i)=>i); }

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

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const body = this.form.getRawValue() as UserRequest;
    const action = this.editingId ? this.admin.updateUser(this.editingId, body) : this.admin.createUser(body);
    action.subscribe({
      next: () => {
        this.showForm = false;
        this.success = this.translate.instant(this.editingId ? 'adminUsers.userUpdated' : 'adminUsers.userCreated');
        this.load(); setTimeout(()=>this.success=null,3000);
      },
      error: err => this.formError = err.error?.message || this.translate.instant('common.errorOccurred')
    });
  }

  deactivate(id: number): void { if (!confirm(this.translate.instant('adminUsers.confirmDeactivate'))) return; this.admin.deactivateUser(id).subscribe(()=>this.load()); }
  activate(id: number): void { this.admin.activateUser(id).subscribe(()=>this.load()); }
  cancel(): void { this.showForm = false; }
}
