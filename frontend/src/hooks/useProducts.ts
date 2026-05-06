import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listProducts } from "../api/products";
import { createEmptyPage, type PaginatedResponse } from "../types/api";
import type { Product, ProductQueryParams } from "../types/product";

export const useProducts = (params: ProductQueryParams = {}) => {
  const [data, setData] = useState<PaginatedResponse<Product>>(
    createEmptyPage<Product>()
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listProducts(params);
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(getErrorMessage(requestError, "Не удалось загрузить товары"));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void load();

    return () => {
      active = false;
    };
  }, [
    params.page,
    params.size,
    params.sort,
    params.name,
    params.categoryName,
    reloadKey
  ]);

  return {
    data,
    loading,
    error,
    refresh: () => setReloadKey((current) => current + 1)
  };
};
