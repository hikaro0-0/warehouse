import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listDispatches } from "../api/dispatches";
import type { Dispatch } from "../types/dispatch";

export const useDispatches = () => {
  const [data, setData] = useState<Dispatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listDispatches();
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(
            getErrorMessage(requestError, "Не удалось загрузить отгрузки")
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
