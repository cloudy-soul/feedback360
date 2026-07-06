export type FeedbackStatus = 'NOT_SUBMITTED' | 'IN_PROGRESS' | 'SUBMITTED';

export interface FeedbackSummary {
  feedbackId: number;
  moduleId: number;
  moduleTitle: string;
  moduleCategory: string;
  completionDate: string;
  status: FeedbackStatus;
  submittedAt: string | null;
}

export interface FeedbackDetail {
  feedbackId: number;
  moduleTitle: string;
  submittedBy: string;
  submittedAt: string;
  rating: number;
  comment: string;
}

export interface FeedbackSubmitRequest {
  rating: number;
  comment: string;
}

export interface FeedbackBrowse {
  feedbackId: number;
  moduleTitle: string;
  employeeName: string;
  employeeEmail: string;
  employeeId: number;
  moduleId: number;
  department: string;
  rating: number | null;
  status: FeedbackStatus;
  submittedAt: string | null;
}
