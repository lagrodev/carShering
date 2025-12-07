# Краткая памятка по DDD для Car Sharing проекта

## 🎯 Основные принципы

### 1. Агрегат (Aggregate)
- **Что**: Кластер связанных объектов, рассматриваемых как единое целое
- **Пример**: `Contract` - корень агрегата аренды
- **Правила**:
  - ✅ Только один корень агрегата (Aggregate Root)
  - ✅ Внешние объекты ссылаются только на корень
  - ✅ Изменения только через корень
  - ✅ Транзакция = один агрегат

```java
// ✅ Правильно
Contract contract = Contract.create(clientId, carId, period, dailyRate);
contract.confirm();
contractRepository.save(contract);

// ❌ Неправильно
contract.state = RentalStateType.CONFIRMED; // Прямое изменение поля
```

### 2. Value Object (VO)
- **Что**: Объект без идентичности, определяется значениями
- **Примеры**: `Money`, `RentalPeriod`, `ContractId`, `ClientId`, `CarId`
- **Правила**:
  - ✅ Immutable (неизменяемый)
  - ✅ Equals по значению, не по ссылке
  - ✅ Валидация в конструкторе
  - ✅ Бизнес-логика внутри VO

```java
// ✅ Value Object
@Value
public class ContractId {
    Long value;
    
    public ContractId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid ContractId");
        }
        this.value = value;
    }
}

// ✅ Использование
ContractId id = new ContractId(1L);
```

### 3. Entity (Сущность)
- **Что**: Объект с уникальной идентичностью
- **Примеры**: `Contract`, `Client`, `Car`
- **Правила**:
  - ✅ Equals по ID, не по полям
  - ✅ Бизнес-логика внутри сущности
  - ✅ Изменяемые атрибуты
  - ✅ Неизменяемый ID

```java
// ✅ Entity
public class Contract {
    private final ContractId id; // ✅ Immutable ID
    private RentalStateType state; // ✅ Mutable
    
    public void confirm() {
        if (!state.canTransitionTo(CONFIRMED)) {
            throw new InvalidContractStateException();
        }
        this.state = CONFIRMED;
    }
}
```

### 4. Domain Service
- **Что**: Бизнес-логика, не привязанная к одному агрегату
- **Пример**: `RentalDomainService`
- **Когда использовать**:
  - ✅ Операция затрагивает несколько агрегатов
  - ✅ Логика не относится естественно ни к одному агрегату
  - ✅ Требуется доступ к репозиториям

```java
@Service
public class RentalDomainService {
    private final ContractRepository contractRepository;
    
    // ✅ Проверка доступности - нужны данные из репозитория
    public boolean isCarAvailable(CarId carId, RentalPeriod period) {
        List<Contract> activeContracts = 
            contractRepository.findActiveForCar(carId, period);
        return activeContracts.stream()
            .noneMatch(c -> c.getRentalPeriod().overlaps(period));
    }
}
```

### 5. Application Service
- **Что**: Оркестратор use case
- **Пример**: `ContractApplicationService`
- **Ответственность**:
  - ✅ Управление транзакциями
  - ✅ Вызов Domain Services
  - ✅ Координация нескольких агрегатов
  - ✅ Внешние эффекты (уведомления, события)

```java
@Service
@Transactional
public class ContractApplicationService {
    
    public ContractId createContract(CreateContractRequest request) {
        // 1. Вызов Domain Service
        Contract contract = rentalDomainService.createRental(...);
        
        // 2. Сохранение
        contract = contractRepository.save(contract);
        
        // 3. Внешние эффекты
        notificationService.sendContractCreated(contract);
        
        return contract.getId();
    }
}
```

### 6. Repository
- **Что**: Абстракция доступа к данным
- **Структура**:
  - ✅ Интерфейс в `domain/repository/`
  - ✅ Реализация в `infrastructure/persistence/`
  - ✅ Работает с Domain объектами, не с JPA entities

```java
// domain/repository/ContractRepository.java
public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(ContractId id);
}

// infrastructure/persistence/ContractRepositoryImpl.java
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
}
```

---

## 📦 Структура слоёв

```
┌─────────────────────────────────────────┐
│          API Layer (REST)               │  ← HTTP, Валидация входа
│      ContractController.java            │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Application Layer                  │  ← Use Cases, Транзакции
│  ContractApplicationService.java        │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         Domain Layer                    │  ← Бизнес-логика
│  ┌────────────────────────────────┐     │
│  │ Contract.java (Aggregate)      │     │
│  │ RentalDomainService.java       │     │
│  │ ContractRepository (interface) │     │
│  └────────────────────────────────┘     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Infrastructure Layer               │  ← БД, JPA, Маппинг
│  ContractRepositoryImpl.java            │
│  ContractJpaEntity.java                 │
│  ContractMapper.java                    │
└─────────────────────────────────────────┘
```

---

## 🚫 Типичные ошибки

### ❌ Anemic Domain Model
```java
// Плохо - только getters/setters
public class Contract {
    private Long id;
    private String state;
    
    public void setState(String state) { this.state = state; }
}

// Сервис делает всю работу
public class ContractService {
    public void confirm(Contract contract) {
        contract.setState("CONFIRMED"); // ❌
    }
}
```

### ✅ Rich Domain Model
```java
// Хорошо - бизнес-логика внутри
public class Contract {
    private RentalStateType state;
    
    public void confirm() {
        if (!state.canTransitionTo(CONFIRMED)) {
            throw new InvalidContractStateException();
        }
        this.state = CONFIRMED;
    }
}
```

### ❌ Нарушение границ агрегата
```java
// Плохо - прямые связи между агрегатами
@Entity
public class Contract {
    @ManyToOne
    private Client client; // ❌
    
    @ManyToOne
    private Car car; // ❌
}
```

### ✅ Ссылки через ID
```java
// Хорошо - только ID
public class Contract {
    private final ContractId id;
    private final ClientId clientId; // ✅
    private final CarId carId; // ✅
}
```

### ❌ Primitive Obsession
```java
// Плохо - примитивные типы
public void createContract(Long clientId, Long carId) { ... }
```

### ✅ Value Objects
```java
// Хорошо - Value Objects
public void createContract(ClientId clientId, CarId carId) { ... }
```

---

## 🔧 Практические решения

### 1. Как работать с внешними агрегатами?

```java
// Application Service координирует несколько агрегатов
@Service
@Transactional
public class ContractApplicationService {
    
    public ContractDetailsDto getContractDetails(ContractId id) {
        // Загружаем каждый агрегат отдельно
        Contract contract = contractRepository.findById(id);
        Client client = clientRepository.findById(contract.getClientId());
        Car car = carRepository.findById(contract.getCarId());
        
        // Собираем DTO
        return new ContractDetailsDto(contract, client, car);
    }
}
```

### 2. Как делать Soft Delete?

```java
public class Contract {
    private boolean deleted;
    private LocalDateTime deletedAt;
    
    public void markAsDeleted() {
        if (!state.isTerminal()) {
            throw new IllegalStateException("Can only delete terminal contracts");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

// Repository
public interface ContractRepository {
    @Query("SELECT c FROM Contract c WHERE c.deleted = false")
    List<Contract> findAllActive();
}
```

### 3. Как делать аудит изменений?

```java
// Domain Events
public class Contract {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public void confirm() {
        RentalStateType oldState = this.state;
        this.state = CONFIRMED;
        
        // Добавляем событие
        domainEvents.add(new ContractConfirmedEvent(this.id, oldState, CONFIRMED));
    }
    
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}

// После сохранения - публикуем события
@Service
public class ContractApplicationService {
    
    @Transactional
    public void confirmContract(ContractId id) {
        Contract contract = contractRepository.findById(id);
        contract.confirm();
        contract = contractRepository.save(contract);
        
        // Публикуем события
        contract.getDomainEvents().forEach(eventPublisher::publish);
        contract.clearDomainEvents();
    }
}
```

### 4. Как тестировать Domain?

```java
// Unit тесты для агрегатов - без БД!
class ContractTest {
    
    @Test
    void shouldConfirmPendingContract() {
        // Given
        Contract contract = Contract.create(
            new ClientId(1L),
            new CarId(2L),
            RentalPeriod.of(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
            Money.of(100, "USD")
        );
        
        // When
        contract.confirm();
        
        // Then
        assertThat(contract.getState()).isEqualTo(RentalStateType.CONFIRMED);
    }
    
    @Test
    void shouldNotConfirmActiveContract() {
        // Given
        Contract contract = createActiveContract();
        
        // When & Then
        assertThatThrownBy(() -> contract.confirm())
            .isInstanceOf(InvalidContractStateException.class);
    }
}
```

---

## 📚 Полезные ссылки

- [DDD Questions Answered](./DDD_QUESTIONS_ANSWERED.md) - подробные ответы на ваши вопросы
- [Refactor Plan](./refactor_plan.md) - план перехода на DDD
- [Entity Dependencies Analysis](./ENTITY_DEPENDENCIES_ANALYSIS.md) - анализ зависимостей

---

## ✅ Чек-лист перехода на DDD

- [ ] Определить Bounded Contexts
- [ ] Выделить агрегаты с корнями
- [ ] Создать Value Objects для всех концептов
- [ ] Переместить бизнес-логику в агрегаты
- [ ] Убрать прямые связи между агрегатами (только ID)
- [ ] Разделить Domain и Infrastructure репозитории
- [ ] Создать Application Services для use cases
- [ ] Убрать Anemic Domain Model
- [ ] Внедрить Domain Events
- [ ] Написать unit-тесты для Domain

