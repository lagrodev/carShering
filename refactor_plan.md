# План рефакторинга Car Sharing Backend

## 📊 Текущее состояние проекта

### Архитектурный статус
- **Версия Spring Boot**: 3.5.6
- **Java**: 21
- **Архитектура**: Монолит с элементами слоистой архитектуры
- **Покрытие тестами**: ~50% (807/1232 тестов проходят)
- **Текущая структура**: 
  - `domain/entity` — JPA-сущности
  - `domain/valueobject` — Value Objects (частично реализованы, например `Money`)
  - `repository` — Spring Data репозитории
  - `service` — бизнес-логика (impl + interfaces + domain)
  - `dto` — DTO для API (request/response)
  - `rest` — REST контроллеры
  - `mapper` — MapStruct мапперы

### Выявленные архитектурные проблемы

#### 🔴 Критичные нарушения DDD
1. **DTO в репозиториях** — `CarRepository.findMinMaxPriceByFilter()` возвращает `MinMaxCellForFilters` (DTO), нарушая границу слоёв
2. **Зависимости в мапперах** — `CarMapper` содержит `@Autowired CarModelRepository`, что нарушает Single Responsibility Principle
3. **Отсутствие чётких агрегатов** — сущности связаны напрямую через `@ManyToOne` без выделения корней агрегатов
4. **Сервисы обращаются к чужим сущностям** — `ContractServiceImpl` напрямую работает с `Car`, `Client`, `Document`
5. **Бизнес-логика в сервисах приложения** — логика расчёта стоимости, проверки доступности машины находится в `RentalDomainService`, но вызывается из Application Service

#### 🟡 Средние проблемы
1. **Нет разделения Domain Services и Application Services**
2. **Helper-сервисы** (`CarServiceHelperService`, `ClientServiceHelper`) — признак нечётких границ
3. **Анемичная модель** — сущности (Car, Contract, Client) не содержат бизнес-логики
4. **Отсутствие явных Bounded Contexts** — всё в одном пакете `org.example.carshering`

#### 🟢 Что уже хорошо
- ✅ Глобальный обработчик ошибок (`GlobalExceptionHandler`)
- ✅ README с документацией
- ✅ Value Object `Money` реализован правильно
- ✅ JWT аутентификация
- ✅ Flyway миграции
- ✅ MapStruct для маппинга
- ✅ Testcontainers для интеграционных тестов

---

## 🎯 Цели рефакторинга

### Этап 0-1 (Текущий фокус)
1. Подготовка к переходу на микросервисную архитектуру
2. Внедрение принципов DDD (агрегаты, слои, Bounded Contexts)
3. Устранение архитектурных нарушений
4. Модуляризация без дробления на микросервисы

### Будущие этапы (2+)
- CI/CD (GitHub Actions)
- Кэширование (Redis)
- WebSocket
- Rate limiting
- Метрики
- Liquibase
- Разделение на микросервисы

---

## 📋 ЭТАП 0: Подготовка и очистка (1-2 дня)

### 0.1. Анализ и инвентаризация
- [x] **Создать документ `ARCHITECTURE.md`** с текущим состоянием архитектуры
- [ ] **Инвентаризация мёртвого кода**:
  - Проверить использование `main.java`, `home.java` в `rest/`
  - Найти неиспользуемые DTO/мапперы
  - Удалить закомментированный код (например, `// todo` в `Contract.java`)
- [x] **Анализ зависимостей между сущностями** — построить граф связей
- [x] **Документировать Bounded Contexts** — выделить предметные области

**Результат**: Чистая кодовая база, документация текущей архитектуры

### 0.2. Устранение технического долга
- [ ] **Удалить мёртвый код**:
  ```bash
  # Проверить использование:
  - src/main/java/org/example/carshering/main.java
  - src/main/java/org/example/carshering/rest/home.java
  - Неиспользуемые TODO-комментарии
  ```
- [ ] **Обновить документацию**:
  - Дополнить README секцией "Архитектура"
  - Описать текущие Bounded Contexts
  - Добавить диаграммы (PlantUML/Mermaid)

**Результат**: Кодовая база готова к рефакторингу

---

## 📋 ЭТАП 1: Выравнивание структуры под DDD (3-5 дней)

### 1.1. Определение Bounded Contexts

Выделить 4 основных контекста:

#### **1. Rental Context (Контекст аренды)** — ЯДРО
- **Агрегаты**: `Contract` (корень), `RentalState`
- **Сервисы**: `RentalDomainService`, `ContractService`
- **Value Objects**: `RentalPeriod`, `RentalCost`
- **Ответственность**: Управление процессом аренды

#### **2. Fleet Context (Контекст автопарка)**
- **Агрегаты**: `Car` (корень), `CarModel`, `CarState`
- **Сервисы**: `CarService`, `CarAvailabilityService`
- **Value Objects**: `VIN`, `GosNumber`, `Money`
- **Ответственность**: Управление автомобилями

#### **3. Client Context (Контекст клиентов)**
- **Агрегаты**: `Client` (корень), `Document`, `Favorite`
- **Сервисы**: `ClientService`, `DocumentVerificationService`
- **Value Objects**: `Email`, `Phone`, `ClientStatus`
- **Ответственность**: Управление клиентами и документами

#### **4. Identity Context (Контекст идентификации)**
- **Агрегаты**: `Client` (projection), `Role`, `RefreshToken`
- **Сервисы**: `AuthService`, `JwtService`
- **Value Objects**: `Token`, `Credentials`
- **Ответственность**: Аутентификация и авторизация

### 1.2. Реорганизация структуры пакетов

**Целевая структура "луковичной" архитектуры**:

```
org.example.carshering/
├── common/                          # Общие компоненты
│   ├── domain/
│   │   └── valueobject/             # Общие Value Objects
│   │       └── Money.java
│   ├── exceptions/                  # Общие исключения
│   └── infrastructure/              # Общая инфраструктура
│
├── rental/                          # Bounded Context: Rental
│   ├── domain/                      # DOMAIN LAYER
│   │   ├── model/                   # Агрегаты и сущности
│   │   │   ├── Contract.java        # Aggregate Root
│   │   │   ├── RentalState.java
│   │   │   └── RentalPeriod.java    # Value Object
│   │   ├── service/                 # Domain Services
│   │   │   └── RentalDomainService.java
│   │   └── repository/              # Repository интерфейсы (!)
│   │       └── ContractRepository.java
│   │
│   ├── application/                 # APPLICATION LAYER
│   │   ├── service/                 # Application Services
│   │   │   └── ContractApplicationService.java
│   │   ├── dto/                    # DTO для внутреннего использования
│   │   └── mapper/                 # Мапперы Domain <-> DTO
│   │       └── ContractDtoMapper.java
│   │
│   ├── infrastructure/              # INFRASTRUCTURE LAYER
│   │   ├── persistence/             # Реализации репозиториев
│   │   │   ├── ContractRepositoryImpl.java
│   │   │   ├── ContractJpaRepository.java
│   │   │   └── entity/              # JPA-сущности (!)
│   │   │       └── ContractJpaEntity.java
│   │   └── mapper/                  # Мапперы Domain <-> JPA
│   │       └── ContractEntityMapper.java
│   │
│   └── api/                         # API/PRESENTATION LAYER
│       ├── rest/                    # REST контроллеры
│       │   └── ContractController.java
│       ├── dto/                     # API DTO (request/response)
│       └── mapper/              # Мапперы API DTO <-> Application DTO
│
├── fleet/                           # Bounded Context: Fleet
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Car.java             # Aggregate Root
│   │   │   ├── CarModel.java
│   │   │   ├── VIN.java             # Value Object
│   │   │   └── GosNumber.java       # Value Object
│   │   ├── service/
│   │   └── repository/
│   ├── application/
│   ├── infrastructure/
│   └── api/
│
├── client/                          # Bounded Context: Client
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Client.java          # Aggregate Root
│   │   │   ├── Document.java
│   │   │   └── Email.java           # Value Object
│   │   ├── service/
│   │   └── repository/
│   ├── application/
│   ├── infrastructure/
│   └── api/
│
└── identity/                        # Bounded Context: Identity
    ├── domain/
    ├── application/
    ├── infrastructure/
    └── api/
```

### 1.3. Пошаговая миграция

#### **Шаг 1.3.1: Создание новой структуры (1 день)**
- [x] Создать пакеты для каждого Bounded Context
- [x] Создать слои (domain/application/infrastructure/api)
- [x] **НЕ УДАЛЯТЬ** старый код — работать параллельно

#### **Шаг 1.3.2: Value Objects (1 день)**
- [x] **Fleet Context**:
  - Создать `VIN.java` (вместо String в Car)
  - Создать `GosNumber.java`
  - Добавить валидацию в конструкторы
- [x] **Client Context**:
  - Создать `Email.java` (с валидацией)
  - Создать `Phone.java`
  - Создать `ClientStatus.java` (enum wrapper)
- [x] **Rental Context**:
  - Создать `RentalPeriod.java` (инкапсулирует start/end + валидацию)
  - Создать `RentalCost.java` (обёртка над Money)

#### **Шаг 1.3.3: Domain Models (2 дня)**

**Правила для агрегатов**:
1. Aggregate Root имеет публичные методы для всех операций
2. Вложенные сущности изменяются только через Root
3. Инварианты проверяются в конструкторах и методах
4. Нет сеттеров — только бизнес-методы

**Пример: Contract (Rental Context)**
 - [x] создать Contract
```java
// rental/domain/model/Contract.java
@Getter
public class Contract {
    private final ContractId id;
    private final ClientId clientId;        // Ссылка на агрегат Client
    private final CarId carId;              // Ссылка на агрегат Car
    private RentalPeriod period;
    private Money totalCost;
    private RentalState state;
    private String comment;
    
    // Фабричный метод вместо конструктора
    public static Contract create(ClientId clientId, CarId carId, 
                                   RentalPeriod period, Money dailyRate) {
        validateCreation(clientId, carId, period);
        Money totalCost = calculateCost(period, dailyRate);
        return new Contract(null, clientId, carId, period, totalCost, 
                           RentalState.PENDING, null);
    }
    
    // Бизнес-методы
    public void confirm() {
        if (!state.canConfirm()) {
            throw new InvalidStateTransitionException("Cannot confirm");
        }
        this.state = RentalState.CONFIRMED;
    }
    
    public void cancel() {
        if (!state.canCancel()) {
            throw new InvalidStateTransitionException("Cannot cancel");
        }
        this.state = RentalState.CANCELLED;
    }
    
    public void updatePeriod(RentalPeriod newPeriod, Money dailyRate) {
        validatePeriodUpdate(newPeriod);
        this.period = newPeriod;
        this.totalCost = calculateCost(newPeriod, dailyRate);
    }
    
    // Инварианты
    private static void validateCreation(...) { /* ... */ }
    private static Money calculateCost(...) { /* ... */ }
}
```

**Пример: Car (Fleet Context)**
```java
// fleet/domain/model/Car.java
@Getter
public class Car {
    private final CarId id;
    private final VIN vin;
    private final GosNumber gosNumber;
    private CarModelId modelId;           // Ссылка на CarModel
    private Money dailyRate;
    private Integer yearOfIssue;
    private CarState state;
    
    public static Car create(VIN vin, GosNumber gosNumber, 
                            CarModelId modelId, Money dailyRate, 
                            Integer yearOfIssue) {
        validateCreation(vin, gosNumber, dailyRate, yearOfIssue);
        return new Car(null, vin, gosNumber, modelId, dailyRate, 
                      yearOfIssue, CarState.available());
    }
    
    public void markAsUnavailable(String reason) {
        this.state = CarState.unavailable(reason);
    }
    
    public void markAsAvailable() {
        this.state = CarState.available();
    }
    
    public void updateDailyRate(Money newRate) {
        if (newRate.isLessThanOrEqual(Money.zeroRubles())) {
            throw new InvalidDailyRateException("Rate must be positive");
        }
        this.dailyRate = newRate;
    }
    
    public boolean isAvailableForRental() {
        return state.isAvailable();
    }
}
```

- [ ] Создать Domain модели для всех агрегатов
- [ ] Добавить бизнес-методы вместо сеттеров
- [ ] Убрать JPA аннотации из Domain моделей

#### **Шаг 1.3.4: Domain Services (1 день)**
- [ ] Переместить `RentalDomainService` в `rental/domain/service/`
- [ ] Создать `CarAvailabilityService` (проверка доступности)
- [ ] Создать `DocumentVerificationService`
- [ ] **Правило**: Domain Service НЕ зависит от инфраструктуры

#### **Шаг 1.3.5: Repository Interfaces (1 день)**
- [ ] Переместить интерфейсы репозиториев в `domain/repository/`
- [ ] Изменить сигнатуры — работать с Domain моделями, а не JPA
- [ ] Убрать методы, возвращающие DTO

**Пример**:
```java
// rental/domain/repository/ContractRepository.java
public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(ContractId id);
    List<Contract> findByClientId(ClientId clientId);
    List<Contract> findOverlapping(CarId carId, RentalPeriod period);
    void delete(ContractId id);
}
```

#### **Шаг 1.3.6: Infrastructure — JPA Entities (1-2 дня)**
- [ ] Создать JPA-сущности в `infrastructure/persistence/entity/`
- [ ] Добавить JPA аннотации
- [ ] Создать мапперы Domain Model <-> JPA Entity

**Пример**:
```java
// rental/infrastructure/persistence/entity/ContractJpaEntity.java
@Entity
@Table(name = "contract", schema = "car_rental")
@Data
public class ContractJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id")
    private Long clientId;
    
    @Column(name = "car_id")
    private Long carId;
    
    @Column(name = "data_start")
    private LocalDateTime dataStart;
    
    @Column(name = "data_end")
    private LocalDateTime dataEnd;
    
    @Embedded
    private MoneyEmbeddable totalCost;
    
    // ... остальные поля
}
```

```java
// rental/infrastructure/mapper/ContractEntityMapper.java
@Mapper(componentModel = "spring")
public interface ContractEntityMapper {
    ContractJpaEntity toJpaEntity(Contract contract);
    Contract toDomain(ContractJpaEntity entity);
}
```

#### **Шаг 1.3.7: Infrastructure — Repository Implementations (1 день)**
- [ ] Создать Spring Data JPA репозитории
- [ ] Создать реализации Domain репозиториев
- [ ] Использовать мапперы для преобразования

**Пример**:
```java
// rental/infrastructure/persistence/ContractJpaRepository.java
public interface ContractJpaRepository extends JpaRepository<ContractJpaEntity, Long> {
    List<ContractJpaEntity> findByClientId(Long clientId);
    
    @Query("""
        SELECT c FROM ContractJpaEntity c
        WHERE c.carId = :carId
          AND c.dataStart <= :end
          AND c.dataEnd >= :start
    """)
    List<ContractJpaEntity> findOverlapping(
        @Param("carId") Long carId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

// rental/infrastructure/persistence/ContractRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {
    private final ContractJpaRepository jpaRepository;
    private final ContractEntityMapper mapper;
    
    @Override
    public Contract save(Contract contract) {
        ContractJpaEntity entity = mapper.toJpaEntity(contract);
        ContractJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public List<Contract> findOverlapping(CarId carId, RentalPeriod period) {
        List<ContractJpaEntity> entities = jpaRepository.findOverlapping(
            carId.value(), period.getStart(), period.getEnd()
        );
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    // ...
}
```

#### **Шаг 1.3.8: Application Services (1-2 дня)**
- [ ] Создать Application Services в `application/service/`
- [ ] **Правило**: Application Service — оркестратор, координирует агрегаты
- [ ] Используют Domain Services и Repositories
- [ ] Управляют транзакциями

**Пример**:
```java
// rental/application/service/ContractApplicationService.java
@Service
@Transactional
@RequiredArgsConstructor
public class ContractApplicationService {
    private final ContractRepository contractRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final DocumentVerificationService documentVerificationService;
    private final RentalDomainService rentalDomainService;
    
    public ContractDto createContract(ClientId clientId, CreateContractCommand command) {
        // 1. Валидация клиента
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));
            
        if (!documentVerificationService.isVerified(clientId)) {
            throw new UnverifiedClientException(clientId);
        }
        
        // 2. Проверка доступности машины
        Car car = carRepository.findById(command.carId())
            .orElseThrow(() -> new CarNotFoundException(command.carId()));
            
        RentalPeriod period = RentalPeriod.of(command.startDate(), command.endDate());
        
        if (!rentalDomainService.isCarAvailable(car.getId(), period)) {
            throw new CarUnavailableException(car.getId(), period);
        }
        
        // 3. Создание контракта (бизнес-логика в агрегате)
        Contract contract = Contract.create(
            clientId, 
            car.getId(), 
            period, 
            car.getDailyRate()
        );
        
        // 4. Сохранение
        Contract saved = contractRepository.save(contract);
        
        return ContractDtoMapper.toDto(saved);
    }
}
```

#### **Шаг 1.3.9: API Layer (1 день)**
- [ ] Переместить контроллеры в `api/rest/`
- [ ] Создать API DTO в `api/dto/`
- [ ] Создать мапперы API DTO <-> Application DTO
- [ ] Контроллеры вызывают Application Services

**Пример**:
```java
// rental/api/rest/ContractController.java
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractApplicationService contractService;
    private final ContractApiMapper apiMapper;
    
    @PostMapping
    public ResponseEntity<ContractResponse> createContract(
            @AuthenticationPrincipal ClientDetails clientDetails,
            @RequestBody @Valid CreateContractRequest request) {
        
        CreateContractCommand command = apiMapper.toCommand(request);
        ContractDto contract = contractService.createContract(
            ClientId.of(clientDetails.getId()),
            command
        );
        
        ContractResponse response = apiMapper.toResponse(contract);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## 📋 ЭТАП 2: Устранение нарушений (2-3 дня)

### 2.1. Проблема: DTO в репозиториях
**Текущее состояние**:
```java
// CarRepository.java
MinMaxCellForFilters findMinMaxPriceByFilter(...);
```

**Решение**:
```java
// fleet/domain/repository/CarRepository.java
PriceRange findPriceRange(CarSearchCriteria criteria);

// fleet/domain/model/PriceRange.java (Value Object)
public record PriceRange(Money min, Money max) {
    public static PriceRange of(BigDecimal min, BigDecimal max) {
        return new PriceRange(Money.rubles(min), Money.rubles(max));
    }
}
```

- [ ] Заменить DTO на Value Objects в репозиториях
- [ ] Переместить преобразование в Infrastructure слой

### 2.2. Проблема: Зависимости в мапперах
**Текущее состояние**:
```java
@Mapper
public abstract class CarMapper {
    @Autowired
    protected CarModelRepository carModelRepository;
}
```

**Решение**: Убрать репозитории из мапперов, использовать Service
```java
// fleet/application/mapper/CarDtoMapper.java
@Mapper(componentModel = "spring")
public interface CarDtoMapper {
    // Простой маппинг без логики
    CarDto toDto(Car car, CarModel model);
}

// fleet/application/service/CarApplicationService.java
public CarDto getCarDetails(CarId carId) {
    Car car = carRepository.findById(carId);
    CarModel model = carModelRepository.findById(car.getModelId());
    return carDtoMapper.toDto(car, model);
}
```

- [ ] Убрать `@Autowired` из мапперов
- [ ] Переместить логику в Application Services

### 2.3. Проблема: Helper Services
**Текущее состояние**:
```java
CarServiceHelperService
ClientServiceHelper
ContractServiceHelper
```

**Решение**: Удалить Helper Services, использовать прямые вызовы Repository через Application Services

- [ ] Переместить логику из Helper Services в Application Services
- [ ] Удалить Helper Services

### 2.4. Проблема: Связи между агрегатами
**Текущее состояние**:
```java
@ManyToOne
@JoinColumn(name = "client_id")
private Client client;  // Прямая JPA-связь
```

**Решение**: Использовать ID для связи
```java
// rental/domain/model/Contract.java
private ClientId clientId;  // Только ID
private CarId carId;        // Только ID

// При необходимости загружать через Repository:
Client client = clientRepository.findById(contract.getClientId());
```

- [ ] Заменить `@ManyToOne` на ID в Domain моделях
- [ ] Сохранить JPA-связи в JPA entities (Infrastructure)

---

## 📋 ЭТАП 3: Тестирование (2-3 дня)

### 3.1. Unit тесты Domain Layer
- [ ] Тесты для Value Objects (валидация)
- [ ] Тесты для бизнес-методов агрегатов
- [ ] Тесты для Domain Services

**Пример**:
```java
class ContractTest {
    @Test
    void shouldCreateContractWithValidData() {
        RentalPeriod period = RentalPeriod.of(start, end);
        Contract contract = Contract.create(clientId, carId, period, rate);
        
        assertThat(contract.getState()).isEqualTo(RentalState.PENDING);
        assertThat(contract.getTotalCost()).isGreaterThan(Money.zeroRubles());
    }
    
    @Test
    void shouldThrowExceptionWhenConfirmingCancelledContract() {
        Contract contract = createCancelledContract();
        
        assertThatThrownBy(contract::confirm)
            .isInstanceOf(InvalidStateTransitionException.class);
    }
}
```

### 3.2. Integration тесты Infrastructure Layer
- [ ] Тесты для Repository implementations
- [ ] Тесты для мапперов Domain <-> JPA
- [ ] Использовать Testcontainers

### 3.3. Integration тесты API Layer
- [ ] Обновить существующие интеграционные тесты
- [ ] Убедиться, что все 1232 теста проходят
- [ ] Цель: покрытие > 80%

---

## 📋 ЭТАП 4: Модуляризация (2-3 дня)

### 4.1. Создание Maven модулей
Подготовка к будущему разделению на микросервисы

**Целевая структура**:
```
backend/
├── pom.xml (parent)
├── common-module/
│   └── pom.xml
├── rental-module/
│   └── pom.xml
├── fleet-module/
│   └── pom.xml
├── client-module/
│   └── pom.xml
├── identity-module/
│   └── pom.xml
└── application/         # Точка входа (монолит)
    └── pom.xml
```

- [ ] Создать parent POM
- [ ] Создать модули для каждого Bounded Context
- [ ] Настроить зависимости между модулями
- [ ] **Правило**: Модули общаются только через API (interfaces)

### 4.2. Разделение зависимостей
- [ ] `common-module` — общие библиотеки (Money, Exceptions)
- [ ] Каждый модуль имеет свои зависимости
- [ ] Использовать Maven Enforcer Plugin для контроля зависимостей

### 4.3. Shared Kernel
- [ ] Создать пакет `common/domain/shared` для общих концептов
- [ ] Переместить `Money`, базовые исключения
- [ ] Документировать, что является Shared Kernel

---

## 📋 ЭТАП 5: Документация и стандарты (1-2 дня)

### 5.1. Архитектурная документация
- [ ] Создать `ARCHITECTURE.md` с описанием слоёв и Bounded Contexts
- [ ] Добавить диаграммы (PlantUML):
  - Context Map (связи между BC)
  - Агрегаты и их связи
  - Слои архитектуры
- [ ] Документировать правила (например, "Агрегаты связываются через ID")

### 5.2. ADR (Architecture Decision Records)
Создать папку `docs/adr/` и документировать ключевые решения:
- [ ] `001-ddd-bounded-contexts.md` — выбор BC
- [ ] `002-hexagonal-architecture.md` — выбор луковичной архитектуры
- [ ] `003-aggregate-references.md` — связи через ID
- [ ] `004-modularization.md` — подход к модуляризации

### 5.3. Coding Standards
- [ ] Создать `CODING_STANDARDS.md`:
  - Правила именования
  - Структура пакетов
  - Использование Value Objects
  - Правила тестирования
- [ ] Настроить Checkstyle/SpotBugs

---

## 🎯 Критерии успеха

### Этап 0-1
- ✅ Нет мёртвого кода
- ✅ Все тесты проходят (1232/1232)
- ✅ Покрытие тестами > 80%
- ✅ Чёткое разделение на 4 Bounded Contexts
- ✅ Реализована луковичная архитектура
- ✅ Нет DTO в репозиториях
- ✅ Мапперы не содержат бизнес-логику
- ✅ Агрегаты имеют бизнес-методы
- ✅ Связи между агрегатами через ID

### Технические метрики
- **Coupling**: Низкая связанность между модулями
- **Cohesion**: Высокая связность внутри модулей
- **Cyclomatic Complexity**: < 10 для методов
- **Test Coverage**: > 80%

---

## 🚀 План действий по дням (14 дней)

### Неделя 1: Подготовка и Domain Layer
- **День 1-2**: Этап 0 (анализ, очистка, документация)
- **День 3**: Создание структуры пакетов + Value Objects
- **День 4-5**: Domain Models (агрегаты)
- **День 6**: Domain Services + Repository Interfaces
- **День 7**: Ревью, корректировки

### Неделя 2: Infrastructure, Application, API
- **День 8-9**: Infrastructure Layer (JPA entities, Repository impl)
- **День 10-11**: Application Services + API Layer
- **День 12-13**: Этап 2 (устранение нарушений) + тестирование
- **День 14**: Этап 4-5 (модуляризация, документация)

---

## 📚 Дополнительные рекомендации

### Инструменты для анализа
- **ArchUnit** — тестирование архитектурных правил
- **JDepend** — анализ зависимостей
- **SonarQube** — качество кода
- **JaCoCo** — покрытие тестами (уже используется)

### Паттерны для использования
- **Specification Pattern** — для сложных фильтров (CarSearchCriteria)
- **Factory Pattern** — для создания агрегатов
- **Repository Pattern** — уже используется
- **Domain Events** — для коммуникации между BC (в будущем)

### Подготовка к микросервисам
После завершения этапов 0-1:
1. Каждый модуль может стать отдельным сервисом
2. Добавить REST API между модулями
3. Добавить Message Broker (RabbitMQ/Kafka) для событий
4. Вынести Identity Context в отдельный Authentication Service
5. Добавить API Gateway

---

## 🔄 Миграционная стратегия

### Параллельная работа (Strangler Fig Pattern)
1. Создавать новый код рядом со старым
2. Постепенно переключать контроллеры на новые сервисы
3. Удалять старый код только после полного переноса
4. Держать тесты зелёными на каждом шаге

---

## 📋 GitHub Issues для Kanban-доски

### Рекомендуемые Labels

```
🏷️ Labels:
- stage-0-preparation (Этап 0: Подготовка)
- stage-1-ddd-structure (Этап 1: Структура DDD)
- stage-2-violations (Этап 2: Устранение нарушений)
- stage-3-testing (Этап 3: Тестирование)
- stage-4-modularization (Этап 4: Модуляризация)

- context-rental (Rental Context)
- context-fleet (Fleet Context)
- context-client (Client Context)
- context-identity (Identity Context)
- context-common (Общий код)

- layer-domain (Domain Layer)
- layer-application (Application Layer)
- layer-infrastructure (Infrastructure Layer)
- layer-api (API Layer)

- priority-critical (Критично)
- priority-high (Высокий приоритет)
- priority-medium (Средний приоритет)
- priority-low (Низкий приоритет)

- type-refactoring (Рефакторинг)
- type-documentation (Документация)
- type-testing (Тестирование)
- type-bugfix (Исправление ошибок)

- blocked (Заблокировано)
- in-progress (В процессе)
- ready-for-review (Готово к ревью)
```

---

### STAGE 0: Preparation & Analysis (Этап 0)

#### Issue #1: 📊 Анализ текущей архитектуры и создание документации

**Labels**: `stage-0-preparation`, `type-documentation`, `priority-critical`

**Описание**:
Провести полный анализ текущей архитектуры, задокументировать текущее состояние и выявить все проблемы.

**Checklist**:
- [ ] Создать файл `ARCHITECTURE.md` с описанием текущей архитектуры
- [ ] Задокументировать все существующие слои (domain/entity, repository, service, dto, rest)
- [ ] Построить граф зависимостей между сущностями (можно использовать PlantUML)
- [ ] Выявить и задокументировать все архитектурные проблемы
- [ ] Создать список всех Entity, DTO, Services, Controllers
- [ ] Определить границы будущих Bounded Contexts
- [ ] Задокументировать текущие связи между сущностями (JPA @ManyToOne, @OneToMany)
- [ ] Создать файл `docs/context_map.puml` для визуализации контекстов

**Результат**: Полная документация текущего состояния

**Зависимости**: Нет

---

#### Issue #2: 🧹 Инвентаризация и удаление мёртвого кода

**Labels**: `stage-0-preparation`, `type-refactoring`, `priority-high`

**Описание**:
Найти и удалить весь неиспользуемый код, закомментированные участки, пустые файлы.

**Checklist**:
- [ ] Проверить использование `main.java` в корне пакета
- [ ] Проверить использование `rest/home.java`
- [ ] Найти все TODO-комментарии и оценить их актуальность
- [ ] Найти закомментированный код (например, в `Contract.java`)
- [ ] Удалить неиспользуемые методы в репозиториях
- [ ] Проверить неиспользуемые DTO классы
- [ ] Удалить неиспользуемые мапперы
- [ ] Запустить статический анализ (IntelliJ IDEA: Analyze > Inspect Code)
- [ ] Убедиться, что все тесты проходят после удаления

**Результат**: Чистая кодовая база без мёртвого кода

**Зависимости**: Issue #1

---

#### Issue #3: 📝 Определение Bounded Contexts

**Labels**: `stage-0-preparation`, `type-documentation`, `priority-critical`

**Описание**:
Определить и задокументировать 4 основных Bounded Context для системы Car Sharing.

**Checklist**:
- [ ] **Rental Context**: Определить границы (Contract, RentalState, RentalPeriod)
- [ ] **Fleet Context**: Определить границы (Car, CarModel, CarState)
- [ ] **Client Context**: Определить границы (Client, Document, Favorite)
- [ ] **Identity Context**: Определить границы (Auth, JWT, Roles)
- [ ] Определить Aggregate Root для каждого контекста
- [ ] Определить связи между контекстами (Customer-Supplier, Shared Kernel, etc.)
- [ ] Создать Context Map диаграмму
- [ ] Задокументировать в `BOUNDED_CONTEXTS.md`
- [ ] Определить Shared Kernel (Money, общие исключения)

**Результат**: Документация с четкими границами контекстов

**Зависимости**: Issue #1

---

### STAGE 1: DDD Structure (Этап 1)

#### Issue #4: 🏗️ Создание новой структуры пакетов

**Labels**: `stage-1-ddd-structure`, `context-common`, `priority-critical`

**Описание**:
Создать новую пакетную структуру для всех Bounded Contexts в соответствии с луковичной архитектурой.

**Checklist**:
- [ ] Создать пакет `common/domain/valueobject/`
- [ ] Создать пакет `common/exceptions/`
- [ ] Создать пакет `common/infrastructure/`
- [ ] Создать структуру для `rental/` (domain, application, infrastructure, api)
- [ ] Создать структуру для `fleet/` (domain, application, infrastructure, api)
- [ ] Создать структуру для `client/` (domain, application, infrastructure, api)
- [ ] Создать структуру для `identity/` (domain, application, infrastructure, api)
- [ ] Создать файл `STRUCTURE.md` с описанием новой структуры
- [ ] НЕ УДАЛЯТЬ старый код (параллельная работа)

**Результат**: Пустая структура пакетов готова для миграции

**Зависимости**: Issue #3

---

#### Issue #5: 💎 Создание Value Objects для Fleet Context

**Labels**: `stage-1-ddd-structure`, `context-fleet`, `layer-domain`, `priority-high`

**Описание**:
Создать Value Objects для Fleet Context: VIN, GosNumber.

**Checklist**:
- [ ] Создать `fleet/domain/model/valueobject/VIN.java`
  - [ ] Добавить валидацию формата VIN (17 символов)
  - [ ] Сделать immutable (final поля)
  - [ ] Реализовать equals/hashCode
  - [ ] Добавить метод `of(String value)`
- [ ] Создать `fleet/domain/model/valueobject/GosNumber.java`
  - [ ] Добавить валидацию российского гос номера
  - [ ] Сделать immutable
  - [ ] Реализовать equals/hashCode
  - [ ] Добавить метод `of(String value)`
- [ ] Написать unit-тесты для VIN (позитивные и негативные сценарии)
- [ ] Написать unit-тесты для GosNumber
- [ ] Добавить документацию в JavaDoc

**Результат**: Готовые Value Objects для Fleet Context

**Зависимости**: Issue #4

---

#### Issue #6: 💎 Создание Value Objects для Client Context

**Labels**: `stage-1-ddd-structure`, `context-client`, `layer-domain`, `priority-high`

**Описание**:
Создать Value Objects для Client Context: Email, Phone, ClientStatus.

**Checklist**:
- [ ] Создать `client/domain/model/valueobject/Email.java`
  - [ ] Добавить валидацию email (RFC 5321)
  - [ ] Проверка длины, наличие @, валидность домена
  - [ ] Сделать immutable
  - [ ] Добавить метод `of(String value)`
- [ ] Создать `client/domain/model/valueobject/Phone.java`
  - [ ] Добавить валидацию телефона (формат +7 или 8)
  - [ ] Нормализация формата
  - [ ] Сделать immutable
- [ ] Создать `client/domain/model/valueobject/ClientStatus.java`
  - [ ] Enum wrapper (ACTIVE, BLOCKED, PENDING_VERIFICATION)
  - [ ] Бизнес-методы (canRent(), isBlocked())
- [ ] Написать unit-тесты для Email
- [ ] Написать unit-тесты для Phone
- [ ] Написать unit-тесты для ClientStatus

**Результат**: Готовые Value Objects для Client Context

**Зависимости**: Issue #4

---

#### Issue #7: 💎 Создание Value Objects для Rental Context

**Labels**: `stage-1-ddd-structure`, `context-rental`, `layer-domain`, `priority-high`

**Описание**:
Создать Value Objects для Rental Context: RentalPeriod, RentalCost.

**Checklist**:
- [ ] Создать `rental/domain/model/valueobject/RentalPeriod.java`
  - [ ] Инкапсулировать startDate и endDate
  - [ ] Добавить валидацию (start < end, не в прошлом)
  - [ ] Метод `getDurationInDays()`
  - [ ] Метод `overlaps(RentalPeriod other)`
  - [ ] Сделать immutable
- [ ] Создать `rental/domain/model/valueobject/RentalCost.java`
  - [ ] Обёртка над Money
  - [ ] Методы для расчёта (базовая стоимость, скидки, доп. услуги)
  - [ ] Метод `calculateTotal()`
- [ ] Создать `rental/domain/model/valueobject/RentalState.java`
  - [ ] Enum (PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED)
  - [ ] Методы `canConfirm()`, `canCancel()`, `canComplete()`
- [ ] Написать unit-тесты для RentalPeriod (включая overlaps)
- [ ] Написать unit-тесты для RentalCost
- [ ] Написать unit-тесты для RentalState

**Результат**: Готовые Value Objects для Rental Context

**Зависимости**: Issue #4

---

#### Issue #8: 🎯 Создание Domain Model: Car (Aggregate Root)

**Labels**: `stage-1-ddd-structure`, `context-fleet`, `layer-domain`, `priority-critical`

**Описание**:
Создать Domain модель Car как Aggregate Root для Fleet Context с бизнес-логикой.

**Checklist**:
- [ ] Создать `fleet/domain/model/Car.java`
  - [ ] Поля: CarId, VIN, GosNumber, CarModelId, Money dailyRate, Integer yearOfIssue, CarState
  - [ ] Использовать Value Objects (VIN, GosNumber)
  - [ ] НЕ использовать JPA аннотации (это Domain модель)
  - [ ] Убрать сеттеры, сделать поля final где возможно
- [ ] Добавить фабричный метод `Car.create(...)`
  - [ ] Валидация всех параметров
  - [ ] Проверка year of issue (не в будущем, не слишком старый)
  - [ ] Установка начального состояния CarState.available()
- [ ] Добавить бизнес-методы:
  - [ ] `markAsUnavailable(String reason)`
  - [ ] `markAsAvailable()`
  - [ ] `updateDailyRate(Money newRate)` с валидацией
  - [ ] `isAvailableForRental()` - проверка состояния
  - [ ] `canBeRented()` - комплексная проверка
- [ ] Добавить инварианты (частные методы валидации)
- [ ] Создать `fleet/domain/model/CarId.java` (типизированный ID)
- [ ] Написать unit-тесты для всех бизнес-методов
- [ ] Написать тесты для валидации инвариантов

**Результат**: Domain модель Car с богатой бизнес-логикой

**Зависимости**: Issue #5

---

#### Issue #9: 🎯 Создание Domain Model: Contract (Aggregate Root)

**Labels**: `stage-1-ddd-structure`, `context-rental`, `layer-domain`, `priority-critical`

**Описание**:
Создать Domain модель Contract как Aggregate Root для Rental Context.

**Checklist**:
- [ ] Создать `rental/domain/model/Contract.java`
  - [ ] Поля: ContractId, ClientId, CarId, RentalPeriod, Money totalCost, RentalState, comment
  - [ ] Использовать Value Objects (RentalPeriod, RentalState)
  - [ ] Связь через ID (ClientId, CarId), НЕ через объекты
  - [ ] НЕ использовать JPA аннотации
- [ ] Добавить фабричный метод `Contract.create(...)`
  - [ ] Валидация clientId, carId, period
  - [ ] Расчёт totalCost = period.getDurationInDays() * dailyRate
  - [ ] Установка начального состояния PENDING
- [ ] Добавить бизнес-методы:
  - [ ] `confirm()` - подтверждение контракта
  - [ ] `cancel()` - отмена контракта
  - [ ] `complete()` - завершение аренды
  - [ ] `updatePeriod(RentalPeriod newPeriod, Money dailyRate)` - изменение периода
  - [ ] `addComment(String comment)`
- [ ] Добавить проверки переходов состояний (State Machine)
- [ ] Создать `rental/domain/model/ContractId.java`
- [ ] Создать `client/domain/model/ClientId.java`
- [ ] Написать unit-тесты для создания контракта
- [ ] Написать тесты для переходов состояний
- [ ] Написать тесты для расчёта стоимости

**Результат**: Domain модель Contract с полной бизнес-логикой

**Зависимости**: Issue #7

---

#### Issue #10: 🎯 Создание Domain Model: Client (Aggregate Root)

**Labels**: `stage-1-ddd-structure`, `context-client`, `layer-domain`, `priority-high`

**Описание**:
Создать Domain модель Client как Aggregate Root для Client Context.

**Checklist**:
- [ ] Создать `client/domain/model/Client.java`
  - [ ] Поля: ClientId, Email, Phone, firstName, lastName, ClientStatus, registrationDate
  - [ ] Использовать Value Objects (Email, Phone, ClientStatus)
  - [ ] НЕ использовать JPA аннотации
- [ ] Добавить фабричный метод `Client.register(...)`
  - [ ] Валидация имени (не пустое, длина)
  - [ ] Установка начального статуса PENDING_VERIFICATION
  - [ ] Установка даты регистрации
- [ ] Добавить бизнес-методы:
  - [ ] `verify()` - верификация клиента (PENDING -> ACTIVE)
  - [ ] `block(String reason)` - блокировка клиента
  - [ ] `unblock()` - разблокировка
  - [ ] `updateContactInfo(Email email, Phone phone)`
  - [ ] `canRentCar()` - проверка возможности аренды
- [ ] Добавить проверку переходов статусов
- [ ] Написать unit-тесты для регистрации
- [ ] Написать тесты для переходов статусов
- [ ] Написать тесты для бизнес-правил

**Результат**: Domain модель Client с бизнес-логикой

**Зависимости**: Issue #6

---

#### Issue #11: 🔧 Создание Domain Services

**Labels**: `stage-1-ddd-structure`, `layer-domain`, `priority-high`

**Описание**:
Создать Domain Services для бизнес-логики, которая не принадлежит одному агрегату.

**Checklist**:
- [ ] Переместить и рефакторить `rental/domain/service/RentalDomainService.java`
  - [ ] Метод `calculateRentalCost(RentalPeriod, Money dailyRate)`
  - [ ] Метод `isCarAvailable(CarId, RentalPeriod)` (проверка через Repository)
  - [ ] Убрать зависимости от Infrastructure
- [ ] Создать `fleet/domain/service/CarAvailabilityService.java`
  - [ ] Метод `checkAvailability(CarId, RentalPeriod)`
  - [ ] Логика проверки пересечений аренд
- [ ] Создать `client/domain/service/DocumentVerificationService.java`
  - [ ] Метод `isDocumentValid(Document)`
  - [ ] Метод `canClientRent(ClientId)`
- [ ] Убедиться, что Domain Services НЕ зависят от Infrastructure
- [ ] Написать unit-тесты для каждого Domain Service

**Результат**: Domain Services с чистой бизнес-логикой

**Зависимости**: Issue #8, Issue #9, Issue #10

---

#### Issue #12: 📦 Создание Repository Interfaces в Domain Layer

**Labels**: `stage-1-ddd-structure`, `layer-domain`, `priority-high`

**Описание**:
Создать интерфейсы Repository в Domain Layer, работающие с Domain моделями.

**Checklist**:
- [ ] Создать `rental/domain/repository/ContractRepository.java`
  - [ ] `Contract save(Contract contract)`
  - [ ] `Optional<Contract> findById(ContractId id)`
  - [ ] `List<Contract> findByClientId(ClientId clientId)`
  - [ ] `List<Contract> findOverlapping(CarId carId, RentalPeriod period)`
  - [ ] `void delete(ContractId id)`
- [ ] Создать `fleet/domain/repository/CarRepository.java`
  - [ ] `Car save(Car car)`
  - [ ] `Optional<Car> findById(CarId id)`
  - [ ] `List<Car> findByFilter(CarSearchCriteria criteria)`
  - [ ] `PriceRange findPriceRange(CarSearchCriteria criteria)` (НЕ DTO!)
- [ ] Создать `client/domain/repository/ClientRepository.java`
  - [ ] `Client save(Client client)`
  - [ ] `Optional<Client> findById(ClientId id)`
  - [ ] `Optional<Client> findByEmail(Email email)`
- [ ] Убедиться, что методы работают с Domain типами (не JPA)
- [ ] Убедиться, что методы НЕ возвращают DTO

**Результат**: Repository интерфейсы в Domain Layer

**Зависимости**: Issue #8, Issue #9, Issue #10

---

#### Issue #13: 🗄️ Создание JPA Entities в Infrastructure Layer

**Labels**: `stage-1-ddd-structure`, `layer-infrastructure`, `priority-high`

**Описание**:
Создать JPA сущности в Infrastructure Layer для persistence.

**Checklist**:
- [ ] Создать `rental/infrastructure/persistence/entity/ContractJpaEntity.java`
  - [ ] Скопировать поля из старой `Contract` entity
  - [ ] Добавить все JPA аннотации (@Entity, @Table, @Id, etc.)
  - [ ] Сохранить JPA связи (@ManyToOne для client, car)
  - [ ] Использовать @Embeddable для Money
- [ ] Создать `fleet/infrastructure/persistence/entity/CarJpaEntity.java`
  - [ ] Скопировать поля из старой `Car` entity
  - [ ] Добавить JPA аннотации
  - [ ] Сохранить связи с CarModel
- [ ] Создать `client/infrastructure/persistence/entity/ClientJpaEntity.java`
  - [ ] Скопировать поля из старой `Client` entity
  - [ ] Добавить JPA аннотации
- [ ] НЕ УДАЛЯТЬ старые entity (параллельная работа)
- [ ] Использовать новые имена таблиц или схему для избежания конфликтов (временно)

**Результат**: JPA entities готовы для persistence

**Зависимости**: Issue #4

---

#### Issue #14: 🔄 Создание Entity Mappers (Domain ↔️ JPA)

**Labels**: `stage-1-ddd-structure`, `layer-infrastructure`, `priority-high`

**Описание**:
Создать MapStruct мапперы для преобразования Domain моделей в JPA entities и обратно.

**Checklist**:
- [ ] Создать `rental/infrastructure/mapper/ContractEntityMapper.java`
  - [ ] Метод `ContractJpaEntity toJpaEntity(Contract domain)`
  - [ ] Метод `Contract toDomain(ContractJpaEntity entity)`
  - [ ] Маппинг Value Objects (RentalPeriod, Money, RentalState)
  - [ ] Маппинг ID (ContractId ↔️ Long)
- [ ] Создать `fleet/infrastructure/mapper/CarEntityMapper.java`
  - [ ] Маппинг VIN, GosNumber (Value Objects ↔️ String)
  - [ ] Маппинг CarId
- [ ] Создать `client/infrastructure/mapper/ClientEntityMapper.java`
  - [ ] Маппинг Email, Phone (Value Objects ↔️ String)
  - [ ] Маппинг ClientStatus
- [ ] Написать интеграционные тесты для мапперов
- [ ] Проверить корректность маппинга в обе стороны

**Результат**: Мапперы для преобразования Domain ↔️ JPA

**Зависимости**: Issue #8, Issue #9, Issue #10, Issue #13

---

#### Issue #15: 🗄️ Создание Repository Implementations в Infrastructure

**Labels**: `stage-1-ddd-structure`, `layer-infrastructure`, `priority-critical`

**Описание**:
Создать реализации Domain репозиториев в Infrastructure Layer.

**Checklist**:
- [ ] Создать Spring Data JPA репозитории:
  - [ ] `rental/infrastructure/persistence/ContractJpaRepository.java`
    - [ ] `extends JpaRepository<ContractJpaEntity, Long>`
    - [ ] Custom query для `findOverlapping`
  - [ ] `fleet/infrastructure/persistence/CarJpaRepository.java`
  - [ ] `client/infrastructure/persistence/ClientJpaRepository.java`
- [ ] Создать реализации Domain репозиториев:
  - [ ] `rental/infrastructure/persistence/ContractRepositoryImpl.java`
    - [ ] `implements ContractRepository`
    - [ ] Использовать `ContractJpaRepository` и `ContractEntityMapper`
    - [ ] Преобразовать Domain → JPA при save
    - [ ] Преобразовать JPA → Domain при load
  - [ ] `fleet/infrastructure/persistence/CarRepositoryImpl.java`
  - [ ] `client/infrastructure/persistence/ClientRepositoryImpl.java`
- [ ] Написать интеграционные тесты с Testcontainers
- [ ] Убедиться, что методы работают корректно

**Результат**: Полностью работающие Repository реализации

**Зависимости**: Issue #12, Issue #13, Issue #14

---

#### Issue #16: 🎬 Создание Application Services

**Labels**: `stage-1-ddd-structure`, `layer-application`, `priority-critical`

**Описание**:
Создать Application Services как оркестраторов бизнес-логики.

**Checklist**:
- [ ] Создать `rental/application/service/ContractApplicationService.java`
  - [ ] Метод `createContract(ClientId, CreateContractCommand)`
  - [ ] Метод `confirmContract(ContractId)`
  - [ ] Метод `cancelContract(ContractId)`
  - [ ] Метод `getContractDetails(ContractId)`
  - [ ] Использовать Domain Services, Repositories
  - [ ] Добавить @Transactional
- [ ] Создать `fleet/application/service/CarApplicationService.java`
  - [ ] Метод `getAvailableCars(CarSearchCriteria)`
  - [ ] Метод `getCarDetails(CarId)`
  - [ ] Метод `updateCarStatus(CarId, CarState)`
- [ ] Создать `client/application/service/ClientApplicationService.java`
  - [ ] Метод `registerClient(RegisterClientCommand)`
  - [ ] Метод `verifyClient(ClientId)`
  - [ ] Метод `getClientProfile(ClientId)`
- [ ] Создать Command объекты (CreateContractCommand, etc.)
- [ ] Написать unit-тесты (мокировать репозитории и domain services)
- [ ] Написать интеграционные тесты

**Результат**: Application Services готовы для использования

**Зависимости**: Issue #11, Issue #15

---

#### Issue #17: 🌐 Миграция API Layer (Controllers)

**Labels**: `stage-1-ddd-structure`, `layer-api`, `priority-high`

**Описание**:
Переместить и рефакторить REST контроллеры для использования новых Application Services.

**Checklist**:
- [ ] Создать `rental/api/rest/ContractController.java`
  - [ ] Переместить эндпоинты из старого контроллера
  - [ ] Использовать `ContractApplicationService`
  - [ ] Создать API DTO (CreateContractRequest, ContractResponse)
- [ ] Создать `fleet/api/rest/CarController.java`
  - [ ] Использовать `CarApplicationService`
  - [ ] Создать API DTO (CarResponse, CarFilterRequest)
- [ ] Создать `client/api/rest/ProfileController.java`
  - [ ] Использовать `ClientApplicationService`
- [ ] Создать API мапперы (Request/Response ↔️ Command/DTO)
- [ ] Добавить валидацию (@Valid, javax.validation)
- [ ] Постепенно переключать эндпоинты на новые сервисы
- [ ] Оставить старые контроллеры до полной миграции
- [ ] Обновить интеграционные тесты API

**Результат**: API Layer использует новую архитектуру

**Зависимости**: Issue #16

---

### STAGE 2: Fix Violations (Этап 2)

#### Issue #18: 🔧 Устранение DTO в Repository методах

**Labels**: `stage-2-violations`, `type-refactoring`, `priority-critical`

**Описание**:
Заменить все DTO в методах Repository на Value Objects или Domain модели.

**Checklist**:
- [ ] Найти все методы Repository, возвращающие DTO
  - [ ] `CarRepository.findMinMaxPriceByFilter()` → вернуть `PriceRange`
  - [ ] Другие методы с DTO
- [ ] Создать Value Objects вместо DTO:
  - [ ] `fleet/domain/model/valueobject/PriceRange.java`
  - [ ] Другие необходимые Value Objects
- [ ] Обновить сигнатуры методов в Repository интерфейсах
- [ ] Обновить реализации в Infrastructure
- [ ] Обновить запросы (JPQL/Native) для возврата правильных типов
- [ ] Обновить вызовы в Application Services
- [ ] Написать тесты для новых методов
- [ ] Удалить старые DTO из пакета repository

**Результат**: Repository не содержат DTO, только Domain типы

**Зависимости**: Issue #15

---

#### Issue #19: 🧹 Удаление зависимостей из MapStruct мапперов

**Labels**: `stage-2-violations`, `type-refactoring`, `priority-high`

**Описание**:
Убрать @Autowired репозитории и сервисы из MapStruct мапперов.

**Checklist**:
- [ ] Найти все мапперы с @Autowired полями
  - [ ] `CarMapper` с `CarModelRepository`
  - [ ] Другие мапперы с зависимостями
- [ ] Переместить логику из мапперов в Application Services
  - [ ] Загрузка связанных сущностей → делать в Service
  - [ ] Преобразование → оставить в Mapper
- [ ] Обновить сигнатуры мапперов (передавать данные параметрами)
  - [ ] `CarDto toDto(Car car, CarModel model)` вместо загрузки внутри
- [ ] Обновить Application Services для передачи всех данных
- [ ] Написать тесты для обновленных мапперов
- [ ] Убедиться, что мапперы стали stateless

**Результат**: Мапперы не имеют зависимостей, только чистый маппинг

**Зависимости**: Issue #16

---

#### Issue #20: 🗑️ Удаление Helper Services

**Labels**: `stage-2-violations`, `type-refactoring`, `priority-medium`

**Описание**:
Удалить все Helper сервисы, переместив их логику в Application Services.

**Checklist**:
- [ ] Инвентаризация Helper Services:
  - [ ] `CarServiceHelperService`
  - [ ] `ClientServiceHelper`
  - [ ] `ContractServiceHelper`
  - [ ] Другие Helper'ы
- [ ] Для каждого Helper Service:
  - [ ] Найти все места использования
  - [ ] Переместить методы в соответствующие Application Services
  - [ ] Обновить вызовы
  - [ ] Написать тесты для перемещенных методов
  - [ ] Удалить Helper Service
- [ ] Убедиться, что все тесты проходят
- [ ] Убедиться, что нет неиспользуемых импортов

**Результат**: Нет Helper Services, логика в Application Services

**Зависимости**: Issue #16

---

#### Issue #21: 🔗 Замена JPA связей на ID в Domain моделях

**Labels**: `stage-2-violations`, `layer-domain`, `priority-high`

**Описание**:
Заменить JPA связи (@ManyToOne, @OneToMany) на ID-based ссылки в Domain моделях.

**Checklist**:
- [ ] В `Contract` Domain модели:
  - [ ] Заменить `@ManyToOne Client client` на `ClientId clientId`
  - [ ] Заменить `@ManyToOne Car car` на `CarId carId`
- [ ] В `Car` Domain модели:
  - [ ] Заменить `@ManyToOne CarModel model` на `CarModelId modelId`
- [ ] В других Domain моделях:
  - [ ] Проверить все связи
  - [ ] Заменить на typed ID
- [ ] Сохранить JPA связи в JPA entities (Infrastructure Layer)
- [ ] Обновить мапперы для правильного преобразования
- [ ] Обновить Application Services для загрузки связанных объектов при необходимости
- [ ] Написать тесты

**Результат**: Domain модели связаны через ID, не через объекты

**Зависимости**: Issue #8, Issue #9, Issue #10

---

### STAGE 3: Testing (Этап 3)

#### Issue #22: ✅ Unit тесты для Value Objects

**Labels**: `stage-3-testing`, `type-testing`, `priority-high`

**Описание**:
Написать полные unit-тесты для всех Value Objects.

**Checklist**:
- [ ] Тесты для `VIN`:
  - [ ] Валидный VIN (17 символов)
  - [ ] Невалидный VIN (короткий, длинный, пустой)
  - [ ] equals/hashCode
- [ ] Тесты для `GosNumber`:
  - [ ] Валидные форматы (А123ВС77, etc.)
  - [ ] Невалидные форматы
- [ ] Тесты для `Email`:
  - [ ] Валидные email
  - [ ] Невалидные (без @, без домена, специальные символы)
  - [ ] Граничные случаи (длина)
- [ ] Тесты для `Phone`:
  - [ ] Валидные форматы (+7, 8)
  - [ ] Невалидные форматы
  - [ ] Нормализация
- [ ] Тесты для `RentalPeriod`:
  - [ ] Валидный период
  - [ ] start > end (должен падать)
  - [ ] Метод `overlaps()`
  - [ ] Метод `getDurationInDays()`
- [ ] Тесты для `Money` (если еще нет полного покрытия)
- [ ] Покрытие > 95% для всех Value Objects

**Результат**: Полное покрытие Value Objects тестами

**Зависимости**: Issue #5, Issue #6, Issue #7

---

#### Issue #23: ✅ Unit тесты для Domain Models (Aggregates)

**Labels**: `stage-3-testing`, `type-testing`, `priority-critical`

**Описание**:
Написать unit-тесты для всех Aggregate Root и их бизнес-методов.

**Checklist**:
- [ ] Тесты для `Contract`:
  - [ ] Создание контракта (`Contract.create()`)
  - [ ] Переходы состояний (confirm, cancel, complete)
  - [ ] Невалидные переходы (должны падать)
  - [ ] Обновление периода и пересчёт стоимости
  - [ ] Валидация инвариантов
- [ ] Тесты для `Car`:
  - [ ] Создание машины
  - [ ] Изменение статуса (available/unavailable)
  - [ ] Обновление тарифа
  - [ ] Проверка `isAvailableForRental()`
- [ ] Тесты для `Client`:
  - [ ] Регистрация клиента
  - [ ] Верификация
  - [ ] Блокировка/разблокировка
  - [ ] Проверка `canRentCar()`
- [ ] Использовать BDD-стиль (Given-When-Then)
- [ ] Покрытие > 90% для всех агрегатов

**Результат**: Полное покрытие Domain моделей тестами

**Зависимости**: Issue #8, Issue #9, Issue #10

---

#### Issue #24: ✅ Unit тесты для Domain Services

**Labels**: `stage-3-testing`, `type-testing`, `priority-high`

**Описание**:
Написать unit-тесты для всех Domain Services.

**Checklist**:
- [ ] Тесты для `RentalDomainService`:
  - [ ] Расчёт стоимости аренды
  - [ ] Проверка доступности машины
  - [ ] Мокировать Repository
- [ ] Тесты для `CarAvailabilityService`:
  - [ ] Проверка пересечений периодов
  - [ ] Различные сценарии (машина свободна, занята, частично)
- [ ] Тесты для `DocumentVerificationService`:
  - [ ] Валидация документа
  - [ ] Проверка возможности аренды
- [ ] Использовать Mockito для мокирования Repository
- [ ] Покрытие > 85%

**Результат**: Полное покрытие Domain Services тестами

**Зависимости**: Issue #11

---

#### Issue #25: ✅ Integration тесты для Repository Implementations

**Labels**: `stage-3-testing`, `type-testing`, `layer-infrastructure`, `priority-high`

**Описание**:
Написать интеграционные тесты для Repository реализаций с реальной БД.

**Checklist**:
- [ ] Настроить Testcontainers (PostgreSQL)
- [ ] Тесты для `ContractRepositoryImpl`:
  - [ ] save и findById
  - [ ] findByClientId
  - [ ] findOverlapping (различные сценарии пересечений)
  - [ ] delete
- [ ] Тесты для `CarRepositoryImpl`:
  - [ ] save и findById
  - [ ] findByFilter (различные критерии)
  - [ ] findPriceRange
- [ ] Тесты для `ClientRepositoryImpl`:
  - [ ] save и findById
  - [ ] findByEmail
- [ ] Проверить корректность маппинга Domain ↔️ JPA
- [ ] Проверить транзакционность
- [ ] Использовать @DataJpaTest или @SpringBootTest

**Результат**: Репозитории покрыты интеграционными тестами

**Зависимости**: Issue #15

---

#### Issue #26: ✅ Integration тесты для Application Services

**Labels**: `stage-3-testing`, `type-testing`, `layer-application`, `priority-high`

**Описание**:
Написать интеграционные тесты для Application Services.

**Checklist**:
- [ ] Тесты для `ContractApplicationService`:
  - [ ] Создание контракта (полный flow)
  - [ ] Проверка валидации клиента
  - [ ] Проверка доступности машины
  - [ ] Подтверждение контракта
  - [ ] Отмена контракта
  - [ ] Обработка ошибок (клиент не найден, машина занята)
- [ ] Тесты для `CarApplicationService`:
  - [ ] Получение списка машин
  - [ ] Фильтрация
  - [ ] Обновление статуса
- [ ] Тесты для `ClientApplicationService`:
  - [ ] Регистрация клиента
  - [ ] Верификация
- [ ] Использовать Testcontainers
- [ ] Проверить транзакционность
- [ ] Покрытие > 80%

**Результат**: Application Services покрыты интеграционными тестами

**Зависимости**: Issue #16

---

#### Issue #27: ✅ Integration тесты для API Layer

**Labels**: `stage-3-testing`, `type-testing`, `layer-api`, `priority-critical`

**Описание**:
Обновить и расширить интеграционные тесты для REST API.

**Checklist**:
- [ ] Обновить существующие тесты под новую структуру:
  - [ ] `AdminCarControllerIntegrationTests`
  - [ ] `AdminClientControllerIntegrationTests`
  - [ ] `AdminContractControllerIntegrationTests`
  - [ ] `CarControllerIntegrationTests`
  - [ ] `ProfileControllerIntegrationTests`
- [ ] Добавить тесты для новых эндпоинтов
- [ ] Проверить все HTTP методы (GET, POST, PUT, DELETE)
- [ ] Проверить валидацию входных данных
- [ ] Проверить обработку ошибок (404, 400, 500)
- [ ] Проверить JWT аутентификацию
- [ ] Использовать MockMvc или RestAssured
- [ ] Убедиться, что все 1232 теста проходят
- [ ] Цель: 0 падающих тестов

**Результат**: Все API тесты проходят, покрытие > 80%

**Зависимости**: Issue #17

---

#### Issue #28: 📊 Настройка JaCoCo и проверка покрытия

**Labels**: `stage-3-testing`, `type-testing`, `priority-medium`

**Описание**:
Настроить JaCoCo для отчётов по покрытию и достичь целевого покрытия.

**Checklist**:
- [ ] Проверить конфигурацию JaCoCo в pom.xml
- [ ] Настроить минимальные пороги покрытия (80% для line, 70% для branch)
- [ ] Запустить `mvn clean test` и проверить отчёт
- [ ] Найти участки с низким покрытием
- [ ] Добавить тесты для покрытия критичных участков
- [ ] Исключить из отчёта:
  - [ ] JPA entities (Infrastructure)
  - [ ] DTO классы
  - [ ] Конфигурационные классы
- [ ] Настроить CI для автоматической проверки покрытия
- [ ] Достичь покрытия > 80%

**Результат**: Покрытие тестами > 80%

**Зависимости**: Issue #22-27

---

### STAGE 4: Modularization (Этап 4)

#### Issue #29: 📦 Создание Maven Multi-Module структуры

**Labels**: `stage-4-modularization`, `priority-medium`

**Описание**:
Подготовить проект к разделению на микросервисы через Maven модули.

**Checklist**:
- [ ] Создать parent POM в корне проекта
  - [ ] Определить общие зависимости
  - [ ] Определить версии библиотек
  - [ ] Настроить Spring Boot parent
- [ ] Создать модуль `common-module`
  - [ ] Переместить общие Value Objects (Money)
  - [ ] Переместить общие исключения
  - [ ] Переместить общую инфраструктуру
  - [ ] Создать pom.xml
- [ ] Создать модуль `rental-module`
  - [ ] Переместить rental/* код
  - [ ] Настроить зависимости
  - [ ] Создать pom.xml
- [ ] Создать модуль `fleet-module`
  - [ ] Переместить fleet/* код
  - [ ] Создать pom.xml
- [ ] Создать модуль `client-module`
  - [ ] Переместить client/* код
  - [ ] Создать pom.xml
- [ ] Создать модуль `identity-module`
  - [ ] Переместить identity/* код
  - [ ] Создать pom.xml
- [ ] Создать модуль `application` (точка входа)
  - [ ] Main класс
  - [ ] Зависимости на все модули
  - [ ] application.yaml
- [ ] Настроить зависимости между модулями
- [ ] Убедиться, что проект собирается

**Результат**: Maven multi-module структура готова

**Зависимости**: Issue #17, Issue #21

---

#### Issue #30: 🔒 Настройка Maven Enforcer для контроля зависимостей

**Labels**: `stage-4-modularization`, `priority-low`

**Описание**:
Настроить Maven Enforcer Plugin для контроля зависимостей между модулями.

**Checklist**:
- [ ] Добавить Maven Enforcer Plugin в parent POM
- [ ] Настроить правила:
  - [ ] Domain модули НЕ зависят от Infrastructure
  - [ ] Application НЕ зависит напрямую от JPA entities
  - [ ] Запретить циклические зависимости
- [ ] Настроить Banned Dependencies (если нужно)
- [ ] Добавить проверку в CI pipeline
- [ ] Документировать правила в `MODULARIZATION.md`

**Результат**: Автоматический контроль зависимостей

**Зависимости**: Issue #29

---

#### Issue #31: 📝 Документация Shared Kernel

**Labels**: `stage-4-modularization`, `type-documentation`, `priority-low`

**Описание**:
Документировать концепцию Shared Kernel и что в него входит.

**Checklist**:
- [ ] Создать файл `docs/SHARED_KERNEL.md`
- [ ] Описать концепцию Shared Kernel в DDD
- [ ] Перечислить, что входит в Shared Kernel:
  - [ ] Money Value Object
  - [ ] Базовые исключения (DomainException, etc.)
  - [ ] Базовые интерфейсы (если есть)
- [ ] Описать правила изменения Shared Kernel
  - [ ] Требует согласования всех команд
  - [ ] Обратная совместимость обязательна
- [ ] Добавить диаграмму в PlantUML

**Результат**: Документация Shared Kernel

**Зависимости**: Issue #29

---

### DOCUMENTATION & FINALIZATION

#### Issue #32: 📖 Создание финальной архитектурной документации

**Labels**: `type-documentation`, `priority-high`

**Описание**:
Создать полную архитектурную документацию после завершения рефакторинга.

**Checklist**:
- [ ] Обновить `ARCHITECTURE.md`:
  - [ ] Описание всех Bounded Contexts
  - [ ] Описание слоёв (Domain, Application, Infrastructure, API)
  - [ ] Диаграммы архитектуры
- [ ] Создать `docs/BOUNDED_CONTEXTS.md`:
  - [ ] Детальное описание каждого контекста
  - [ ] Aggregate Root'ы
  - [ ] Value Objects
  - [ ] Domain Services
  - [ ] Границы контекстов
- [ ] Создать `docs/LAYERS.md`:
  - [ ] Описание каждого слоя
  - [ ] Правила зависимостей
  - [ ] Что можно и нельзя делать в каждом слое
- [ ] Обновить Context Map диаграмму
- [ ] Создать диаграммы агрегатов (PlantUML)
- [ ] Обновить README с новой структурой

**Результат**: Полная документация архитектуры

**Зависимости**: Issue #29

---

#### Issue #33: 📋 Создание ADR (Architecture Decision Records)

**Labels**: `type-documentation`, `priority-medium`

**Описание**:
Документировать все ключевые архитектурные решения в виде ADR.

**Checklist**:
- [ ] Создать папку `docs/adr/`
- [ ] Создать `001-ddd-bounded-contexts.md`
  - [ ] Контекст решения
  - [ ] Рассмотренные варианты
  - [ ] Выбранное решение
  - [ ] Последствия
- [ ] Создать `002-hexagonal-architecture.md`
  - [ ] Почему выбрана луковичная архитектура
  - [ ] Альтернативы (слоистая, чистая)
- [ ] Создать `003-aggregate-references-by-id.md`
  - [ ] Почему связи через ID, а не объекты
- [ ] Создать `004-modularization-approach.md`
  - [ ] Подход к модуляризации
  - [ ] Maven multi-module vs монолит
- [ ] Создать `005-value-objects-strategy.md`
  - [ ] Когда использовать Value Objects
  - [ ] Примеры
- [ ] Использовать стандартный формат ADR (MADR)

**Результат**: Документированные архитектурные решения

**Зависимости**: Issue #32

---

#### Issue #34: 📜 Создание Coding Standards

**Labels**: `type-documentation`, `priority-medium`

**Описание**:
Создать документ с кодировочными стандартами для проекта.

**Checklist**:
- [ ] Создать `CODING_STANDARDS.md`
- [ ] Раздел: Именование
  - [ ] Классы, методы, переменные
  - [ ] Domain моделей vs JPA entities
  - [ ] Value Objects (должны быть immutable)
- [ ] Раздел: Структура пакетов
  - [ ] Где размещать новые классы
  - [ ] Правила для каждого слоя
- [ ] Раздел: Value Objects
  - [ ] Когда создавать
  - [ ] Как валидировать
  - [ ] Примеры
- [ ] Раздел: Агрегаты
  - [ ] Бизнес-методы вместо сеттеров
  - [ ] Фабричные методы
  - [ ] Инварианты
- [ ] Раздел: Тестирование
  - [ ] Unit vs Integration тесты
  - [ ] Требования к покрытию
  - [ ] BDD-стиль
- [ ] Раздел: Обработка ошибок
  - [ ] Domain исключения
  - [ ] GlobalExceptionHandler
- [ ] Настроить Checkstyle
- [ ] Настроить SpotBugs (опционально)

**Результат**: Кодировочные стандарты проекта

**Зависимости**: Issue #32

---

#### Issue #35: ✅ Финальная проверка и валидация

**Labels**: `priority-critical`

**Описание**:
Провести финальную проверку всего рефакторинга перед завершением.

**Checklist**:
- [ ] Проверить, что все тесты проходят (1232/1232)
- [ ] Проверить покрытие тестами (цель > 80%)
- [ ] Проверить отсутствие DTO в Repository
- [ ] Проверить отсутствие зависимостей в мапперах
- [ ] Проверить отсутствие Helper Services
- [ ] Проверить, что Domain модели используют ID для связей
- [ ] Запустить статический анализ (SonarQube, если есть)
- [ ] Проверить, что проект собирается (`mvn clean install`)
- [ ] Проверить, что приложение запускается
- [ ] Проверить работу основных API эндпоинтов (Postman/Swagger)
- [ ] Код-ревью всех изменений
- [ ] Проверить документацию (полнота, актуальность)
- [ ] Удалить старый код (если не удален ранее)
- [ ] Создать Release Notes
- [ ] Сделать Git tag для релиза

**Результат**: Проект полностью рефакторен и готов к продакшену

**Зависимости**: Все предыдущие Issues

---

## 🎯 Kanban Board Columns

Рекомендуемая структура досок:

### Backlog
- Все Issues в начальном состоянии

### To Do
- Issues готовые к выполнению (все зависимости выполнены)

### In Progress
- Issues в работе (максимум 3-5 одновременно)

### Code Review
- Issues готовые к ревью

### Testing
- Issues на тестировании

### Done
- Завершенные Issues

---

## 📈 Dependencies Graph

```
Stage 0:
  Issue #1 → Issue #2, #3
  Issue #3 → Issue #4

Stage 1:
  Issue #4 → Issue #5, #6, #7, #13
  Issue #5 → Issue #8
  Issue #6 → Issue #10
  Issue #7 → Issue #9
  Issue #8, #9, #10 → Issue #11, #12
  Issue #12, #13 → Issue #14
  Issue #14 → Issue #15
  Issue #11, #15 → Issue #16
  Issue #16 → Issue #17

Stage 2:
  Issue #15 → Issue #18
  Issue #16 → Issue #19, #20
  Issue #8, #9, #10 → Issue #21

Stage 3:
  Issue #5, #6, #7 → Issue #22
  Issue #8, #9, #10 → Issue #23
  Issue #11 → Issue #24
  Issue #15 → Issue #25
  Issue #16 → Issue #26
  Issue #17 → Issue #27
  Issue #22-27 → Issue #28

Stage 4:
  Issue #17, #21 → Issue #29
  Issue #29 → Issue #30, #31

Documentation:
  Issue #29 → Issue #32
  Issue #32 → Issue #33, #34
  All → Issue #35
```

---

## 🚀 Пошаговый план выполнения

### Неделя 1: Подготовка и Foundation
1. **День 1**: Issue #1, #2 (Анализ и очистка)
2. **День 2**: Issue #3, #4 (Bounded Contexts + структура)
3. **День 3**: Issue #5, #6, #7 (Value Objects)
4. **День 4**: Issue #8 (Car Domain Model)
5. **День 5**: Issue #9, #10 (Contract, Client Domain Models)
6. **День 6**: Issue #11, #12 (Domain Services + Repository Interfaces)
7. **День 7**: Ревью недели, Issue #22 (Тесты Value Objects)

### Неделя 2: Infrastructure & Application
8. **День 8**: Issue #13, #14 (JPA Entities + Mappers)
9. **День 9**: Issue #15 (Repository Implementations) + Issue #25
10. **День 10**: Issue #16 (Application Services) + Issue #26
11. **День 11**: Issue #17 (API Layer) + Issue #27
12. **День 12**: Issue #18, #19, #20 (Fix Violations)
13. **День 13**: Issue #21, #23, #24 (ID References + Domain Tests)
14. **День 14**: Issue #28 (Coverage), Ревью недели

### Неделя 3: Modularization & Documentation
15. **День 15**: Issue #29 (Maven Multi-Module)
16. **День 16**: Issue #30, #31 (Enforcer + Shared Kernel)
17. **День 17**: Issue #32 (Architecture Docs)
18. **День 18**: Issue #33, #34 (ADR + Coding Standards)
19. **День 19**: Issue #35 (Final Validation)
20. **День 20**: Финальное ревью, релиз

---

## 📊 Метрики успеха

### Технические
- ✅ Тесты: 1232/1232 проходят
- ✅ Покрытие: > 80%
- ✅ Цикломатическая сложность: < 10
- ✅ Нет DTO в Repository
- ✅ Нет зависимостей в Mappers
- ✅ Нет Helper Services

### Архитектурные
- ✅ 4 четких Bounded Context
- ✅ Луковичная архитектура
- ✅ Агрегаты с бизнес-логикой
- ✅ Связи через ID
- ✅ Maven multi-module структура

### Документация
- ✅ ARCHITECTURE.md
- ✅ BOUNDED_CONTEXTS.md
- ✅ ADR (5+ решений)
- ✅ CODING_STANDARDS.md
- ✅ Context Map диаграммы

### Откат
- Все изменения в feature branches
- Код-ревью перед merge
- Сохранить старые классы с `@Deprecated` до полного переноса

---

## 📝 Заключение

Этот план рефакторинга обеспечивает:
1. **Постепенный переход** к DDD без "big bang" рефакторинга
2. **Подготовку к микросервисам** через чёткие границы и модули
3. **Улучшение качества кода** через устранение архитектурных нарушений
4. **Maintainability** через правильную структуру и документацию

После завершения этапов 0-1 проект будет готов к:
- Добавлению Redis для кэширования
- Интеграции WebSocket
- Внедрению CI/CD
- Переходу на микросервисную архитектуру

**Следующий шаг**: Начать с Этапа 0.1 — анализ и инвентаризация.

