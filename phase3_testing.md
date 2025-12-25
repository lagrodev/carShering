# Фаза 3: Комплексное тестирование

**Цель фазы:** Создать полноценную пирамиду тестирования с unit, integration, contract, и e2e тестами. Покрыть новые bounded contexts (favorites, authorization).

**Время выполнения:** 12-16 часов (8-10 задач по 1.5-2 часа)

**Приоритет:** ⭐⭐⭐ Критический

**Важно:** Обязательно покрыть тестами:
- `favorites/` bounded context (FavoriteCar, cross-context integration)
- `authorization/` bounded context (RolePermissionService, field-level security)

---

## 3.1. Актуализация существующих unit тестов

### Конкретное действие
Пройти по всем существующим тестам (~30 тестовых классов) и:
- Удалить устаревшие тесты для удалённого кода
- Обновить тесты для изменённой логики (DDD рефакторинг)
- Привести к единому стилю (BDD: given-when-then)
- Добавить AssertJ для readable assertions

### Что нужно изучить
- JUnit 5 best practices
- AssertJ fluent assertions
- Test naming conventions (shouldDoSomethingWhenCondition)
- Given-When-Then pattern
- Mockito для мокирования

### Возможные сложности
- Старые тесты могут использовать устаревший API
- Тесты могут быть тесно связаны с реализацией (brittle tests)
- Мокирование зависимостей может быть сложным после DDD рефакторинга

### Как проверить результат
```java
// ДО (плохой тест)
@Test
public void test1() {
    CarService service = new CarService();
    Car car = service.getCar(1L);
    assertEquals("Model S", car.getModel());
}

// ПОСЛЕ (хороший тест)
@Test
@DisplayName("Should return car details when car exists")
void shouldReturnCarDetailsWhenCarExists() {
    // given
    var carId = new CarId(1L);
    var expectedCar = Car.builder()
        .id(carId)
        .modelName("Model S")
        .build();
    
    when(carRepository.findById(carId))
        .thenReturn(Optional.of(expectedCar));
    
    // when
    var result = carApplicationService.findCar(carId);
    
    // then
    assertThat(result)
        .isNotNull()
        .extracting(CarDto::modelName)
        .isEqualTo("Model S");
    
    verify(carRepository).findById(carId);
}
```

Запуск тестов:
```bash
mvn test

# Проверка всех unit тестов
mvn test -Dtest=*Test

# Проверить, что нет игнорированных тестов
mvn test | grep "@Disabled"
```

### Как это отразится в Git
```
test: modernize existing unit tests

- Updated all 30+ existing test classes
- Applied given-when-then structure
- Replaced JUnit assertions with AssertJ
- Fixed tests after DDD refactoring
- Removed obsolete tests for deleted code
- All tests now use @DisplayName

Closes #30
```

---

## 3.2. Unit тесты для Domain логики

### Конкретное действие
Создать pure unit тесты для Domain layer (без Spring context):
- Тесты для Aggregates (Car, Client, Contract)
- Тесты для Value Objects (CarId, Money, DateRange)
- Тесты для Domain Services
- Тесты для Domain Events
- Цель: 100% coverage Domain layer

### Что нужно изучить
- Domain-Driven Design testing strategies
- Testing без Spring (POJO tests)
- Property-based testing (опционально, jqwik)
- Testing invariants и business rules

### Возможные сложности
- Domain объекты могут требовать сложной настройки (builders помогают)
- Тестирование Domain Events
- Тестирование exception cases

### Как проверить результат
```java
class ContractTest {
    
    @Test
    @DisplayName("Should create contract when car is available")
    void shouldCreateContractWhenCarIsAvailable() {
        // given
        var car = CarMother.availableCar();
        var client = ClientMother.verifiedClient();
        var period = new DateRange(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(5)
        );
        
        // when
        var contract = Contract.create(car, client, period);
        
        // then
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.CREATED);
        assertThat(contract.getCar()).isEqualTo(car);
        assertThat(contract.getClient()).isEqualTo(client);
        assertThat(contract.getDomainEvents())
            .hasSize(1)
            .first()
            .isInstanceOf(ContractCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should throw exception when car is not available")
    void shouldThrowExceptionWhenCarIsNotAvailable() {
        // given
        var car = CarMother.rentedCar();
        var client = ClientMother.verifiedClient();
        var period = DateRange.of(1, 5);
        
        // when / then
        assertThatThrownBy(() -> Contract.create(car, client, period))
            .isInstanceOf(CarNotAvailableException.class)
            .hasMessageContaining("not available");
    }
}

// Object Mother pattern для test data
class CarMother {
    static Car availableCar() {
        return Car.builder()
            .id(new CarId(1L))
            .status(CarStatus.AVAILABLE)
            .build();
    }
    
    static Car rentedCar() {
        return Car.builder()
            .id(new CarId(2L))
            .status(CarStatus.RENTED)
            .build();
    }
}
```

Запуск:
```bash
# Только domain тесты
mvn test -Dtest=**/domain/**/*Test

# Coverage только для domain
mvn test jacoco:report
# Проверить target/site/jacoco/org.example.carshering.*.domain/index.html
```

### Как это отразится в Git
```
test(domain): add comprehensive domain logic tests

- Added unit tests for all Aggregates (Car, Client, Contract)
- Added tests for Value Objects with validation
- Added tests for Domain Services
- Added tests for Domain Events publishing
- Implemented Object Mother pattern for test data
- Achieved 100% coverage for domain layer

Closes #31
```

---

## 3.3. Integration тесты для Application Services

### Конкретное действие
Создать интеграционные тесты для Application Services с реальной БД (Testcontainers):
- Тесты для `CarApplicationService`
- Тесты для `ClientApplicationService`
- Тесты для `ContractApplicationService`
- Проверка транзакций, событий, взаимодействия с БД

### Что нужно изучить
- Spring Boot Test (`@SpringBootTest`, `@DataJpaTest`)
- Testcontainers для PostgreSQL
- `@Transactional` в тестах
- Test slices (`@WebMvcTest`, `@DataJpaTest`)

### Возможные сложности
- Testcontainers требует Docker
- Медленные тесты (поднятие контейнера)
- Управление тестовыми данными (cleanup между тестами)
- Flyway миграции должны выполняться в тест БД

### Как проверить результат
```java
@SpringBootTest
@Testcontainers
class CarApplicationServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private CarApplicationService carService;
    
    @Autowired
    private CarRepository carRepository;
    
    @Test
    @DisplayName("Should create car and persist to database")
    void shouldCreateCarAndPersistToDatabase() {
        // given
        var request = new CreateCarRequest(
            "Model S",
            new BigDecimal("100.00"),
            2023,
            "1HGBH41JXMN109186"
        );
        
        // when
        var carId = carService.createCar(request);
        
        // then
        var savedCar = carRepository.findById(carId);
        assertThat(savedCar).isPresent();
        assertThat(savedCar.get().getModelName()).isEqualTo("Model S");
    }
    
    @Test
    @DisplayName("Should rollback transaction on validation error")
    void shouldRollbackTransactionOnValidationError() {
        // given
        var invalidRequest = new CreateCarRequest(null, null, 0, "INVALID");
        var countBefore = carRepository.count();
        
        // when / then
        assertThatThrownBy(() -> carService.createCar(invalidRequest))
            .isInstanceOf(ValidationException.class);
        
        var countAfter = carRepository.count();
        assertThat(countAfter).isEqualTo(countBefore); // rollback произошёл
    }
}
```

Запуск:
```bash
# Integration тесты
mvn verify -Dtest=*IntegrationTest

# Проверить, что Testcontainers работает
docker ps
# Должен показать запущенный PostgreSQL контейнер во время тестов
```

### Как это отразится в Git
```
test(integration): add application service integration tests

- Added Testcontainers for PostgreSQL integration tests
- Implemented integration tests for all Application Services
- Tests validate transactions, persistence, and rollbacks
- Flyway migrations run automatically in test DB
- All integration tests passing

Closes #32
```

---

## 3.4. API тесты с MockMvc

### Конкретное действие
Создать тесты для REST API контроллеров:
- Тесты для всех endpoints (GET, POST, PUT, DELETE)
- Проверка статус кодов (200, 201, 400, 404, 500)
- Проверка JSON response structure
- Проверка валидации request body
- Проверка авторизации (@WithMockUser)

### Что нужно изучить
- Spring MockMvc
- `@WebMvcTest` для тестирования контроллеров
- `@WithMockUser` для Security тестов
- JSON matchers (JsonPath)

### Возможные сложности
- MockMvc не поднимает полный контекст (нужно мокировать сервисы)
- Security configuration может блокировать тесты
- JSON serialization/deserialization в тестах

### Как проверить результат
```java
@WebMvcTest(CarController.class)
class CarControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CarApplicationService carService;
    
    @MockBean
    private CarResponseFacade responseFacade;
    
    @Test
    @DisplayName("GET /api/car/{id} should return 200 with car details")
    @WithMockUser
    void shouldReturnCarDetailsWhenCarExists() throws Exception {
        // given
        var carId = new CarId(1L);
        var carDto = new CarDto(carId, "Model S", new BigDecimal("100"));
        var carResponse = new CarResponse(1L, "Model S", new BigDecimal("100"));
        
        when(carService.findCar(carId)).thenReturn(carDto);
        when(responseFacade.toResponse(carDto)).thenReturn(carResponse);
        
        // when / then
        mockMvc.perform(get("/api/car/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.modelName").value("Model S"))
            .andExpect(jsonPath("$.pricePerDay").value(100.0));
    }
    
    @Test
    @DisplayName("GET /api/car/{id} should return 404 when car not found")
    @WithMockUser
    void shouldReturn404WhenCarNotFound() throws Exception {
        // given
        when(carService.findCar(any()))
            .thenThrow(new NotFoundException("Car not found"));
        
        // when / then
        mockMvc.perform(get("/api/car/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    @DisplayName("POST /api/admin/cars should return 400 on validation error")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400OnValidationError() throws Exception {
        // given
        var invalidRequest = """
            {
                "modelName": "",
                "pricePerDay": -100
            }
            """;
        
        // when / then
        mockMvc.perform(post("/api/admin/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.modelName").exists())
            .andExpect(jsonPath("$.errors.pricePerDay").exists());
    }
    
    @Test
    @DisplayName("POST /api/admin/cars should return 403 for non-admin user")
    @WithMockUser(roles = "USER")
    void shouldReturn403ForNonAdminUser() throws Exception {
        // given
        var request = """
            {
                "modelName": "Model S",
                "pricePerDay": 100
            }
            """;
        
        // when / then
        mockMvc.perform(post("/api/admin/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
    }
}
```

Запуск:
```bash
# API тесты
mvn test -Dtest=*ControllerTest

# С детальным выводом
mvn test -Dtest=CarControllerTest -X
```

### Как это отразится в Git
```
test(api): add comprehensive REST API tests with MockMvc

- Added MockMvc tests for all controllers
- Tests cover success, error, and validation scenarios
- Added security tests with @WithMockUser
- JSON response validation with JsonPath
- All HTTP status codes tested

Closes #33
```

---

## 3.5. Security тесты

**КРИТИЧНО:** Покрыть authorization/ bounded context тестами:
- RolePermissionService.canAccessField() для всех ролей
- Field-level masking в Response Facades
- Custom PermissionEvaluator с @PreAuthorize
- Cache invalidation при изменении permissions

### Конкретное действие
Создать тесты для Spring Security конфигурации:
- Тесты для JWT аутентификации
- Тесты для авторизации (ADMIN vs USER роли)
- Тесты для public endpoints
- Тесты для CORS
- Тесты для защиты от CSRF (если включен)

### Что нужно изучить
- Spring Security Test
- `@WithMockUser`, `@WithAnonymousUser`
- Testing JWT generation and validation
- Testing method security (@PreAuthorize)

### Возможные сложности
- Security конфигурация может быть сложной для тестирования
- JWT токены нужно генерировать в тестах
- CORS и CSRF могут влиять на тесты

### Как проверить результат
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Public endpoints should be accessible without authentication")
    void publicEndpointsShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/car/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Admin endpoints should require ADMIN role")
    @WithMockUser(roles = "USER")
    void adminEndpointsShouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Admin endpoints should be accessible with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void adminEndpointsShouldBeAccessibleWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Protected endpoints should return 401 without authentication")
    void protectedEndpointsShouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isUnauthorized());
    }
}

@WebMvcTest(AuthController.class)
class JwtAuthenticationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthApplicationService authService;
    
    @Test
    @DisplayName("Should return JWT token on successful login")
    void shouldReturnJwtTokenOnSuccessfulLogin() throws Exception {
        // given
        var authRequest = """
            {
                "username": "user@example.com",
                "password": "password123"
            }
            """;
        
        when(authService.authenticate(any()))
            .thenReturn(new AuthResponse("mock-jwt-token", "refresh-token"));
        
        // when / then
        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequest))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("refreshToken"))
            .andExpect(jsonPath("$.token").exists());
    }
}
```

### Как это отразится в Git
```
test(security): add comprehensive security tests

- Added tests for JWT authentication flow
- Added authorization tests for ADMIN vs USER roles
- Added tests for public vs protected endpoints
- Added CORS configuration tests
- All security paths covered with tests

Closes #34
```

---

## 3.6. Repository тесты с @DataJpaTest

### Конкретное действие
Создать тесты для кастомных методов Repository:
- Тесты для сложных запросов
- Тесты для `@Query` аннотаций
- Тесты для пагинации и сортировки
- Тесты для уникальных constraint

### Что нужно изучить
- `@DataJpaTest` slice
- Spring Data JPA test utilities
- Testing pagination and sorting
- Testing unique constraints и database errors

### Возможные сложности
- Testcontainers для реального PostgreSQL (иначе H2 может вести себя по-другому)
- Тестовые данные должны быть минимальными
- Flyway миграции должны работать

### Как проверить результат
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CarRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private CarJpaRepository carRepository;
    
    @Test
    @DisplayName("Should find available cars by date range")
    void shouldFindAvailableCarsByDateRange() {
        // given
        var car1 = createCar("Model S", CarStatus.AVAILABLE);
        var car2 = createCar("Model 3", CarStatus.RENTED);
        carRepository.saveAll(List.of(car1, car2));
        
        var startDate = LocalDateTime.now();
        var endDate = LocalDateTime.now().plusDays(7);
        
        // when
        var availableCars = carRepository.findAvailableInPeriod(startDate, endDate);
        
        // then
        assertThat(availableCars)
            .hasSize(1)
            .extracting(CarJpaEntity::getModelName)
            .containsExactly("Model S");
    }
    
    @Test
    @DisplayName("Should enforce unique VIN constraint")
    void shouldEnforceUniqueVinConstraint() {
        // given
        var car1 = createCarWithVin("VIN123");
        carRepository.save(car1);
        
        var car2 = createCarWithVin("VIN123"); // same VIN
        
        // when / then
        assertThatThrownBy(() -> carRepository.save(car2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
    
    @Test
    @DisplayName("Should paginate car results")
    void shouldPaginateCarResults() {
        // given
        for (int i = 0; i < 25; i++) {
            carRepository.save(createCar("Model " + i, CarStatus.AVAILABLE));
        }
        
        var pageable = PageRequest.of(0, 10, Sort.by("modelName"));
        
        // when
        var page = carRepository.findAll(pageable);
        
        // then
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }
}
```

### Как это отразится в Git
```
test(repository): add comprehensive repository tests

- Added @DataJpaTest tests for all custom queries
- Tests use Testcontainers with real PostgreSQL
- Added tests for pagination and sorting
- Added tests for database constraints
- All repository methods covered

Closes #35
```

---

## 3.7. Contract Testing для API

### Конкретное действие
Внедрить Contract Testing для подготовки к микросервисам:
- Использовать Spring Cloud Contract или Pact
- Создать контракты для API endpoints
- Producer-side verification
- Consumer-driven contracts

### Что нужно изучить
- Contract Testing concept
- Spring Cloud Contract или Pact JVM
- Consumer-Driven Contracts pattern
- API versioning strategy

### Возможные сложности
- Новая библиотека и подход
- Контракты нужно поддерживать в актуальном состоянии
- Может быть избыточно для монолита, но готовит к микросервисам

### Как проверить результат
```groovy
// contracts/car/shouldReturnCarDetails.groovy
Contract.make {
    description "Should return car details by ID"
    request {
        method GET()
        url '/api/car/1'
        headers {
            accept(applicationJson())
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            id: 1,
            modelName: "Model S",
            pricePerDay: 100.0,
            status: "AVAILABLE",
            features: []
        ])
        bodyMatchers {
            jsonPath('$.id', byType())
            jsonPath('$.pricePerDay', byType())
            jsonPath('$.features', byType())
        }
    }
}
```

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-contract-maven-plugin</artifactId>
    <configuration>
        <baseClassForTests>org.example.carshering.BaseContractTest</baseClassForTests>
    </configuration>
</plugin>
```

Генерация и запуск тестов:
```bash
mvn clean install

# Контракты будут в target/stubs
ls target/stubs/

# Автосгенерированные тесты
ls target/generated-test-sources/contracts/
```

### Как это отразится в Git
```
test(contract): implement contract testing for API

- Added Spring Cloud Contract dependency
- Created contracts for all public API endpoints
- Producer-side verification passing
- Stubs published for future consumers
- Preparation for microservices decomposition

Refs: #microservices-prep
```

---

## 3.8. Performance тесты с JMeter/Gatling

### Конкретное действие
Создать базовые performance тесты:
- Load testing для основных endpoints
- Тестирование под нагрузкой (100-1000 RPS)
- Выявление bottlenecks
- Baseline metrics для будущих сравнений

### Что нужно изучить
- Gatling для Scala-based load testing
- JMeter (альтернатива)
- Performance testing metrics (throughput, latency, percentiles)
- Database connection pool tuning

### Возможные сложности
- Нужен отдельный environment для перф-тестов
- База данных должна быть заполнена реалистичными данными
- Результаты могут сильно зависеть от окружения

### Как проверить результат
```scala
// src/test/scala/simulations/CarCatalogSimulation.scala
class CarCatalogSimulation extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8082")
    .acceptHeader("application/json")
  
  val scn = scenario("Browse Car Catalog")
    .exec(
      http("Get All Cars")
        .get("/api/car")
        .queryParam("page", "0")
        .queryParam("size", "20")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get Car Details")
        .get("/api/car/#{carId}")
        .check(status.is(200))
    )
  
  setUp(
    scn.inject(
      rampUsers(100) during (30.seconds)
    ).protocols(httpProtocol)
  ).assertions(
    global.responseTime.max.lt(1000),
    global.successfulRequests.percent.gt(95)
  )
}
```

Запуск:
```bash
# С Gatling
mvn gatling:test

# Результаты в target/gatling/
open target/gatling/carcatalogsimulation-*/index.html

# Проверить метрики:
# - p95 latency < 500ms
# - p99 latency < 1000ms
# - throughput > 100 RPS
# - error rate < 1%
```

### Как это отразится в Git
```
test(perf): add performance tests with Gatling

- Implemented load tests for main API endpoints
- Tests simulate 100 concurrent users
- Established baseline: p95 < 500ms, p99 < 1000ms
- Performance report generated in target/gatling/
- Added to CI pipeline (optional run)

Closes #37
```

---

## 3.9. Mutation Testing с PITest

### Конкретное действие
Внедрить Mutation Testing для проверки качества тестов:
- Добавить PITest Maven Plugin
- Запустить mutation coverage analysis
- Проверить, что тесты действительно ловят баги (не просто покрытие)
- Цель: 70%+ mutation score

### Что нужно изучить
- Mutation Testing concept
- PITest configuration
- Mutation operators
- Interpreting mutation coverage reports

### Возможные сложности
- Mutation testing очень медленный (может занять 10-30 минут)
- Низкий mutation score на первом запуске — нормально
- Нужно исключить DTOs и конфигурацию

### Как проверить результат
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.3</version>
    <configuration>
        <targetClasses>
            <param>org.example.carshering.*.domain.*</param>
            <param>org.example.carshering.*.application.*</param>
        </targetClasses>
        <targetTests>
            <param>org.example.carshering.*Test</param>
        </targetTests>
        <excludedClasses>
            <param>*Application</param>
            <param>*.dto.*</param>
            <param>*.config.*</param>
        </excludedClasses>
        <outputFormats>
            <outputFormat>HTML</outputFormat>
            <outputFormat>XML</outputFormat>
        </outputFormats>
        <mutationThreshold>70</mutationThreshold>
    </configuration>
</plugin>
```

Запуск:
```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage

# Результаты
open target/pit-reports/index.html

# Проверить mutation score
# Должен быть > 70%
```

Пример улучшения теста на основе mutation report:
```java
// Тест убивает мутацию "заменить > на >="
@Test
void shouldRejectNegativePrice() {
    assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
        .isInstanceOf(IllegalArgumentException.class);
}

// После mutation testing - добавить граничный случай
@Test
void shouldRejectZeroPrice() {
    assertThatThrownBy(() -> new Money(BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### Как это отразится в Git
```
test(mutation): add mutation testing with PITest

- Added PITest Maven Plugin
- Configured mutation coverage for domain and application layers
- Initial mutation score: 72%
- Identified and fixed weak tests
- Mutation testing added to quality gates

Closes #38
```

---

## 3.10. Test Data Builders и фикстуры

### Конкретное действие
Создать утилиты для удобного создания тестовых данных:
- Object Mother pattern
- Test Data Builders
- Shared test fixtures
- Faker для генерации реалистичных данных

### Что нужно изучить
- Object Mother pattern
- Builder pattern для тестов
- JavaFaker library
- Test fixtures management

### Возможные сложности
- Builders могут стать сложными для больших объектов
- Нужно поддерживать в актуальном состоянии
- Баланс между реюзом и специфичностью тестов

### Как проверить результат
```java
// Object Mother
public class CarMother {
    public static Car defaultCar() {
        return Car.builder()
            .id(new CarId(1L))
            .modelName("Default Model")
            .status(CarStatus.AVAILABLE)
            .pricePerDay(new Money(new BigDecimal("100")))
            .build();
    }
    
    public static Car availableCar(String model, BigDecimal price) {
        return defaultCar().toBuilder()
            .modelName(model)
            .pricePerDay(new Money(price))
            .build();
    }
    
    public static Car rentedCar() {
        return defaultCar().toBuilder()
            .status(CarStatus.RENTED)
            .build();
    }
}

// Builder
public class CarTestDataBuilder {
    private CarId id = new CarId(1L);
    private String modelName = "Default Model";
    private CarStatus status = CarStatus.AVAILABLE;
    private Money price = new Money(new BigDecimal("100"));
    
    public CarTestDataBuilder withId(Long id) {
        this.id = new CarId(id);
        return this;
    }
    
    public CarTestDataBuilder withModel(String model) {
        this.modelName = model;
        return this;
    }
    
    public CarTestDataBuilder rented() {
        this.status = CarStatus.RENTED;
        return this;
    }
    
    public Car build() {
        return Car.builder()
            .id(id)
            .modelName(modelName)
            .status(status)
            .pricePerDay(price)
            .build();
    }
}

// Использование с Faker
public class TestDataGenerator {
    private static final Faker faker = new Faker();
    
    public static CreateCarRequest randomCarRequest() {
        return new CreateCarRequest(
            faker.car().model(),
            new BigDecimal(faker.number().numberBetween(50, 500)),
            faker.number().numberBetween(2015, 2024),
            faker.regexify("[A-Z0-9]{17}")
        );
    }
}

// В тесте
@Test
void shouldCreateCar() {
    var car = new CarTestDataBuilder()
        .withModel("Model S")
        .rented()
        .build();
    
    assertThat(car.getStatus()).isEqualTo(CarStatus.RENTED);
}
```

### Как это отразится в Git
```
test(fixtures): add test data builders and object mothers

- Implemented Object Mother pattern for domain entities
- Created Test Data Builders for flexible test data creation
- Added JavaFaker for realistic test data generation
- Centralized test fixtures in test-utils module
- Improved test readability and maintainability

Closes #39
```

---

## Чеклист выполнения Фазы 3

- [ ] 3.1. Все существующие тесты актуализированы
- [ ] 3.2. 100% coverage для Domain layer
- [ ] 3.3. Integration тесты для Application Services
- [ ] 3.4. API тесты с MockMvc для всех endpoints
- [ ] 3.5. Security тесты покрывают авторизацию
- [ ] 3.6. Repository тесты с реальной БД
- [ ] 3.7. Contract tests готовы к микросервисам
- [ ] 3.8. Performance baseline установлен
- [ ] 3.9. Mutation score > 70%
- [ ] 3.10. Test data builders внедрены

## Результат Фазы 3

✅ Полная пирамида тестирования (unit → integration → e2e)
✅ 80%+ code coverage с качественными тестами
✅ Mutation testing показывает качество тестов
✅ Contract tests готовы к декомпозиции
✅ Performance baseline для мониторинга деградации
✅ Тесты читаемые и поддерживаемые

**Следующая фаза:** Infrastructure — Docker, compose, оркестрация
