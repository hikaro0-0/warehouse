import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listCategories } from "../api/categories";
import type { Category } from "../types/category";

export const useCategories = () => {
  const [data, setData] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listCategories();
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(
            getErrorMessage(requestError, "Не удалось загрузить категории")
          );
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
  }, [reloadKey]);

  return {
    data,
    loading,
    error,
    refresh: () => setReloadKey((current) => current + 1)
  };
};
