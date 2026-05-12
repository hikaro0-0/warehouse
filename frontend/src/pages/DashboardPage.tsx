import {
  ArrowRight,
  Building2,
  Boxes,
  Layers3,
  Package,
  Plus,
  Send,
  Truck
} from "lucide-react";
import { Link } from "react-router-dom";
import { Badge } from "../components/ui/Badge";
import { EmptyState } from "../components/ui/EmptyState";
import { Loader } from "../components/ui/Loader";
import { useCategories } from "../hooks/useCategories";
import { useProducts } from "../hooks/useProducts";
import { useSuppliers } from "../hooks/useSuppliers";
import { useWarehouses } from "../hooks/useWarehouses";
import { formatCompactNumber } from "../utils/format";
import { getStockLabel, getStockTone } from "../utils/stock";
import styles from "./pages.module.css";

export function DashboardPage() {
  const products = useProducts({ page: 0, size: 48, sort: "id,desc" });
  const warehouses = useWarehouses();
  const suppliers = useSuppliers();
  const categories = useCategories();

  const isLoading =
    products.loading &&
    warehouses.loading &&
    suppliers.loading &&
    categories.loading;

  const error =
    products.error || warehouses.error || suppliers.error || categories.error;

  const lowStockProducts = [...products.data.content]
    .filter((product) => product.quantity <= 5)
    .sort((left, right) => left.quantity - right.quantity)
    .slice(0, 5);

  const latestProductsByDate = [...products.data.content]
    .sort((left, right) => {
    const leftTime = left.createdAt ? new Date(left.createdAt).getTime() : 0;
    const rightTime = right.createdAt ? new Date(right.createdAt).getTime() : 0;

    return rightTime - leftTime;
    })
    .slice(0, 6);

  const stats = [
    {
      label: "Товаров",
      value: products.data.totalElements,
      note: "активных SKU в системе",
      icon: Package
    },
    {
      label: "Складов",
      value: warehouses.data.length,
      note: "площадок хранения",
      icon: Boxes
    },
    {
      label: "Поставщиков",
      value: suppliers.data.length,
      note: "контрагентов в базе",
      icon: Truck
    },
    {
      label: "Категорий",
      value: categories.data.length,
      note: "категорий техники",
      icon: Layers3
    }
  ];

  if (isLoading) {
    return <Loader label="Собираем дашборд склада..." />;
  }

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroSurface}>
          <div className={styles.heroCopy}>
            <span className={styles.heroEyebrow}>Warehouse Hub</span>
            <h2>Современная панель складского учёта техники</h2>
            <p>
              Контролируйте остатки, распределение товаров по складам,
              поставщиков, категории и исходящие отгрузки в одном интерфейсе.
            </p>
            <div className={styles.heroActions}>
              <Link to="/products/new" className="primary-link-button">
                <Plus size={16} />
                <span>Добавить товар</span>
              </Link>
              <Link to="/products" className="secondary-link-button">
                <span>Открыть каталог</span>
                <ArrowRight size={16} />
              </Link>
            </div>
          </div>

          <div className={styles.heroStats}>
            {stats.map((item) => {
              const Icon = item.icon;
              return (
                <article key={item.label} className={styles.statCard}>
                  <div className={styles.statIcon}>
                    <Icon size={18} />
                  </div>
                  <span className={styles.statLabel}>{item.label}</span>
                  <strong className={styles.statValue}>
                    {formatCompactNumber(item.value)}
                  </strong>
                  <span className={styles.statNote}>{item.note}</span>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось полностью собрать dashboard.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      <section className={styles.metricStrip}>
        <div>
          <span>Низкий остаток</span>
          <strong>{lowStockProducts.length}</strong>
        </div>
        <div>
          <span>Последние позиции</span>
          <strong>{latestProductsByDate.length}</strong>
        </div>
        <div>
          <span>Всего категорий</span>
          <strong>{categories.data.length}</strong>
        </div>
      </section>

      <section className={styles.sectionGrid}>
        <article className={styles.sectionCard}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionTitleGroup}>
              <h3>Товары с низким остатком</h3>
              <p>Требуют внимания в первую очередь</p>
            </div>
          </div>

          <div className={styles.sectionBody}>
            {lowStockProducts.length ? (
              <div className={styles.list}>
                {lowStockProducts.map((product) => (
                  <div key={product.id} className={styles.listItem}>
                    <div className={styles.listAccent} />
                    <div className={styles.listMeta}>
                      <strong>{product.name}</strong>
                      <span>
                        {product.warehouseName ?? "Склад не указан"} ·{" "}
                        {product.supplierName ?? "Поставщик не указан"}
                      </span>
                    </div>
                    <Badge tone={getStockTone(product.quantity)}>
                      {getStockLabel(product.quantity)} · {product.quantity} шт
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Все под контролем"
                description="Сейчас нет товаров с низким остатком."
              />
            )}
          </div>
        </article>

        <article className={styles.sectionCard}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionTitleGroup}>
              <h3>Последние добавленные товары</h3>
            </div>
          </div>

          <div className={styles.productQuickGrid}>
            {latestProductsByDate.map((product) => (
              <Link
                key={product.id}
                to={`/products/${product.id}`}
                className={styles.productPreviewCard}
              >
                <div className={styles.productPreviewMeta}>
                  <Badge tone="accent">{product.sku || "SKU"}</Badge>
                  <Badge tone={getStockTone(product.quantity)}>
                    {product.quantity} шт
                  </Badge>
                </div>
                <strong>{product.name}</strong>
                <span>
                  {product.categories.length
                    ? product.categories.join(", ")
                    : "Категории пока не назначены"}
                </span>
              </Link>
            ))}
          </div>
        </article>
      </section>

      <section className={styles.quickLinks}>
        <Link to="/products/new" className={styles.quickLink}>
          <Package size={18} />
          <div>
            <strong>Добавить товар</strong>
            <span>Новая техника, аксессуар или партия</span>
          </div>
        </Link>
        <Link to="/warehouses" className={styles.quickLink}>
          <Boxes size={18} />
          <div>
            <strong>Добавить склад</strong>
            <span>Локация хранения или распределения</span>
          </div>
        </Link>
        <Link to="/suppliers" className={styles.quickLink}>
          <Truck size={18} />
          <div>
            <strong>Добавить поставщика</strong>
            <span>Новый партнёр или дистрибьютор</span>
          </div>
        </Link>
        <Link to="/categories" className={styles.quickLink}>
          <Layers3 size={18} />
          <div>
            <strong>Добавить категорию</strong>
            <span>Например, ноутбуки или аксессуары</span>
          </div>
        </Link>
        <Link to="/clients" className={styles.quickLink}>
          <Building2 size={18} />
          <div>
            <strong>Добавить клиента</strong>
            <span>Фирма или магазин для отгрузки</span>
          </div>
        </Link>
        <Link to="/dispatches" className={styles.quickLink}>
          <Send size={18} />
          <div>
            <strong>Оформить отгрузку</strong>
            <span>Списать товар со склада в один документ</span>
          </div>
        </Link>
      </section>
    </div>
  );
}
