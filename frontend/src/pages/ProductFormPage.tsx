import { startTransition, useEffect, useState, type FormEvent } from "react";
import { Info, Save } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import { getErrorMessage } from "../api/client";
import { createProduct, getProduct, updateProduct } from "../api/products";
import { Button } from "../components/ui/Button";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Textarea } from "../components/ui/Textarea";
import { useToast } from "../components/ui/ToastProvider";
import { useCategories } from "../hooks/useCategories";
import { useSuppliers } from "../hooks/useSuppliers";
import { useWarehouses } from "../hooks/useWarehouses";
import type { Product, ProductFormValues } from "../types/product";
import styles from "./pages.module.css";

const initialForm: ProductFormValues = {
  sku: "",
  name: "",
  description: "",
  quantity: 0,
  warehouseId: "",
  supplierId: "",
  categoryIds: [],
  price: ""
};

export function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const warehouses = useWarehouses();
  const suppliers = useSuppliers();
  const categories = useCategories();
  const [form, setForm] = useState<ProductFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [product, setProduct] = useState<Product | null>(null);
  const [loadingProduct, setLoadingProduct] = useState(Boolean(id));
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const isEditing = Boolean(id);
  const referencesLoading =
    warehouses.loading || suppliers.loading || categories.loading;
  const referencesError =
    warehouses.error || suppliers.error || categories.error;

  useEffect(() => {
    if (!id) {
      setLoadingProduct(false);
      return;
    }

    let active = true;

    const loadProduct = async () => {
      setLoadingProduct(true);
      setLoadError(null);
      try {
        const response = await getProduct(Number(id));
        if (active) {
          setProduct(response);
        }
      } catch (requestError) {
        if (active) {
          setLoadError(getErrorMessage(requestError, "Не удалось загрузить товар"));
        }
      } finally {
        if (active) {
          setLoadingProduct(false);
        }
      }
    };

    void loadProduct();

    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    if (!product) {
      return;
    }

    const categoryIds = categories.data
      .filter((category) => product.categories.includes(category.name))
      .map((category) => category.id);

    setForm({
      sku: product.sku ?? "",
      name: product.name ?? "",
      description: product.description ?? "",
      quantity: product.quantity ?? 0,
      warehouseId: product.warehouseId ? String(product.warehouseId) : "",
      supplierId: product.supplierId ? String(product.supplierId) : "",
      categoryIds,
      price: product.price != null ? String(product.price) : ""
    });
  }, [product, categories.data]);

  const setField = <K extends keyof ProductFormValues>(
    field: K,
    value: ProductFormValues[K]
  ) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: "" }));
  };

  const toggleCategory = (categoryId: number) => {
    setForm((current) => ({
      ...current,
      categoryIds: current.categoryIds.includes(categoryId)
        ? current.categoryIds.filter((idValue) => idValue !== categoryId)
        : [...current.categoryIds, categoryId]
    }));
    setErrors((current) => ({ ...current, categoryIds: "" }));
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};

    if (!form.sku.trim()) {
      nextErrors.sku = "SKU обязателен";
    }
    if (!form.name.trim()) {
      nextErrors.name = "Название обязательно";
    }
    if (Number.isNaN(form.quantity) || Number(form.quantity) < 0) {
      nextErrors.quantity = "Количество не может быть отрицательным";
    }
    if (!form.warehouseId) {
      nextErrors.warehouseId = "Выберите склад";
    }
    if (!form.supplierId) {
      nextErrors.supplierId = "Выберите поставщика";
    }
    if (!form.categoryIds.length) {
      nextErrors.categoryIds = "Нужно выбрать хотя бы одну категорию";
    }
    if (form.price && Number.isNaN(Number(form.price))) {
      nextErrors.price = "Цена должна быть числом";
    }
    if (form.price && Number(form.price) < 0) {
      nextErrors.price = "Цена не может быть отрицательной";
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }

    setSubmitting(true);
    setSubmitError(null);

    try {
      if (isEditing && id) {
        await updateProduct(Number(id), form);
        showToast({
          kind: "success",
          title: "Товар обновлён",
          description: `Изменения для "${form.name}" сохранены.`
        });
      } else {
        await createProduct(form);
        showToast({
          kind: "success",
          title: "Товар создан",
          description: `Позиция "${form.name}" добавлена в каталог.`
        });
      }

      startTransition(() => {
        navigate("/products");
      });
    } catch (requestError) {
      const message =
        getErrorMessage(requestError, "Не удалось сохранить товар");
      setSubmitError(message);
      showToast({
        kind: "error",
        title: "Ошибка сохранения",
        description: message
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (referencesLoading || loadingProduct) {
    return <Loader label="Подготавливаем форму товара..." />;
  }

  if (referencesError || loadError) {
    return (
      <div className={styles.page}>
        <div className={styles.errorBanner}>
          <strong>Не удалось открыть форму.</strong>
          <span>{referencesError ?? loadError}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <section className={styles.formShell}>
        <form className={styles.formPanel} onSubmit={handleSubmit}>
          <div className={styles.formHeader}>
            <div>
              <span className={styles.heroEyebrow}>
                {isEditing ? "Редактирование товара" : "Новый товар"}
              </span>
              <h2>
                {isEditing
                  ? "Обновить карточку складской позиции"
                  : "Создать новую карточку товара"}
              </h2>
              <p>
                Форма уже совпадает с текущим Spring Boot API и теперь
                сохраняет не только основные привязки товара, но и описание с
                ценой.
              </p>
            </div>
          </div>

          <div className={styles.noteCard}>
            <Info size={18} />
            <div>
              <strong>Описание и цена уже сохраняются</strong>
              <p>
                Если оставить поле пустым, значение сохранится как `null`. Для
                цены можно указывать обычное число, например `1299.99`.
              </p>
            </div>
          </div>

          <div className={styles.formGrid}>
            <Input
              label="Артикул / SKU"
              value={form.sku}
              onChange={(event) => setField("sku", event.target.value)}
              error={errors.sku}
              placeholder="SKU-1001"
            />
            <Input
              label="Название товара"
              value={form.name}
              onChange={(event) => setField("name", event.target.value)}
              error={errors.name}
              placeholder="Например, MacBook Air 15"
            />
            <Input
              label="Количество"
              type="number"
              min={0}
              value={form.quantity}
              onChange={(event) =>
                setField("quantity", Number(event.target.value))
              }
              error={errors.quantity}
            />
            <label className={styles.stack}>
              <span className="field-caption">Склад</span>
              <select
                className="native-select"
                value={form.warehouseId}
                onChange={(event) => setField("warehouseId", event.target.value)}
              >
                <option value="">Выберите склад</option>
                {warehouses.data.map((warehouse) => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name}
                  </option>
                ))}
              </select>
              {errors.warehouseId ? (
                <span className="field-error-text">{errors.warehouseId}</span>
              ) : null}
            </label>
            <label className={styles.stack}>
              <span className="field-caption">Поставщик</span>
              <select
                className="native-select"
                value={form.supplierId}
                onChange={(event) => setField("supplierId", event.target.value)}
              >
                <option value="">Выберите поставщика</option>
                {suppliers.data.map((supplier) => (
                  <option key={supplier.id} value={supplier.id}>
                    {supplier.name}
                  </option>
                ))}
              </select>
              {errors.supplierId ? (
                <span className="field-error-text">{errors.supplierId}</span>
              ) : null}
            </label>
            <Input
              label="Цена"
              type="number"
              min={0}
              step="0.01"
              value={form.price}
              onChange={(event) => setField("price", event.target.value)}
              placeholder="Например, 1299"
              error={errors.price}
              hint="Можно оставить пустым, если цена пока неизвестна."
            />
          </div>

          <Textarea
            label="Описание"
            value={form.description}
            onChange={(event) => setField("description", event.target.value)}
            rows={4}
            hint="Например: характеристики, состояние, комплектность или примечания."
          />

          <div className={styles.stack}>
            <span className="field-caption">Категории</span>
            <div className={styles.checkboxGrid}>
              {categories.data.map((category) => {
                const isSelected = form.categoryIds.includes(category.id);

                return (
                  <label
                    key={category.id}
                    className={[
                      styles.checkboxCard,
                      isSelected ? styles.checkboxCardActive : ""
                    ]
                      .filter(Boolean)
                      .join(" ")}
                  >
                    <input
                      type="checkbox"
                      checked={isSelected}
                      onChange={() => toggleCategory(category.id)}
                    />
                    <div>
                      <strong>{category.name}</strong>
                      <span className={styles.checkboxMeta}>
                        {category.description}
                      </span>
                    </div>
                  </label>
                );
              })}
            </div>
            {errors.categoryIds ? (
              <span className="field-error-text">{errors.categoryIds}</span>
            ) : null}
          </div>

          {submitError ? (
            <div className={styles.errorBanner}>
              <strong>Ошибка сохранения.</strong>
              <span>{submitError}</span>
            </div>
          ) : null}

          <div className={styles.formActions}>
            <Button
              type="button"
              variant="ghost"
              onClick={() => navigate("/products")}
            >
              Отмена
            </Button>
            <Button type="submit" loading={submitting}>
              <Save size={16} />
              <span>{isEditing ? "Сохранить изменения" : "Создать товар"}</span>
            </Button>
          </div>
        </form>
      </section>
    </div>
  );
}
