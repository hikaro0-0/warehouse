export interface Supplier {
  id: number;
  name: string;
  email: string;
  phone?: string | null;
  address?: string | null;
  notes?: string | null;
}

export interface SupplierFormValues {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  notes?: string;
}
