import { useEffect, type ReactNode } from "react";
import { X } from "lucide-react";
import styles from "./ui.module.css";

interface ModalProps {
  open: boolean;
  title: string;
  description?: string;
  children: ReactNode;
  footer?: ReactNode;
  onClose: () => void;
  size?: "sm" | "md" | "lg";
}

export function Modal({
  open,
  title,
  description,
  children,
  footer,
  onClose,
  size = "md"
}: ModalProps) {
  useEffect(() => {
    if (!open) {
      return undefined;
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [open, onClose]);

  if (!open) {
    return null;
  }

  return (
    <div className={styles.modalOverlay} onClick={onClose} role="presentation">
      <div
        className={[styles.modal, styles[`modal-${size}`]].join(" ")}
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        <div className={styles.modalHeader}>
          <div>
            <h3 id="modal-title" className={styles.modalTitle}>
              {title}
            </h3>
            {description ? (
              <p className={styles.modalDescription}>{description}</p>
            ) : null}
          </div>
          <button
            type="button"
            className={styles.iconButton}
            onClick={onClose}
            aria-label="Закрыть"
          >
            <X size={18} />
          </button>
        </div>
        <div className={styles.modalBody}>{children}</div>
        {footer ? <div className={styles.modalFooter}>{footer}</div> : null}
      </div>
    </div>
  );
}
