import { useState } from "react";
import { Outlet } from "react-router-dom";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";
import styles from "./layout.module.css";

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className={styles.shell}>
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />
      <div className={styles.contentArea}>
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <main className={styles.mainContent}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
