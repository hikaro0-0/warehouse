import { useEffect, useState } from "react";
import { getErrorMessage } from "../api/client";
import { listRecipients } from "../api/recipients";
import type { Recipient } from "../types/recipient";

export const useRecipients = () => {
  const [data, setData] = useState<Recipient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await listRecipients();
        if (active) {
          setData(response);
        }
      } catch (requestError) {
        if (active) {
          setError(
            getErrorMessage(requestError, "Не удалось загрузить клиентов")
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
