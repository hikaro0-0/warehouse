# Warehouse Accounting System

## Что сделано

Проект `warehouse` реализует Spring Boot REST API по теме складского учёта.
Структура базы данных и миграций теперь организована по образцу `FinanceTracker`:

- `src/main/resources/db/db.changelog-master.yaml`
- `src/main/resources/db/migrations/*.yaml`
- инициализация схемы и данных через `Liquibase`

Из проекта удалены старые ресурсы, которые больше не используются:

- `data.sql`
- `application-postgres.properties`

## Выполненные требования

1. Создано Spring Boot приложение.
2. Реализован REST API для ключевой сущности `Product`.
3. Есть `GET` с `@PathVariable`: `GET /api/products/{id}`.
4. Есть `GET` с `@RequestParam`: `GET /api/products?name=lap`.
5. Использованы слои `Controller -> Service -> Repository`.
6. Реализованы DTO и mapper (`ProductMapper`).
7. Подключена реляционная БД PostgreSQL.
8. В модели данных есть 5 сущностей:
   - `Product`
   - `Category`
   - `Supplier`
   - `Warehouse`
   - `Shipment`
9. Есть связь `OneToMany`:
   - `Warehouse -> Product`
   - `Supplier -> Product`
   - `Supplier -> Shipment`
10. Есть связь `ManyToMany`:
   - `Product <-> Category`
   - `Shipment <-> Product`
11. Реализованы CRUD операции.
12. Продемонстрирована проблема `N+1` и решение через `@EntityGraph`.
13. Реализовано сохранение нескольких связанных сущностей с демонстрацией поведения без транзакции и с транзакцией.

## Liquibase migrations

## API

Основная сущность:

- `GET /api/products/{id}`
- `GET /api/products?name=lap&page=0&size=10&sort=id,asc`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

Дополнительный CRUD:

- `/api/categories`
- `/api/suppliers`
- `/api/warehouses`
- `/api/shipments`

N+1:

- проблемный endpoint: `GET /api/products/n-plus-one`
- оптимизированный endpoint: `GET /api/products/optimized?page=0&size=10`
- JPQL по вложенной сущности: `GET /api/products/search/jpql?name=top&categoryName=Premium&page=0&size=10`
- native SQL по вложенной сущности: `GET /api/products/search/native?name=mon&categoryName=Premium&page=0&size=10`

Транзакции:

- `POST /api/demo/without-transaction`
- `POST /api/demo/with-transaction`

JMeter:

- `docs/jmeter/all-endpoints.jmx` - CRUD/search/async/counter flow по образцу `FinanceTracker`, без намеренно падающих demo endpoints
- `docs/jmeter/race-condition.jmx` - отдельный нагрузочный сценарий для `POST /api/demo/race-condition`

## CascadeType и FetchType

В проекте выбраны следующие настройки:

- `FetchType.LAZY` для коллекций и связей `ManyToOne`
- `CascadeType.PERSIST, MERGE` для `Supplier.products`, `Supplier.shipments`, `Warehouse.products`
- для `ManyToMany` каскад удаления не используется

Это уменьшает количество лишних запросов, позволяет отдельно контролировать загрузку графа и не удаляет связанные справочники по цепочке.

## Запуск

1. Настрой `.env` на основе [`.env.example`](./.env.example).
2. Подними PostgreSQL через Docker Compose.
3. Из-за сетевых ограничений Docker на Fedora контейнер запускается в `host` network mode и использует порт `5432` напрямую на хосте.
4. Запусти:

```bash
cd /home/hikaro/java/warehouse
./mvnw spring-boot:run
```

При старте Liquibase сам применит все миграции к твоей БД.

Для отдельного frontend на Vite CORS уже настроен на `http://localhost:5173`.
Если frontend запускается с другого origin, укажи его в `.env`:

```properties
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Можно перечислить несколько origin через запятую:

```properties
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Команды запуска:

```bash
docker compose down -v --remove-orphans
docker compose up -d
cp .env.example .env
./mvnw spring-boot:run
```

Примеры запуска JMeter:

```bash
jmeter -n -t docs/jmeter/all-endpoints.jmx -Jhost=localhost -Jport=8080 -Jusers=50 -JrampUpSeconds=2
jmeter -n -t docs/jmeter/race-condition.jmx -Jhost=localhost -Jport=8080 -JraceUsers=5 -JdemoThreadCount=64 -JdemoIncrementsPerThread=2000
```

`all-endpoints.jmx` использует уникальные имена на поток и очищает созданные CRUD-сущности в конце сценария. Асинхронный demo flow сохраняет отдельные demo-записи с уникальными именами, как часть проверки `POST /api/demo/async/with-transaction`.

## Troubleshooting: Liquibase и права на схему

Если при старте появляется ошибка `нет доступа к схеме public` / `Failed SQL: CREATE TABLE public.databasechangelog`, это означает, что пользователь БД не имеет прав на схему `public`.

Исправление для существующей БД (выполнить от суперпользователя PostgreSQL):

```sql
GRANT USAGE, CREATE ON SCHEMA public TO hikaro;
ALTER SCHEMA public OWNER TO hikaro;
```

В проекте схема теперь настраивается через `.env`:

```properties
DB_SCHEMA=public
```

Если `public` ограничена политикой вашей БД, можно использовать отдельную схему, например `warehouse`, и выдать права пользователю на нее.
