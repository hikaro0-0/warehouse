import { Menu } from "lucide-react";
import { useLocation } from "react-router-dom";
import styles from "./layout.module.css";

const pageLabels: Record<string, string> = {
  products: "Товары",
  new: "Создание товара",
  edit: "Редактирование",
  warehouses: "Склады",
  suppliers: "Поставщики",
  categories: "Категории",
  clients: "Клиенты",
  dispatches: "Отгрузки"
};

interface HeaderProps {
  onMenuClick: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const location = useLocation();
  const segments = location.pathname.split("/").filter(Boolean);
  const isDashboard = segments.length === 0;
  const title = isDashboard
    ? null
    : pageLabels[segments.at(-1) ?? ""] ?? "Карточка товара";
  const crumbs = isDashboard
    ? []
    : ["Dashboard", ...segments.map((segment) => pageLabels[segment] ?? segment)];

  return (
    <header className={styles.header}>
      <div className={styles.headerMain}>
        <button
          type="button"
          className={styles.menuButton}
          onClick={onMenuClick}
          aria-label="Открыть меню"
        >
          <Menu size={18} />
        </button>
        <div>
          {crumbs.length ? (
            <nav className={styles.breadcrumbs} aria-label="Breadcrumbs">
              {crumbs.map((crumb, index) => (
                <span key={`${crumb}-${index}`}>{crumb}</span>
              ))}
            </nav>
          ) : null}
          <div className={styles.pageTitleRow}>
            {title ? <h1>{title}</h1> : null}
          </div>
        </div>
      </div>
      <div id="header-action-root" className={styles.headerActions} />
    </header>
  );
}
