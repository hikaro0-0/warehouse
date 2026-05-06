import type { RecipientType } from "./recipient";

export type DispatchStatus = "DRAFT" | "CONFIRMED" | "CANCELLED";

export interface DispatchItem {
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
}

export interface Dispatch {
  id: number;
  referenceNumber: string;
  warehouseId: number | null;
  warehouseName: string | null;
  recipientId: number | null;
  recipientName: string | null;
  recipientType: RecipientType | null;
  status: DispatchStatus;
  createdAt?: string | null;
  updatedAt?: string | null;
  items: DispatchItem[];
}

export interface DispatchFormItemValues {
  productId: string;
  quantity: string;
}

export interface DispatchFormValues {
  referenceNumber: string;
  warehouseId: string;
  recipientId: string;
  items: DispatchFormItemValues[];
}

export const getDispatchStatusLabel = (status: DispatchStatus): string => {
  if (status === "CONFIRMED") {
    return "Подтверждена";
  }

  if (status === "CANCELLED") {
    return "Отменена";
  }

  return "Черновик";
};

export const getDispatchStatusTone = (
  status: DispatchStatus
): "warning" | "success" | "danger" => {
  if (status === "CONFIRMED") {
    return "success";
  }

  if (status === "CANCELLED") {
    return "danger";
  }

  return "warning";
};

export const isDispatchEditable = (status: DispatchStatus): boolean =>
  status === "DRAFT";
