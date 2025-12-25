# Рекомендуемая архитектура: DDD Modular Monolith

## Обзор

Данная архитектура представляет собой **модульный монолит**, построенный по принципам **Domain-Driven Design (DDD)** с явным разделением на **Bounded Contexts**. Это обеспечивает:
- Простоту разработки и деплоя (один артефакт)
- Ясные границы между доменами (подготовка к микросервисам)
- Возможность независимого развития модулей
- Готовность к event-driven архитектуре (Kafka)

---

## Архитектурные принципы

### 1. Bounded Contexts как модули

Приложение разделено на bounded contexts:
- **Fleet** (Автопарк) — управление машинами, моделями, брендами
- **Identity** (Идентификация) — пользователи, документы, аутентификация
- **Rental** (Аренда) — контракты, бронирования, оплата
- **Favorites** (Избранное) — cross-cutting: связь Client ↔ Car *(планируется)*
- **Authorization** (Авторизация) — роли, permissions, field-level security *(планируется)*

Каждый context — это полноценный модуль с собственными слоями.

### 2. Слоистая архитектура внутри контекста

Каждый bounded context имеет 4 слоя:
- **API** — REST контроллеры, DTO, фасады
- **Application** — Application Services (use cases, orchestration)
- **Domain** — Бизнес-логика (Entities, Value Objects, Domain Services)
- **Infrastructure** — Адаптеры (JPA, REST clients, Email, MinIO)

### 3. Dependency Rule (Clean Architecture)

Зависимости направлены только **внутрь**:
```
API → Application → Domain
       ↓
  Infrastructure (реализует интерфейсы из Domain)
```

**Domain** не зависит ни от чего — это чистая бизнес-логика.

### 4. Shared Kernel

Общий код для всех контекстов:
- `common/domain/` — базовые Value Objects (ClientId, Money)
- `common/events/` — Domain Events интерфейсы
- `common/exceptions/` — Базовые исключения
- `common/utils/` — Утилиты

---

## Структура проекта

```
src/main/java/org/example/carshering/
│
├── CarSheringApplication.java          # Spring Boot Entry Point
│
├── common/                              # Shared Kernel
│   ├── domain/
│   │   ├── valueobject/                # Общие Value Objects
│   │   │   ├── BaseId.java
│   │   │   ├── Money.java
│   │   │   ├── Email.java
│   │   │   └── DateRange.java
│   │   └── event/                      # Domain Events
│   │       ├── DomainEvent.java
│   │       └── DomainEventPublisher.java
│   ├── exception/                      # Общие исключения
│   │   ├── DomainException.java
│   │   ├── NotFoundException.java
│   │   └── ValidationException.java
│   └── util/                           # Утилиты
│
├── fleet/                               # Fleet Bounded Context
│   ├── api/                            # API Layer
│   │   ├── rest/                       # REST Controllers
│   │   │   ├── all/                    # Public endpoints
│   │   │   │   └── CarController.java
│   │   │   └── admin/                  # Admin endpoints
│   │   │       └── AdminCarController.java
│   │   ├── dto/                        # API DTOs
│   │   │   ├── request/
│   │   │   │   ├── CreateCarRequest.java
│   │   │   │   └── UpdateCarRequest.java
│   │   │   └── response/
│   │   │       └── CarResponse.java
│   │   └── facade/                     # Response Facades
│   │       └── CarResponseFacade.java
│   │
│   ├── application/                    # Application Layer
│   │   ├── service/                    # Application Services
│   │   │   ├── CarApplicationService.java
│   │   │   ├── BrandApplicationService.java
│   │   │   └── CarModelApplicationService.java
│   │   └── dto/                        # Application DTOs
│   │       └── CarDto.java
│   │
│   ├── domain/                         # Domain Layer
│   │   ├── model/                      # Domain Entities & Aggregates
│   │   │   ├── Car.java                # Aggregate Root
│   │   │   ├── CarModel.java
│   │   │   ├── Brand.java
│   │   │   └── CarClass.java
│   │   ├── repository/                 # Repository Interfaces
│   │   │   ├── CarRepository.java
│   │   │   └── BrandRepository.java
│   │   ├── service/                    # Domain Services
│   │   │   └── CarAvailabilityService.java
│   │   └── valueobject/                # Value Objects
│   │       ├── CarId.java
│   │       ├── CarStatus.java
│   │       └── VehicleIdentificationNumber.java
│   │
│   └── infrastructure/                 # Infrastructure Layer
│       ├── persistence/                # Database
│       │   ├── entity/                 # JPA Entities
│       │   │   └── CarJpaEntity.java
│       │   ├── repository/             # JPA Repositories
│       │   │   └── CarJpaRepository.java
│       │   ├── adapter/                # Repository Adapters
│       │   │   └── CarRepositoryAdapter.java
│       │   └── mapper/                 # JPA ↔ Domain Mappers
│       │       └── CarJpaMapper.java
│       └── external/                   # External Services
│           └── MinioCarPhotoStorage.java
│
├── identity/                            # Identity Bounded Context
│   ├── api/
│   │   ├── rest/
│   │   │   ├── all/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── ProfileController.java
│   │   │   └── admin/
│   │   │       └── AdminClientController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── RegistrationRequest.java
│   │   │   │   └── AuthRequest.java
│   │   │   └── response/
│   │   │       └── UserResponse.java
│   │   └── facade/
│   │       └── ClientResponseFacade.java
│   │
│   ├── application/
│   │   ├── service/
│   │   │   ├── ClientApplicationService.java
│   │   │   └── AuthApplicationService.java
│   │   └── dto/
│   │       └── ClientDto.java
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Client.java             # Aggregate Root
│   │   │   ├── Document.java
│   │   │   ├── Role.java
│   │   │   └── VerificationCode.java
│   │   ├── repository/
│   │   │   ├── ClientRepository.java
│   │   │   └── DocumentRepository.java
│   │   ├── service/
│   │   │   ├── PasswordService.java
│   │   │   └── EmailVerificationService.java
│   │   ├── valueobject/
│   │   │   ├── ClientId.java
│   │   │   ├── Password.java
│   │   │   └── DocumentNumber.java
│   │   └── exception/
│   │       ├── InvalidCredentialsException.java
│   │       └── DocumentNotVerifiedException.java
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   ├── entity/
│       │   │   └── ClientJpaEntity.java
│       │   ├── repository/
│       │   │   └── ClientJpaRepository.java
│       │   ├── adapter/
│       │   │   └── ClientRepositoryAdapter.java
│       │   └── mapper/
│       │       └── ClientJpaMapper.java
│       ├── email/                      # Email Service
│       │   └── EmailService.java
│       ├── security/                   # JWT & Security
│       │   ├── JwtService.java
│       │   └── SecurityConfig.java
│       └── tokens/
│           └── RefreshTokenService.java
│
├── rental/                              # Rental Bounded Context
│   ├── api/
│   │   ├── rest/
│   │   │   ├── all/
│   │   │   │   └── ContractController.java
│   │   │   └── admin/
│   │   │       └── AdminContractController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   └── CreateContractRequest.java
│   │   │   └── response/
│   │   │       └── ContractResponse.java
│   │   └── facade/
│   │       └── ContractResponseFacade.java
│   │
│   ├── application/
│   │   ├── service/
│   │   │   └── ContractApplicationService.java
│   │   └── dto/
│   │       └── ContractDto.java
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Contract.java           # Aggregate Root
│   │   │   └── RentalPeriod.java
│   │   ├── repository/
│   │   │   └── ContractRepository.java
│   │   ├── service/
│   │   │   ├── ContractPricingService.java
│   │   │   └── ContractValidationService.java
│   │   ├── valueobject/
│   │   │   ├── ContractId.java
│   │   │   ├── ContractStatus.java
│   │   │   └── RentalPrice.java
│   │   └── event/                      # Domain Events
│   │       ├── ContractCreatedEvent.java
│   │       ├── ContractConfirmedEvent.java
│   │       └── ContractCompletedEvent.java
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   ├── entity/
│       │   │   └── ContractJpaEntity.java
│       │   ├── repository/
│       │   │   └── ContractJpaRepository.java
│       │   ├── adapter/
│       │   │   └── ContractRepositoryAdapter.java
│       │   └── mapper/
│       │       └── ContractJpaMapper.java
│       └── scheduler/
│           └── ContractActivationScheduler.java
│
├── security/                            # Shared Security (cross-cutting)
│   ├── ClientDetails.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityExceptionHandler.java
│
└── config/                              # Spring Configuration
    ├── OpenApiConfig.java
    ├── CorsConfig.java
    ├── JacksonConfig.java
    └── AsyncConfig.java
```

---

## Описание слоёв

### API Layer (`api/`)

**Ответственность:**
- REST endpoints (controllers)
- HTTP request/response handling
- DTO validation (Bean Validation)
- Маппинг API DTOs ↔ Application DTOs
- Swagger/OpenAPI документация

**Зависимости:**
- Application Layer (вызывает Application Services)
- Не имеет прямого доступа к Domain или Infrastructure

**Правила:**
- Контроллеры должны быть "тонкими" — только валидация и делегирование
- Не содержат бизнес-логики
- Используют Facades для построения сложных response

**Пример:**
```java
@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarController {
    
    private final CarApplicationService carService;
    private final CarResponseFacade responseFacade;
    
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long id) {
        var carDto = carService.findCar(new CarId(id));
        var response = responseFacade.toResponse(carDto);
        return ResponseEntity.ok(response);
    }
}
```

---

### Application Layer (`application/`)

**Ответственность:**
- Orchestration (координация между доменами)
- Use cases implementation
- Transaction management (@Transactional)
- Маппинг Application DTOs ↔ Domain Models
- Публикация Domain Events

**Зависимости:**
- Domain Layer (использует Domain Models, Repositories, Domain Services)
- Не зависит от Infrastructure (работает с интерфейсами)

**Правила:**
- Application Services — это orchestrators, не business logic
- Должны быть stateless
- Операции должны быть атомарными (одна транзакция)
- Не содержат сложной бизнес-логики (делегируют Domain)

**Пример:**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ContractApplicationService {
    
    private final ContractRepository contractRepo;
    private final CarRepository carRepo;
    private final ClientRepository clientRepo;
    private final ApplicationEventPublisher eventPublisher;
    
    public ContractId createContract(CreateContractRequest request) {
        // 1. Получить агрегаты
        var car = carRepo.findById(new CarId(request.carId()))
            .orElseThrow(() -> new NotFoundException("Car not found"));
        var client = clientRepo.findById(new ClientId(request.clientId()))
            .orElseThrow(() -> new NotFoundException("Client not found"));
        
        // 2. Делегировать бизнес-логику Domain
        var contract = Contract.create(car, client, request.period());
        
        // 3. Сохранить
        contractRepo.save(contract);
        
        // 4. Опубликовать события
        contract.getDomainEvents().forEach(eventPublisher::publishEvent);
        contract.clearDomainEvents();
        
        return contract.getId();
    }
}
```

---

### Domain Layer (`domain/`)

**Ответственность:**
- Бизнес-логика и инварианты
- Domain Entities, Value Objects, Aggregates
- Domain Services (для логики, которая не принадлежит одному Entity)
- Repository interfaces (порты)
- Domain Events

**Зависимости:**
- НИКАКИХ! Domain layer полностью изолирован
- Может зависеть только от `common/domain/`

**Правила:**
- Вся бизнес-логика здесь
- Aggregates инкапсулируют инварианты
- Методы должны иметь говорящие имена (domain language)
- Нет зависимостей на фреймворки (чистая Java)

**Пример:**
```java
// Aggregate Root
public class Contract {
    private final ContractId id;
    private final ClientId clientId;
    private final CarId carId;
    private final RentalPeriod period;
    private ContractStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Factory method
    public static Contract create(Car car, Client client, RentalPeriod period) {
        // Инварианты
        if (!car.isAvailable()) {
            throw new CarNotAvailableException(car.getId());
        }
        if (!client.hasValidDocument()) {
            throw new ClientDocumentNotValidException(client.getId());
        }
        if (period.getDurationDays() < 1) {
            throw new InvalidRentalPeriodException("Period must be at least 1 day");
        }
        
        var contract = new Contract(
            ContractId.generate(),
            client.getId(),
            car.getId(),
            period,
            ContractStatus.CREATED
        );
        
        // Domain Event
        contract.addDomainEvent(new ContractCreatedEvent(
            contract.getId(),
            client.getId(),
            car.getId(),
            period,
            LocalDateTime.now()
        ));
        
        return contract;
    }
    
    // Бизнес-метод
    public void confirm() {
        if (this.status != ContractStatus.CREATED) {
            throw new InvalidContractStateException("Can only confirm CREATED contracts");
        }
        
        this.status = ContractStatus.CONFIRMED;
        addDomainEvent(new ContractConfirmedEvent(this.id, LocalDateTime.now()));
    }
    
    // Value Object
    public record RentalPeriod(LocalDateTime start, LocalDateTime end) {
        public RentalPeriod {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start must be before end");
            }
        }
        
        public long getDurationDays() {
            return ChronoUnit.DAYS.between(start, end);
        }
    }
}
```

---

### Infrastructure Layer (`infrastructure/`)

**Ответственность:**
- Реализация Repository интерфейсов (из Domain)
- JPA entities и маппинг Domain ↔ JPA
- Внешние сервисы (Email, MinIO, REST clients)
- Scheduled tasks
- Security implementation

**Зависимости:**
- Domain Layer (реализует интерфейсы)
- Зависит от фреймворков (Spring, JPA, etc.)

**Правила:**
- Адаптеры между Domain и внешним миром
- Маппинг Domain Models ↔ JPA Entities
- Никакой бизнес-логики

**Пример:**
```java
// Repository Adapter
@Repository
@RequiredArgsConstructor
public class ContractRepositoryAdapter implements ContractRepository {
    
    private final ContractJpaRepository jpaRepo;
    private final ContractJpaMapper mapper;
    
    @Override
    public Optional<Contract> findById(ContractId id) {
        return jpaRepo.findById(id.value())
            .map(mapper::toDomain);
    }
    
    @Override
    public void save(Contract contract) {
        var entity = mapper.toEntity(contract);
        jpaRepo.save(entity);
    }
}

// JPA Entity (отдельно от Domain)
@Entity
@Table(name = "contracts")
@Getter @Setter
public class ContractJpaEntity {
    @Id
    private Long id;
    
    @Column(name = "client_id")
    private Long clientId;
    
    @Column(name = "car_id")
    private Long carId;
    
    @Enumerated(EnumType.STRING)
    private ContractStatus status;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
}

// Mapper
@Mapper(componentModel = "spring")
public interface ContractJpaMapper {
    
    default Contract toDomain(ContractJpaEntity entity) {
        return Contract.reconstitute(
            new ContractId(entity.getId()),
            new ClientId(entity.getClientId()),
            new CarId(entity.getCarId()),
            new RentalPeriod(entity.getStartDate(), entity.getEndDate()),
            entity.getStatus()
        );
    }
    
    default ContractJpaEntity toEntity(Contract domain) {
        var entity = new ContractJpaEntity();
        entity.setId(domain.getId().value());
        entity.setClientId(domain.getClientId().value());
        entity.setCarId(domain.getCarId().value());
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getPeriod().start());
        entity.setEndDate(domain.getPeriod().end());
        return entity;
    }
}
```

---

## Взаимодействие между Bounded Contexts

### Правила коммуникации

1. **Прямых зависимостей НЕТ**
   - Fleet не знает о Rental
   - Identity не знает о Fleet
   - Rental не знает о Identity (использует только ID)

2. **Через Domain Events** (асинхронно)
   ```java
   // В Rental context
   contract.create();  // Генерирует ContractCreatedEvent
   
   // В Fleet context (слушатель)
   @EventListener
   void on(ContractCreatedEvent event) {
       var car = carRepo.findById(event.carId());
       car.markAsRented(event.contractId());
       carRepo.save(car);
   }
   ```

3. **Через Application Services** (синхронно, если необходимо)
   ```java
   // В Rental Application Service
   public ContractId createContract(CreateContractRequest request) {
       // Получить данные из других contexts через их Application Services
       var car = carApplicationService.findCar(request.carId());
       var client = clientApplicationService.findClient(request.clientId());
       
       // Создать contract в своём контексте
       // ...
   }
   ```

4. **Shared Kernel** для общих концепций
   - `ClientId`, `CarId` в `common/domain/valueobject/`
   - Базовые Domain Events в `common/domain/event/`

---

## Подготовка к микросервисам

### Текущая структура уже готова к разделению:

**Монолит:**
```
backend.jar
├── fleet/
├── identity/
└── rental/
```

**Микросервисы (будущее):**
```
fleet-service.jar       (8081)
identity-service.jar    (8082)
rental-service.jar      (8083)
```

### Шаги к микросервисам:

1. **Разделить базы данных**
   - Сейчас: Одна БД, разные схемы (car_rental.fleet, car_rental.identity, car_rental.rental)
   - Потом: Три отдельные БД

2. **REST API между сервисами** (или gRPC)
   - Заменить прямые вызовы Application Services на HTTP клиенты

3. **Event Bus (Kafka)**
   - Заменить Spring ApplicationEventPublisher на Kafka Producer
   - Domain Events → Kafka topics

4. **Service Discovery** (Eureka, Consul)
   - Для динамического обнаружения сервисов

5. **API Gateway** (Spring Cloud Gateway)
   - Единая точка входа для клиентов

---

## Kafka Integration (будущее)

### Domain Events → Kafka Topics

```java
// Вместо Spring ApplicationEventPublisher
@Component
public class KafkaEventPublisher implements DomainEventPublisher {
    
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    @Override
    public void publish(DomainEvent event) {
        String topic = event.getClass().getSimpleName();  // "ContractCreatedEvent"
        kafkaTemplate.send(topic, event.getAggregateId(), event);
    }
}

// Consumers в других сервисах
@KafkaListener(topics = "ContractCreatedEvent")
public void on(ContractCreatedEvent event) {
    // Update car status
}
```

### SAGA Pattern для distributed transactions

```java
// Rental Service
public void createContract(CreateContractRequest request) {
    // 1. Create contract (local transaction)
    var contract = contractRepo.save(...);
    
    // 2. Publish event
    eventPublisher.publish(new ContractCreatedEvent(...));
    
    // 3. Wait for compensating events if failure
}

// Fleet Service (compensating transaction)
@KafkaListener(topics = "ContractCancelledEvent")
public void on(ContractCancelledEvent event) {
    var car = carRepo.findById(event.carId());
    car.releaseFromContract();
    carRepo.save(car);
}
```

---

## Преимущества данной архитектуры

### Для тестирования
✅ Domain layer тестируется без Spring (pure unit tests)
✅ Каждый слой тестируется изолированно
✅ Легко мокировать зависимости (interfaces)

### Для масштабирования
✅ Bounded contexts изолированы
✅ Можно масштабировать по context (Fleet отдельно от Rental)
✅ Готовность к микросервисам без refactoring

### Для декомпозиции
✅ Каждый context — кандидат на отдельный микросервис
✅ Нет циклических зависимостей
✅ Domain Events готовы к Kafka

### Для поддержки
✅ Ясная ответственность каждого слоя
✅ Бизнес-логика изолирована в Domain
✅ Инфраструктурные детали скрыты

---

## Заключение

Данная архитектура представляет собой **баланс** между:
- **Простотой монолита** (один деплой, одна БД, простая разработка)
- **Гибкостью микросервисов** (четкие границы, готовность к разделению)

Это **эволюционная архитектура**, которая позволяет:
1. Начать с монолита
2. Развивать bounded contexts независимо
3. При необходимости декомпозировать в микросервисы
4. Внедрить event-driven архитектуру (Kafka)

**Идеально для pet-проекта**, демонстрирующего:
- Понимание DDD и Clean Architecture
- Готовность к production (модульность, тестируемость)
- Системное мышление (подготовка к микросервисам)
- DevOps readiness (Docker, CI/CD, мониторинг)
