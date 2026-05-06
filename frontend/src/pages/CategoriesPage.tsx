import { useState, type FormEvent } from "react";
import { Layers3, Pencil, Plus, Trash2 } from "lucide-react";
import { getErrorMessage } from "../api/client";
import {
  createCategory,
  deleteCategory,
  updateCategory
} from "../api/categories";
import { HeaderActionPortal } from "../components/layout/HeaderActionPortal";
import { Button } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Modal } from "../components/ui/Modal";
import { Textarea } from "../components/ui/Textarea";
import { useToast } from "../components/ui/ToastProvider";
import { useCategories } from "../hooks/useCategories";
import { useProducts } from "../hooks/useProducts";
import type { Category, CategoryFormValues } from "../types/category";
import styles from "./pages.module.css";

const initialForm: CategoryFormValues = {
  name: "",
  description: ""
};

export function CategoriesPage() {
  const { showToast } = useToast();
  const categories = useCategories();
  const products = useProducts({ page: 0, size: 200, sort: "id,desc" });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null);
  const [form, setForm] = useState<CategoryFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const error = categories.error || products.error;

  const openCreate = () => {
    setEditingCategory(null);
    setForm(initialForm);
    setErrors({});
    setModalOpen(true);
  };

  const openEdit = (category: Category) => {
    setEditingCategory(category);
    setForm({
      name: category.name,
      description: category.description
    });
    setErrors({});
    setModalOpen(true);
  };

  const countProducts = (categoryName: string) =>
    products.data.content.filter((product) =>
      product.categories.includes(categoryName)
    ).length;

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    if (!form.name.trim()) {
      nextErrors.name = "Название категории обязательно";
    }
    if (!form.description.trim()) {
      nextErrors.description = "Описание категории обязательно";
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
    try {
      if (editingCategory) {
        await updateCategory(editingCategory.id, form);
        showToast({
          kind: "success",
          title: "Категория обновлена",
          description: `Категория "${form.name}" сохранена.`
        });
      } else {
        await createCategory(form);
        showToast({
          kind: "success",
          title: "Категория создана",
          description: `Категория "${form.name}" добавлена.`
        });
      }
      categories.refresh();
      setModalOpen(false);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось сохранить категорию",
        description: getErrorMessage(
          requestError,
          "Проверьте введённые данные."
        )
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!categoryToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteCategory(categoryToDelete.id);
      categories.refresh();
      showToast({
        kind: "success",
        title: "Категория удалена",
        description: `Категория "${categoryToDelete.name}" удалена из базы.`
      });
      setCategoryToDelete(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить категорию",
        description: getErrorMessage(
          requestError,
          "Категория может быть связана с товарами."
        )
      });
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <HeaderActionPortal>
        <Button onClick={openCreate}>
          <Plus size={16} />
          <span>Создать категорию</span>
        </Button>
      </HeaderActionPortal>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить категории.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      {categories.loading ? (
        <Loader label="Загружаем категории..." />
      ) : categories.data.length === 0 ? (
        <EmptyState
          title="Категории ещё не созданы"
          description="Создайте первую категорию, чтобы структурировать каталог техники."
          action={<Button onClick={openCreate}>Создать категорию</Button>}
        />
      ) : (
        <div className={styles.entityGrid}>
          {categories.data.map((category) => (
            <article key={category.id} className={styles.entityCard}>
              <div className={styles.entityHeader}>
                <div>
                  <strong>{category.name}</strong>
                  <span>{countProducts(category.name)} товаров</span>
                </div>
                <Layers3 size={18} />
              </div>
              <p>{category.description}</p>
              <div className={styles.entityFooter}>
                <Button variant="secondary" size="sm" onClick={() => openEdit(category)}>
                  <Pencil size={16} />
                  <span>Редактировать</span>
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => setCategoryToDelete(category)}
                >
                  <Trash2 size={16} />
                  <span>Удалить</span>
                </Button>
              </div>
            </article>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        title={editingCategory ? "Редактировать категорию" : "Создать категорию"}
        description="Категория отправляется в backend со всеми полями: name и description."
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="ghost" onClick={() => setModalOpen(false)}>
              Отмена
            </Button>
            <Button form="category-form" type="submit" loading={submitting}>
              Сохранить
            </Button>
          </>
        }
      >
        <form id="category-form" className={styles.stack} onSubmit={handleSubmit}>
          <Input
            label="Название категории"
            value={form.name}
            onChange={(event) =>
              setForm((current) => ({ ...current, name: event.target.value }))
            }
            error={errors.name}
          />
          <Textarea
            label="Описание"
            rows={4}
            value={form.description}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                description: event.target.value
              }))
            }
            error={errors.description}
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(categoryToDelete)}
        title="Удалить категорию?"
        description={
          categoryToDelete
            ? `Категория "${categoryToDelete.name}" будет удалена из базы.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setCategoryToDelete(null)}
      />
    </div>
  );
}
