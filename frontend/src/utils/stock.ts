export type StockStatus = "in-stock" | "low" | "out-of-stock";

export const getStockStatus = (quantity: number): StockStatus => {
  if (quantity <= 0) {
    return "out-of-stock";
  }

  if (quantity <= 5) {
    return "low";
  }

  return "in-stock";
};

export const getStockLabel = (quantity: number): string => {
  const status = getStockStatus(quantity);

  if (status === "out-of-stock") {
    return "Нет в наличии";
  }

  if (status === "low") {
    return "Мало на складе";
  }

  return "В наличии";
};

export const getStockTone = (
  quantity: number
): "success" | "warning" | "danger" => {
  const status = getStockStatus(quantity);

  if (status === "out-of-stock") {
    return "danger";
  }

  if (status === "low") {
    return "warning";
  }

  return "success";
};
