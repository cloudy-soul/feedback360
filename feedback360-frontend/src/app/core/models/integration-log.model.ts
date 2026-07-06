export interface IntegrationLog {
  id: number;
  type: string;
  status: 'SUCCESS' | 'FAILED';
  message: string;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}
