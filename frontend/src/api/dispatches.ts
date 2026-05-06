import { apiClient } from "./client";
import type {
  Dispatch,
  DispatchStatus,
  DispatchFormValues,
  DispatchItem
} from "../types/dispatch";
import type { RecipientType } from "../types/recipient";

interface ApiDispatchItem {
  productId?: number;
  productSku?: string | null;
  productName?: string | null;
  quantity?: number | null;
}

interface ApiDispatch {
  id?: number;
  referenceNumber?: string;
  warehouseId?: number | null;
  warehouseName?: string | null;
  recipientId?: number | null;
  recipientName?: string | null;
  recipientType?: RecipientType | null;
  status?: DispatchStatus | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  items?: ApiDispatchItem[];
}

const normalizeDispatchItem = (item: ApiDispatchItem): DispatchItem => ({
  productId: Number(item.productId ?? 0),
  productSku: item.productSku ?? "",
  productName: item.productName ?? "Товар",
  quantity: Number(item.quantity ?? 0)
});

const normalizeDispatch = (item: ApiDispatch): Dispatch => ({
  id: Number(item.id ?? 0),
  referenceNumber: item.referenceNumber ?? "",
  warehouseId: item.warehouseId ?? null,
  warehouseName: item.warehouseName ?? null,
  recipientId: item.recipientId ?? null,
  recipientName: item.recipientName ?? null,
  recipientType: item.recipientType ?? null,
  status: item.status ?? "DRAFT",
  createdAt: item.createdAt ?? null,
  updatedAt: item.updatedAt ?? null,
  items: (item.items ?? []).map(normalizeDispatchItem)
});

const toDispatchRequest = (values: DispatchFormValues) => ({
  referenceNumber: values.referenceNumber.trim(),
  warehouseId: Number(values.warehouseId),
  recipientId: Number(values.recipientId),
  items: values.items.map((item) => ({
    productId: Number(item.productId),
    quantity: Number(item.quantity)
  }))
});

export const listDispatches = async (): Promise<Dispatch[]> => {
  const { data } = await apiClient.get<ApiDispatch[]>("/dispatches");
  return data.map(normalizeDispatch);
};

export const getDispatch = async (id: number): Promise<Dispatch> => {
  const { data } = await apiClient.get<ApiDispatch>(`/dispatches/${id}`);
  return normalizeDispatch(data);
};

export const createDispatch = async (
  values: DispatchFormValues
): Promise<Dispatch> => {
  const { data } = await apiClient.post<ApiDispatch>(
    "/dispatches",
    toDispatchRequest(values)
  );
  return normalizeDispatch(data);
};

export const updateDispatch = async (
  id: number,
  values: DispatchFormValues
): Promise<Dispatch> => {
  const { data } = await apiClient.put<ApiDispatch>(
    `/dispatches/${id}`,
    toDispatchRequest(values)
  );
  return normalizeDispatch(data);
};

export const deleteDispatch = async (id: number): Promise<void> => {
  await apiClient.delete(`/dispatches/${id}`);
};

export const confirmDispatch = async (id: number): Promise<Dispatch> => {
  const { data } = await apiClient.post<ApiDispatch>(`/dispatches/${id}/confirm`);
  return normalizeDispatch(data);
};

export const cancelDispatch = async (id: number): Promise<Dispatch> => {
  const { data } = await apiClient.post<ApiDispatch>(`/dispatches/${id}/cancel`);
  return normalizeDispatch(data);
};
