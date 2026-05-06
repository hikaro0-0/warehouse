import type { SelectHTMLAttributes } from "react";
import styles from "./ui.module.css";

export interface SelectOption {
  label: string;
  value: string;
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  hint?: string;
  options: SelectOption[];
  placeholder?: string;
}

export function Select({
  label,
  error,
  hint,
  options,
  placeholder,
  className = "",
  ...props
}: SelectProps) {
  return (
    <label className={styles.field}>
      {label ? <span className={styles.fieldLabel}>{label}</span> : null}
      <select
        className={[styles.select, error ? styles.inputError : "", className]
          .filter(Boolean)
          .join(" ")}
        {...props}
      >
        {placeholder ? <option value="">{placeholder}</option> : null}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error ? <span className={styles.fieldError}>{error}</span> : null}
      {!error && hint ? <span className={styles.fieldHint}>{hint}</span> : null}
    </label>
  );
}
