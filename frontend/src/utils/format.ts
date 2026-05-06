export const formatCompactNumber = (value: number): string =>
  new Intl.NumberFormat("ru-RU", {
    notation: "compact",
    maximumFractionDigits: 1
  }).format(value);

export const formatDate = (value?: string | null): string => {
  if (!value) {
    return "Нет данных";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Нет данных";
  }

  return new Intl.DateTimeFormat("ru-RU", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
};

export const formatCurrency = (value?: number | null): string => {
  if (value == null) {
    return "Не указана";
  }

  return new Intl.NumberFormat("ru-RU", {
    minimumFractionDigits: Number.isInteger(value) ? 0 : 2,
    maximumFractionDigits: 2
  }).format(value);
};

export const formatLongDate = (): string =>
  new Intl.DateTimeFormat("ru-RU", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
  }).format(new Date());
