export interface Product {
  id: number;
  sku: string;
  name: string;
  description?: string | null;
  quantity: number;
  categories: string[];
  categoryIds?: number[];
  warehouseId: number | null;
  warehouseName: string | null;
  supplierId: number | null;
  supplierName: string | null;
  price?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface ProductFormValues {
  sku: string;
  name: string;
  description?: string;
  quantity: number;
  warehouseId: string;
  supplierId: string;
  categoryIds: number[];
  price?: string;
}

export interface ProductQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  name?: string;
  categoryName?: string;
}
