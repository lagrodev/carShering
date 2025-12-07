# Ответы на вопросы по переходу на DDD

## 1. Mapper - что было не так?

### ❌ Проблема
В вашем маппере был **неработающий `@ObjectFactory`**:

```java
@ObjectFactory
protected Contract createContract(ContractId id, ClientId clientId, ...) {
    return Contract.restore(...);
}
```

**Почему не работало:**
- MapStruct вызывает `@ObjectFactory` с параметрами, которые он распознаёт из SOURCE объекта
- Ваш метод принимал уже распакованные Value Objects, а MapStruct работает с `ContractJpaEntity`

### ✅ Правильное решение
```java
@ObjectFactory
protected Contract createContract(ContractJpaEntity entity) {
    return Contract.restore(
        toContractId(entity.getId()),
        toClientId(entity.getClientId()),
        toCarId(entity.getCarId()),
        entity.getPeriod(),
        entity.getTotalCost(),
        entity.getState(),
        entity.getComment()
    );
}
```

Теперь MapStruct:
1. Вызывает `createContract(entity)` для создания объекта
2. Использует метод `restore()` вашего агрегата
3. Все поля правильно маппятся

---

## 2. ClientId, CarId, ContractId - это просто Long?

### ❌ НЕТ! Они Value Objects

```java
@Getter
@EqualsAndHashCode
public class ContractId {
    private final Long value;
    
    public ContractId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid ContractId");
        }
        this.value = value;
    }
}
```

### Зачем это нужно?

1. **Type Safety** - компилятор не даст перепутать:
   ```java
   void method(ContractId id, ClientId clientId) { }
   // Нельзя вызвать: method(clientId, contractId) ❌
   ```

2. **Валидация в одном месте** - правила проверки ID в конструкторе

3. **Явность кода** - сразу видно, что это за ID:
   ```java
   Contract contract = new Contract(contractId, clientId, carId); // ✅ Понятно
   Contract contract = new Contract(1L, 2L, 3L); // ❌ Что это за цифры?
   ```

4. **Domain-driven язык** - говорим на языке предметной области

---

## 3. RentalStateType - как лучше реализовать?

### ✅ Правильная реализация (State Pattern)

```java
public enum RentalStateType {
    PENDING {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == CONFIRMED || target == CANCELLED;
        }
        
        @Override
        public boolean isUpdatable() {
            return true;
        }
    },
    
    CONFIRMED {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == ACTIVE || target == CANCELLATION_REQUESTED;
        }
        
        @Override
        public boolean isUpdatable() {
            return true;
        }
    },
    
    ACTIVE {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == COMPLETED || target == CANCELLATION_REQUESTED;
        }
        
        @Override
        public boolean isUpdatable() {
            return false;
        }
    },
    
    CANCELLATION_REQUESTED {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == CANCELLED || target == CONFIRMED;
        }
        
        @Override
        public boolean isUpdatable() {
            return false;
        }
    },
    
    CANCELLED {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return false; // Terminal state
        }
        
        @Override
        public boolean isUpdatable() {
            return false;
        }
    },
    
    COMPLETED {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return false; // Terminal state
        }
        
        @Override
        public boolean isUpdatable() {
            return false;
        }
    };

    public abstract boolean canTransitionTo(RentalStateType target);
    public abstract boolean isUpdatable();
}
```

### Преимущества:
- ✅ Логика переходов инкапсулирована в enum
- ✅ Легко добавлять новые состояния
- ✅ JPA нативно работает с `@Enumerated(EnumType.STRING)`
- ✅ Не нужна отдельная таблица `RentalState`

---

## 4. RentalState таблица - нужна ли?

### ❌ В DDD она НЕ нужна!

**Старый подход (не DDD):**
```sql
-- Таблица со статусами
CREATE TABLE rental_state (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50)
);

-- Ссылка на статус
CREATE TABLE contract (
    state_id BIGINT REFERENCES rental_state(id)
);
```

### ✅ DDD подход:

```sql
CREATE TABLE contract (
    id BIGSERIAL PRIMARY KEY,
    state VARCHAR(50) NOT NULL, -- Прямо enum
    -- ... другие поля
);
```

```java
@Enumerated(EnumType.STRING)
@Column(name = "state")
private RentalStateType state;
```

**Почему так лучше:**
1. **Простота** - нет лишних JOIN'ов
2. **Производительность** - меньше запросов
3. **Domain-ориентированность** - состояние - часть агрегата, а не отдельная сущность
4. **Миграции** - добавление статуса = просто добавление enum значения

---

## 5. Логика отмены - внутри агрегата или в Domain Service?

### ✅ Правильное разделение:

#### В агрегате `Contract` - ПРОСТАЯ логика:
```java
public class Contract {
    public void cancel() {
        if (!state.canTransitionTo(RentalStateType.CANCELLED)) {
            throw new InvalidContractStateException(
                "Cannot cancel contract in state: " + state
            );
        }
        this.state = RentalStateType.CANCELLED;
    }
}
```

#### В `RentalDomainService` - СЛОЖНАЯ логика:

```java
@Service
public class RentalDomainService {
    
    private final ContractRepository contractRepository;
    private final NotificationService notificationService;
    
    /**
     * Отмена контракта пользователем
     * - Можно отменить до начала аренды
     * - Штраф не берется
     */
    public void requestUserCancellation(Contract contract) {
        if (contract.getRentalPeriod().hasStarted()) {
            throw new CancellationNotAllowedException(
                "Cannot cancel contract after rental started"
            );
        }
        
        contract.requestCancellation();
        contractRepository.save(contract);
        notificationService.notifyAdminCancellationRequested(contract);
    }
    
    /**
     * Отмена контракта администратором
     * - Можно отменить в любой момент
     * - Возврат денег по политике
     */
    public void adminCancelContract(Contract contract, String reason, Money refundAmount) {
        contract.cancel();
        contract.addComment("Admin cancellation: " + reason);
        
        // Рассчитываем возврат
        if (refundAmount != null && refundAmount.isGreaterThan(Money.ZERO)) {
            // Логика возврата денег
            processRefund(contract, refundAmount);
        }
        
        contractRepository.save(contract);
        notificationService.notifyClientCancellation(contract, refundAmount);
    }
}
```

### Правило:
- **В агрегате** - инварианты и переходы состояний
- **В Domain Service** - оркестрация нескольких агрегатов, внешние правила

---

## 6. updateDates() - надо ли делать, если поля final?

### ❌ Ваша текущая проблема:
```java
private final RentalPeriod rentalPeriod;
private final Money totalCost;
```

### ✅ Правильное решение - убрать `final`:

```java
public class Contract {
    // Immutable identifiers
    private final ContractId id;
    private final ClientId clientId;
    private final CarId carId;
    
    // Mutable value objects
    private RentalPeriod rentalPeriod;
    private Money totalCost;
    private RentalStateType state;
    private String comment;
    
    public void updateDates(RentalPeriod newPeriod, Money dailyRate) {
        if (!state.isUpdatable()) {
            throw new InvalidContractStateException(
                "Cannot update dates in state: " + state
            );
        }
        this.rentalPeriod = newPeriod;
        this.totalCost = dailyRate.multiply(newPeriod.getDurationInDays());
    }
}
```

### Почему так правильно:
1. **ID агрегата неизменен** - `final` для идентификаторов
2. **Бизнес-данные изменяемы** - через контролируемые методы
3. **Инкапсуляция** - изменение только через `updateDates()` с валидацией
4. **Value Objects иммутабельны** - вы заменяете целый объект `RentalPeriod`

---

## 7. Создание нового контракта VS обновление

### ❌ Плохая идея - создавать новый контракт при обновлении:
```java
// НЕ ДЕЛАТЬ ТАК!
contractRepository.delete(oldContract);
Contract newContract = Contract.create(...);
contractRepository.save(newContract);
```

**Проблемы:**
1. Потеря истории изменений
2. Потеря связей (если на контракт ссылаются другие сущности)
3. Проблемы с транзакциями

### ✅ Правильно - Event Sourcing LITE:

```java
// В агрегате Contract
@Getter
public class Contract {
    private final List<ContractEvent> events = new ArrayList<>();
    
    public void updateDates(RentalPeriod newPeriod, Money dailyRate) {
        if (!state.isUpdatable()) {
            throw new InvalidContractStateException();
        }
        
        RentalPeriod oldPeriod = this.rentalPeriod;
        this.rentalPeriod = newPeriod;
        this.totalCost = dailyRate.multiply(newPeriod.getDurationInDays());
        
        // Записываем событие
        events.add(new ContractDatesUpdatedEvent(this.id, oldPeriod, newPeriod));
    }
}

// Отдельная таблица событий
@Entity
@Table(name = "contract_events")
public class ContractEventJpaEntity {
    @Id
    @GeneratedValue
    private Long id;
    
    private Long contractId;
    private String eventType;
    private String eventData; // JSON
    private LocalDateTime occurredAt;
}
```

Тогда:
- ✅ Контракт обновляется (один ID)
- ✅ История сохраняется в событиях
- ✅ Можно построить аудит

---

## 8. Удаление контракта - потеря данных

### ✅ НИКОГДА не удаляйте контракты физически!

```java
public class Contract {
    private boolean deleted; // Soft delete
    private LocalDateTime deletedAt;
    
    public void markAsDeleted() {
        if (this.state != RentalStateType.CANCELLED) {
            throw new IllegalStateException("Can only delete cancelled contracts");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
```

В репозитории:
```java
public interface ContractRepository {
    // По умолчанию только не удаленные
    List<Contract> findAllActive();
    
    // Для аудита
    List<Contract> findAllIncludingDeleted();
}
```

### Когда можно удалять физически:
- По регуляторным требованиям (GDPR - "право на забвение")
- После архивации в отдельное хранилище
- **Никогда автоматически!**

---

## 9. Структура DDD - общая картина

```
┌─────────────────────────────────────────────────────────┐
│                    API Layer (REST)                     │
│  ContractController.java                                │
│  - Принимает HTTP запросы                               │
│  - Валидация входных данных                             │
│  - Маппинг Request DTO -> Application DTO               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Application Layer                          │
│  ContractApplicationService.java                        │
│  - Оркестрация use cases                                │
│  - Управление транзакциями                              │
│  - Вызов Domain Services                                │
│  - Работа с несколькими агрегатами                      │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                 Domain Layer                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Aggregates                                       │   │
│  │  Contract.java (Aggregate Root)                  │   │
│  │  - Бизнес-логика                                 │   │
│  │  - Инварианты                                    │   │
│  │  - Переходы состояний                            │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Domain Services                                  │   │
│  │  RentalDomainService.java                        │   │
│  │  - Логика между агрегатами                       │   │
│  │  - Проверка доступности машины                   │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Repository Interfaces                            │   │
│  │  ContractRepository.java                         │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│            Infrastructure Layer                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Repository Implementations                       │   │
│  │  ContractRepositoryImpl.java                     │   │
│  │  - Маппинг Domain ↔ JPA                          │   │
│  │  - Работа с БД                                   │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ JPA Entities                                     │   │
│  │  ContractJpaEntity.java                          │   │
│  │  - Аннотации JPA                                 │   │
│  │  - Связи с таблицами                             │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 10. Foreign Key и связанность между таблицами

### ❌ Старый подход (Anemic Domain):
```java
@Entity
public class ContractJpaEntity {
    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientJpaEntity client; // ❌ Связь на уровне объектов
    
    @ManyToOne
    @JoinColumn(name = "car_id")
    private CarJpaEntity car; // ❌ Связь на уровне объектов
}
```

### ✅ DDD подход:

**В Domain:**
```java
public class Contract {
    private final ContractId id;
    private final ClientId clientId;  // ✅ Только ID!
    private final CarId carId;        // ✅ Только ID!
}
```

**В Infrastructure (JPA Entity):**
```java
@Entity
@Table(name = "contract")
public class ContractJpaEntity {
    @Id
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId; // ✅ Только FK
    
    @Column(name = "car_id", nullable = false)
    private Long carId;    // ✅ Только FK
    
    // НЕТ @ManyToOne!
}
```

**В БД:**
```sql
CREATE TABLE contract (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    
    -- ✅ FK для целостности данных
    CONSTRAINT fk_contract_client 
        FOREIGN KEY (client_id) REFERENCES client(id),
    CONSTRAINT fk_contract_car 
        FOREIGN KEY (car_id) REFERENCES car(id)
);
```

### Почему так правильно:

1. **Слабая связанность** - агрегаты независимы
2. **Границы агрегатов** - Contract не знает о внутренностях Car/Client
3. **Производительность** - нет автоматических JOIN'ов
4. **Целостность данных** - FK в БД защищает от orphan записей

### Как получить связанные данные:

```java
@Service
public class ContractApplicationService {
    
    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    
    public ContractDetailDto getContractDetails(ContractId contractId) {
        Contract contract = contractRepository.findById(contractId);
        
        // Явно загружаем связанные агрегаты
        Client client = clientRepository.findById(contract.getClientId());
        Car car = carRepository.findById(contract.getCarId());
        
        return ContractDetailDto.builder()
            .contract(contract)
            .client(client)
            .car(car)
            .build();
    }
}
```

---

## 11. List<Contract> в Client/Car - убрать?

### ✅ ДА, убрать!

**Старый код (не DDD):**
```java
@Entity
public class Client {
    @OneToMany(mappedBy = "client")
    private List<Contract> contracts; // ❌ Нарушение границ агрегата
}
```

**DDD подход:**
```java
// Domain model Client - без контрактов
public class Client {
    private final ClientId id;
    private PersonalInfo personalInfo;
    private List<Document> documents; // ✅ Часть агрегата Client
    // НЕТ contracts!
}

// Контракты ищутся через репозиторий
@Service
public class ClientApplicationService {
    
    public List<Contract> getClientContracts(ClientId clientId) {
        return contractRepository.findByClientId(clientId);
    }
}
```

### Почему убирать:
1. **Границы агрегатов** - Client и Contract - разные агрегаты
2. **Производительность** - не грузим все контракты при загрузке клиента
3. **Ответственность** - Client отвечает за клиентские данные, не за аренду

---

## 12. Репозитории в Domain - зачем папка?

### ✅ Правильная структура:

```
rental/
├── domain/
│   ├── model/
│   │   └── Contract.java
│   ├── service/
│   │   └── RentalDomainService.java
│   └── repository/               # ✅ ИНТЕРФЕЙСЫ
│       └── ContractRepository.java
│
└── infrastructure/
    └── persistence/
        ├── ContractRepositoryImpl.java    # ✅ РЕАЛИЗАЦИЯ
        ├── ContractJpaRepository.java     # Spring Data
        └── entity/
            └── ContractJpaEntity.java
```

### Domain Repository (интерфейс):
```java
// rental/domain/repository/ContractRepository.java
public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(ContractId id);
    List<Contract> findByClientId(ClientId clientId);
    List<Contract> findActiveForCar(CarId carId, RentalPeriod period);
    void delete(Contract contract);
}
```

### Infrastructure Repository (реализация):
```java
// rental/infrastructure/persistence/ContractRepositoryImpl.java
@Repository
public class ContractRepositoryImpl implements ContractRepository {
    
    private final ContractJpaRepository jpaRepository;
    private final ContractMapper mapper;
    
    @Override
    public Contract save(Contract contract) {
        ContractJpaEntity entity = mapper.toEntity(contract);
        ContractJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Contract> findById(ContractId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }
}

// Spring Data JPA репозиторий
interface ContractJpaRepository extends JpaRepository<ContractJpaEntity, Long> {
    List<ContractJpaEntity> findByClientId(Long clientId);
}
```

### Зачем так сложно?

1. **Dependency Inversion** - Domain не зависит от Infrastructure
2. **Тестируемость** - можно сделать mock репозитория
3. **Гибкость** - можно поменять БД без изменения Domain

---

## 13. RentalDomainService - что туда переносить?

### ❌ НЕ переносить всю логику из ContractServiceImpl!

**Application Service** (ContractApplicationService):
```java
@Service
@Transactional
public class ContractApplicationService {
    
    private final RentalDomainService rentalDomainService;
    private final ContractRepository contractRepository;
    private final NotificationService notificationService;
    
    /**
     * USE CASE: Создать контракт аренды
     */
    public ContractId createContract(CreateContractRequest request) {
        // 1. Валидация запроса
        validateRequest(request);
        
        // 2. Вызов Domain Service
        Contract contract = rentalDomainService.createRental(
            new ClientId(request.getClientId()),
            new CarId(request.getCarId()),
            request.getPeriod(),
            request.getDailyRate()
        );
        
        // 3. Сохранение
        contract = contractRepository.save(contract);
        
        // 4. Внешние эффекты
        notificationService.sendContractCreated(contract);
        
        return contract.getId();
    }
}
```

**Domain Service** (RentalDomainService):
```java
@Service
public class RentalDomainService {
    
    private final ContractRepository contractRepository;
    private final CarRepository carRepository;
    
    /**
     * БИЗНЕС-ПРАВИЛО: Создание аренды с проверкой доступности
     */
    public Contract createRental(ClientId clientId, CarId carId, 
                                  RentalPeriod period, Money dailyRate) {
        // 1. Проверка доступности машины
        if (!isCarAvailable(carId, period)) {
            throw new CarNotAvailableException(
                "Car is already rented for this period"
            );
        }
        
        // 2. Проверка бизнес-правил
        validateRentalPeriod(period);
        
        // 3. Создание агрегата
        return Contract.create(clientId, carId, period, dailyRate);
    }
    
    /**
     * БИЗНЕС-ПРАВИЛО: Проверка доступности машины
     */
    private boolean isCarAvailable(CarId carId, RentalPeriod requestedPeriod) {
        List<Contract> activeContracts = contractRepository
            .findActiveForCar(carId, requestedPeriod);
        
        return activeContracts.stream()
            .noneMatch(contract -> 
                contract.getRentalPeriod().overlaps(requestedPeriod)
            );
    }
}
```

### Правило разделения:

| Layer | Ответственность | Пример |
|-------|----------------|---------|
| **Domain Service** | Бизнес-правила между агрегатами | Проверка доступности машины |
| **Application Service** | Оркестрация use case | Создание контракта + уведомление |
| **Aggregate** | Инварианты агрегата | Переход между состояниями |

---

## 14. Бизнес-правило доступности - зависит от репозитория?

### ✅ ДА, и это нормально в DDD!

```java
@Service
public class RentalDomainService {
    
    private final ContractRepository contractRepository; // ✅ Domain repository
    
    public boolean isCarAvailable(CarId carId, RentalPeriod period) {
        List<Contract> activeContracts = contractRepository
            .findActiveForCar(carId, period);
        
        return activeContracts.stream()
            .noneMatch(c -> c.getRentalPeriod().overlaps(period));
    }
}
```

### Почему это правильно:

1. **Domain Service может использовать репозитории** - это норма DDD
2. **Репозиторий - часть Domain** - интерфейс в domain/, реализация в infrastructure/
3. **Бизнес-логика остаётся в Domain** - мы не идём в БД за логикой, мы идём за данными

### Что НЕЛЬЗЯ:
```java
// ❌ Не делать так!
@Service
public class RentalDomainService {
    
    @Autowired
    private EntityManager em; // ❌ Прямая работа с JPA
    
    public boolean isCarAvailable(CarId carId) {
        String sql = "SELECT * FROM contract WHERE car_id = ?";
        // ❌ SQL в Domain Service
    }
}
```

---

## 15. Итоговая структура проекта

```
rental/
├── domain/                              # ЯДРО
│   ├── model/
│   │   ├── Contract.java                # Aggregate Root
│   │   │   - create()
│   │   │   - confirm()
│   │   │   - cancel()
│   │   │   - updateDates()
│   │   └── valueobject/
│   │       ├── ContractId.java
│   │       ├── ClientId.java
│   │       ├── CarId.java
│   │       ├── RentalPeriod.java
│   │       └── RentalStateType.java
│   ├── service/
│   │   └── RentalDomainService.java     # Бизнес-правила между агрегатами
│   └── repository/
│       └── ContractRepository.java       # Интерфейс
│
├── application/                          # USE CASES
│   ├── service/
│   │   └── ContractApplicationService.java
│   ├── dto/
│   │   ├── CreateContractRequest.java
│   │   └── ContractDetailDto.java
│   └── mapper/
│       └── ContractDtoMapper.java
│
├── infrastructure/                       # ТЕХНИЧЕСКИЕ ДЕТАЛИ
│   ├── persistence/
│   │   ├── ContractRepositoryImpl.java   # Реализация
│   │   ├── ContractJpaRepository.java    # Spring Data
│   │   ├── entity/
│   │   │   └── ContractJpaEntity.java    # JPA сущность
│   │   └── mapper/
│   │       └── ContractMapper.java       # Domain ↔ JPA
│   └── messaging/
│       └── ContractEventPublisher.java
│
└── api/                                  # REST API
    ├── rest/
    │   └── ContractController.java
    └── dto/
        ├── CreateContractApiRequest.java
        └── ContractApiResponse.java
```

---

## Коротко: Чек-лист правильного DDD

- ✅ Агрегаты с бизнес-логикой (не anemic domain)
- ✅ Value Objects для типобезопасности
- ✅ ID как Value Objects (ContractId, ClientId, CarId)
- ✅ Только ID для связей между агрегатами
- ✅ Enum для состояний (не отдельная таблица)
- ✅ Domain Service для логики между агрегатами
- ✅ Soft delete вместо физического удаления
- ✅ Event Sourcing для истории изменений
- ✅ Repository интерфейсы в Domain
- ✅ Repository реализации в Infrastructure
- ✅ Mapper с @ObjectFactory для восстановления агрегатов
- ✅ Foreign Keys в БД для целостности

---

Успехов в рефакторинге! 🚀

