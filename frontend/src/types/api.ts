export interface ApiValidationIssue {
  field: string;
  message: string;
  rejectedValue: unknown;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: ApiValidationIssue[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export const createEmptyPage = <T,>(): PaginatedResponse<T> => ({
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 0,
  number: 0,
  first: true,
  last: true,
  empty: true
});
