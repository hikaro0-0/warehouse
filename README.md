# Warehouse Management System

## Описание

Проект представляет собой REST API на Spring Boot для управления складом.
Основная БД проекта сейчас `H2 (in-memory)`.

Модель данных включает 5 сущностей:

- `Product`
- `Category`
- `Supplier`
- `Warehouse`
- `Shipment`

Связи:

- `Warehouse -> Product` и `Supplier -> Product` реализованы как `OneToMany / ManyToOne`
- `Product <-> Category` реализована как `ManyToMany`
- `Shipment <-> Product` реализована как дополнительная `ManyToMany`

## Технологии

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- Checkstyle

## Конфигурация БД

- приложение работает на встроенной `H2`
- схема создаётся автоматически при запуске
- `data.sql` загружает стартовые данные

## Обоснование CascadeType и FetchType

В проекте используются следующие настройки:

- `FetchType.LAZY` для коллекций и связей `ManyToOne`
  Это уменьшает количество лишних SQL-запросов и позволяет явно управлять загрузкой.

- `CascadeType.PERSIST, MERGE` для `Supplier -> products`, `Supplier -> shipments`,
  `Warehouse -> products`
  Это удобно для сохранения и обновления связанных объектов без каскадного удаления.

- для `ManyToMany` каскад удаления не используется
  Категории, товары и поставки остаются самостоятельными сущностями.

## CRUD API

Реализованы CRUD-операции для:

- `/api/products`
- `/api/categories`
- `/api/suppliers`
- `/api/warehouses`
- `/api/shipments`

Пример создания товара:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-500",
    "name": "USB Hub",
    "quantity": 25,
    "warehouseId": 1,
    "supplierId": 1,
    "categoryIds": [1, 2]
  }'
```

## Демонстрация N+1

Проблемный сценарий:

- `GET /api/products/n-plus-one`

Оптимизированный сценарий:

- `GET /api/products/optimized`

Оптимизация выполнена через `@EntityGraph`.

## Демонстрация транзакций

Endpoints:

- `POST /api/demo/without-transaction`
- `POST /api/demo/with-transaction`

Без `@Transactional` часть данных сохраняется.
С `@Transactional` операция откатывается полностью.

## Запуск

1. Запусти проект:

```bash
./mvnw spring-boot:run
```

После запуска:

- API: `http://localhost:8080/api/products`
- H2 console: `http://localhost:8080/h2-console`

## Тесты

```bash
./mvnw test
./mvnw verify
```
