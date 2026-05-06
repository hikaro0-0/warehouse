import type { TextareaHTMLAttributes } from "react";
import styles from "./ui.module.css";

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export function Textarea({
  label,
  error,
  hint,
  className = "",
  ...props
}: TextareaProps) {
  return (
    <label className={styles.field}>
      {label ? <span className={styles.fieldLabel}>{label}</span> : null}
      <textarea
        className={[styles.textarea, error ? styles.inputError : "", className]
          .filter(Boolean)
          .join(" ")}
        {...props}
      />
      {error ? <span className={styles.fieldError}>{error}</span> : null}
      {!error && hint ? <span className={styles.fieldHint}>{hint}</span> : null}
    </label>
  );
}
