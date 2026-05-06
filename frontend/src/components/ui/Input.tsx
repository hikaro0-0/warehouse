import type { InputHTMLAttributes } from "react";
import styles from "./ui.module.css";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export function Input({
  label,
  error,
  hint,
  className = "",
  id,
  ...props
}: InputProps) {
  return (
    <label className={styles.field}>
      {label ? <span className={styles.fieldLabel}>{label}</span> : null}
      <input
        id={id}
        className={[styles.input, error ? styles.inputError : "", className]
          .filter(Boolean)
          .join(" ")}
        {...props}
      />
      {error ? <span className={styles.fieldError}>{error}</span> : null}
      {!error && hint ? <span className={styles.fieldHint}>{hint}</span> : null}
    </label>
  );
}
