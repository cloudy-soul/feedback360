export interface ModuleRating {
  title: string;
  avgRating: number;
  responses: number;
}

export interface TrendPoint {
  period: string;
  avgRating: number;
}

export interface Dashboard {
  totalCompletions: number;
  totalSubmitted: number;
  responseRatePercent: number;
  averageRatingOverall: number;
  topModules: ModuleRating[];
  bottomModules: ModuleRating[];
  byDepartment: Record<string, number>;
  ratingTrend: TrendPoint[];
  pendingCount: number;
}
