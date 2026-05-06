import { apiClient } from "./client";
import type { Supplier, SupplierFormValues } from "../types/supplier";

interface ApiSupplier {
  id?: number;
  name?: string;
  contactEmail?: string;
  email?: string;
  phone?: string | null;
  address?: string | null;
  notes?: string | null;
}

const normalizeSupplier = (item: ApiSupplier): Supplier => ({
  id: Number(item.id ?? 0),
  name: item.name ?? "Без названия",
  email: item.contactEmail ?? item.email ?? "",
  phone: item.phone ?? null,
  address: item.address ?? null,
  notes: item.notes ?? null
});

const toSupplierRequest = (values: SupplierFormValues) => ({
  name: values.name,
  contactEmail: values.email
  // При расширении backend DTO можно добавить phone, address и notes сюда.
});

export const listSuppliers = async (): Promise<Supplier[]> => {
  const { data } = await apiClient.get<ApiSupplier[]>("/suppliers");
  return data.map(normalizeSupplier);
};

export const getSupplier = async (id: number): Promise<Supplier> => {
  const { data } = await apiClient.get<ApiSupplier>(`/suppliers/${id}`);
  return normalizeSupplier(data);
};

export const createSupplier = async (
  values: SupplierFormValues
): Promise<Supplier> => {
  const { data } = await apiClient.post<ApiSupplier>(
    "/suppliers",
    toSupplierRequest(values)
  );
  return normalizeSupplier(data);
};

export const updateSupplier = async (
  id: number,
  values: SupplierFormValues
): Promise<Supplier> => {
  const { data } = await apiClient.put<ApiSupplier>(
    `/suppliers/${id}`,
    toSupplierRequest(values)
  );
  return normalizeSupplier(data);
};

export const deleteSupplier = async (id: number): Promise<void> => {
  await apiClient.delete(`/suppliers/${id}`);
};
