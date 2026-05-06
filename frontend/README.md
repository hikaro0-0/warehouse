# Warehouse Frontend

Современный frontend-клиент для Spring Boot REST API проекта “Склад”. Интерфейс собран на `React + TypeScript + Vite` и рассчитан на работу с backend из этого репозитория.

## Возможности

- dashboard с ключевыми метриками;
- каталог товаров в режиме карточек и таблицы;
- поиск товаров по названию;
- фильтрация по категории, складу и поставщику;
- карточка товара с детальной информацией;
- создание и редактирование товара;
- CRUD для складов, поставщиков и категорий;
- loader, empty state, confirm dialog и toast-уведомления.

## Стек

- React
- TypeScript
- Vite
- React Router
- Axios
- CSS Modules + глобальные дизайн-токены

## Запуск

1. Перейдите в папку frontend:

```bash
cd frontend
```

2. Установите зависимости:

```bash
npm install
```

3. Создайте `.env` на основе примера:

```bash
cp .env.example .env
```

4. Укажите базовый URL backend API:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

5. Запустите dev-сервер:

```bash
npm run dev
```

## Сборка

```bash
npm run build
```

## Важные примечания по адаптации backend

Текущий Spring Boot backend из репозитория уже поддерживает:

- `Product`: `sku`, `name`, `description`, `quantity`, `price`, `warehouseId`, `supplierId`, `categoryIds`
- `Warehouse`: `name`, `address`
- `Supplier`: `name`, `contactEmail`
- `Category`: `name`, `description`

Поля `description` и `price` уже сохраняются для товаров. Дополнительные поля вроде `phone`, `address`, `createdAt`, `updatedAt` пока остаются расширяемыми. Чтобы подключить их:

1. расширьте DTO и entity на backend;
2. обновите маппинг в:
   - `src/api/products.ts`
   - `src/api/suppliers.ts`
   - `src/api/warehouses.ts`

## CORS

Если frontend запущен отдельно от Spring Boot (`5173 -> 8080`), backend должен разрешать CORS для этого origin. Если CORS ещё не настроен, добавьте его в Spring Boot конфигурацию.

## Структура

```text
src/
  api/
  components/
  hooks/
  pages/
  router/
  styles/
  types/
  utils/
```
