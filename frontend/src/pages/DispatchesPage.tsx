import { useMemo, useState, type FormEvent } from "react";
import {
  Building2,
  Package,
  Pencil,
  Plus,
  Send,
  Store,
  Trash2
} from "lucide-react";
import { getErrorMessage } from "../api/client";
import {
  cancelDispatch,
  confirmDispatch,
  createDispatch,
  deleteDispatch,
  updateDispatch
} from "../api/dispatches";
import { HeaderActionPortal } from "../components/layout/HeaderActionPortal";
import { Badge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Modal } from "../components/ui/Modal";
import { Select } from "../components/ui/Select";
import { useToast } from "../components/ui/ToastProvider";
import { useDispatches } from "../hooks/useDispatches";
import { useProducts } from "../hooks/useProducts";
import { useRecipients } from "../hooks/useRecipients";
import { useWarehouses } from "../hooks/useWarehouses";
import type {
  Dispatch,
  DispatchFormValues,
  DispatchItem,
  DispatchStatus
} from "../types/dispatch";
import {
  getDispatchStatusLabel,
  getDispatchStatusTone,
  isDispatchEditable
} from "../types/dispatch";
import { getRecipientTypeLabel } from "../types/recipient";
import { formatDate } from "../utils/format";
import { getStockLabel, getStockTone } from "../utils/stock";
import styles from "./pages.module.css";

const createEmptyItem = () => ({
  productId: "",
  quantity: "1"
});

const initialForm: DispatchFormValues = {
  referenceNumber: "",
  warehouseId: "",
  recipientId: "",
  items: [createEmptyItem()]
};

export function DispatchesPage() {
  const { showToast } = useToast();
  const dispatches = useDispatches();
  const warehouses = useWarehouses();
  const recipients = useRecipients();
  const products = useProducts({ page: 0, size: 200, sort: "name,asc" });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingDispatch, setEditingDispatch] = useState<Dispatch | null>(null);
  const [dispatchToDelete, setDispatchToDelete] = useState<Dispatch | null>(
    null
  );
  const [dispatchToConfirm, setDispatchToConfirm] = useState<Dispatch | null>(
    null
  );
  const [dispatchToCancel, setDispatchToCancel] = useState<Dispatch | null>(
    null
  );
  const [form, setForm] = useState<DispatchFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [cancelLoading, setCancelLoading] = useState(false);

  const error =
    dispatches.error || warehouses.error || recipients.error || products.error;

  const selectedWarehouseProducts = useMemo(() => {
    return products.data.content
      .filter((product) => {
        if (!form.warehouseId) {
          return true;
        }

        const belongsToWarehouse =
          String(product.warehouseId ?? "") === form.warehouseId;
        const alreadySelected = form.items.some(
          (item) => item.productId === String(product.id)
        );

        return belongsToWarehouse || alreadySelected;
      })
      .sort((left, right) => left.name.localeCompare(right.name, "ru"));
  }, [form.items, form.warehouseId, products.data.content]);

  const openCreate = () => {
    setEditingDispatch(null);
    setForm(initialForm);
    setErrors({});
    setModalOpen(true);
  };

  const openEdit = (dispatch: Dispatch) => {
    if (!isDispatchEditable(dispatch.status)) {
      return;
    }

    setEditingDispatch(dispatch);
    setForm({
      referenceNumber: dispatch.referenceNumber,
      warehouseId: dispatch.warehouseId != null ? String(dispatch.warehouseId) : "",
      recipientId: dispatch.recipientId != null ? String(dispatch.recipientId) : "",
      items:
        dispatch.items.length > 0
          ? dispatch.items.map((item) => ({
              productId: String(item.productId),
              quantity: String(item.quantity)
            }))
          : [createEmptyItem()]
    });
    setErrors({});
    setModalOpen(true);
  };

  const resetAndCloseModal = () => {
    setModalOpen(false);
    setEditingDispatch(null);
    setForm(initialForm);
    setErrors({});
  };

  const setWarehouse = (warehouseId: string) => {
    setForm((current) => ({
      ...current,
      warehouseId,
      items: current.items.map((item) => {
        if (!item.productId) {
          return item;
        }

        const product = products.data.content.find(
          (candidate) => String(candidate.id) === item.productId
        );
        if (!product) {
          return { ...item, productId: "" };
        }

        return String(product.warehouseId ?? "") === warehouseId
          ? item
          : { ...item, productId: "" };
      })
    }));
  };

  const updateItem = (
    index: number,
    field: "productId" | "quantity",
    value: string
  ) => {
    setForm((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      )
    }));
  };

  const addItem = () => {
    setForm((current) => ({
      ...current,
      items: [...current.items, createEmptyItem()]
    }));
  };

  const removeItem = (index: number) => {
    setForm((current) => ({
      ...current,
      items:
        current.items.length > 1
          ? current.items.filter((_, itemIndex) => itemIndex !== index)
          : [createEmptyItem()]
    }));
  };

  const getAvailableQuantity = (productId: string): number | null => {
    if (!productId) {
      return null;
    }

    const product = products.data.content.find(
      (candidate) => String(candidate.id) === productId
    );

    if (!product) {
      return null;
    }

    return product.quantity;
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};

    if (!form.referenceNumber.trim()) {
      nextErrors.referenceNumber = "Номер отгрузки обязателен";
    }

    if (!form.warehouseId) {
      nextErrors.warehouseId = "Выбери склад";
    }

    if (!form.recipientId) {
      nextErrors.recipientId = "Выбери клиента";
    }

    if (form.items.length === 0) {
      nextErrors.items = "Добавь хотя бы один товар";
    }

    const selectedProducts = new Set<string>();

    form.items.forEach((item, index) => {
      if (!item.productId) {
        nextErrors[`productId-${index}`] = "Выбери товар";
      } else if (selectedProducts.has(item.productId)) {
        nextErrors[`productId-${index}`] = "Товар уже выбран в этой отгрузке";
      } else {
        selectedProducts.add(item.productId);
      }

      const quantity = Number(item.quantity);
      if (!item.quantity || Number.isNaN(quantity) || quantity <= 0) {
        nextErrors[`quantity-${index}`] = "Количество должно быть больше 0";
      } else if (item.productId) {
        const availableQuantity = getAvailableQuantity(item.productId);
        if (availableQuantity != null && quantity > availableQuantity) {
          nextErrors[`quantity-${index}`] =
            `Доступно только ${availableQuantity} шт`;
        }
      }
    });

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
      if (editingDispatch) {
        await updateDispatch(editingDispatch.id, form);
        showToast({
          kind: "success",
          title: "Черновик отгрузки обновлён",
          description: `Документ "${form.referenceNumber}" сохранён без списания товара.`
        });
      } else {
        await createDispatch(form);
        showToast({
          kind: "success",
          title: "Черновик отгрузки создан",
          description: `Документ "${form.referenceNumber}" ждёт подтверждения перед списанием товара.`
        });
      }

      dispatches.refresh();
      products.refresh();
      resetAndCloseModal();
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось сохранить отгрузку",
        description: getErrorMessage(
          requestError,
          "Проверь количество и склад у выбранных товаров."
        )
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!dispatchToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteDispatch(dispatchToDelete.id);
      dispatches.refresh();
      products.refresh();
      showToast({
        kind: "success",
        title: "Отгрузка удалена",
        description: `Документ "${dispatchToDelete.referenceNumber}" удалён из списка отгрузок.`
      });
      setDispatchToDelete(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить отгрузку",
        description: getErrorMessage(
          requestError,
          "Попробуйте ещё раз или проверьте связи документа."
        )
      });
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!dispatchToConfirm) {
      return;
    }

    setConfirmLoading(true);
    try {
      await confirmDispatch(dispatchToConfirm.id);
      dispatches.refresh();
      products.refresh();
      showToast({
        kind: "success",
        title: "Отгрузка подтверждена",
        description: `Товар по документу "${dispatchToConfirm.referenceNumber}" списан со склада.`
      });
      setDispatchToConfirm(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось подтвердить отгрузку",
        description: getErrorMessage(
          requestError,
          "Проверь остатки выбранных товаров и попробуй снова."
        )
      });
    } finally {
      setConfirmLoading(false);
    }
  };

  const handleCancelDispatch = async () => {
    if (!dispatchToCancel) {
      return;
    }

    setCancelLoading(true);
    try {
      await cancelDispatch(dispatchToCancel.id);
      dispatches.refresh();
      showToast({
        kind: "success",
        title: "Отгрузка отменена",
        description: `Документ "${dispatchToCancel.referenceNumber}" закрыт без списания товара.`
      });
      setDispatchToCancel(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось отменить отгрузку",
        description: getErrorMessage(
          requestError,
          "Отменить можно только черновик отгрузки."
        )
      });
    } finally {
      setCancelLoading(false);
    }
  };

  const getProductStock = (productId: number) =>
    products.data.content.find((product) => product.id === productId)?.quantity ??
    null;

  const getDispatchUnits = (items: DispatchItem[]) =>
    items.reduce((total, item) => total + item.quantity, 0);

  const renderStatusBadge = (status: DispatchStatus) => (
    <Badge tone={getDispatchStatusTone(status)}>
      {getDispatchStatusLabel(status)}
    </Badge>
  );

  return (
    <div className={styles.page}>
      <HeaderActionPortal>
        <Button onClick={openCreate}>
          <Plus size={16} />
          <span>Оформить отгрузку</span>
        </Button>
      </HeaderActionPortal>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить раздел отгрузок.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      {dispatches.loading ? (
        <Loader label="Загружаем отгрузки..." />
      ) : dispatches.data.length === 0 ? (
        <EmptyState
          title="Отгрузок пока нет"
          description="Создай первый документ, чтобы списывать товары со склада в фирму или магазин."
          action={<Button onClick={openCreate}>Оформить отгрузку</Button>}
        />
      ) : (
        <div className={styles.entityGrid}>
          {dispatches.data.map((dispatch) => {
            const RecipientIcon =
              dispatch.recipientType === "STORE" ? Store : Building2;

            return (
              <article key={dispatch.id} className={styles.entityCard}>
                <div className={styles.dispatchCardTop}>
                  <div>
                    <strong>{dispatch.referenceNumber}</strong>
                    <span>
                      {dispatch.warehouseName ?? "Склад не указан"} ·{" "}
                      {dispatch.recipientName ?? "Клиент не указан"}
                    </span>
                  </div>
                  <div className={styles.dispatchCardIcons}>
                    {renderStatusBadge(dispatch.status)}
                    <Badge tone="accent">
                      {getDispatchUnits(dispatch.items)} шт
                    </Badge>
                  </div>
                </div>

                <div className={styles.dispatchMetaGrid}>
                  <span>
                    <Package size={15} />
                    {dispatch.items.length} позиций
                  </span>
                  <span>
                    <RecipientIcon size={15} />
                    {getRecipientTypeLabel(dispatch.recipientType)}
                  </span>
                  <span>{getDispatchStatusLabel(dispatch.status)}</span>
                  <span>Создан: {formatDate(dispatch.createdAt)}</span>
                  <span>Обновлён: {formatDate(dispatch.updatedAt)}</span>
                </div>

                <div className={styles.dispatchItemsList}>
                  {dispatch.items.map((item) => {
                    const currentStock = getProductStock(item.productId);

                    return (
                      <div key={`${dispatch.id}-${item.productId}`} className={styles.dispatchItemRow}>
                        <div>
                          <strong>{item.productName}</strong>
                          <span>{item.productSku || "Без SKU"}</span>
                        </div>
                        <div className={styles.dispatchItemBadges}>
                          <Badge tone="accent">{item.quantity} шт</Badge>
                          {currentStock != null ? (
                            <Badge tone={getStockTone(currentStock)}>
                              Остаток: {getStockLabel(currentStock)} · {currentStock} шт
                            </Badge>
                          ) : null}
                        </div>
                      </div>
                    );
                  })}
                </div>

                <div className={styles.entityFooter}>
                  {dispatch.status === "DRAFT" ? (
                    <>
                      <Button variant="secondary" size="sm" onClick={() => openEdit(dispatch)}>
                        <Pencil size={16} />
                        <span>Редактировать</span>
                      </Button>
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => setDispatchToConfirm(dispatch)}
                      >
                        <Send size={16} />
                        <span>Подтвердить</span>
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => setDispatchToCancel(dispatch)}
                      >
                        <Trash2 size={16} />
                        <span>Отменить</span>
                      </Button>
                    </>
                  ) : null}
                  {dispatch.status === "CANCELLED" ? (
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => setDispatchToDelete(dispatch)}
                    >
                      <Trash2 size={16} />
                      <span>Удалить</span>
                    </Button>
                  ) : null}
                </div>
              </article>
            );
          })}
        </div>
      )}

      <Modal
        open={modalOpen}
        size="lg"
        title={editingDispatch ? "Редактировать отгрузку" : "Создать отгрузку"}
        description="Черновик можно редактировать. Списание со склада произойдёт только после подтверждения документа."
        onClose={resetAndCloseModal}
        footer={
          <>
            <Button variant="ghost" onClick={resetAndCloseModal}>
              Отмена
            </Button>
            <Button form="dispatch-form" type="submit" loading={submitting}>
              Сохранить
            </Button>
          </>
        }
      >
        <form id="dispatch-form" className={styles.stack} onSubmit={handleSubmit}>
          <div className={styles.formGrid}>
            <Input
              label="Номер отгрузки"
              value={form.referenceNumber}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  referenceNumber: event.target.value
                }))
              }
              error={errors.referenceNumber}
              placeholder="Например, OUT-2026-015"
            />
            <Select
              label="Склад"
              value={form.warehouseId}
              onChange={(event) => setWarehouse(event.target.value)}
              error={errors.warehouseId}
              placeholder="Выбери склад"
              options={warehouses.data.map((warehouse) => ({
                value: String(warehouse.id),
                label: warehouse.name
              }))}
            />
          </div>

          <Select
            label="Клиент"
            value={form.recipientId}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                recipientId: event.target.value
              }))
            }
            error={errors.recipientId}
            placeholder="Выбери клиента"
            options={recipients.data.map((recipient) => ({
              value: String(recipient.id),
              label: `${recipient.name} · ${getRecipientTypeLabel(recipient.type)}`
            }))}
          />

          <div className={styles.noteCard}>
            <Send size={18} />
            <div>
              <strong>Логика списания</strong>
              <p>
                Создание и редактирование не списывают товар. После подтверждения
                backend спишет остатки со склада и заблокирует дальнейшее
                редактирование документа.
              </p>
            </div>
          </div>

          <div className={styles.dispatchRows}>
            <div className={styles.dispatchRowsHeader}>
              <div>
                <strong>Состав отгрузки</strong>
                <p>Добавь одну или несколько товарных позиций.</p>
              </div>
              <Button type="button" variant="ghost" onClick={addItem}>
                <Plus size={16} />
                <span>Добавить строку</span>
              </Button>
            </div>

            {errors.items ? (
              <span className={styles.inlineError}>{errors.items}</span>
            ) : null}

            {form.items.map((item, index) => (
              <div key={`dispatch-item-${index}`} className={styles.dispatchRow}>
                <div className={styles.dispatchRowGrid}>
                  <Select
                    label={`Товар ${index + 1}`}
                    value={item.productId}
                    onChange={(event) =>
                      updateItem(index, "productId", event.target.value)
                    }
                    error={errors[`productId-${index}`]}
                    placeholder="Выбери товар"
                    options={selectedWarehouseProducts.map((product) => ({
                      value: String(product.id),
                      label: `${product.name} · ${product.sku} · ${product.quantity} шт сейчас`
                    }))}
                  />
                  <Input
                    label="Количество"
                    type="number"
                    min={1}
                    value={item.quantity}
                    onChange={(event) =>
                      updateItem(index, "quantity", event.target.value)
                    }
                    error={errors[`quantity-${index}`]}
                    hint={
                      item.productId
                        ? `Доступно:\u00A0${getAvailableQuantity(item.productId) ?? 0}\u00A0шт`
                        : undefined
                    }
                  />
                  <div className={styles.dispatchRowActionField}>
                    <span
                      className={styles.dispatchRowActionLabel}
                      aria-hidden="true"
                    >
                      Действие
                    </span>
                    <Button
                      type="button"
                      className={styles.dispatchRowActionButton}
                      variant="ghost"
                      size="sm"
                      onClick={() => removeItem(index)}
                    >
                      <Trash2 size={16} />
                      <span>Убрать</span>
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(dispatchToConfirm)}
        title="Подтвердить отгрузку?"
        description={
          dispatchToConfirm
            ? `После подтверждения документ "${dispatchToConfirm.referenceNumber}" спишет товар со склада и станет недоступен для редактирования.`
            : ""
        }
        confirmLabel="Подтвердить"
        confirmVariant="primary"
        details="Списание будет выполнено прямо сейчас по фактическим остаткам на складе."
        loading={confirmLoading}
        onConfirm={handleConfirm}
        onClose={() => setDispatchToConfirm(null)}
      />

      <ConfirmDialog
        open={Boolean(dispatchToCancel)}
        title="Отменить черновик отгрузки?"
        description={
          dispatchToCancel
            ? `Документ "${dispatchToCancel.referenceNumber}" будет отменён без списания товара со склада.`
            : ""
        }
        confirmLabel="Отменить отгрузку"
        details="Черновик будет закрыт и больше не сможет быть подтверждён или отредактирован."
        loading={cancelLoading}
        onConfirm={handleCancelDispatch}
        onClose={() => setDispatchToCancel(null)}
      />

      <ConfirmDialog
        open={Boolean(dispatchToDelete)}
        title="Удалить отгрузку?"
        description={
          dispatchToDelete
            ? `Документ "${dispatchToDelete.referenceNumber}" будет удалён из списка отменённых отгрузок.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setDispatchToDelete(null)}
      />
    </div>
  );
}
