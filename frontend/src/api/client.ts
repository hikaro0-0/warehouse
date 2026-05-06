import axios from "axios";
import type { ApiErrorResponse } from "../types/api";

const baseURL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json"
  }
});

export const getErrorMessage = (
  error: unknown,
  fallback = "Не удалось выполнить запрос"
): string => {
  if (!axios.isAxiosError(error)) {
    return fallback;
  }

  const payload = error.response?.data as ApiErrorResponse | undefined;
  if (!payload) {
    return error.message || fallback;
  }

  if (payload.errors?.length) {
    return payload.errors.map((issue) => issue.message).join(" ");
  }

  return payload.message || fallback;
};
