export interface Warehouse {
  id: number;
  name: string;
  address: string;
  description?: string | null;
  contactInfo?: string | null;
  productCount?: number | null;
}

export interface WarehouseFormValues {
  name: string;
  address: string;
  description?: string;
  contactInfo?: string;
}
