import {
  Building2,
  Boxes,
  Layers3,
  Package,
  Send,
  Truck,
  X
} from "lucide-react";
import { NavLink } from "react-router-dom";
import styles from "./layout.module.css";

const navigation = [
  { to: "/products", label: "Товары", icon: Package },
  { to: "/warehouses", label: "Склады", icon: Boxes },
  { to: "/suppliers", label: "Поставщики", icon: Truck },
  { to: "/categories", label: "Категории", icon: Layers3 },
  { to: "/clients", label: "Клиенты", icon: Building2 },
  { to: "/dispatches", label: "Отгрузки", icon: Send }
];

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  return (
    <>
      <div
        className={[styles.sidebarBackdrop, isOpen ? styles.backdropOpen : ""]
          .filter(Boolean)
          .join(" ")}
        onClick={onClose}
        role="presentation"
      />
      <aside
        className={[styles.sidebar, isOpen ? styles.sidebarOpen : ""]
          .filter(Boolean)
          .join(" ")}
      >
        <div className={styles.brand}>
          <NavLink
            to="/"
            onClick={onClose}
            className={styles.brandLink}
          >
            <div className={styles.brandMark}>WH</div>
            <div>
              <strong>Warehouse Hub</strong>
              <p>Учет техники и запасов</p>
            </div>
          </NavLink>
          <button
            type="button"
            className={styles.sidebarClose}
            onClick={onClose}
            aria-label="Закрыть навигацию"
          >
            <X size={18} />
          </button>
        </div>

        <nav className={styles.nav}>
          {navigation.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  [styles.navLink, isActive ? styles.navLinkActive : ""]
                    .filter(Boolean)
                    .join(" ")
                }
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
    </>
  );
}
