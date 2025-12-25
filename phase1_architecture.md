# Фаза 1: Завершение перехода на DDD архитектуру (ОБНОВЛЕНО)

**Цель фазы:** Довести DDD структуру до production-ready состояния, мигрировать оставшиеся legacy сервисы (Favorite, Analysis), внедрить Aggregate Root pattern и Domain Events.

**Время выполнения:** 10-14 часов (6 задач по 1.5-2 часа)

**Приоритет:** ⭐⭐⭐ Критический

---

## Текущее состояние проекта

### ✅ УЖЕ РЕАЛИЗОВАНО:

**1. Value Objects (25+ штук):**
- ✅ Common: `CarId`, `ClientId`, `Money`
- ✅ Fleet: `Vin`, `GosNumber`, `Year`, `ImageUrl`, `ImageData`, `FileName`, `DateOfIssue`, `CarStateType`, `IssuingAuthority`
- ✅ Identity: `Email`, `Password`, `Phone`, `Login`, `RoleId`, `RoleName`, `Permission` (enum), `DocumentId`, `DocumentTypeId`, `DocumentNumber`, `DocumentSeries`
- ✅ Rental: `ContractId`, `RentalPeriod`, `RentalStateType`

**2. DDD Structure:**
- ✅ `fleet/`, `identity/`, `rental/` - все с правильной структурой `api/`, `application/`, `domain/`, `infrastructure/`

**3. Domain Models (Aggregates):**
- ✅ Fleet: `CarDomain`, `CarBrandDomain`, `CarClassDomain`, `CarModelDomain`, `CarModelNameDomain`
- ✅ Identity: `Client`, `Document`, `RoleModel`, `DocumentTypeModel`
- ✅ Rental: `Contract`

**4. Role-Based Security:**
- ✅ `RoleModel` с `Set<Permission>`
- ✅ Enum `Permission` (11 разрешений: VIEW_USERS, MANAGE_USERS, BAN_USERS, VIEW_DOCUMENTS, VERIFY_DOCUMENTS, CREATE_CONTRACTS, UPDATE_CONTRACTS, CANCEL_CONTRACTS, MANAGE_CARS, VIEW_REPORTS, GENERATE_REPORTS)
- ✅ Value Objects: `RoleId`, `RoleName`

**5. Domain Events (начало):**
- ✅ `EmailVerificationCompletedEvent`, `PasswordResetCompletedEvent`
- ✅ `@EventListener` используется в `ClientApplicationServiceImpl`

### ❌ ТРЕБУЕТ ДОРАБОТКИ:

- ❌ **FavoriteServiceImpl** и **AnalysisServiceImpl** в старой структуре `service/impl/` - нет domain моделей
- ❌ **AggregateRoot интерфейс** НЕ создан (domain models его не implement)
- ❌ **Domain Events инфраструктура** не завершена:
  - Нет базового интерфейса `DomainEvent`
  - Нет коллекции событий в aggregates
  - Нет событий для ключевых бизнес-операций (CarRented, ContractConfirmed и т.д.)
- ❌ **Repository интерфейсы** частично в domain, но адаптеры в infrastructure не везде
- ❌ **Domain Service для Roles** - планируется для fine-grained access control

---

## 1.1. Создание Favorites bounded context (отдельный контекст)

### Конкретное действие

Создать **отдельный bounded context `favorites/`** для функциональности избранного (сейчас `service/impl/FavoriteServiceImpl`):

**Обоснование:** Favorites - это cross-cutting concern, который связывает Identity (Client) и Fleet (Car). По принципам DDD, такая функциональность заслуживает отдельного контекста.

1. Создать структуру нового bounded context:
   ```
   favorites/
   ├── api/
   │   └── rest/
   │       └── all/
   │           └── FavoriteController.java
   ├── application/
   │   └── service/
   │       └── FavoriteApplicationService.java
   ├── domain/
   │   ├── model/
   │   │   └── FavoriteCar.java (Aggregate Root)
   │   └── repository/
   │       └── FavoriteCarRepository.java
   └── infrastructure/
       └── persistence/
           ├── entity/
           │   └── FavoriteCarJpaEntity.java
           ├── repository/
           │   └── FavoriteCarJpaRepository.java
           └── adapter/
               └── FavoriteCarRepositoryAdapter.java
   ```

2. Создать `favorites/domain/model/FavoriteCar` (Aggregate Root)
   - Инкапсулирует связь ClientId → CarId
   - Методы: `addToFavorites()`, `removeFromFavorites()`
   - Валидация: клиент не может добавить одну машину дважды

3. Создать `favorites/domain/repository/FavoriteCarRepository` (интерфейс в domain)
   ```java
   public interface FavoriteCarRepository {
       Optional<FavoriteCar> findByClientAndCar(ClientId clientId, CarId carId);
       List<FavoriteCar> findAllByClient(ClientId clientId);
       void save(FavoriteCar favoriteCar);
       void delete(FavoriteCar favoriteCar);
   }
   ```

3. Создать `fleet/application/service/FavoriteApplicationService`
   - Orchestrator для use cases (add to favorites, remove, get all favorites)
   - Вызывает domain методы

4. Перенести JPA entity в `fleet/infrastructure/persistence/entity/FavoriteCarJpaEntity`
   - Маппер между FavoriteCar (domain) и FavoriteCarJpaEntity (JPA)

5. Создать `fleet/infrastructure/persistence/repository/FavoriteCarRepositoryAdapter`
   - Implements FavoriteCarRepository (domain interface)
   - Использует FavoriteCarJpaRepository (Spring Data)

6. Обновить API контроллер (если нужно)

7. **Удалить** старый `FavoriteServiceImpl`

### Что нужно изучить

- **Отдельный Bounded Context** для cross-cutting concerns
  - Когда функциональность затрагивает 2+ контекста
  - Context Mapping patterns (Customer-Supplier, Conformist, etc.)
- **DDD Aggregates** для случая "избранное" (отношение ClientId → CarId)
- **Anti-Corruption Layer** между контекстами
  - Favorites не зависит напрямую от Fleet/Identity domain models
  - Использует только их ID (CarId, ClientId из common/)
- **Repository Adapter pattern** - domain interface, infrastructure implementation
- **Eventual Consistency** между контекстами (через Domain Events)

### Возможные сложности

- **Cross-context references**: FavoriteCar ссылается на ClientId (из Identity) и CarId (из Fleet)
  - Решение: использовать только ID, не загружать объекты напрямую
- Текущий код использует прямые вызовы `carService.getCarById()` и `clientService.findClient()`
  - Решение: Application Service может вызывать другие Application Services для получения данных
- **Дублирование данных**: при отображении списка избранного нужны данные Car
  - Решение: Response Facade собирает данные из разных contexts

### Как проверить результат

**Структура файлов:**
```bash
# Новая структура - отдельный bounded context
tree src/main/java/org/example/carshering/favorites/ -L 3
ls src/main/java/org/example/carshering/favorites/domain/model/FavoriteCar.java
ls src/main/java/org/example/carshering/favorites/domain/repository/FavoriteCarRepository.java
ls src/main/java/org/example/carshering/favorites/application/service/FavoriteApplicationService.java
ls src/main/java/org/example/carshering/favorites/infrastructure/persistence/entity/FavoriteCarJpaEntity.java
ls src/main/java/org/example/carshering/favorites/infrastructure/persistence/adapter/FavoriteCarRepositoryAdapter.java

# Удален старый
! test -f src/main/java/org/example/carshering/service/impl/FavoriteServiceImpl.java
echo $?  # Должно вывести 0
```

**Пример кода FavoriteCar (domain):**
```javaavorites
package org.example.carshering.fleet.domain.model;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;

import java.time.LocalDateTime;

public class FavoriteCar {
    private final ClientId clientId;
    private final CarId carId;
    private final LocalDateTime addedAt;
    
    private FavoriteCar(ClientId clientId, CarId carId, LocalDateTime addedAt) {
        this.clientId = clientId;
        this.carId = carId;
        this.addedAt = addedAt;
    }
    
    // Factory method
    public static FavoriteCar add(ClientId clientId, CarId carId) {
        if (clientId == null || carId == null) {
            throw new IllegalArgumentException("ClientId and CarId cannot be null");
        }
        return new FavoriteCar(clientId, carId, LocalDateTime.now());
    }
    
    // Reconstruct from DB
    public static FavoriteCar restore(ClientId clientId, CarId carId, LocalDateTime addedAt) {
        return new FavoriteCar(clientId, carId, addedAt);
    }
}
```

**Тесты:**
```bash
# Unit тесты domain model
mvn test -Dtest=FavoriteCarTest

# Integration тесты application service
mvn test -Dtest=FavoriteApplicationServiceTest

# E2E тест API
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/favorites \
  -d '{"carId": 1}' \
  -H "Content-Type: application/json"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/favorites | jq .
```

**Проверка компиляции:**
```bash
mvn clean compile
mvn test
```

### Как это отразится в Git

```
feat(favorites): create separate Favorites bounded context

- Created new bounded context favorites/ for cross-cutting favorite functionality
- FavoriteCar aggregate with ClientId → CarId relationship
- FavoriteCarRepository interface in domain, adapter in infrastructure
- FavoriteApplicationService for use cases
- Anti-Corruption Layer: uses only CarId/ClientId from common, no direct dependencies
- Removed legacy FavoriteServiceImpl from service/impl
- No tests yet

BREAKING CHANGE: Favorites moved to separate bounded context

Closes #favorite-ddd-migration #separate-bounded-context
```

---

## 1.2. Создание Authorization bounded context для RBAC

### Конкретное действие

Создать **отдельный bounded context `authorization/`** для управления ролями и разрешениями (сейчас это частично в `identity/`):

**Обоснование:** Сложная система RBAC с field-level permissions, динамическими ролями и audit - это отдельная domain область, заслуживающая собственного контекста.

1. Создать структуру нового bounded context:
   ```
   authorization/
   ├── api/
   │   └── rest/
   │       └── admin/
   │           ├── RoleController.java
   │           └── PermissionController.java
   ├── application/
   │   └── service/
   │       ├── RoleApplicationService.java
   │       └── PermissionApplicationService.java
   ├── domain/
   │   ├── model/
   │   │   ├── Role.java (Aggregate Root)
   │   │   └── FieldAccessPolicy.java (Value Object)
   │   ├── repository/
   │   │   └── RoleRepository.java
   │   ├── service/
   │   │   └── RolePermissionService.java (Domain Service)
   │   └── valueobject/
   │       ├── Permission.java (enum)
   │       └── RoleId.java
   Расширить `Permission` enum для field-level:
   ```java
   public enum Permission {
       // User management
       VIEW_USERS, MANAGE_USERS, BAN_USERS,
       
       // Documents
       VIEW_DOCUMENTS, VERIFY_DOCUMENTS,
       
       // Contracts
       CREATE_CONTRACTS, UPDATE_CONTRACTS, CANCEL_CONTRACTS,
       
       // Fleet
       MANAGE_CARS, VIEW_ALL_CARS,
       
    RBAC (Role-Based Access Control)** vs **ABAC (Attribute-Based Access Control)**
- **Field-level security** patterns и best practices
- **Domain Services** в DDD - когда создавать отдельный сервис
- **Policy pattern** для authorization rules
- **Separate Authorization Context** - когда выделять в отдельный BC
- Spring Security integration с custom permission evaluator
- Audit logging для authorization decisions
   }
   ```
Производительность**: проверка permissions на каждый запрос
  - Решение: кэширование permissions для роли (Redis, Caffeine)
- **Конфигурация field-level policies**: где хранить?
  - В коде (enum) - просто, но негибко
  - В БД - гибко, можно менять через UI
  - В конфиге (YAML) - компромисс
  - **Рекомендация**: начать с БД
- **Audit**: логирование всех authorization decisions
  - Кто, когда, какой ресурс, разрешено/запрещено
- **Data masking**: как скрывать поля в JSON?
  - Jackson `@JsonView` - статично
  - Custom JsonSerializer - динамично
  - **Рекомендация**: Response Facades с проверкой permissions
- **Миграция**: существующие Client.roleId → новый Authorization context
  - Нужна синхронизация данных
   ```
   - `grantPermission()`, `revokePermission()`
   - `createCustomRole()` - динамические роли

4. Создать `FieldAccessPolicy` для field-level permissions:
   ```java
   public record FieldAccessPolicy(
       String entityType,   // "Client", "Car", "Contract"
       String fieldName,    // "phone", "email", "totalCost"
       Set<Permission> requiredPermissions
   ) {}
   ```

5. **Identity context** будет использовать Authorization через:
   - `Client` хранит только `RoleId` (reference by ID)
   - При проверке прав - вызов `authorization.RolePermissionService`

1. Создать структуру каталогов:
   ```
   analytics/
   ├── api/
   │   └── rest/
   │       └── all/
   │           └── StatisticsController.java
   ├── application/
   │   ├── service/
   │   │   ├── UserStatisticsApplicationService.java
   │   │   └── CarUtilizationApplicationService.java
   │   └── dto/
   ├── domain/
   │   ├── model/
   │   │   ├── UserRentalStatistics.java
   │   │   └── CarUtilizationReport.java
   │   └── repository/
   │       ├── UserStatisticsRepository.java
   │       └── CarUtilizationRepository.java
   └── infrastructure/
       └── persistence/
           ├── repository/
           └── clickhouse/  # Опционально для будущего
   ```

2. Определить domain модели:
   - `UserRentalStatistics` - статистика аренды для пользователя
   - `CarUtilizationReport` - отчет по использованию автомобиля
   - Value Objects: `AverageStartTime`, `RentalFrequency`, `UtilizationRate`

3. Создать Repository интерфейсы для аналитических запросов

4. Реализовать Application Services для отчетов

5. **Опционально**: подготовить интеграцию с ClickHouse (как упомянуто в комментариях AnalysisServiceImpl)
uthorization/ -L 3

# authorization/
# ├── api/
# │   └── rest/
# │       └── admin/
# ├── application/
# │   └── service/
# ├── domain/
# │   ├── model/
# │   │   ├── Role.java
# │   │   └── FieldAccessPolicy.java
# │   ├── repository/
# │   ├── service/
# │   │   └── RolePermissionService.java
# │   └── valueobject/
# └── infrastructure/
#     └── persistence/
```

**Миграция из identity:**
```bash
# Роли перенесены из identity в authorization
! test -f src/main/java/org/example/carshering/identity/domain/model/RoleModel.java
test -f src/main/java/org/example/carshering/authorization/domain/model/Role.java
```

**Пример Domain Service:**
```java
package org.example.carshering.authorization.domain.service;

import org.example.carshering.authorization.domain.model.Role;
import org.example.carshering.authorization.domain.valueobject.Permission;

public class RolePermissionService {
    
    private final FieldAccessPolicyRepository policyRepo;
    
    public boolean hasPermission(Role role, Permission permission) {
        return role.getPermissions().contains(permission);
    }
    
    public boolean canAccessField(Role role, String entityType, String fieldName) {
        // Загрузить policy из БД или кэша
        var policy = policyRepo.findByEntityAndField(entityType, fieldName);
        
        if (policy.isEmpty()) {
            return true;  // Поле не защищено
        }
        
        // Проверить, есть ли у роли хотя бы одно требуемое permission
        return policy.get().getRequiredPermissions().stream()
            .anyMatch(role::hasPermission);
    }
    
    public Role grantPermission(Role role, Permission permission) {
        if (role.isSystemRole()) {
            throw new SystemRoleImmutableException();
        }
        role.addPermission(permission);
        return role;
    }
}
```

**Тесты:**
```bash
mvn test -Dtest=RolePermissionServiceTest
mvn test -Dtest=FieldAccessPolicyTest

# API endpoint для управления ролями
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8082/api/authorization/roles \
  -d '{"name": "MANAGER", "permissions": ["VIEW_USERS", "VIEW_REPORTS"]}' \
  -H "Content-Type: application/json"

# Проверка field-level access
curl -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8082/api/client/1 | jq .
# Должно замаскировать поля без доступа
public class UserRentalStatistics {
    private final ClientId clientId;
    pruthorization): create separate Authorization bounded context for RBAC

- Created authorization bounded context for role and permission management
- Migrated Role, Permission from identity to authorization
- Implemented RolePermissionService with field-level security
- Added FieldAccessPolicy for fine-grained access control
- Created field_access_policies table for dynamic configuration
- Identity context references authorization via RoleId only
- Support for custom roles and dynamic permission assignment

BREAKING CHANGE: Role management moved from identity to authorization context

Refs: #authorization-context #rbac #field-level-security
Closes #fine-grained-access-control
        // Business logic для расчета средних значений
        // ...
        return new UserRentalStatistics(...);
    }
}
```

**Тесты:**
```bash
mvn test -Dtest=*Analytics*Test
mvn test -Dtest=UserRentalStatisticsTest
mvn test -Dtest=CarUtilizationReportTest

# API endpoint
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8082/api/analytics/user/1/statistics | jq .
```

### Как это отразится в Git

```
feat(analytics): create Analytics bounded context for business reports
   - `Role implements AggregateRoot<RoleId>` (после задачи 1.2)

- Created analytics bounded context structure
- Defined domain models: UserRentalStatistics, CarUtilizationReport
- Added repository interfaces for analytical queries
- Implemented Application Services for report generation
- Prepared structure for future ClickHouse integration
- Removed legacy AnalysisServiceImpl

Refs: #analytics-context
```

---

## 1.3. Создание AggregateRoot интерфейса и явных границ агрегатов

### Конкретное действие

Создать маркерный интерфейс `AggregateRoot<ID>` и применить к существующим domain моделям:

1. Создать `common/domain/AggregateRoot.java`:
   ```java
   public interface AggregateRoot<ID> {
       ID getId();
       
       List<DomainEvent> getDomainEvents();
       void clearDomainEvents();
       void registerEvent(DomainEvent event);
   }
   ```

2. Пометить существующие Aggregate Roots:
   - `CarDomain implements AggregateRoot<CarId>`
   - `Client implements AggregateRoot<ClientId>`
   - `Contract implements AggregateRoot<ContractId>`
   - `FavoriteCar implements AggregateRoot<FavoriteId>` (после задачи 1.1)

3. Добавить поля и методы для Domain Events:
   ```java
   private final List<DomainEvent> domainEvents = new ArrayList<>();
   ```

4. Документировать границы агрегатов в `package-info.java`:
   - Что является частью агрегата
   - Что должно быть отдельным Aggregate Root
   - Почему выбраны такие границы

5. Обеспечить, что Repository интерфейсы работают только с AggregateRoot

### Что нужно изучить

- **Aggregate pattern** в DDD (Eric Evans, Vaughn Vernon)
- **Aggregate boundaries** - правило "один AR на транзакцию"
- **Invariants enforcement** внутри агрегата
- **Reference by ID** vs object reference (между агрегатами только по ID)
- Transaction boundaries должны совпадать с aggregate boundaries

### Возможные сложности

- **Размер агрегата**: 
  - Слишком большой = производительность страдает, сложность
  - Слишком маленький = много координации между агрегатами
- **Document - часть Client aggregate?**
  - ✅ Да, если документ не имеет смысла без клиента
  - ❌ Нет, если документ может существовать независимо
  - **Рекомендация**: Document - часть Client aggregate (используется только вместе)
- **CarModel/CarBrand - часть Car aggregate?**
  - ❌ Нет, это справочники (reference data)
  - CarModel и CarBrand - отдельные Aggregate Roots
  - Car ссылается на CarModel по ModelId
- **JPA Lazy Loading** может нарушать границы агрегатов
  - Решение: явно загружать все части агрегата в Repository

### Как проверить результат

**Код интерфейса:**
```java
// common/domain/AggregateRoot.java
package org.example.carshering.common.domain;

import java.util.List;

public interface AggregateRoot<ID> {
    ID getId();
    
    List<DomainEvent> getDomainEvents();
    void clearDomainEvents();
    void registerEvent(DomainEvent event);
}
```

**Применение в CarDomain:**
```java
package org.example.carshering.fleet.domain.model;

import org.example.carshering.common.domain.AggregateRoot;
import org.example.carshering.common.domain.DomainEvent;
import org.example.carshering.common.domain.valueobject.CarId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CarDomain implements AggregateRoot<CarId> {
    private final CarId id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // ... existing fields
    
    @Override
    public CarId getId() { 
        return id; 
    }
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @Override
    public void clearDomainEvents() { 
        domainEvents.clear(); 
    }
    
    @Override
    public void registerEvent(DomainEvent event) { 
        domainEvents.add(event);
    }
    
    // Business method с событием
    public void rent(ClientId clientId, RentalPeriod period) {
        if (!this.isAvailable()) {
            throw new CarNotAvailableException(this.id);
        }
        this.state = CarStateType.RENTED;
        registerEvent(new CarRentedEvent(this.id, clientId, period, LocalDateTime.now()));
    }
    
    public void returnCar() {
        if (this.state != CarStateType.RENTED) {
            throw new IllegalStateException("Car is not rented");
        }
        this.state = CarStateType.AVAILABLE;
        registerEvent(new CarReturnedEvent(this.id, LocalDateTime.now()));
    }
}
```

**Проверки:**
```bash
# Найти все AggregateRoot implementations
grep -r "implements AggregateRoot" src/main/java/
# Должно найти: CarDomain, Client, Contract, FavoriteCar

# Repository работают только с AR
grep -r "extends JpaRepository" src/main/java/ | grep -v "(AggregateRoot"
# Не должно быть репозиториев для non-AR entities

# Компиляция и тесты
mvn clean compile
mvn test
```

**package-info.java для документации:**
```java
// fleet/domain/model/package-info.java
/**
 * Fleet Bounded Context - Domain Models
 * 
 * <h2>Aggregate Roots:</h2>
 * <ul>
 *   <li>{@link CarDomain} - автомобиль с его состоянием</li>
 *   <li>{@link CarBrandDomain} - справочник брендов</li>
 *   <li>{@link CarModelDomain} - справочник моделей</li>
 *   <li>{@link FavoriteCar} - избранное клиента</li>
 * </ul>
 * 
 * <h2>Aggregate Boundaries:</h2>
 * <ul>
 *   <li>Car агрегат включает только сам автомобиль (не Model, не Brand)</li>
 *   <li>Car ссылается на CarModel по ModelId (reference by ID)</li>
 *   <li>CarModel ссылается на CarBrand по BrandId</li>
 * </ul>
 */
package org.example.carshering.fleet.domain.model;
```

### Как это отразится в Git

```
refactor(domain): introduce AggregateRoot interface and define boundaries

- Created AggregateRoot<ID> marker interface with domain events support
- Marked CarDomain, Client, Contract, FavoriteCar as Aggregate Roots
- Added domain events collection management to aggregates
- Documented aggregate boundaries in package-info.java
- Business invariants enforced within aggregate methods
- Repository interfaces constrained to AggregateRoot only

Refs: #ddd-aggregates
```

---

## 1.4. Завершение инфраструктуры Domain Events

### Конкретное действие

Создать полноценную инфраструктуру Domain Events для event-driven архитектуры:

1. Создать базовый интерфейс `DomainEvent`:
   ```java
   public interface DomainEvent {
       LocalDateTime occurredOn();
       String getAggregateId();
   }
   ```

2. Создать события для ключевых бизнес-операций:
   **Fleet context:**
   - `CarCreatedEvent`
   - `CarRentedEvent`
   - `CarReturnedEvent`
   - `CarDeletedEvent`
   
   **Rental context:**
   - `ContractCreatedEvent`
   - `ContractConfirmedEvent`
   - `ContractCancelledEvent`
   - `ContractCompletedEvent`
   
   **Identity context:**
   - `ClientRegisteredEvent` (новый)
   - `DocumentVerifiedEvent` (новый)
   - `EmailVerificationCompletedEvent` (уже есть)
   - `PasswordResetCompletedEvent` (уже есть)

3. Интегрировать Spring `ApplicationEventPublisher`:
   ```java
   @Component
   public class DomainEventPublisher {
       private final ApplicationEventPublisher publisher;
       
       public void publish(DomainEvent event) {
           publisher.publishEvent(event);
       }
       
       public void publishAll(List<DomainEvent> events) {
           events.forEach(publisher::publishEvent);
       }
   }
   ```

4. Создать Event Handlers с `@TransactionalEventListener`:
   ```java
   @Component
   class CarEventHandler {
       @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
       void on(CarRentedEvent event) {
           log.info("Car {} rented to client {}", event.carId(), event.clientId());
           // Send notification, update statistics, etc.
       }
   }
   ```

5. Обновить Application Services для публикации событий:
   ```java
   @Service
   class ContractApplicationService {
       public ContractId createContract(...) {
           var contract = Contract.create(...);
           contractRepo.save(contract);
           
           // Publish events
           eventPublisher.publishAll(contract.getDomainEvents());
           contract.clearDomainEvents();
           
           return contract.getId();
       }
   }
   ```

### Что нужно изучить

- **Domain Events pattern** в DDD (Vaughn Vernon)
- **Spring ApplicationEventPublisher** и `@EventListener`
- **@TransactionalEventListener** - BEFORE_COMMIT, AFTER_COMMIT, AFTER_ROLLBACK
- **Event Storming** technique для поиска domain events
- **Event Sourcing basics** (для будущего)
- Difference: Domain Events vs Integration Events (для Kafka)

### Возможные сложности

- **Транзакционность**: события должны публиковаться ПОСЛЕ успешного commit
  - Решение: `@TransactionalEventListener(phase = AFTER_COMMIT)`
- **Immutability**: события должны быть immutable
  - Решение: использовать `record` для событий
- **Serialization**: для будущей интеграции с Kafka
  - Решение: все поля должны быть serializable
- **Синхронная vs Асинхронная обработка**:
  - Синхронно: проще, но медленнее
  - Асинхронно: быстрее, но сложнее (нужен @Async)
  - Рекомендация: начать с синхронной
- **Ordering**: порядок событий может быть важен
  - Решение: добавить `sequenceNumber` в DomainEvent

### Как проверить результат

**DomainEvent интерфейс:**
```java
// common/domain/DomainEvent.java
package org.example.carshering.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface DomainEvent extends Serializable {
    LocalDateTime occurredOn();
    String getAggregateId();
    
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
```

**Пример события:**
```java
// rental/domain/event/ContractCreatedEvent.java
package org.example.carshering.rental.domain.event;

import org.example.carshering.common.domain.DomainEvent;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;

import java.time.LocalDateTime;

public record ContractCreatedEvent(
    ContractId contractId,
    ClientId clientId,
    CarId carId,
    RentalPeriod period,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public ContractCreatedEvent {
        if (contractId == null) {
            throw new IllegalArgumentException("ContractId cannot be null");
        }
        if (occurredOn == null) {
            occurredOn = LocalDateTime.now();
        }
    }
    
    @Override
    public String getAggregateId() {
        return contractId.value().toString();
    }
}
```

**Event Handler:**
```java
// rental/application/event/ContractEventHandler.java
package org.example.carshering.rental.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.example.carshering.rental.domain.event.ContractCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractEventHandler {
    
    private final CarApplicationService carService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContractCreated(ContractCreatedEvent event) {
        log.info("Contract created: {}, marking car {} as rented", 
            event.contractId(), event.carId());
        
        // Update car status (cross-context communication через Application Service)
        carService.markAsRented(event.carId(), event.contractId());
    }
}
```

**Использование в Aggregate:**
```java
public class Contract implements AggregateRoot<ContractId> {
    public static Contract create(ClientId clientId, CarId carId, 
                                   RentalPeriod period, Money dailyRate) {
        Money total = dailyRate.multiply(period.getDurationInDays());
        var contract = new Contract(null, clientId, carId, period, total,
                RentalStateType.PENDING, null);
        
        // Register event
        contract.registerEvent(new ContractCreatedEvent(
            contract.getId(),
            clientId,
            carId,
            period,
            LocalDateTime.now()
        ));
        
        return contract;
    }
}
```

**Тесты:**
```java
@SpringBootTest
class ContractEventHandlerTest {
    
    @MockBean
    private CarApplicationService carService;
    
    @Autowired
    private ContractApplicationService contractService;
    
    @Test
    void shouldPublishEventWhenContractCreated() {
        // when
        contractService.createContract(...);
        
        // then
        verify(carService).markAsRented(any(CarId.class), any(ContractId.class));
    }
}
```

**Проверки:**
```bash
# Найти все DomainEvent implementations
grep -r "implements DomainEvent\|record.*Event.*implements DomainEvent" src/main/java/

# Найти Интеграция Authorization context с остальными контекстами

### Конкретное действие

Интегрировать новый `authorization/` context с `identity/`, `fleet/`, `rental/` для проверки прав доступа

# Логи при создании контракта (должны показать события)
mvn spring-boot:run
# В другом терминале:
curl -X POST http://localhost:8082/api/contract \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -**Identity context integration:**
   - `Client` хранит только `RoleId` (reference by ID)
   - При аутентификации загружать `Role` из `authorization.RoleRepository`
   - Security config использует `authorization.RolePermissionService`

2. **Response Facades с field-level security:**
   
   В каждом контексте (Fleet, Identity, Rental) обновить Response Facades:
   
   ```java
   // identity/api/facade/ClientResponseFacade.java
   @Component
   @RequiredArgsConstructor
   public class ClientResponseFacade {
       
       private final RolePermissionService permissionService;  // из authorization
       private final RoleRepository roleRepo;  // из authorization
       
       public ClientResponse toResponse(ClientDto client, RoleId currentUserRoleId) {
           var role = roleRepo.findById(currentUserRoleId).orElseThrow();
           
           var response = new ClientResponse();
           response.setId(client.id().value());
           response.setFirstName(client.firstName());
           response.setLastName(client.lastName());
           
           // Field-level access control
           if (permissionService.canAccessField(role, "Client", "email")) {
               response.setEmail(client.email().getValue());
           } else {
               response.setEmail("***@***.***");  // Masked
           }
           
           if (permissionService.canAccessField(role, "Client", "phone")) {
               response.setPhone(client.phone().getValue());
           } else {
               response.setPhone("+7***");  // Masked
           }
           
           return response;
       }
   }
   ```

3. **Spring Security integration:**
   
   Создать custom `PermissionEvaluator`:
   ```java
   @Component
   public class DomainPermissionEvaluator implements PermissionEvaluator {
       
       private final RolePermissionService permissionService;
       
       @Override
       public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
           var userDetails = (ClientDetails) auth.getPrincipal();
           var role = roleRepo.findById(userDetails.getRoleId()).orElseThrow();
           
           return permissionService.hasPermission(role, (Permission) permission);
       }
   }
   ```
   
   Использование в контроллерах:
   ```java
   @PreAuthorize("hasPermission(null, 'MANAGE_USERS')")
   @PutMapping("/{id}/ban")
   public ResponseEntity<Void> banUser(@PathVariable Long id) {
       // ...
   }
   `Context Integration** patterns в DDD
  - Customer-Supplier
  - Published Language
  - Anti-Corruption Layer
- **Cross-context communication:**
  - Синхронно через Application Services
  - Асинхронно через Domain Events
- Spring Security **PermissionEvaluator** custom implementation
- **Caching** для permissions (Spring Cache, Caffeine, Redis)
- **@PreAuthorize** с custom permissions
- **N+1 проблема**: загрузка Role для каждого запроса
  - Решение: кэширование Role в ClientDetails (после login)
  - Или кэш permissions в Redis с TTL
- **Циклическая зависимость**: Authorization ← Identity → Authorization
  - Решение: только reference by ID, не прямые зависимости
  - Authorization не зависит от Identity
- **Инвалидация кэша**: при изменении permissions
  - Решение: Domain Events → слушатель очищает кэш
- **Тестирование**: mock RolePermissionService в тестах других контекстов
  - Использовать test doubles
- **Migration**: существующие roleId в Client должны остаться валидными
           return role.getPermissions();
       }
       
       @CacheEvict("rolePermissions")
       public void clearCache(RoleId roleId) {
           // Вызывается при RolePermissionsChangedEvent
       }
  Custom PermissionEvaluator:**
```java
// security/DomainPermissionEvaluator.java
package org.example.carshering.security
3. Создать `FieldAccessPolicy` для контроля доступа к полям:
   ```java
   publlombok.RequiredArgsConstructor;
import org.example.carshering.authorization.domain.service.RolePermissionService;
import org.example.carshering.authorization.domain.repository.RoleRepository;
import org.example.carshering.authorization.domain.valueobject.Permission;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class DomainPermissionEvaluator implements PermissionEvaluator {
    
    private final RolePermissionService permissionService;
    private final RoleRepository roleRepository;
    
    @Override
    public boolean hasPermission(Authentication authentication, 
                                  Object targetDomainObject, 
                                  Object permission) {
        if (authentication == null || !(permission instanceof String)) {
            return false;
        }
        
        var userDetails = (ClientDetails) authentication.getPrincipal();
        var role = roleRepository.findById(userDetails.getRoleId())
            .orElseThrow(() -> new RoleNotFoundException(userDetails.getRoleId()));
        
        Permission perm = Permission.valueOf((String) permission);
        return permissionService.hasPermission(role, perm);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, 
                                  Serializable targetId, 
                                  String targetType, 
                                  Object permission) {
        return hasPermission(authentication, null, permission)езультат

**RolePermissionService (Domain Service):**
```java
// identity/domain/service/RolePermissionService.java
package org.example.carshering.identity.domain.service;

import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.valueobject.role.Permission;

import java.util.Map;
import java.util.Set;

public class RolePermissionService {
    
    // Field-level access policies (можно вынести в конфиг)
    private static final Map<String, Set<Permission>> FIELD_POLICIES = Map.of(
  Controller с @PreAuthorize:**
```java
// identity/api/rest/admin/AdminClientController.java
@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
public class AdminClientController {
    
    private final ClientApplicationService clientService;
    
    @PreAuthorize("hasPermission(null, 'MANAGE_USERS')")
    @PutMapping("/{id}/ban")
    public ResponseEntity<Void> banClient(@PathVariable Long id) {
        clientService.banClient(new ClientId(id));
        return ResponseEntity.ok().build();
    }
    
    @PreAuthorize("hasPermission(null, 'VIEW_USERS')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(
        @PathVariable Long id,
        @AuthenticationPrincipal ClientDetails currentUser
    ) {
        var client = clientService.findById(new ClientId(id));
        var response = responseFacade.toResponse(client, currentUser.getRoleId());
        return ResponseEntity.ok(response)
        role.getPermissions().add(permission);
        return role;
    }
    
    public RoleModel revokePermission(RoleModel role, Permission permission) {
        if (role.isSystemRole()) {
            throw new IllegalArgumentException("Cannot modify system role");
        }
        role.getPermissions().remove(permission);
        return role;
    }
@SpringBootTest
class AuthorizationIntegrationTest {
    
    @Autowired
    private RolePermissionService permissionService;
    
    @Autowired
    private RoleRepository roleRepo;
    
    @Test
    void shouldEnforceFieldLevelSecurity() {
        var adminRole = roleRepo.findByName(new RoleName("ADMIN")).orElseThrow();
        var userRole = roleRepo.findByName(new RoleName("USER")).orElseThrow();
        
        // Admin can access sensitive fields
        assertTrue(permissionService.canAccessField(adminRole, "Client", "phone"));
        assertTrue(permissionService.canAccessField(adminRole, "Client", "email"));
        
        // User cannot
        assertFalse(permissionService.canAccessField(userRole, "Client", "phone"));
        assertFalse(permissionService.canAccessField(userRole, "Client", "email"));
    }
    
    @Test
    @WithMockUser(authorities = "MANAGE_USERS")
    void shouldAllowBanWithCorrectPermission() throws Exception {
        mockMvc.perform(put("/api/admin/clients/1/ban")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(authorities = "VIEW_USERS")  // Нет MANAGE_USERS
    void shouldDenyBanWithoutPermission() throws Exception {
        mockMvc.perform(put("/api/admin/clients/1/ban")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden(email")) {
            response.setEmail(client.email().getValue());
        } else {
            response.setEmail("***@***.***");  // Masked
        }
        
        if (permissionService.canAccessField(currentUserRole, "Client", "phone")) {
            response.setPhone(client.phone().getValue());
        } else {
            response.setPhone("+7***");  // Masked
        }
        
        // Password никогда не отдаем в response
        
        return response;
    }
}
```

**Тесты:**
```java
class RolePermissionServiceTest {
    
    private RolePermissionService service;
    
    @Test
    void adminShouldAccessAllFields() {
        var adminRole = createAdminRole();
        
        assertTrue(service.canAccessField(adminRole, "Client", "phone"));
        assertTrue(service.canAccessField(adminRole, "Client", "email"));
        assertTrue(service.canAccessField(adminRole, "Client", "password"));
    }
    
    @Test
    void regularUserCannotAccessSensitiveFields() {
        var userRole = createUserRole();
        
        assertFalse(service.canAccessField(userRole, "Client", "phone"));
        assertFalse(service.canAccessField(userRole, "Client", "email"));
        assertFalse(service.canAccessField(userRole, "Client", "password"));
    }
}
```

**Проверки:**
```bash
# Unit tests
mvn test -Dtest=RolePermissionServiceTest

# Integration test
curl -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8082/api/client/1 | jq .
# Должно показать:
# {
#   "id": 1,
#   "firstName": "Иван",
#   "lastName": "Иванов",
#   "email": "***@***.***",    # Masked
#   "phone": "+7***"            # Masked
# }

curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8082/api/client/1 | jq .
# Должно показать полные данные:
# {
#   "id": 1,
#   "firstName": "Иван",
#   "lastName": "Иванов",
#   "email": "ivan@example.com",
#   "phone": "+79991234567"
# }
```
authorization): integrate Authorization context with other contexts

- Integrated authorization context with identity, fleet, rental
- Created custom PermissionEvaluator for Spring Security
- Implemented @PreAuthorize with domain permissions in controllers
- Response Facades use RolePermissionService for field-level masking
- Added permission caching with Spring Cache
- Domain Events for role changes → cache invalidation
- Tests for cross-context authorization

Refs: #authorization-integration #spring-security
Closes #field-level-security-implementation
- Tests for all permission scenarios

Refs: #rbac #field-level-security
Closes #fine-grained-access-control
```

---

## 1.6. Рефакторинг Application Services - тонкие orchestrators

### Конкретное действие

Провести рефакторинг Application Services — они должны быть тонкими orchestrators, вся бизнес-логика в Domain:

1. **Аудит существующих Application Services**:
   - `CarApplicationService`
   - `ClientApplicationService`
   - `ContractApplicationService`
   - Найти бизнес-логику в этих сервисах

2. **Переместить бизнес-логику**:
   - Из Application Services → в Domain Methods (Aggregate методы)
   - Из Application Services → в Domain Services (если логика затрагивает несколько Aggregates)

3. **Application Service должен только**:
   ```java
   public ContractId createContract(CreateContractRequest request) {
       // 1. Load aggregates
       var car = carRepo.findById(request.carId());
       var client = clientRepo.findById(request.clientId());
       
       // 2. Delegate to domain
       var contract = Contract.create(car, client, request.period());
       
       // 3. Save
       contractRepo.save(contract);
       
       // 4. Publish events
       eventPublisher.publishAll(contract.getDomainEvents());
       contract.clearDomainEvents();
       
       // 5. Return result
       return contract.getId();
   }
   ```

4. **Удалить Transaction Script antipattern**:
   - Нет процедурной логики в Application Service
   - Нет if/else для бизнес-правил в Application Service
   - Все бизнес-правила инкапсулированы в Domain

5. **Примеры переноса**:
   - Валидация → Domain Model constructor или factory method
   - Вычисление цены → Domain Service или Aggregate method
   - Изменение статуса → Aggregate method с инвариантами

### Что нужно изучить

- **Application Service vs Domain Service** разница:
  - Application Service - orchestration, use cases
  - Domain Service - business logic spanning multiple aggregates
- **Transaction Script antipattern** - почему это плохо
- **Anemic Domain Model antipattern** - модели без логики
- **Rich Domain Model** - модели с инкапсулированной логикой
- Tell, Don't Ask principle

### Возможные сложности

- **Где граница?** Что должно быть в Domain, что в Application?
  - Domain: бизнес-правила, инварианты, вычисления
  - Application: orchestration, транзакции, события
- **Множественные агрегаты**: если нужно изменить 2+ агрегата в одной транзакции
  - Решение: координация через Application Service, но логика в Domain
- **Domain Services vs Application Services**: когда использовать каждый
  - Domain Service: бизнес-логика, которая не принадлежит одному Aggregate
  - Application Service: use case, координация
- **Тестирование**: Domain должен тестироваться без Spring context

### Как проверить результат

**ДО (плохо - бизнес-логика в Application Service):**
```java
@Service
@RequiredArgsConstructor
public class ContractApplicationService {
    
    public void createContract(CreateContractRequest request) {
        var car = carRepo.findById(request.carId());
        
        // ❌ Бизнес-логика в Application Service
        if (car.getState() != CarStateType.AVAILABLE) {
            throw new CarNotAvailableException();
        }
        
        var client = clientRepo.findById(request.clientId());
        
        // ❌ Бизнес-логика в Application Service
        if (!client.hasValidDocument()) {
            throw new InvalidDocumentException();
        }
        
        // ❌ Бизнес-логика в Application Service
        if (request.startDate().isAfter(request.endDate())) {
            throw new InvalidPeriodException();
        }
        
        var contract = new Contract();
        contract.setCarId(car.getId());
        contract.setClientId(client.getId());
        contract.setPeriod(new RentalPeriod(request.startDate(), request.endDate()));
        
        // ❌ Бизнес-логика (вычисление цены) в Application Service
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        BigDecimal total = car.getDailyRate().multiply(BigDecimal.valueOf(days));
        contract.setTotalCost(Money.rubles(total));
        
        contractRepo.save(contract);
    }
}
```

**ПОСЛЕ (хорошо - логика в Domain):**
```java
@Service
@RequiredArgsConstructor
public class ContractApplicationService {
    
    private final ContractRepository contractRepo;
    private final CarRepository carRepo;
    private final ClientRepository clientRepo;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public ContractId createContract(CreateContractRequest request) {
        // 1. Load aggregates
        var car = carRepo.findById(new CarId(request.carId()))
            .orElseThrow(() -> new NotFoundException("Car not found"));
        var client = clientRepo.findById(new ClientId(request.clientId()))
            .orElseThrow(() -> new NotFoundException("Client not found"));
        
        var period = RentalPeriod.of(request.startDate(), request.endDate());
        
        // 2. Delegate to domain (ВСЯ логика внутри)
        var contract = Contract.create(client.getId(), car.getId(), period, car.getDailyRate());
        
        // Domain будет проверять:
        // - car.isAvailable()
        // - client.hasValidDocument()
        // - period.isValid()
        // - вычислять totalCost
        // - регистрировать ContractCreatedEvent
        
        // 3. Save
        contractRepo.save(contract);
        
        // 4. Publish events
        eventPublisher.publishAll(contract.getDomainEvents());
        contract.clearDomainEvents();
        
        return contract.getId();
    }
}
```

**Domain Model (Contract) с бизнес-логикой:**
```java
public class Contract implements AggregateRoot<ContractId> {
    
    public static Contract create(ClientId clientId, CarId carId, 
                                   RentalPeriod period, Money dailyRate) {
        // ✅ Вся бизнес-логика здесь
        
        // Валидация периода (уже в RentalPeriod.of())
        // Период должен быть валидным, иначе исключение
        
        // Вычисление цены
        Money total = dailyRate.multiply(period.getDurationInDays());
        
        var contract = new Contract(
            null,  // id будет назначен при save
            clientId,
            carId,
            period,
            total,
            RentalStateType.PENDING,
            null
        );
        
        // Регистрация события
        contract.registerEvent(new ContractCreatedEvent(
            contract.getId(),
            clientId,
            carId,
            period,
            LocalDateTime.now()
        ));
        
        return contract;
    }
}
```

**Тесты Domain без Spring:**
```java
// Pure unit test - без Spring context
class ContractTest {
    
    @Test
    void shouldCalculateTotalCostWhenCreated() {
        // given
        var period = RentalPeriod.of(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(3)
        );
        var dailyRate = Money.rubles(1000);
        
        // when
        var contract = Contract.create(
            new ClientId(1L),
            new CarId(1L),
            period,
            dailyRate
        );
        
        // then
        assertEquals(Money.rubles(3000), contract.getTotalCost());
    }
    
    @Test
    void shouldRegisterEventWhenCreated() {
        // when
        var contract = Contract.create(...);
        
        // then
        assertEquals(1, contract.getDomainEvents().size());
        assertTrue(contract.getDomainEvents().get(0) instanceof ContractCreatedEvent);
    }
}
```

**Проверки:**
```bash
# Domain unit tests (без Spring)
mvn test -Dtest=*DomainTest
mvn test -Dtest=ContractTest,CarDomainTest,ClientTest

# Не должно быть if/else в Application Services
grep -r "if.*throw" src/main/java/**/application/service/*.java
# Если есть - это бизнес-логика, нужно переносить в Domain

# Application Services должны быть короткими (<50 строк на метод)
# Проверка вручную через IDE

mvn clean test
```

### Как это отразится в Git

```
refactor(application): move business logic from Application to Domain layer

- Extracted business rules from Application Services to Domain
- Implemented Rich Domain Model pattern (vs Anemic Domain Model)
- Application Services are now thin orchestrators (load, delegate, save, publish)
- Domain logic testable without Spring context
- Improved separation of concerns
- Validation, calculations, state transitions - all in Domain

BREAKING CHANGE: Application Service methods signatures may change

Refs: #rich-domain-model #clean-architecture
Closes #anemic-domain-refactoring
```

---

## Чеклист выполнения Фазы 1
s bounded context создан (отдельный от Fleet/Identity)
- [ ] 1.2. Authorization bounded context создан (с RolePermissionService)
- [ ] 1.3. AggregateRoot интерфейс создан и применен ко всем AR
- [ ] 1.4. Domain Events инфраструктура работает (DomainEvent interface, события, handlers)
- [ ] 1.5. Authorization интегрирован с другими контекстами (PermissionEvaluator, caching)
- [ ] 1.6. Application Services - тонкие orchestrators (бизнес-логика в Domain) создан
- [ ] 1.6. Application Services - тонкие orchestrators

## Результат Фазы 1

✅ **Чистая DDD архитектура** с явными границами слоев и агрегатов  
✅ **Domain независим от Infrastructure** (Dependency Inversion)  
✅ **Rich Domain Model** вместо Anemic (вся бизнес-логика в domain)  
✅ **Domain Events** для event-driven архитектуры  
✅ **Fine-grained RBAC** с field-level security  
✅ **Готовность к декомпозиции** на микросервисы (bounded contexts изолированы)  
✅ **Готовность к Kafka** integration (через Domain Events)  


**Примечание:** Analytics bounded context (для AnalysisServiceImpl) перенесен на **Фазу 7: Observability**, так как требует инфраструктуры метрик и мониторинга.
**Следующая фаза:** [Phase 2: Code Quality](phase2_code_quality.md) — исправление TODO, null handling, глобальный error handling
