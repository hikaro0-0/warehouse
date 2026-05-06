export type RecipientType = "COMPANY" | "STORE";

export interface Recipient {
  id: number;
  name: string;
  type: RecipientType;
  email?: string | null;
  address?: string | null;
}

export interface RecipientFormValues {
  name: string;
  type: RecipientType;
  email: string;
  address: string;
}

export const RECIPIENT_TYPE_OPTIONS: Array<{
  value: RecipientType;
  label: string;
}> = [
  { value: "COMPANY", label: "Фирма" },
  { value: "STORE", label: "Магазин" }
];

export const getRecipientTypeLabel = (
  type?: RecipientType | null
): string => {
  if (type === "STORE") {
    return "Магазин";
  }

  if (type === "COMPANY") {
    return "Фирма";
  }

  return "Клиент";
};
