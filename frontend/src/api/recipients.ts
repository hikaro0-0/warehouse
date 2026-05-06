import { apiClient } from "./client";
import type {
  Recipient,
  RecipientFormValues,
  RecipientType
} from "../types/recipient";

interface ApiRecipient {
  id?: number;
  name?: string;
  type?: RecipientType | null;
  contactEmail?: string | null;
  email?: string | null;
  address?: string | null;
}

const normalizeRecipient = (item: ApiRecipient): Recipient => ({
  id: Number(item.id ?? 0),
  name: item.name ?? "Без названия",
  type: item.type ?? "COMPANY",
  email: item.contactEmail ?? item.email ?? null,
  address: item.address ?? null
});

const toRecipientRequest = (values: RecipientFormValues) => ({
  name: values.name.trim(),
  type: values.type,
  contactEmail: values.email.trim() ? values.email.trim() : null,
  address: values.address.trim() ? values.address.trim() : null
});

export const listRecipients = async (): Promise<Recipient[]> => {
  const { data } = await apiClient.get<ApiRecipient[]>("/recipients");
  return data.map(normalizeRecipient);
};

export const getRecipient = async (id: number): Promise<Recipient> => {
  const { data } = await apiClient.get<ApiRecipient>(`/recipients/${id}`);
  return normalizeRecipient(data);
};

export const createRecipient = async (
  values: RecipientFormValues
): Promise<Recipient> => {
  const { data } = await apiClient.post<ApiRecipient>(
    "/recipients",
    toRecipientRequest(values)
  );
  return normalizeRecipient(data);
};

export const updateRecipient = async (
  id: number,
  values: RecipientFormValues
): Promise<Recipient> => {
  const { data } = await apiClient.put<ApiRecipient>(
    `/recipients/${id}`,
    toRecipientRequest(values)
  );
  return normalizeRecipient(data);
};

export const deleteRecipient = async (id: number): Promise<void> => {
  await apiClient.delete(`/recipients/${id}`);
};
