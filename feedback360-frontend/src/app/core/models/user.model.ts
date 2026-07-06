export type Role = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  department: string;
  active: boolean;
  createdAt: string;
}

export interface UserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role: Role;
  department: string;
}
