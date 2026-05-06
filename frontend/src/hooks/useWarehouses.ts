import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listWarehouses } from "../api/warehouses";
import type { Warehouse } from "../types/warehouse";

export const useWarehouses = () => {
  const [data, setData] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listWarehouses();
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(getErrorMessage(requestError, "Не удалось загрузить склады"));
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
