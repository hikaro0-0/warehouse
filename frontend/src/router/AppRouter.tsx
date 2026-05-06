import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "../components/layout/AppLayout";
import { EmptyState } from "../components/ui/EmptyState";
import { CategoriesPage } from "../pages/CategoriesPage";
import { DashboardPage } from "../pages/DashboardPage";
import { DispatchesPage } from "../pages/DispatchesPage";
import { ProductDetailsPage } from "../pages/ProductDetailsPage";
import { ProductFormPage } from "../pages/ProductFormPage";
import { ProductsPage } from "../pages/ProductsPage";
import { RecipientsPage } from "../pages/RecipientsPage";
import { SuppliersPage } from "../pages/SuppliersPage";
import { WarehousesPage } from "../pages/WarehousesPage";

function NotFoundPage() {
  return (
    <div style={{ padding: "32px 0" }}>
      <EmptyState
        title="Страница не найдена"
        description="Похоже, этот раздел ещё не создан или адрес введён неверно."
        action={
          <a href="/" style={{ color: "var(--accent)" }}>
            Вернуться на dashboard
          </a>
        }
      />
    </div>
  );
}

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/dashboard" element={<Navigate to="/" replace />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/products/new" element={<ProductFormPage />} />
        <Route path="/products/:id" element={<ProductDetailsPage />} />
        <Route path="/products/:id/edit" element={<ProductFormPage />} />
        <Route path="/warehouses" element={<WarehousesPage />} />
        <Route path="/suppliers" element={<SuppliersPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/clients" element={<RecipientsPage />} />
        <Route path="/recipients" element={<Navigate to="/clients" replace />} />
        <Route path="/dispatches" element={<DispatchesPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
