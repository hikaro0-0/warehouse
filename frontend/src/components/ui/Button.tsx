import type { ButtonHTMLAttributes, ReactNode } from "react";
import styles from "./ui.module.css";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";
type ButtonSize = "sm" | "md" | "lg";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
  loading?: boolean;
}

export const getButtonClassName = ({
  variant = "primary",
  size = "md",
  fullWidth = false
}: {
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
}) =>
  [
    styles.button,
    styles[`button-${variant}`],
    styles[`button-${size}`],
    fullWidth ? styles.fullWidth : ""
  ]
    .filter(Boolean)
    .join(" ");

export function Button({
  children,
  variant = "primary",
  size = "md",
  fullWidth = false,
  className = "",
  loading = false,
  disabled,
  ...props
}: ButtonProps) {
  const classes = [
    getButtonClassName({ variant, size, fullWidth }),
    className
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button className={classes} disabled={disabled || loading} {...props}>
      {loading ? <span className={styles.buttonSpinner} /> : null}
      <span>{children}</span>
    </button>
  );
}
