# Warehouse accounting
Spring Boot REST API для складского учёта
## Технологический стек
- **Java 21**
- **Spring Boot 4.0.2** - основной фреймворк
  - Spring Web MVC - для построения REST API
- **Maven** - сборка проекта и управление зависимостями
- **Checkstyle** - контроль качества кода

com.hikaro.warehouse/
├── controller/ # REST контроллеры (входные точки API)
│ ├── HomeController.java # Базовые endpoint'ы (главная страница)
│ └── ProductController.java # CRUD операции для товаров
├── dto/ # Data Transfer Objects
│ └── ProductResponseDto.java # DTO для ответов API
├── entity/ # JPA сущности (Entity)
│ └── Product.java # Модель товара
├── mapper/ # Преобразование Entity ↔ DTO
│ └── ProductMapper.java # Маппер для Product
├── repository/ # Доступ к данным (Spring Data JPA)
│ └── ProductRepository.java # Репозиторий для Product
└── service/ # Бизнес-логика
└── ProductService.java # Сервис для работы с товарами
