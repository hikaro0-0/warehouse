import { apiClient } from "./client";
import type { PaginatedResponse } from "../types/api";
import type {
  Product,
  ProductFormValues,
  ProductQueryParams
} from "../types/product";

interface ApiProduct {
  id?: number;
  sku?: string;
  name?: string;
  description?: string | null;
  quantity?: number | null;
  categories?: Array<string | { id?: number; name?: string }>;
  categoryIds?: number[];
  warehouseId?: number | null;
  warehouseName?: string | null;
  warehouse?: { id?: number; name?: string | null } | null;
  supplierId?: number | null;
  supplierName?: string | null;
  supplier?: { id?: number; name?: string | null } | null;
  price?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

const normalizeProduct = (item: ApiProduct): Product => ({
  id: Number(item.id ?? 0),
  sku: item.sku ?? "",
  name: item.name ?? "Без названия",
  description: item.description ?? null,
  quantity: Number(item.quantity ?? 0),
  categories: (item.categories ?? []).map((category) =>
    typeof category === "string" ? category : category.name ?? "Категория"
  ),
  categoryIds: item.categoryIds ?? [],
  warehouseId: item.warehouseId ?? item.warehouse?.id ?? null,
  warehouseName: item.warehouseName ?? item.warehouse?.name ?? null,
  supplierId: item.supplierId ?? item.supplier?.id ?? null,
  supplierName: item.supplierName ?? item.supplier?.name ?? null,
  price: item.price ?? null,
  createdAt: item.createdAt ?? null,
  updatedAt: item.updatedAt ?? null
});

const normalizePage = (
  page: PaginatedResponse<ApiProduct>
): PaginatedResponse<Product> => ({
  ...page,
  content: page.content.map(normalizeProduct)
});

const toProductRequest = (values: ProductFormValues) => ({
  sku: values.sku.trim(),
  name: values.name.trim(),
  description: values.description?.trim() ? values.description.trim() : null,
  quantity: Number(values.quantity),
  price:
    values.price && values.price.trim() !== ""
      ? Number(values.price)
      : null,
  warehouseId: Number(values.warehouseId),
  supplierId: Number(values.supplierId),
  categoryIds: values.categoryIds
});

export const listProducts = async (
  params: ProductQueryParams = {}
): Promise<PaginatedResponse<Product>> => {
  const query = {
    page: params.page ?? 0,
    size: params.size ?? 24,
    sort: params.sort ?? "id,desc"
  };

  if (params.categoryName) {
    const { data } = await apiClient.get<PaginatedResponse<ApiProduct>>(
      "/products/search/native",
      {
        params: {
          ...query,
          name: params.name || undefined,
          categoryName: params.categoryName
        }
      }
    );
    return normalizePage(data);
  }

  const { data } = await apiClient.get<PaginatedResponse<ApiProduct>>(
    "/products",
    {
      params: {
        ...query,
        name: params.name || undefined
      }
    }
  );

  return normalizePage(data);
};

export const getProduct = async (id: number): Promise<Product> => {
  const { data } = await apiClient.get<ApiProduct>(`/products/${id}`);
  return normalizeProduct(data);
};

export const createProduct = async (
  values: ProductFormValues
): Promise<Product> => {
  const { data } = await apiClient.post<ApiProduct>(
    "/products",
    toProductRequest(values)
  );
  return normalizeProduct(data);
};

export const updateProduct = async (
  id: number,
  values: ProductFormValues
): Promise<Product> => {
  const { data } = await apiClient.put<ApiProduct>(
    `/products/${id}`,
    toProductRequest(values)
  );
  return normalizeProduct(data);
};

export const deleteProduct = async (id: number): Promise<void> => {
  await apiClient.delete(`/products/${id}`);
};
