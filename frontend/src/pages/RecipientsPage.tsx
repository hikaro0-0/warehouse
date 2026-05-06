import { useMemo, useState, type FormEvent } from "react";
import {
  Building2,
  Mail,
  MapPin,
  Pencil,
  Plus,
  Store,
  Trash2
} from "lucide-react";
import { getErrorMessage } from "../api/client";
import {
  createRecipient,
  deleteRecipient,
  updateRecipient
} from "../api/recipients";
import { Button } from "../components/ui/Button";
import { ConfirmDialog } from "../components/ui/ConfirmDialog";
import { EmptyState } from "../components/ui/EmptyState";
import { Input } from "../components/ui/Input";
import { Loader } from "../components/ui/Loader";
import { Modal } from "../components/ui/Modal";
import { Select } from "../components/ui/Select";
import { useToast } from "../components/ui/ToastProvider";
import { useDispatches } from "../hooks/useDispatches";
import { useRecipients } from "../hooks/useRecipients";
import {
  getRecipientTypeLabel,
  RECIPIENT_TYPE_OPTIONS,
  type Recipient,
  type RecipientFormValues
} from "../types/recipient";
import styles from "./pages.module.css";

const initialForm: RecipientFormValues = {
  name: "",
  type: "COMPANY",
  email: "",
  address: ""
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function RecipientsPage() {
  const { showToast } = useToast();
  const recipients = useRecipients();
  const dispatches = useDispatches();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecipient, setEditingRecipient] = useState<Recipient | null>(
    null
  );
  const [recipientToDelete, setRecipientToDelete] = useState<Recipient | null>(
    null
  );
  const [form, setForm] = useState<RecipientFormValues>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const error = recipients.error || dispatches.error;

  const dispatchCountByRecipient = useMemo(() => {
    const counts = new Map<number, number>();

    dispatches.data.forEach((dispatch) => {
      if (dispatch.recipientId == null) {
        return;
      }

      counts.set(
        dispatch.recipientId,
        (counts.get(dispatch.recipientId) ?? 0) + 1
      );
    });

    return counts;
  }, [dispatches.data]);

  const openCreate = () => {
    setEditingRecipient(null);
    setForm(initialForm);
    setErrors({});
    setModalOpen(true);
  };

  const openEdit = (recipient: Recipient) => {
    setEditingRecipient(recipient);
    setForm({
      name: recipient.name,
      type: recipient.type,
      email: recipient.email ?? "",
      address: recipient.address ?? ""
    });
    setErrors({});
    setModalOpen(true);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};

    if (!form.name.trim()) {
      nextErrors.name = "Название клиента обязательно";
    }

    if (form.email.trim() && !emailPattern.test(form.email.trim())) {
      nextErrors.email = "Введите корректный email";
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
      if (editingRecipient) {
        await updateRecipient(editingRecipient.id, form);
        showToast({
          kind: "success",
          title: "Клиент обновлён",
          description: `Данные по "${form.name}" сохранены.`
        });
      } else {
        await createRecipient(form);
        showToast({
          kind: "success",
          title: "Клиент создан",
          description: `Новый клиент "${form.name}" добавлен.`
        });
      }

      recipients.refresh();
      setModalOpen(false);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось сохранить клиента",
        description: getErrorMessage(
          requestError,
          "Проверьте обязательные поля и повторите попытку."
        )
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!recipientToDelete) {
      return;
    }

    setDeleteLoading(true);
    try {
      await deleteRecipient(recipientToDelete.id);
      recipients.refresh();
      showToast({
        kind: "success",
        title: "Клиент удалён",
        description: `Клиент "${recipientToDelete.name}" удалён из системы.`
      });
      setRecipientToDelete(null);
    } catch (requestError) {
      showToast({
        kind: "error",
        title: "Не удалось удалить клиента",
        description: getErrorMessage(
          requestError,
          "Клиент может быть связан с уже оформленными отгрузками."
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
            <span className={styles.heroEyebrow}>Клиенты</span>
            <h2>Фирмы и магазины, куда ты отгружаешь технику</h2>
            <p>Справочник клиентов для исходящих отгрузок.</p>
          </div>
          <div className={styles.heroActions}>
            <Button onClick={openCreate}>
              <Plus size={16} />
              <span>Добавить клиента</span>
            </Button>
          </div>
        </div>
      </section>

      {error ? (
        <div className={styles.errorBanner}>
          <strong>Не удалось загрузить клиентов.</strong>
          <span>{error}</span>
        </div>
      ) : null}

      {recipients.loading ? (
        <Loader label="Загружаем клиентов..." />
      ) : recipients.data.length === 0 ? (
        <EmptyState
          title="Клиенты ещё не добавлены"
          description="Создай первую фирму или магазин, чтобы оформлять отгрузки."
          action={<Button onClick={openCreate}>Добавить клиента</Button>}
        />
      ) : (
        <div className={styles.entityGrid}>
          {recipients.data.map((recipient) => {
            const Icon = recipient.type === "STORE" ? Store : Building2;
            const dispatchCount = dispatchCountByRecipient.get(recipient.id) ?? 0;

            return (
              <article key={recipient.id} className={styles.entityCard}>
                <div className={styles.entityHeader}>
                  <div>
                    <strong>{recipient.name}</strong>
                    <span>{getRecipientTypeLabel(recipient.type)}</span>
                  </div>
                  <Icon size={18} />
                </div>
                <div className={styles.entityMeta}>
                  <span>{dispatchCount} оформленных отгрузок</span>
                  {recipient.email ? (
                    <span>
                      <Mail size={15} />
                      {recipient.email}
                    </span>
                  ) : null}
                  {recipient.address ? (
                    <span>
                      <MapPin size={15} />
                      {recipient.address}
                    </span>
                  ) : null}
                </div>
                <div className={styles.entityFooter}>
                  <Button variant="ghost" size="sm" onClick={() => openEdit(recipient)}>
                    <Pencil size={16} />
                    <span>Редактировать</span>
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setRecipientToDelete(recipient)}
                  >
                    <Trash2 size={16} />
                    <span>Удалить</span>
                  </Button>
                </div>
              </article>
            );
          })}
        </div>
      )}

      <Modal
        open={modalOpen}
        title={editingRecipient ? "Редактировать клиента" : "Создать клиента"}
        description="Клиент понадобится в каждой отгрузке, чтобы фиксировать, куда ушёл товар."
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="ghost" onClick={() => setModalOpen(false)}>
              Отмена
            </Button>
            <Button form="recipient-form" type="submit" loading={submitting}>
              Сохранить
            </Button>
          </>
        }
      >
        <form id="recipient-form" className={styles.stack} onSubmit={handleSubmit}>
          <Input
            label="Название"
            value={form.name}
            onChange={(event) =>
              setForm((current) => ({ ...current, name: event.target.value }))
            }
            error={errors.name}
          />
          <Select
            label="Тип клиента"
            value={form.type}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                type: event.target.value as RecipientFormValues["type"]
              }))
            }
            options={RECIPIENT_TYPE_OPTIONS.map((option) => ({
              value: option.value,
              label: option.label
            }))}
          />
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) =>
              setForm((current) => ({ ...current, email: event.target.value }))
            }
            error={errors.email}
            hint="Необязательно, но удобно для связи и документов."
          />
          <Input
            label="Адрес"
            value={form.address}
            onChange={(event) =>
              setForm((current) => ({ ...current, address: event.target.value }))
            }
            hint="Можно указать офис, магазин или адрес доставки."
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(recipientToDelete)}
        title="Удалить клиента?"
        description={
          recipientToDelete
            ? `Клиент "${recipientToDelete.name}" будет удалён из базы.`
            : ""
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onClose={() => setRecipientToDelete(null)}
      />
    </div>
  );
}
