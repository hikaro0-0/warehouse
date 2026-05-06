import { apiClient } from "./client";
import type { Warehouse, WarehouseFormValues } from "../types/warehouse";

interface ApiWarehouse {
  id?: number;
  name?: string;
  address?: string;
  description?: string | null;
  contactInfo?: string | null;
}

const normalizeWarehouse = (item: ApiWarehouse): Warehouse => ({
  id: Number(item.id ?? 0),
  name: item.name ?? "Без названия",
  address: item.address ?? "",
  description: item.description ?? null,
  contactInfo: item.contactInfo ?? null
});

const toWarehouseRequest = (values: WarehouseFormValues) => ({
  name: values.name,
  address: values.address
  // description и contactInfo можно подключить здесь после расширения backend DTO.
});

export const listWarehouses = async (): Promise<Warehouse[]> => {
  const { data } = await apiClient.get<ApiWarehouse[]>("/warehouses");
  return data.map(normalizeWarehouse);
};

export const getWarehouse = async (id: number): Promise<Warehouse> => {
  const { data } = await apiClient.get<ApiWarehouse>(`/warehouses/${id}`);
  return normalizeWarehouse(data);
};

export const createWarehouse = async (
  values: WarehouseFormValues
): Promise<Warehouse> => {
  const { data } = await apiClient.post<ApiWarehouse>(
    "/warehouses",
    toWarehouseRequest(values)
  );
  return normalizeWarehouse(data);
};

export const updateWarehouse = async (
  id: number,
  values: WarehouseFormValues
): Promise<Warehouse> => {
  const { data } = await apiClient.put<ApiWarehouse>(
    `/warehouses/${id}`,
    toWarehouseRequest(values)
  );
  return normalizeWarehouse(data);
};

export const deleteWarehouse = async (id: number): Promise<void> => {
  await apiClient.delete(`/warehouses/${id}`);
};
