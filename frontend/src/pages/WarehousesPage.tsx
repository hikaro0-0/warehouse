import { useState, type FormEvent } from "react";
import { MapPin, Pencil, Plus, Trash2, Warehouse as WarehouseIcon } from "lucide-react";
import { getErrorMessage } from "../api/client";
import { createWarehouse, deleteWarehouse, updateWarehouse } from "../api/warehouses";
import { Button } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Modal } from "../components/ui/Modal";
import { useToast } from "../components/ui/ToastProvider";
import { useProducts } from "../hooks/useProducts";
import { useWarehouses } from "../hooks/useWarehouses";
import type { WarehouseFormValues, Warehouse } from "../types/warehouse";
import styles from "./pages.module.css";

const initialForm: WarehouseFormValues = {
  name: "",
  address: "",
  description: "",
  contactInfo: ""
};

export function WarehousesPage() {
  const { showToast } = useToast();
  const warehouses = useWarehouses();
  const products = useProducts({ page: 0, size: 200, sort: "id,desc" });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null);
  const [warehouseToDelete, setWarehouseToDelete] = useState<Warehouse | null>(null);
  const [form, setForm] = useState<WarehouseFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const error = warehouses.error || products.error;

  const openCreate = () => {
    setEditingWarehouse(null);
    setForm(initialForm);
    setErrors({});
    setModalOpen(true);
  };

  const openEdit = (warehouse: Warehouse) => {
    setEditingWarehouse(warehouse);
    setForm({
      name: warehouse.name,
      address: warehouse.address,
      description: warehouse.description ?? "",
      contactInfo: warehouse.contactInfo ?? ""
    });
    setErrors({});
    setModalOpen(true);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    if (!form.name.trim()) {
      nextErrors.name = "Название склада обязательно";
    }
    if (!form.address.trim()) {
      nextErrors.address = "Адрес обязателен";
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const countProducts = (warehouseId: number) =>
    products.data.content.filter((product) => product.warehouseId === warehouseId)
      .length;

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }

    setSubmitting(true);
    try {
      if (editingWarehouse) {
        await updateWarehouse(editingWarehouse.id, form);
        showToast({
          kind: "success",
          title: "Склад обновлён",
          description: `Данные по складу "${form.name}" сохранены.`
        });
      } else {
        await createWarehouse(form);
        showToast({
          kind: "success",
          title: "Склад создан",
          description: `Новый склад "${form.name}" добавлен.`
        });
      }
      warehouses.refresh();
      setModalOpen(false);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось сохранить склад",
        description: getErrorMessage(
          requestError,
          "Проверьте введённые данные и повторите попытку."
        )
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!warehouseToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteWarehouse(warehouseToDelete.id);
      warehouses.refresh();
      showToast({
        kind: "success",
        title: "Склад удалён",
        description: `Склад "${warehouseToDelete.name}" удалён из системы.`
      });
      setWarehouseToDelete(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить склад",
        description: getErrorMessage(
          requestError,
          "Склад может быть связан с товарами."
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
            <span className={styles.heroEyebrow}>Склады</span>
            <h2>Локации хранения и распределения техники</h2>
            <p>Управляй площадками хранения и распределения техники.</p>
          </div>
          <div className={styles.heroActions}>
            <Button onClick={openCreate}>
              <Plus size={16} />
              <span>Создать склад</span>
            </Button>
          </div>
        </div>
      </section>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить список складов.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      {warehouses.loading ? (
        <Loader label="Загружаем склады..." />
      ) : warehouses.data.length === 0 ? (
        <EmptyState
          title="Склады ещё не созданы"
          description="Добавьте первую площадку хранения, чтобы начать распределять товары."
          action={<Button onClick={openCreate}>Создать склад</Button>}
        />
      ) : (
        <div className={styles.entityGrid}>
          {warehouses.data.map((warehouse) => (
            <article key={warehouse.id} className={styles.entityCard}>
              <div className={styles.entityHeader}>
                <div>
                  <strong>{warehouse.name}</strong>
                </div>
                <WarehouseIcon size={18} />
              </div>
              <div className={styles.entityMeta}>
                <span>
                  <MapPin size={15} />
                  {warehouse.address}
                </span>
                <span>{countProducts(warehouse.id)} товаров привязано</span>
              </div>
              <div className={styles.entityFooter}>
                <Button variant="ghost" size="sm" onClick={() => openEdit(warehouse)}>
                  <Pencil size={16} />
                  <span>Редактировать</span>
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => setWarehouseToDelete(warehouse)}
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
        title={editingWarehouse ? "Редактировать склад" : "Создать склад"}
        description="Минимальный набор полей полностью совместим с текущим backend DTO."
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="ghost" onClick={() => setModalOpen(false)}>
              Отмена
            </Button>
            <Button form="warehouse-form" type="submit" loading={submitting}>
              Сохранить
            </Button>
          </>
        }
      >
        <form id="warehouse-form" className={styles.stack} onSubmit={handleSubmit}>
          <Input
            label="Название склада"
            value={form.name}
            onChange={(event) =>
              setForm((current) => ({ ...current, name: event.target.value }))
            }
            error={errors.name}
          />
          <Input
            label="Адрес"
            value={form.address}
            onChange={(event) =>
              setForm((current) => ({ ...current, address: event.target.value }))
            }
            error={errors.address}
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(warehouseToDelete)}
        title="Удалить склад?"
        description={
          warehouseToDelete
            ? `Склад "${warehouseToDelete.name}" будет удалён из системы.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setWarehouseToDelete(null)}
      />
    </div>
  );
}
