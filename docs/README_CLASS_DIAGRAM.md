# 📊 Class Diagram Documentation

Документация по диаграмме классов Car Sharing Backend

## 📁 Файлы

- **PlantUML диаграмма**: [`class_diagram.puml`](./class_diagram.puml)
- **Изображение (PNG)**: [`class_diagram.png`](./class_diagram.png) _(если сгенерировано)_

---

## 🎯 Описание

Диаграмма классов отображает **полную статическую структуру** проекта Car Sharing Backend с применением принципов **Domain-Driven Design (DDD)**.

### Ключевые особенности:
- ✅ Разделение на **4 Bounded Context** (Fleet, Rental, Client, Identity)
- ✅ Выделение **Aggregate Roots** (Car, Contract, Client)
- ✅ Все **Value Objects** с валидационными методами
- ✅ Многослойная архитектура (Domain, Application, Infrastructure, API)
- ✅ Паттерны: Repository, Domain Service, Application Service
- ✅ Цветовое кодирование контекстов
- ✅ Легенда с объяснением стереотипов

---

## 🏗 Структура диаграммы

### 1. **Fleet Context** (Управление автопарком) 🟢
**Цвет**: Зеленый (#E8F5E9)

**Основные компоненты:**
- **Aggregate Root**: `Car`
- **Entities**: `CarModel`, `Brand`, `Model`, `CarClass`, `CarState`, `Image`
- **Value Objects**: `GosNumber`, `Vin`, `Year`
- **Services**: `CarService`, `CarServiceImpl`
- **Repository**: `CarRepository`
- **Controller**: `CarController`

**Ответственность**: Управление каталогом автомобилей, моделями, брендами, статусами.

---

### 2. **Rental Context** (Управление арендой) 🟠
**Цвет**: Оранжевый (#FFF3E0)

**Основные компоненты:**
- **Aggregate Root**: `Contract`
- **Entities**: `RentalState`
- **Value Objects**: `RentalPeriod`
- **Domain Service**: `RentalDomainService` (расчет стоимости, проверка доступности)
- **Application Service**: `ContractService`
- **Repository**: `ContractRepository`
- **Controller**: `ContractController`

**Ответственность**: Создание и управление договорами аренды, расчет стоимости, проверка пересечений периодов.

**Ключевые методы Contract:**
```java
+ create(ClientId, CarId, RentalPeriod, Money) : Contract
+ confirm() : void
+ cancel() : void
+ updatePeriod(RentalPeriod, Money) : void
+ canBeConfirmed() : boolean
```

---

### 3. **Client Context** (Управление клиентами) 🔵
**Цвет**: Голубой (#E3F2FD)

**Основные компоненты:**
- **Aggregate Root**: `Client`
- **Entities**: `Document`, `DocumentType`, `Favorite`
- **Value Objects**: `Email`, `Phone`, `Login`, `Password`, `DocumentSeries`, `DocumentNumber`, `DateOfIssue`, `IssuingAuthority`
- **Services**: `ClientService`, `DocumentService`, `DocumentVerificationService`
- **Repositories**: `ClientRepository`, `DocumentRepository`
- **Controller**: `ProfileController`

**Ответственность**: Управление профилями клиентов, документами, верификация, избранное.

**Ключевые методы Client:**
```java
+ verifyEmail() : void
+ ban(String) : void
+ unban() : void
+ canRentCar() : boolean
+ hasVerifiedDocuments() : boolean
```

---

### 4. **Identity Context** (Аутентификация) 🟣
**Цвет**: Фиолетовый (#F3E5F5)

**Основные компоненты:**
- **Entities**: `Role`, `RefreshToken`, `VerificationCode`
- **Services**: `JwtService`, `AuthService`
- **Controller**: `AuthController`

**Ответственность**: JWT-аутентификация, управление токенами, верификация email.

---

### 5. **Common/Shared** (Общие компоненты) ⚪
**Цвет**: Серый (#EEEEEE)

**Компоненты:**
- **Value Object**: `Money` (используется во всех контекстах)
- **Exceptions**: `ResourceNotFoundException`, `InvalidStateTransitionException`, `ValidationException`
- **Exception Handler**: `GlobalExceptionHandler` (обрабатывает все исключения)
- **Security**: `SecurityConfig`, `JwtAuthenticationFilter`

---

## 🎨 Цветовое кодирование

| Цвет | Контекст | Описание |
|------|----------|----------|
| 🟢 Зеленый | Fleet | Управление автомобилями |
| 🟠 Оранжевый | Rental | Управление арендой |
| 🔵 Голубой | Client | Управление клиентами |
| 🟣 Фиолетовый | Identity | Аутентификация |
| ⚪ Серый | Common | Общие компоненты |

---

## 📐 Стереотипы классов

| Стереотип | Описание | Пример |
|-----------|----------|--------|
| `<<AggregateRoot>>` | Корень агрегата (DDD) | Car, Contract, Client |
| `<<Entity>>` | Доменная сущность | CarModel, Document |
| `<<ValueObject>>` | Неизменяемый объект-значение | Money, Email, Vin |
| `<<DomainService>>` | Сервис с бизнес-логикой | RentalDomainService |
| `<<ApplicationService>>` | Сервис оркестрации | CarService, ContractService |
| `<<Repository>>` | Репозиторий (паттерн) | CarRepository |
| `<<Controller>>` | REST контроллер | CarController |
| `<<Configuration>>` | Spring конфигурация | SecurityConfig |
| `<<Filter>>` | Servlet фильтр | JwtAuthenticationFilter |

---

## 🔗 Типы связей

| Связь | Обозначение | Описание | Пример |
|-------|-------------|----------|--------|
| Композиция | `*--` | Сильная связь, жизненный цикл зависит от родителя | Car *-- GosNumber |
| Агрегация | `o--` | Слабая связь | Contract o-- Money |
| Ассоциация | `-->` | Ссылка на другой объект | Contract --> Car |
| Зависимость | `..>` | Использует | CarService ..> Money |
| Реализация | `..\|>` | Реализует интерфейс | CarServiceImpl ..\|> CarService |

---

## 🛠 Как просматривать диаграмму

### IntelliJ IDEA / PyCharm
1. Установите плагин **"PlantUML Integration"**
2. Откройте файл `class_diagram.puml`
3. Диаграмма отобразится в панели справа

### VS Code
1. Установите расширение **"PlantUML"**
2. Откройте файл `class_diagram.puml`
3. Нажмите `Alt+D` для предварительного просмотра

### Online
1. Откройте [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
2. Скопируйте содержимое `class_diagram.puml`
3. Вставьте в редактор

### Генерация изображения
```bash
# PNG (растровый формат)
java -jar plantuml.jar class_diagram.puml

# SVG (векторный формат, рекомендуется)
java -jar plantuml.jar -tsvg class_diagram.puml

# PDF
java -jar plantuml.jar -tpdf class_diagram.puml
```

---

## 📊 Что включено в диаграмму

### Entities (Сущности)
✅ **Fleet**: Car, CarModel, Brand, Model, CarClass, CarState, Image  
✅ **Rental**: Contract, RentalState  
✅ **Client**: Client, Document, DocumentType, Favorite  
✅ **Identity**: Role, RefreshToken, VerificationCode  

### Value Objects (Объекты-значения)
✅ Money, Email, Phone, Login, Password  
✅ GosNumber, Vin, Year  
✅ RentalPeriod  
✅ DocumentSeries, DocumentNumber, DateOfIssue, IssuingAuthority  

### Services (Сервисы)
✅ **Domain Services**: RentalDomainService, DocumentVerificationService  
✅ **Application Services**: CarService, ContractService, ClientService, AuthService  
✅ **Infrastructure Services**: JwtService  

### Repositories (Репозитории)
✅ CarRepository, ContractRepository, ClientRepository, DocumentRepository  

### Controllers (Контроллеры)
✅ CarController, ContractController, ProfileController, AuthController  

### Infrastructure (Инфраструктура)
✅ GlobalExceptionHandler, SecurityConfig, JwtAuthenticationFilter  

---

## 🔍 Ключевые паттерны

### 1. Aggregate Pattern (DDD)
- **Car** управляет CarState, Images
- **Contract** инкапсулирует RentalPeriod, totalCost
- **Client** управляет Documents, Favorites

### 2. Value Object Pattern
- Неизменяемые объекты (immutable)
- Валидация в конструкторе
- Методы сравнения по значению
- Пример: `Money`, `Email`, `Vin`

### 3. Repository Pattern
- Абстракция работы с БД
- Интерфейсы в Domain Layer
- Реализации в Infrastructure Layer

### 4. Service Layers
- **Domain Service**: бизнес-логика, не привязанная к одной сущности
- **Application Service**: оркестрация, координация между domain services и repositories

---

## 📝 Примечания

- Диаграмма упрощена для читаемости (не все вспомогательные классы показаны)
- DTO классы показаны выборочно (основные request/response)
- Mapper классы не включены (MapStruct генерирует их автоматически)
- Helper классы не показаны (будут удалены в рамках рефакторинга)
- Мультипликативность связей основана на JPA аннотациях в коде

---

## 🔄 Связь с другими диаграммами

- **sequence_diagram.puml**: Показывает динамическое взаимодействие этих классов
- **context_map.puml**: Показывает границы Bounded Contexts
- **entity_dependencies.puml**: Граф зависимостей между сущностями

---

## 📅 История изменений

### 2025-12-05
- ✅ Полностью переработана диаграмма
- ✅ Добавлено разделение на Bounded Contexts
- ✅ Добавлены все Value Objects
- ✅ Добавлены Domain и Application Services
- ✅ Добавлены Controllers и Repository паттерны
- ✅ Добавлена цветовая схема и легенда
- ✅ Добавлены бизнес-методы в Aggregate Roots

### Предыдущая версия
- Базовая диаграмма с entities и value objects

---

## 🎯 Для кого эта диаграмма

- **Новые разработчики**: быстрое понимание структуры проекта
- **Архитекторы**: анализ паттернов и зависимостей
- **DevOps**: понимание компонентов для контейнеризации
- **Тестировщики**: понимание границ компонентов для тестирования
- **Документация**: визуализация архитектуры

---

## 🆘 Troubleshooting

**Q: Диаграмма не рендерится**  
A: Убедитесь, что установлен Graphviz и PlantUML plugin

**Q: Диаграмма слишком большая**  
A: Это нормально для enterprise-приложения. Используйте zoom в IDE или экспортируйте в SVG для масштабирования

**Q: Не вижу некоторые классы**  
A: Диаграмма упрощена, вспомогательные классы не включены

---

**Последнее обновление:** 2025-12-05  
**Версия:** 2.0 (DDD Architecture)  
**Автор:** Development Team

