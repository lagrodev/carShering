# UML Диаграммы - Система каршеринга

## Описание

Данная папка содержит UML диаграммы для системы каршеринга, разработанной с использованием Domain-Driven Design (DDD) архитектуры.

**Все диаграммы соответствуют стандарту UML 2.x**

## Список диаграмм

### 1. Activity Diagram (Диаграмма активности)
**Файл:** `activity_diagram.puml`

**Тип UML:** Behavioral Diagram (Поведенческая диаграмма)

**Описание:** Показывает бизнес-процесс аренды автомобиля от начала до конца.

**Основные элементы UML:**
- **Swimlanes (Дорожки)** - разделение ответственности между актёрами
- **Actions (Действия)** - прямоугольники с закругленными углами
- **Decision nodes (Решения)** - ромбы с условиями
- **Control flow (Поток управления)** - стрелки
- **Start/Stop nodes** - черные круги

**Swimlanes:**
- 🟡 Клиент - действия пользователя
- 🔵 Система - автоматические проверки и процессы
- 🟢 Администратор - ручное одобрение заявок
- 🟡 Планировщик - автоматизированные задачи (@Scheduled)

---

### 2. State Chart Diagrams (Диаграммы состояний)
**Файлы:** 
- `statechart_diagram.puml` - Contract State Machine
- `statechart_car.puml` - Car State Machine
- `statechart_auth.puml` - User Authentication State

**Тип UML:** Behavioral Diagram (Поведенческая диаграмма)

**Описание:** Показывает машины состояний для ключевых сущностей системы.

**Основные элементы UML:**
- **States (Состояния)** - прямоугольники с закругленными углами
- **Transitions (Переходы)** - стрелки с условиями
- **Initial state** - черный круг [*]
- **Final state** - черный круг в кружке
- **Composite states** - вложенные состояния

---

### 3. Component Diagram (Диаграмма компонентов)
**Файл:** `component_diagram.puml` ✅ **Переделана 2025-12-18**

**Тип UML:** Structure Diagram (Структурная диаграмма)

**Описание:** Показывает **модульную архитектуру** системы с **портами** и **интерфейсами** по канонам UML 2.x.

**Основные элементы UML (правильные):**
- **[Component]** - модульная, заменяемая часть системы
- **Port** - точка взаимодействия (□ portin/portout)
- **Provided Interface** - что предоставляет компонент (lollipop **─○**)
- **Required Interface** - что требует компонент (socket **○─**)
- **Dependency** - зависимость (..>)
- **<<subsystem>>** - подсистема (bounded context)

**Модульная структура:**

**backend-0.0.1-SNAPSHOT.jar** содержит:

1. **[REST API Module]** with ports
   - portin: HTTP requests
   - portout: HTTP responses
   - Provides: `IRestAPI`
   - Requires: `IAuth`
   - Components: 10 controllers (Auth, Car, Contract, etc.)

2. **[Security Module]**
   - Provides: `IAuthentication`
   - Components: JwtFilter, Spring Security, JWT Utils

3. **[Identity Module]** (subsystem)
   - Requires: `IDB`, `IEmail`
   - Components: Client Service, Auth Service, Email Service

4. **[Fleet Module]** (subsystem)
   - Provides: `ICar`
   - Requires: `IDB`
   - Components: Car Service, Brand Service, Model Service

5. **[Rental Module]** (subsystem)
   - Provides: `IContract`
   - Requires: `IDB`, `ICar`
   - Components: Contract Service, Scheduler

6. **[Persistence Module]**
   - Provides: `IDB`
   - Components: JPA Repositories (7), Hibernate ORM

**Интерфейсы (lollipop/socket):**
- `IRestAPI` - REST endpoints
- `IAuthentication` - security service
- `ICarManagement` - car catalog
- `IContractManagement` - contract service
- `IDatabaseAccess` - data persistence
- `IEmailService` - email sending

**External Components (JARs):**
- spring-boot-starter-web
- spring-boot-starter-security
- spring-data-jpa
- postgresql-driver
- flyway-core
- jjwt

---

### 4. Deployment Diagram (Диаграмма развертывания)
**Файл:** `deployment_diagram.puml`

**Тип UML:** Structure Diagram (Структурная диаграмма)

**Описание:** Показывает физическую архитектуру системы с **ПОЛНЫМ списком всех сервисов** и их развертыванием.

**Основные элементы UML:**
- **Node** - физический узел (device, server, container)
- **Artifact** - развертываемый файл (.jar, .exe, database)
- **Component** - программный сервис/модуль
- **Execution Environment** - среда выполнения (JVM, PostgreSQL, Tomcat)
- **Communication Path** - сетевой протокол (HTTP, JDBC, SMTP)

**Что показано в диаграмме:**

#### 🔷 Client Tier:
- Client Device (node) - пользовательские устройства
- Web Browser (execution environment)
- React/Vue SPA (artifact) - UI компоненты

#### 🔷 Application Tier (Docker Host):

**Backend Container** с **backend.jar** содержит:

**1. REST API Services (10 контроллеров):**
- AuthController - регистрация, логин
- ProfileController - управление профилем
- CarController - каталог автомобилей
- ContractController - контракты аренды
- FavoriteController - избранное
- EmailController - email отправка
- AdminCarController - админ управление авто
- AdminContractController - админ контракты
- AdminClientController - админ клиенты
- CarImageController - загрузка фото

**2. Security Services (middleware):**
- JwtRequestFilter - фильтр запросов
- JwtTokenUtils - JWT токены
- Spring Security - авторизация
- ClientDetails Service - детали пользователя
- CORS Filter - CORS защита

**3. Business Logic Services (3 Bounded Contexts - DDD):**

*Identity Context:*
- ClientApplicationService
- AuthApplicationService
- DocumentApplicationService
- EmailApplicationService
- OpaqueTokenService (cleanup)

*Fleet Context:*
- CarApplicationService
- BrandApplicationService
- ModelApplicationService
- FavoriteApplicationService
- ImageApplicationService

*Rental Context:*
- ContractApplicationService
- ContractScheduler (CRON tasks)
- RentalDomainService

**4. Data Access Services (7 репозиториев):**
- ClientRepository
- DocumentRepository
- CarRepository
- ModelRepository
- ContractRepository
- FavoriteRepository
- RefreshTokenRepository

**5. Infrastructure Services:**
- Hibernate/JPA
- Flyway Migrator
- MapStruct Mapper
- JDBC Driver
- SMTP Client
- MinIO Client

#### 🔷 Database Container:
- PostgreSQL 15 Server (execution environment)
- car_rental database (artifact)
- 8 таблиц: client, document, car, car_model, contract, refresh_token, favorites, role

#### 🔷 MinIO Container:
- MinIO Server (object storage)
- car-photos bucket (40MB max)

#### 🔷 Mailpit Container (dev):
- SMTP Server (port 1025)
- Web UI (port 8025)

#### 🔷 External Services:
- Production SMTP Server

**Протоколы и порты:**
- HTTP/REST: 8082 (external) → 8080 (internal)
- JDBC: 5433 (external) → 5432 (internal)
- SMTP: 1025 (dev) / 587 (production)
- S3 API: 9000 (MinIO)

**Scheduled Tasks (автоматизация):**
- Активация контрактов: каждые 5 минут
- Завершение контрактов: ежедневно в 2 AM
- Очистка токенов: каждый час

**Security:**
- JWT Authentication (30 min)
- Refresh Tokens (7 days)
- Role-based authorization (USER, ADMIN)
- CORS: localhost:5176
- BCrypt password hashing

---

### 5. Package Diagram (Диаграмма пакетов)
**Файл:** `package_diagram.puml`

**Тип UML:** Structure Diagram (Структурная диаграмма)

**Описание:** Показывает структуру пакетов Java и зависимости между ними.

**Основные элементы UML:**
- **Package** - пакет/namespace Java
- **Class** - класс внутри пакета
- **<<Layer>>** - стереотип архитектурного слоя
- **<<Bounded Context>>** - стереотип DDD контекста
- **<<Shared Kernel>>** - стереотип общего кода
- **<<external>>** - стереотип внешней библиотеки
- **Dependencies** - import/use зависимости

**Структура пакетов:**
```
org.example.carshering/
├── rest/                    [Layer]
├── security/                [Layer]
├── identity/                [Bounded Context]
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── fleet/                   [Bounded Context]
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── rental/                  [Bounded Context]
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── common/                  [Shared Kernel]
└── config/                  [Infrastructure]
```

---

### 6. ~~Realization Diagram~~ → Package Diagram
**Примечание:** "Realization Diagram" не является стандартным типом UML диаграммы. 

Вместо неё создана **Package Diagram**, которая показывает:
- Структуру пакетов (организация кода)
- Зависимости между пакетами
- Слоистую архитектуру
- DDD Bounded Contexts

Это соответствует канонам UML 2.x.

---

## Соответствие канонам UML 2.x

### ✅ Все диаграммы соответствуют стандарту:

| Диаграмма | Тип UML | Категория | Стандарт |
|-----------|---------|-----------|----------|
| Activity | Activity Diagram | Behavioral | UML 2.x ✅ |
| State Chart | State Machine Diagram | Behavioral | UML 2.x ✅ |
| Component | Component Diagram | Structure | UML 2.x ✅ |
| Deployment | Deployment Diagram | Structure | UML 2.x ✅ |
| Package | Package Diagram | Structure | UML 2.x ✅ |

### 🔍 Ключевые исправления:

**Deployment Diagram:**
- ✅ Использует **nodes** (узлы) вместо простых прямоугольников
- ✅ Использует **artifacts** (артефакты) - развертываемые файлы
- ✅ Использует **execution environments** - среды выполнения
- ✅ Протоколы показаны как **communication paths**
- ✅ Стереотипы: <<device>>, <<container>>, <<execution environment>>

**Component Diagram:**
- ✅ Использует **components** [Component Name]
- ✅ Использует **interfaces** (круги)
- ✅ Provided interfaces (lollipop notation) ─○
- ✅ Required interfaces (socket notation) ○─
- ✅ Правильные зависимости между компонентами
- ✅ Стереотипы: <<subsystem>> для bounded contexts

**Package Diagram:**
- ✅ Показывает **packages** (пакеты Java)
- ✅ Показывает **import/use** зависимости между пакетами
- ✅ Стереотипы: <<Layer>>, <<Bounded Context>>, <<Shared Kernel>>
- ✅ Соответствует реальной структуре проекта

---

## Как просмотреть диаграммы

### Вариант 1: PlantUML в IDE (рекомендуется)

**IntelliJ IDEA / WebStorm:**
1. Установите плагин "PlantUML Integration"
2. Откройте `.puml` файл
3. Нажмите на иконку preview или используйте сочетание клавиш

**VS Code:**
1. Установите расширение "PlantUML"
2. Откройте `.puml` файл
3. Нажмите `Alt+D` для preview

### Вариант 2: Онлайн редактор
1. Откройте [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Скопируйте содержимое `.puml` файла
3. Вставьте в редактор

### Вариант 3: Генерация PNG/SVG через командную строку

```bash
# Установите PlantUML
# Windows (через Chocolatey):
choco install plantuml

# Или скачайте plantuml.jar с официального сайта

# Генерация всех диаграмм
java -jar plantuml.jar diagrams/*.puml

# Генерация конкретной диаграммы
java -jar plantuml.jar diagrams/activity_diagram.puml

# В формате SVG
java -jar plantuml.jar -tsvg diagrams/*.puml
```

---


## Связанные документы

- Диаграмма классов: `class_diagram.png`
- Use Case диаграмма: `use-case.drawio.pdf`
- Sequence диаграммы: `sequence/`
- Communication диаграммы: `communication/`

---

## Контакты

При вопросах по диаграммам обращайтесь к документации проекта или к разработчикам команды.

