import type { ReactNode } from "react";
import styles from "./ui.module.css";

interface EmptyStateProps {
  title: string;
  description: string;
  action?: ReactNode;
}

export function EmptyState({
  title,
  description,
  action
}: EmptyStateProps) {
  return (
    <div className={styles.emptyState}>
      <div className={styles.emptyStateIcon}>+</div>
      <h3>{title}</h3>
      <p>{description}</p>
      {action ? <div className={styles.emptyStateAction}>{action}</div> : null}
    </div>
  );
}
