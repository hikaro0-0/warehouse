import { useState, type FormEvent } from "react";
import { Mail, Pencil, Plus, Trash2, Truck } from "lucide-react";
import { getErrorMessage } from "../api/client";
import {
  createSupplier,
  deleteSupplier,
  updateSupplier
} from "../api/suppliers";
import { Button } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Modal } from "../components/ui/Modal";
import { useToast } from "../components/ui/ToastProvider";
import { useProducts } from "../hooks/useProducts";
import { useSuppliers } from "../hooks/useSuppliers";
import type { Supplier, SupplierFormValues } from "../types/supplier";
import styles from "./pages.module.css";

const initialForm: SupplierFormValues = {
  name: "",
  email: "",
  phone: "",
  address: "",
  notes: ""
};

export function SuppliersPage() {
  const { showToast } = useToast();
  const suppliers = useSuppliers();
  const products = useProducts({ page: 0, size: 200, sort: "id,desc" });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const [supplierToDelete, setSupplierToDelete] = useState<Supplier | null>(null);
  const [form, setForm] = useState<SupplierFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const error = suppliers.error || products.error;

  const openCreate = () => {
    setEditingSupplier(null);
    setForm(initialForm);
    setErrors({});
    setModalOpen(true);
  };

  const openEdit = (supplier: Supplier) => {
    setEditingSupplier(supplier);
    setForm({
      name: supplier.name,
      email: supplier.email,
      phone: supplier.phone ?? "",
      address: supplier.address ?? "",
      notes: supplier.notes ?? ""
    });
    setErrors({});
    setModalOpen(true);
  };

  const countProducts = (supplierId: number) =>
    products.data.content.filter((product) => product.supplierId === supplierId)
      .length;

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    if (!form.name.trim()) {
      nextErrors.name = "Название поставщика обязательно";
    }
    if (!form.email.trim()) {
      nextErrors.email = "Email обязателен";
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
      if (editingSupplier) {
        await updateSupplier(editingSupplier.id, form);
        showToast({
          kind: "success",
          title: "Поставщик обновлён",
          description: `Данные по "${form.name}" сохранены.`
        });
      } else {
        await createSupplier(form);
        showToast({
          kind: "success",
          title: "Поставщик создан",
          description: `Поставщик "${form.name}" добавлен в систему.`
        });
      }
      suppliers.refresh();
      setModalOpen(false);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось сохранить поставщика",
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
    if (!supplierToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteSupplier(supplierToDelete.id);
      suppliers.refresh();
      showToast({
        kind: "success",
        title: "Поставщик удалён",
        description: `Поставщик "${supplierToDelete.name}" удалён из системы.`
      });
      setSupplierToDelete(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить поставщика",
        description: getErrorMessage(
          requestError,
          "Поставщик может быть связан с товарами."
        )
      });
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroSurface}>
          <div className={styles.heroCopy}>
            <span className={styles.heroEyebrow}>Поставщики</span>
            <h2>Управление дистрибьюторами и партнёрами</h2>
            <p>База партнёров и поставщиков для карточек товаров.</p>
          </div>
          <div className={styles.heroActions}>
            <Button onClick={openCreate}>
              <Plus size={16} />
              <span>Создать поставщика</span>
            </Button>
          </div>
        </div>
      </section>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить поставщиков.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      {suppliers.loading ? (
        <Loader label="Загружаем поставщиков..." />
      ) : suppliers.data.length === 0 ? (
        <EmptyState
          title="Список поставщиков пуст"
          description="Добавьте первого партнёра, чтобы привязывать его к товарам."
          action={<Button onClick={openCreate}>Создать поставщика</Button>}
        />
      ) : (
        <div className={styles.entityGrid}>
          {suppliers.data.map((supplier) => (
            <article key={supplier.id} className={styles.entityCard}>
              <div className={styles.entityHeader}>
                <div>
                  <strong>{supplier.name}</strong>
                </div>
                <Truck size={18} />
              </div>
              <div className={styles.entityMeta}>
                <span>
                  <Mail size={15} />
                  {supplier.email}
                </span>
                <span>{countProducts(supplier.id)} товаров в поставке</span>
              </div>
              <div className={styles.entityFooter}>
                <Button variant="ghost" size="sm" onClick={() => openEdit(supplier)}>
                  <Pencil size={16} />
                  <span>Редактировать</span>
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => setSupplierToDelete(supplier)}
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
        title={
          editingSupplier ? "Редактировать поставщика" : "Создать поставщика"
        }
        description="Сейчас backend сохраняет имя и email. Остальные поля можно активировать после расширения DTO."
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="ghost" onClick={() => setModalOpen(false)}>
              Отмена
            </Button>
            <Button form="supplier-form" type="submit" loading={submitting}>
              Сохранить
            </Button>
          </>
        }
      >
        <form id="supplier-form" className={styles.stack} onSubmit={handleSubmit}>
          <Input
            label="Название"
            value={form.name}
            onChange={(event) =>
              setForm((current) => ({ ...current, name: event.target.value }))
            }
            error={errors.name}
          />
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) =>
              setForm((current) => ({ ...current, email: event.target.value }))
            }
            error={errors.email}
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(supplierToDelete)}
        title="Удалить поставщика?"
        description={
          supplierToDelete
            ? `Поставщик "${supplierToDelete.name}" будет удалён из базы.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setSupplierToDelete(null)}
      />
    </div>
  );
}
