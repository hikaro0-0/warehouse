# Warehouse accounting
Spring Boot REST API для складского учёта
## Технологический стек
- **Java 21**
- **Spring Boot 4.0.2** - основной фреймворк
  - Spring Web MVC - для построения REST API
- **Maven** - сборка проекта и управление зависимостями
- **Checkstyle** - контроль качества кода

Базовый пакет приложения: ```com.hikaro.warehouse/```

```controller/``` # REST контроллеры

```HomeController.java``` # Базовые endpoint'ы

```ProductController.java``` # CRUD операции для товаров

```ProductResponseDto.java``` # DTO для ответов API

```entity/``` # JPA сущности (Entity)

```Product.java``` # Модель товара

```mapper/``` # Преобразование Entity ↔ DTO

```ProductMapper.java``` # Маппер для Product

```repository/``` # Доступ к данным

```ProductRepository.java``` # Репозиторий для Product

```service/``` # Бизнес-логика

```ProductService.java``` # Сервис для работы с товарами

# Запуск

```./mvnw spring-boot:run```

Приложение стартует на ```http://localhost:8080```.
