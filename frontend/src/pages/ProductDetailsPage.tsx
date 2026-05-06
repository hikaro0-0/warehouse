import { useEffect, useState } from "react";
import { ArrowLeft, Pencil, Trash2 } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getErrorMessage } from "../api/client";
import { deleteProduct, getProduct } from "../api/products";
import { Badge } from "../components/ui/Badge";
import { Button, getButtonClassName } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { Loader } from "../components/ui/Loader";
import { useToast } from "../components/ui/ToastProvider";
import type { Product } from "../types/product";
import { formatCurrency, formatDate } from "../utils/format";
import { getStockLabel, getStockTone } from "../utils/stock";
import styles from "./pages.module.css";

export function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    let active = true;

    const productId = Number(id);
    if (!productId) {
      setError("Некорректный идентификатор товара.");
      setLoading(false);
      return undefined;
    }

    const load = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await getProduct(productId);
        if (active) {
          setProduct(response);
        }
      } catch (requestError) {
        if (active) {
          setError(
            getErrorMessage(
              requestError,
              "Не удалось загрузить карточку товара"
            )
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void load();

    return () => {
      active = false;
    };
  }, [id]);

  const handleDelete = async () => {
    if (!product) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteProduct(product.id);
      showToast({
        kind: "success",
        title: "Товар удалён",
        description: `Позиция "${product.name}" удалена из системы.`
      });
      navigate("/products");
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить товар",
        description: getErrorMessage(
          requestError,
          "Проверьте связи товара и повторите попытку."
        )
      });
    } finally {
      setDeleteLoading(false);
      setConfirmOpen(false);
    }
  };

  if (loading) {
    return <Loader label="Открываем карточку товара..." />;
  }

  if (error || !product) {
    return (
      <div className={styles.page}>
        <div className={styles.errorBanner}>
          <strong>Не удалось открыть карточку товара.</strong>
          <span>{error ?? "Товар не найден."}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.detailActions}>
        <Link
          to="/products"
          className={getButtonClassName({ variant: "ghost", size: "md" })}
        >
          <ArrowLeft size={16} />
          <span>К каталогу</span>
        </Link>
        <Link
          to={`/products/${product.id}/edit`}
          className={getButtonClassName({ variant: "secondary", size: "md" })}
        >
          <Pencil size={16} />
          <span>Редактировать</span>
        </Link>
        <Button variant="danger" onClick={() => setConfirmOpen(true)}>
          <Trash2 size={16} />
          <span>Удалить</span>
        </Button>
      </div>

      <section className={styles.hero}>
        <div className={styles.heroSurface}>
          <div className={styles.heroCopy}>
            <span className={styles.heroEyebrow}>Карточка товара</span>
            <h2>{product.name}</h2>
            <p>
              Подробная информация по товарной позиции, привязкам, описанию,
              цене и текущему остатку.
            </p>
          </div>
          <div className={styles.heroActions}>
            <Badge tone="accent">{product.sku || "SKU"}</Badge>
            <Badge tone={getStockTone(product.quantity)}>
              {getStockLabel(product.quantity)} · {product.quantity} шт
            </Badge>
          </div>
        </div>
      </section>

      <section className={styles.detailGrid}>
        <article className={styles.detailPanel}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionTitleGroup}>
              <h3>Основная информация</h3>
              <p>Состояние карточки товара и общие поля</p>
            </div>
          </div>
          <div className={styles.detailList}>
            <div className={styles.detailListItem}>
              <span>Название</span>
              <strong className={styles.detailValue}>{product.name}</strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Артикул / SKU</span>
              <strong className={styles.detailValue}>{product.sku || "—"}</strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Описание</span>
              <strong className={styles.detailValue}>
                {product.description || "Описание не заполнено."}
              </strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Цена</span>
              <strong className={styles.detailValue}>
                {formatCurrency(product.price)}
              </strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Создан</span>
              <strong className={styles.detailValue}>
                {formatDate(product.createdAt)}
              </strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Обновлён</span>
              <strong className={styles.detailValue}>
                {formatDate(product.updatedAt)}
              </strong>
            </div>
          </div>
        </article>

        <article className={styles.detailPanel}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionTitleGroup}>
              <h3>Привязки и остатки</h3>
              <p>Склад, поставщик, категории и статус наличия</p>
            </div>
          </div>
          <div className={styles.detailList}>
            <div className={styles.detailListItem}>
              <span>Склад</span>
              <strong className={styles.detailValue}>
                {product.warehouseName ?? "Не указан"}
              </strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Поставщик</span>
              <strong className={styles.detailValue}>
                {product.supplierName ?? "Не указан"}
              </strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Категории</span>
              <div className={styles.badgeRow}>
                {product.categories.length ? (
                  product.categories.map((category) => (
                    <Badge key={category}>{category}</Badge>
                  ))
                ) : (
                  <Badge tone="neutral">Без категории</Badge>
                )}
              </div>
            </div>
            <div className={styles.detailListItem}>
              <span>Количество</span>
              <strong className={styles.detailValue}>{product.quantity} шт</strong>
            </div>
            <div className={styles.detailListItem}>
              <span>Статус</span>
              <Badge tone={getStockTone(product.quantity)}>
                {getStockLabel(product.quantity)}
              </Badge>
            </div>
          </div>
        </article>
      </section>

      <ConfirmDialog
        open={confirmOpen}
        title="Удалить товар?"
        description={`Позиция "${product.name}" будет удалена из системы.`}
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setConfirmOpen(false)}
      />
    </div>
  );
}
