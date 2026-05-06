import { useDeferredValue, useEffect, useState } from "react";
import {
  Eye,
  LayoutGrid,
  List,
  Pencil,
  Plus,
  Trash2
} from "lucide-react";
import { Link } from "react-router-dom";
import { getErrorMessage } from "../api/client";
import { deleteProduct } from "../api/products";
import { Badge } from "../components/ui/Badge";
import { Button, getButtonClassName } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Select } from "../components/ui/Select";
import { useToast } from "../components/ui/ToastProvider";
import { useCategories } from "../hooks/useCategories";
import { useProducts } from "../hooks/useProducts";
import { useSuppliers } from "../hooks/useSuppliers";
import { useWarehouses } from "../hooks/useWarehouses";
import type { Product } from "../types/product";
import { formatCurrency } from "../utils/format";
import { getStockLabel, getStockTone } from "../utils/stock";
import styles from "./pages.module.css";

type ViewMode = "cards" | "table";

export function ProductsPage() {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [selectedCategoryId, setSelectedCategoryId] = useState("");
  const [selectedWarehouseId, setSelectedWarehouseId] = useState("");
  const [selectedSupplierId, setSelectedSupplierId] = useState("");
  const [viewMode, setViewMode] = useState<ViewMode>("cards");
  const [page, setPage] = useState(1);
  const [productToDelete, setProductToDelete] = useState<Product | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const deferredSearch = useDeferredValue(search.trim());
  const warehouses = useWarehouses();
  const suppliers = useSuppliers();
  const categories = useCategories();

  const selectedCategory = categories.data.find(
    (category) => String(category.id) === selectedCategoryId
  );

  const products = useProducts({
    page: 0,
    size: 120,
    sort: "id,desc",
    name: deferredSearch || undefined,
    categoryName: selectedCategory?.name
  });

  useEffect(() => {
    setPage(1);
  }, [
    deferredSearch,
    selectedCategoryId,
    selectedWarehouseId,
    selectedSupplierId,
    viewMode
  ]);

  const filteredProducts = products.data.content.filter((product) => {
    const matchesWarehouse = selectedWarehouseId
      ? String(product.warehouseId) === selectedWarehouseId
      : true;
    const matchesSupplier = selectedSupplierId
      ? String(product.supplierId) === selectedSupplierId
      : true;

    return matchesWarehouse && matchesSupplier;
  });

  const pageSize = viewMode === "cards" ? 12 : 10;
  const totalPages = Math.max(1, Math.ceil(filteredProducts.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const visibleProducts = filteredProducts.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  const hasActiveFilters =
    Boolean(search) ||
    Boolean(selectedCategoryId) ||
    Boolean(selectedWarehouseId) ||
    Boolean(selectedSupplierId);

  const error =
    products.error || warehouses.error || suppliers.error || categories.error;

  const handleDelete = async () => {
    if (!productToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteProduct(productToDelete.id);
      showToast({
        kind: "success",
        title: "Товар удалён",
        description: `Позиция "${productToDelete.name}" успешно удалена из каталога.`
      });
      setProductToDelete(null);
      products.refresh();
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить товар",
        description: getErrorMessage(
          requestError,
          "Проверьте связи товара с другими сущностями."
        )
      });
    } finally {
      setDeleteLoading(false);
    }
  };

  const resetFilters = () => {
    setSearch("");
    setSelectedCategoryId("");
    setSelectedWarehouseId("");
    setSelectedSupplierId("");
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroSurface}>
          <div className={styles.heroCopy}>
            <span className={styles.heroEyebrow}>Каталог техники</span>
            <h2>Управление ассортиментом, остатками и карточками товаров</h2>
            <p>Поиск, фильтрация и управление товарами в одном разделе.</p>
          </div>
          <div className={styles.heroActions}>
            <Link
              to="/products/new"
              className={getButtonClassName({ variant: "primary", size: "lg" })}
            >
              <Plus size={18} />
              <span>Создать товар</span>
            </Link>
          </div>
        </div>
      </section>

      <section className={styles.filtersBar}>
        <div className={styles.filtersGrid}>
          <Input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Поиск по названию"
            label="Поиск"
          />
          <Select
            value={selectedCategoryId}
            onChange={(event) => setSelectedCategoryId(event.target.value)}
            label="Категория"
            placeholder="Все категории"
            options={categories.data.map((category) => ({
              value: String(category.id),
              label: category.name
            }))}
          />
          <Select
            value={selectedWarehouseId}
            onChange={(event) => setSelectedWarehouseId(event.target.value)}
            label="Склад"
            placeholder="Все склады"
            options={warehouses.data.map((warehouse) => ({
              value: String(warehouse.id),
              label: warehouse.name
            }))}
          />
          <Select
            value={selectedSupplierId}
            onChange={(event) => setSelectedSupplierId(event.target.value)}
            label="Поставщик"
            placeholder="Все поставщики"
            options={suppliers.data.map((supplier) => ({
              value: String(supplier.id),
              label: supplier.name
            }))}
          />
        </div>
        <div className={styles.filterNote}>
          {hasActiveFilters ? (
            <Button variant="ghost" onClick={resetFilters}>
              Сбросить фильтры
            </Button>
          ) : null}
        </div>
      </section>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить каталог товаров.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      <section className={styles.sectionCard}>
        <div className={styles.resultsHeader}>
          <div className={styles.sectionTitleGroup}>
            <h3>Товары</h3>
            <p>
              Найдено {filteredProducts.length} из {products.data.totalElements}
            </p>
          </div>
          <div className={styles.viewToggle}>
            <button
              type="button"
              className={viewMode === "cards" ? styles.viewToggleActive : ""}
              onClick={() => setViewMode("cards")}
            >
              <LayoutGrid size={16} />
              <span>Карточки</span>
            </button>
            <button
              type="button"
              className={viewMode === "table" ? styles.viewToggleActive : ""}
              onClick={() => setViewMode("table")}
            >
              <List size={16} />
              <span>Таблица</span>
            </button>
          </div>
        </div>

        {products.loading ? (
          <Loader label="Загружаем каталог..." />
        ) : visibleProducts.length === 0 ? (
          <EmptyState
            title="Товары не найдены"
            description="Попробуйте сбросить фильтры или создать первую товарную позицию."
            action={
              <Link
                to="/products/new"
                className={getButtonClassName({ variant: "primary", size: "md" })}
              >
                Создать товар
              </Link>
            }
          />
        ) : viewMode === "cards" ? (
          <div className={styles.productsGrid}>
            {visibleProducts.map((product) => (
              <article
                key={product.id}
                className={[
                  styles.productCard,
                  product.quantity <= 5 ? styles.productCardLow : ""
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                <div className={styles.productCardHeader}>
                  <div className={styles.productCardTop}>
                    <Badge tone="accent">{product.sku || "SKU"}</Badge>
                    <Badge tone={getStockTone(product.quantity)}>
                      {getStockLabel(product.quantity)}
                    </Badge>
                  </div>
                  <div>
                    <h4 className={styles.productCardTitle}>{product.name}</h4>
                    <span className={styles.productCardSku}>ID #{product.id}</span>
                  </div>
                </div>

                <div className={styles.badgeRow}>
                  {product.categories.length ? (
                    product.categories.map((category) => (
                      <Badge key={`${product.id}-${category}`}>{category}</Badge>
                    ))
                  ) : (
                    <Badge tone="neutral">Без категории</Badge>
                  )}
                </div>

                <div className={styles.productCardMeta}>
                  <div>
                    <span>Склад</span>
                    <strong>{product.warehouseName ?? "Не указан"}</strong>
                  </div>
                  <div>
                    <span>Поставщик</span>
                    <strong>{product.supplierName ?? "Не указан"}</strong>
                  </div>
                  <div>
                    <span>Количество</span>
                    <strong>{product.quantity} шт</strong>
                  </div>
                  <div>
                    <span>Цена</span>
                    <strong>{formatCurrency(product.price)}</strong>
                  </div>
                </div>

                <p className={styles.productCardDescription}>
                  {product.description || "Описание не заполнено."}
                </p>

                <div className={styles.productCardFooter}>
                  <div className={styles.cardActions}>
                    <Link
                      to={`/products/${product.id}`}
                      className={getButtonClassName({
                        variant: "secondary",
                        size: "sm"
                      })}
                    >
                      <Eye size={16} />
                      <span>Подробнее</span>
                    </Link>
                    <Link
                      to={`/products/${product.id}/edit`}
                      className={getButtonClassName({
                        variant: "ghost",
                        size: "sm"
                      })}
                    >
                      <Pencil size={16} />
                      <span>Редактировать</span>
                    </Link>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => setProductToDelete(product)}
                    >
                      <Trash2 size={16} />
                      <span>Удалить</span>
                    </Button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>SKU</th>
                  <th>Название</th>
                  <th>Категории</th>
                  <th>Склад</th>
                  <th>Поставщик</th>
                  <th>Остаток</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {visibleProducts.map((product) => (
                  <tr key={product.id}>
                    <td>{product.id}</td>
                    <td>{product.sku || "—"}</td>
                    <td>
                      <strong>{product.name}</strong>
                    </td>
                    <td>
                      {product.categories.length
                        ? product.categories.join(", ")
                        : "—"}
                    </td>
                    <td>{product.warehouseName ?? "—"}</td>
                    <td>{product.supplierName ?? "—"}</td>
                    <td className={styles.stockCell}>
                      <span className={styles.stockValue}>{product.quantity}</span>
                      <span className={styles.stockHint}>
                        {getStockLabel(product.quantity)}
                      </span>
                    </td>
                    <td>
                      <div className={styles.cardActions}>
                        <Link
                          to={`/products/${product.id}`}
                          className={getButtonClassName({
                            variant: "secondary",
                            size: "sm"
                          })}
                        >
                          <Eye size={16} />
                        </Link>
                        <Link
                          to={`/products/${product.id}/edit`}
                          className={getButtonClassName({
                            variant: "ghost",
                            size: "sm"
                          })}
                        >
                          <Pencil size={16} />
                        </Link>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => setProductToDelete(product)}
                        >
                          <Trash2 size={16} />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {visibleProducts.length ? (
          <div className={styles.pagination}>
            <span className={styles.pageInfo}>
              Страница {currentPage} из {totalPages}
            </span>
            <div className={styles.paginationActions}>
              <Button
                variant="ghost"
                onClick={() => setPage((current) => Math.max(1, current - 1))}
                disabled={currentPage === 1}
              >
                Назад
              </Button>
              <Button
                variant="ghost"
                onClick={() =>
                  setPage((current) => Math.min(totalPages, current + 1))
                }
                disabled={currentPage === totalPages}
              >
                Вперёд
              </Button>
            </div>
          </div>
        ) : null}
      </section>

      <ConfirmDialog
        open={Boolean(productToDelete)}
        title="Удалить товар?"
        description={
          productToDelete
            ? `Позиция "${productToDelete.name}" будет удалена из каталога.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setProductToDelete(null)}
      />
    </div>
  );
}
