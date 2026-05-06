import type { ReactNode } from "react";
import styles from "./ui.module.css";

interface BadgeProps {
  children: ReactNode;
  tone?: "neutral" | "accent" | "success" | "warning" | "danger";
}

export function Badge({ children, tone = "neutral" }: BadgeProps) {
  return (
    <span className={[styles.badge, styles[`badge-${tone}`]].join(" ")}>
      {children}
    </span>
  );
}
