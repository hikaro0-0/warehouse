import { apiClient } from "./client";
import type { Category, CategoryFormValues } from "../types/category";

interface ApiCategory {
  id?: number;
  name?: string;
  description?: string;
}

const normalizeCategory = (item: ApiCategory): Category => ({
  id: Number(item.id ?? 0),
  name: item.name ?? "Без названия",
  description: item.description ?? ""
});

export const listCategories = async (): Promise<Category[]> => {
  const { data } = await apiClient.get<ApiCategory[]>("/categories");
  return data.map(normalizeCategory);
};

export const getCategory = async (id: number): Promise<Category> => {
  const { data } = await apiClient.get<ApiCategory>(`/categories/${id}`);
  return normalizeCategory(data);
};

export const createCategory = async (
  values: CategoryFormValues
): Promise<Category> => {
  const { data } = await apiClient.post<ApiCategory>("/categories", values);
  return normalizeCategory(data);
};

export const updateCategory = async (
  id: number,
  values: CategoryFormValues
): Promise<Category> => {
  const { data } = await apiClient.put<ApiCategory>(`/categories/${id}`, values);
  return normalizeCategory(data);
};

export const deleteCategory = async (id: number): Promise<void> => {
  await apiClient.delete(`/categories/${id}`);
};
