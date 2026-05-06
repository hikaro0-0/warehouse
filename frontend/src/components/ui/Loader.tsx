import styles from "./ui.module.css";

interface LoaderProps {
  label?: string;
  compact?: boolean;
}

export function Loader({
  label = "Загрузка данных...",
  compact = false
}: LoaderProps) {
  return (
    <div className={compact ? styles.loaderCompact : styles.loader}>
      <span className={styles.loaderSpinner} />
      <span>{label}</span>
    </div>
  );
}
