import {
  createContext,
  useContext,
  useState,
  type ReactNode
} from "react";
import { CheckCircle2, Info, TriangleAlert, X } from "lucide-react";
import styles from "./ui.module.css";

type ToastKind = "success" | "error" | "info";

interface ToastItem {
  id: number;
  title: string;
  description?: string;
  kind: ToastKind;
}

interface ToastContextValue {
  showToast: (toast: Omit<ToastItem, "id">) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const value: ToastContextValue = {
    showToast: ({ title, description, kind }) => {
      const id = Date.now() + Math.floor(Math.random() * 1000);
      setToasts((current) => [...current, { id, title, description, kind }]);

      window.setTimeout(() => {
        setToasts((current) => current.filter((toast) => toast.id !== id));
      }, 3600);
    }
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className={styles.toastViewport}>
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={[styles.toast, styles[`toast-${toast.kind}`]].join(" ")}
          >
            <div className={styles.toastIcon}>
              {toast.kind === "success" ? <CheckCircle2 size={18} /> : null}
              {toast.kind === "error" ? <TriangleAlert size={18} /> : null}
              {toast.kind === "info" ? <Info size={18} /> : null}
            </div>
            <div className={styles.toastContent}>
              <strong>{toast.title}</strong>
              {toast.description ? <p>{toast.description}</p> : null}
            </div>
            <button
              type="button"
              className={styles.toastClose}
              onClick={() =>
                setToasts((current) =>
                  current.filter((item) => item.id !== toast.id)
                )
              }
              aria-label="Закрыть уведомление"
            >
              <X size={16} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used inside ToastProvider");
  }

  return context;
};
