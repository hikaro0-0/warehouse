import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listSuppliers } from "../api/suppliers";
import type { Supplier } from "../types/supplier";

export const useSuppliers = () => {
  const [data, setData] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listSuppliers();
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(
            getErrorMessage(requestError, "Не удалось загрузить поставщиков")
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
