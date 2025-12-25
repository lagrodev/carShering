# Фаза 2: Повышение качества кода (Code Quality)

**Цель фазы:** Устранить технический долг, исправить все TODO/FIXME, убрать null values, внедрить строгую обработку ошибок.

**Время выполнения:** 10-14 часов (7-9 задач по 1-2 часа)

**Приоритет:** ⭐⭐⭐ Критический

---

## 2.1. Систематическое исправление TODO и FIXME

### Конкретное действие
Пройти по всем TODO/FIXME в проекте (найдено 20+ мест) и либо исправить, либо создать GitHub Issue для отложенных задач.

Приоритетные TODO:
- `ContractServiceImpl` — планировщик для активации контрактов
- `ContractServiceImpl` — логика отмены контракта
- `ClientServiceImpl` — инвалидация сессий при смене пароля
- `CarServiceImpl` — фильтр по датам для доступности машин
- `DocumentServiceImpl` — поле для хранения, кто верифицировал документ
- `FavoriteServiceImpl` — перенос в favorites/ bounded context (из service/impl)
- Legacy code cleanup после создания authorization/ и favorites/ contexts

### Что нужно изучить
- Spring `@Scheduled` для планировщиков
- Spring Security Session Management
- PostgreSQL date range queries
- Audit fields в JPA (@CreatedBy, @LastModifiedBy)

### Возможные сложности
- Планировщик должен работать в одном экземпляре (distributed lock при масштабировании)
- Временные зоны при работе с датами (UTC vs local time)
- Session invalidation может затронуть текущего пользователя

### Как проверить результат
```bash
# Найти все TODO/FIXME
grep -r "TODO\|FIXME" src/main/java/ --exclude-dir=target

# После исправления - результат должен быть пустым или минимальным
grep -r "TODO\|FIXME" src/main/java/ | wc -l
# Должно быть 0 или список оставшихся с Issue номерами

# Запуск тестов для проверки регрессии
mvn test
```

Пример исправления (планировщик):
```java
@Component
@EnableScheduling
public class ContractActivationScheduler {
    
    @Scheduled(fixedDelay = 300000) // каждые 5 минут
    @SchedulerLock(name = "activateContracts", 
                   lockAtMostFor = "4m", 
                   lockAtLeastFor = "1m")
    public void activatePendingContracts() {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        var contracts = contractRepo.findByStatusAndStartDateBefore(
            ContractStatus.CONFIRMED, now
        );
        
        contracts.forEach(Contract::activate);
        contractRepo.saveAll(contracts);
    }
}
```

### Как это отразится в Git
```
fix: resolve all TODO and FIXME items in codebase

- Implemented contract activation scheduler
- Added audit trail for document verification
- Implemented session invalidation on password change
- Added date-based car availability filter
- Remaining items converted to GitHub issues #45, #46

Closes #19, #20, #21
```

---

## 2.2. Замена null на значения по умолчанию в API responses

### Конкретное действие
Настроить Jackson и создать DTOs так, чтобы null никогда не возвращался в API:
- Коллекции → пустой список вместо null
- Числа → 0 вместо null (где применимо)
- Строки → "" или Optional, или исключить поле
- Использовать `@JsonInclude(JsonInclude.Include.NON_NULL)` для опциональных полей

### Что нужно изучить
- Jackson annotations (@JsonInclude, @JsonProperty, @JsonIgnore)
- Java Records для immutable DTOs
- Optional в REST API (best practices)
- JSON Schema и null handling

### Возможные сложности
- Legacy clients могут ожидать null
- Нужно решить стратегию: исключать null поля или заменять дефолтами
- Primitive wrappers (Integer, Long) vs primitives (int, long)

### Как проверить результат
```java
// Response DTO с дефолтами
public record CarResponse(
    Long id,
    String modelName,
    BigDecimal pricePerDay,
    List<String> features,      // NEVER null, empty list if no features
    Integer yearOfManufacture,  // или @JsonInclude(NON_NULL)
    String description
) {
    public CarResponse {
        if (features == null) {
            features = Collections.emptyList();
        }
        if (description == null) {
            description = "";
        }
    }
}

// Глобальная конфигурация Jackson
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
```

Тесты:
```java
@Test
void shouldReturnEmptyListInsteadOfNull() {
    var response = carController.getCar(1L);
    
    assertThat(response.features()).isNotNull();
    assertThat(response.features()).isEmpty();
}
```

Проверка через API:
```bash
curl http://localhost:8082/api/car/1 | jq .
# Проверить, что нет полей с null
curl http://localhost:8082/api/car/1 | jq 'select(.features == null)'
# Должно быть пусто
```

### Как это отразится в Git
```
feat(api): replace null values with defaults in API responses

- Collections return empty lists instead of null
- Configured Jackson to exclude null fields
- Updated all Response DTOs with proper defaults
- Added validation tests for null handling

Closes #22
```

---

## 2.3. Внедрение глобальной обработки ошибок

### Конкретное действие
Создать централизованный `@ControllerAdvice` для обработки исключений:
- Кастомные исключения (NotFoundException, ValidationException, BusinessRuleViolationException)
- Стандартные Spring исключения (MethodArgumentNotValidException)
- Security исключения (AccessDeniedException)
- Неожиданные исключения (Exception)

Создать стандартный формат ошибки:
```json
{
  "timestamp": "2025-12-22T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Car with id 123 not found",
  "path": "/api/car/123",
  "traceId": "abc-123-def"
}
```

### Что нужно изучить
- Spring `@ControllerAdvice` и `@ExceptionHandler`
- RFC 7807 Problem Details for HTTP APIs
- Correlation IDs / Trace IDs для логирования
- Exception hierarchy design

### Возможные сложности
- Нужно решить, какую детализацию показывать клиенту (security vs usability)
- Stack traces не должны утекать в production
- Разные форматы для разных типов ошибок (validation errors имеют поля)

### Как проверить результат
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        NotFoundException ex, 
        HttpServletRequest request
    ) {
        var error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        var errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        // ...
    }
}
```

Тесты:
```java
@Test
void shouldReturn404WhenCarNotFound() {
    mockMvc.perform(get("/api/car/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.path").value("/api/car/999"));
}
```

### Как это отразится в Git
```
feat(api): implement global exception handling

- Created @ControllerAdvice for centralized error handling
- Standardized error response format (RFC 7807 inspired)
- Custom exceptions with proper HTTP status codes
- Added correlation IDs for request tracing
- Tests for all error scenarios

Closes #23
```

---

## 2.4. Добавление валидации на всех уровнях

### Конкретное действие
Внедрить валидацию на трёх уровнях:
1. **API level**: `@Valid` на request DTOs с Bean Validation annotations
2. **Application level**: Проверка бизнес-правил в Application Services
3. **Domain level**: Инварианты в конструкторах и методах Aggregates

### Что нужно изучить
- Bean Validation (JSR-303) annotations
- Custom validators
- Validation Groups для разных сценариев
- Domain-Driven Design: Invariants

### Возможные сложности
- Дублирование валидации на разных уровнях
- Различие между технической и бизнес-валидацией
- Перфоманс при сложной валидации

### Как проверить результат
```java
// API level validation
public record CreateCarRequest(
    @NotBlank(message = "Model name is required")
    String modelName,
    
    @NotNull
    @Positive(message = "Price must be positive")
    BigDecimal pricePerDay,
    
    @Min(1900) @Max(2025)
    Integer year,
    
    @Pattern(regexp = "^[A-Z0-9]{17}$", message = "Invalid VIN format")
    String vin
) {}

// Domain level validation (in constructor)
public class Car {
    public Car(CarId id, String vin, BigDecimal pricePerDay) {
        if (pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (!VinValidator.isValid(vin)) {
            throw new IllegalArgumentException("Invalid VIN");
        }
        this.id = id;
        this.vin = vin;
        this.pricePerDay = pricePerDay;
    }
}

// Application level validation
@Service
class CarApplicationService {
    public CarId createCar(CreateCarRequest request) {
        // Бизнес-правило: не более 100 машин с одинаковой моделью
        if (carRepo.countByModel(request.modelName()) >= 100) {
            throw new BusinessRuleViolationException(
                "Cannot add more than 100 cars of the same model"
            );
        }
        // ...
    }
}
```

Тесты:
```java
@Test
void shouldRejectInvalidCarRequest() {
    var request = new CreateCarRequest("", new BigDecimal("-100"), null, "INVALID");
    
    mockMvc.perform(post("/api/admin/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.modelName").exists())
        .andExpect(jsonPath("$.errors.pricePerDay").exists());
}
```

### Как это отразится в Git
```
feat(validation): add multi-layer validation

- API level: Bean Validation on request DTOs
- Application level: Business rules validation
- Domain level: Invariants in aggregates
- Custom validators for complex rules
- Comprehensive validation tests

Closes #24
```

---

## 2.5. Настройка Lombok для уменьшения boilerplate

### Конкретное действие
Оптимизировать использование Lombok:
- `@Data` → заменить на `@Getter` + `@Setter` для entities (для контроля mutability)
- Использовать `@Builder` для сложных объектов
- `@RequiredArgsConstructor` для DI
- `@Slf4j` для логгеров
- Избегать `@Data` на entities (проблемы с equals/hashCode в Hibernate)

### Что нужно изучить
- Lombok annotations best practices
- Lombok vs Java Records
- Hibernate и equals/hashCode
- IntelliJ IDEA Lombok plugin

### Возможные сложности
- Lombok может конфликтовать с MapStruct (порядок annotation processors)
- `@Data` генерирует equals/hashCode, что опасно для JPA entities
- Delombok для CI/CD (если нужно)

### Как проверить результат
```java
// ДО
public class Car {
    private Long id;
    private String model;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... 20 строк геттеров/сеттеров
}

// ПОСЛЕ
@Getter
@Setter
@Entity
@Table(name = "cars")
public class CarJpaEntity {
    @Id
    private Long id;
    private String model;
    
    // equals/hashCode вручную для JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarJpaEntity)) return false;
        return id != null && id.equals(((CarJpaEntity) o).id);
    }
}

// Для DTOs - Records вместо Lombok
public record CarDto(Long id, String model) {}
```

Проверка компиляции:
```bash
mvn clean compile
```

### Как это отразится в Git
```
refactor(lombok): optimize Lombok usage and remove boilerplate

- Replaced @Data with @Getter/@Setter on entities
- Added @Builder for complex object construction
- Used @RequiredArgsConstructor for dependency injection
- Replaced Lombok DTOs with Java Records where possible
- Fixed equals/hashCode for JPA entities

Refs: #code-quality
```

---

## 2.6. Внедрение Code Style и форматирования

### Конкретное действие
Настроить автоматическое форматирование кода:
1. Добавить `checkstyle.xml` с Google Java Style (или другой стандарт)
2. Настроить Maven Checkstyle Plugin
3. Настроить `.editorconfig` для IDE
4. Добавить Maven Formatter Plugin для автоформатирования

### Что нужно изучить
- Checkstyle configuration
- Google Java Style Guide
- EditorConfig
- Maven plugins: checkstyle, formatter, spotless

### Возможные сложности
- Огромный diff при первом применении форматирования
- Конфликты в Git при merge
- Настройка IntelliJ IDEA для автоматического применения

### Как проверить результат
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <failOnViolation>true</failOnViolation>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Команды:
```bash
# Проверка стиля
mvn checkstyle:check

# Автоформатирование
mvn spotless:apply

# Проверка без исправления
mvn spotless:check
```

`.editorconfig`:
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
trim_trailing_whitespace = true
insert_final_newline = true

[*.{yml,yaml}]
indent_size = 2
```

### Как это отразится в Git
```
chore: add code style enforcement

- Added Checkstyle with Google Java Style
- Configured Maven Checkstyle Plugin
- Added .editorconfig for IDE consistency
- Configured Spotless for auto-formatting
- Applied formatting to entire codebase

Refs: #code-quality
```

---

## 2.7. Оптимизация SQL запросов и N+1 проблема

### Конкретное действие
Найти и исправить N+1 проблемы в JPA:
- Включить `spring.jpa.show-sql=true` и `hibernate.format_sql=true`
- Найти запросы в циклах
- Добавить `@EntityGraph` или `JOIN FETCH` для eager loading связей
- Настроить Hibernate statistics для мониторинга

### Что нужно изучить
- N+1 query problem
- JPA `@EntityGraph`
- JPQL `JOIN FETCH`
- Hibernate Second Level Cache
- Database connection pooling (HikariCP settings)

### Возможные сложности
- Cartesian product при multiple bag fetches
- LazyInitializationException
- Баланс между eager и lazy loading
- Производительность при большом количестве данных

### Как проверить результат
```java
// ДО (N+1 problem)
@GetMapping("/contracts")
public List<ContractResponse> getAllContracts() {
    var contracts = contractRepo.findAll(); // 1 запрос
    return contracts.stream()
        .map(c -> {
            var car = carRepo.findById(c.getCarId()); // N запросов!
            var client = clientRepo.findById(c.getClientId()); // N запросов!
            return new ContractResponse(c, car, client);
        })
        .toList();
}

// ПОСЛЕ (решение через JOIN FETCH)
@Query("SELECT c FROM Contract c " +
       "JOIN FETCH c.car " +
       "JOIN FETCH c.client")
List<Contract> findAllWithDetails();

// Или через @EntityGraph
@EntityGraph(attributePaths = {"car", "client"})
List<Contract> findAll();
```

Проверка логов:
```properties
# application.yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
        use_sql_comments: true
```

Проверить количество запросов:
```bash
# В логах должно быть 1-2 запроса, а не N+1
# Включить Hibernate statistics и проверить метрики
```

### Как это отразится в Git
```
perf(database): fix N+1 queries with entity graphs

- Added @EntityGraph for eager loading relationships
- Optimized contract queries with JOIN FETCH
- Configured Hibernate statistics for monitoring
- Fixed lazy loading issues in API endpoints
- Reduced database round trips from N+1 to 1

Closes #27
```

---

## 2.8. Внедрение Dependency Injection best practices

### Конкретное действие
Улучшить использование DI в Spring:
- Использовать constructor injection везде (не field injection)
- `@RequiredArgsConstructor` из Lombok для final полей
- Избегать `@Autowired` на полях
- Использовать интерфейсы для зависимостей, не конкретные классы

### Что нужно изучить
- Constructor injection vs Field injection
- Spring Dependency Injection best practices
- SOLID principles (Dependency Inversion)
- Testing with constructor injection

### Возможные сложности
- Circular dependencies (решаются через `@Lazy` или рефакторинг)
- Lombok `@RequiredArgsConstructor` требует final поля
- Legacy код с field injection

### Как проверить результат
```java
// ДО (плохо - field injection)
@Service
public class CarService {
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private CarMapper mapper;
}

// ПОСЛЕ (хорошо - constructor injection)
@Service
@RequiredArgsConstructor
public class CarApplicationService {
    private final CarRepository carRepository;
    private final CarDtoMapper mapper;
    
    // Конструктор генерируется Lombok
}
```

Поиск field injection:
```bash
grep -r "@Autowired" src/main/java/ --exclude-dir=target

# Должен вернуть пустой результат или минимум
```

Тесты (легче с constructor injection):
```java
@Test
void testCarService() {
    var mockRepo = mock(CarRepository.class);
    var mockMapper = mock(CarDtoMapper.class);
    
    // Легко создать через конструктор
    var service = new CarApplicationService(mockRepo, mockMapper);
    
    // ...
}
```

### Как это отразится в Git
```
refactor(di): replace field injection with constructor injection

- Replaced @Autowired field injection with constructor injection
- Used @RequiredArgsConstructor for DI
- All dependencies now final and injected via constructor
- Improved testability and immutability

Refs: #code-quality
```

---

## 2.9. Code Coverage: достижение целевых метрик

### Конкретное действие
Улучшить покрытие тестами до целевых значений JaCoCo (80% instructions, 60% branches):
- Проверить текущее покрытие: `mvn clean verify`
- Найти непокрытые участки в отчёте: `target/site/jacoco/index.html`
- Добавить недостающие тесты для критичных участков
- Исключить из покрытия: DTOs, configs, main method

### Что нужно изучить
- JaCoCo Maven Plugin configuration
- Code coverage metrics (line, branch, instruction)
- Testing strategies (unit, integration)
- Exclusions в JaCoCo

### Возможные сложности
- 80% coverage не означает качественные тесты
- Сложно покрыть exception handling
- Integration tests могут быть медленными

### Как проверить результат
```xml
<!-- pom.xml - исключения -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/*Application.class</exclude>
            <exclude>**/dto/**</exclude>
            <exclude>**/config/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

Команды:
```bash
# Запуск с coverage
mvn clean verify

# Открыть отчёт
open target/site/jacoco/index.html

# Проверить, что build прошел (достигнуты минимальные метрики)
mvn verify
# Если coverage < 80%, build fail
```

Пример теста для повышения coverage:
```java
@Test
void shouldThrowExceptionWhenCarNotAvailable() {
    var car = new Car(new CarId(1L), CarStatus.RENTED);
    
    assertThatThrownBy(() -> car.rentTo(contractId, period))
        .isInstanceOf(CarNotAvailableException.class)
        .hasMessageContaining("Car is not available");
}
```

### Как это отразится в Git
```
test: improve code coverage to 80%+

- Added unit tests for domain logic
- Added integration tests for critical flows
- Configured JaCoCo exclusions for non-logic code
- Achieved 82% instruction coverage, 65% branch coverage
- All builds now require minimum coverage

Closes #28
```

---

## Чеклист выполнения Фазы 2

- [ ] 2.1. Все TODO/FIXME исправлены или задокументированы в Issues
- [ ] 2.2. API responses не содержат null значений
- [ ] 2.3. Глобальная обработка ошибок работает
- [ ] 2.4. Валидация на трёх уровнях (API, Application, Domain)
- [ ] 2.5. Lombok настроен оптимально
- [ ] 2.6. Code style enforced через Checkstyle/Spotless
- [ ] 2.7. N+1 проблемы решены
- [ ] 2.8. Constructor injection везде
- [ ] 2.9. Code coverage ≥ 80%

## Результат Фазы 2

✅ Production-ready качество кода
✅ Нет технического долга (TODO/FIXME)
✅ Консистентный API контракт без null
✅ Централизованная обработка ошибок
✅ Высокое покрытие тестами
✅ Оптимизированные SQL запросы

**Следующая фаза:** Testing — комплексное тестирование всех слоёв
