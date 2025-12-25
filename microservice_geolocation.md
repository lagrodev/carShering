# Microservice: Location Service (Геолокационный сервис)

## Обзор

**Bounded Context:** Location  
**Назначение:** Управление геолокацией автомобилей, поиск ближайших доступных машин, расчёт расстояний  
**Архитектура:** Отдельный микросервис (Spring Boot) с собственной БД  
**Время реализации:** 15-20 часов

---

## 1. Зачем отдельный микросервис?

### Причины для выделения Location в микросервис:

1. **Performance-критичная задача:**
   - Spatial queries с PostGIS требуют специализированных индексов
   - Высокая нагрузка при поиске (каждый клик на карте = запрос)
   - Независимое масштабирование (больше реплик Location Service)

2. **Отдельная модель данных:**
   - Геолокация ≠ Fleet Management (разные bounded contexts)
   - Данные обновляются часто (GPS-трекеры каждые 30 сек)
   - Нужен Redis для кэширования координат

3. **Демонстрация навыков:**
   - Microservices decomposition
   - Event-driven architecture (CarMoved events)
   - Distributed tracing (Zipkin)
   - API Gateway pattern

---

## 2. Архитектура

### 2.1 Bounded Context: Location

**Aggregates:**
- `CarLocation` (AggregateRoot) — координаты автомобиля
- `SearchArea` (Value Object) — область поиска (lat, lon, radius)

**Value Objects:**
- `Coordinates` (latitude, longitude)
- `Distance` (meters)
- `Radius` (meters)

**Domain Events:**
- `CarLocationUpdatedEvent` (carId, coordinates, timestamp)
- `CarNearbySearchedEvent` (searchArea, resultsCount)

**Repositories:**
- `CarLocationRepository` (spatial queries)

---

### 2.2 Взаимодействие с основным приложением

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (опционально)               │
│                   или Spring Cloud Gateway                   │
└────────────┬──────────────────────────────┬─────────────────┘
             │                              │
             ▼                              ▼
┌────────────────────────┐      ┌──────────────────────────────┐
│   Fleet Service        │      │   Location Service           │
│   (основное приложение)│      │   (новый микросервис)        │
│                        │      │                              │
│ - CarDomain            │      │ - CarLocation                │
│ - Contract             │      │ - Coordinates                │
│ - RentalPeriod         │      │ - SearchArea                 │
│                        │      │                              │
│   PostgreSQL (main)    │      │   PostgreSQL + PostGIS       │
└────────┬───────────────┘      └────────┬─────────────────────┘
         │                               │
         │  Events (Kafka/RabbitMQ)      │
         │◄──────────────────────────────┤
         │                               │
         │  CarCreatedEvent              │
         │  CarStatusChangedEvent        │
         │                               │
         └───────────────────────────────►
                 CarLocationUpdatedEvent
```

---

## 3. Технический стек

### Backend:
- **Framework:** Spring Boot 3.5.6
- **Database:** PostgreSQL 16 + **PostGIS** (spatial extension)
- **Cache:** Redis 7.x (Redis Geospatial commands)
- **Messaging:** Kafka (или RabbitMQ для event-driven communication)
- **API:** REST + GraphQL (опционально для гибкости запросов)

### Infrastructure:
- **Orchestration:** Docker Compose (dev) → Kubernetes (production)
- **Observability:** Prometheus + Grafana + Zipkin
- **API Gateway:** Spring Cloud Gateway

---

## 4. Структура проекта

```
location-service/
├── pom.xml
├── docker-compose.yml
├── src/main/java/org/example/location/
│   ├── api/
│   │   ├── rest/
│   │   │   ├── LocationController.java
│   │   │   └── dto/
│   │   │       ├── CarLocationDto.java
│   │   │       ├── NearbySearchRequest.java
│   │   │       └── NearbySearchResponse.java
│   │   └── events/
│   │       ├── CarLocationUpdatedEvent.java
│   │       └── CarCreatedEventListener.java
│   ├── application/
│   │   ├── LocationApplicationService.java
│   │   └── usecase/
│   │       ├── UpdateCarLocationUseCase.java
│   │       └── FindNearbyCarsUseCase.java
│   ├── domain/
│   │   ├── aggregate/
│   │   │   └── CarLocation.java
│   │   ├── valueobject/
│   │   │   ├── CarId.java (shared)
│   │   │   ├── Coordinates.java
│   │   │   ├── Distance.java
│   │   │   └── SearchArea.java
│   │   ├── event/
│   │   │   ├── CarLocationUpdatedEvent.java
│   │   │   └── CarNearbySearchedEvent.java
│   │   └── repository/
│   │       └── CarLocationRepository.java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   │   │   └── CarLocationEntity.java
│   │   │   ├── repository/
│   │   │   │   └── JpaCarLocationRepository.java
│   │   │   └── mapper/
│   │   │       └── CarLocationMapper.java
│   │   ├── cache/
│   │   │   └── RedisLocationCache.java
│   │   ├── messaging/
│   │   │   ├── KafkaEventPublisher.java
│   │   │   └── KafkaEventListener.java
│   │   └── config/
│   │       ├── KafkaConfig.java
│   │       ├── RedisConfig.java
│   │       └── PostGISConfig.java
│   └── LocationServiceApplication.java
└── src/main/resources/
    ├── application.yml
    └── db/migration/
        └── V1__Create_car_location_table.sql
```

---

## 5. Domain Model

### 5.1 CarLocation (Aggregate Root)

```java
package org.example.location.domain.aggregate;

import org.example.location.domain.valueobject.CarId;
import org.example.location.domain.valueobject.Coordinates;
import org.example.location.domain.event.CarLocationUpdatedEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CarLocation {
    private final CarId carId;
    private Coordinates coordinates;
    private Instant lastUpdated;
    private boolean isActive; // доступна ли машина для аренды
    
    private final List<Object> domainEvents = new ArrayList<>();
    
    public CarLocation(CarId carId, Coordinates coordinates) {
        this.carId = carId;
        this.coordinates = coordinates;
        this.lastUpdated = Instant.now();
        this.isActive = true;
    }
    
    public void updateLocation(Coordinates newCoordinates) {
        if (!this.coordinates.equals(newCoordinates)) {
            this.coordinates = newCoordinates;
            this.lastUpdated = Instant.now();
            
            // Publish Domain Event
            domainEvents.add(new CarLocationUpdatedEvent(
                carId.getValue(),
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                lastUpdated
            ));
        }
    }
    
    public void markAsInactive() {
        this.isActive = false;
    }
    
    public void markAsActive() {
        this.isActive = true;
    }
    
    public List<Object> getDomainEvents() {
        return List.copyOf(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

---

### 5.2 Value Objects

#### Coordinates

```java
package org.example.location.domain.valueobject;

import lombok.Value;

@Value
public class Coordinates {
    double latitude;
    double longitude;
    
    public Coordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * Haversine formula для расчёта расстояния между точками
     */
    public Distance distanceTo(Coordinates other) {
        final int EARTH_RADIUS = 6371000; // meters
        
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(this.latitude)) * 
                   Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return new Distance((int) (EARTH_RADIUS * c));
    }
}
```

#### SearchArea

```java
package org.example.location.domain.valueobject;

import lombok.Value;

@Value
public class SearchArea {
    Coordinates center;
    Radius radius;
    
    public SearchArea(Coordinates center, Radius radius) {
        if (center == null) {
            throw new IllegalArgumentException("Center coordinates cannot be null");
        }
        if (radius == null || radius.getMeters() <= 0) {
            throw new IllegalArgumentException("Invalid radius");
        }
        this.center = center;
        this.radius = radius;
    }
}
```

#### Distance & Radius

```java
package org.example.location.domain.valueobject;

import lombok.Value;

@Value
public class Distance {
    int meters;
    
    public Distance(int meters) {
        if (meters < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        this.meters = meters;
    }
    
    public double toKilometers() {
        return meters / 1000.0;
    }
}

@Value
public class Radius {
    int meters;
    
    public Radius(int meters) {
        if (meters <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.meters = meters;
    }
    
    public static Radius ofKilometers(int km) {
        return new Radius(km * 1000);
    }
}
```

---

## 6. Infrastructure: PostGIS

### 6.1 Database Migration (Flyway)

**V1__Create_car_location_table.sql:**

```sql
-- Включаем расширение PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE car_location (
    car_id UUID PRIMARY KEY,
    coordinates GEOGRAPHY(POINT, 4326) NOT NULL, -- PostGIS spatial type
    last_updated TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Spatial index для быстрых geo-запросов
    CONSTRAINT valid_coordinates CHECK (
        ST_X(coordinates::geometry) BETWEEN -180 AND 180 AND
        ST_Y(coordinates::geometry) BETWEEN -90 AND 90
    )
);

-- GIST индекс для spatial queries (обязательно!)
CREATE INDEX idx_car_location_coordinates ON car_location USING GIST(coordinates);

-- Обычный индекс для фильтрации по is_active
CREATE INDEX idx_car_location_active ON car_location(is_active) WHERE is_active = true;
```

---

### 6.2 JPA Entity

```java
package org.example.location.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "car_location")
@Getter
@Setter
public class CarLocationEntity {
    
    @Id
    @Column(name = "car_id")
    private UUID carId;
    
    @Column(name = "coordinates", columnDefinition = "geography(Point, 4326)")
    private Point coordinates;
    
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    // Вспомогательные методы для работы с координатами
    public void setCoordinates(double latitude, double longitude) {
        GeometryFactory factory = new GeometryFactory();
        this.coordinates = factory.createPoint(new Coordinate(longitude, latitude));
    }
    
    public double getLatitude() {
        return coordinates.getY();
    }
    
    public double getLongitude() {
        return coordinates.getX();
    }
}
```

---

### 6.3 Repository с Spatial Queries

```java
package org.example.location.infrastructure.persistence.repository;

import org.example.location.infrastructure.persistence.entity.CarLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaCarLocationRepository extends JpaRepository<CarLocationEntity, UUID> {
    
    /**
     * Поиск машин в радиусе от точки (PostGIS ST_DWithin)
     * 
     * @param latitude широта точки поиска
     * @param longitude долгота точки поиска
     * @param radiusMeters радиус поиска в метрах
     * @return список машин в радиусе, отсортированных по расстоянию
     */
    @Query(value = """
        SELECT c.*, 
               ST_Distance(c.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) as distance
        FROM car_location c
        WHERE c.is_active = true
          AND ST_DWithin(
              c.coordinates, 
              ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
              :radiusMeters
          )
        ORDER BY distance
        """, nativeQuery = true)
    List<CarLocationEntity> findNearbyCars(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );
    
    /**
     * Найти N ближайших машин (KNN search)
     */
    @Query(value = """
        SELECT c.*
        FROM car_location c
        WHERE c.is_active = true
        ORDER BY c.coordinates <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)
        LIMIT :limit
        """, nativeQuery = true)
    List<CarLocationEntity> findNearestCars(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("limit") int limit
    );
}
```

---

## 7. Redis Caching (опционально, но рекомендуется)

### 7.1 Redis Geospatial Commands

Redis поддерживает встроенные команды для работы с геолокацией:

```java
package org.example.location.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisLocationCache {
    
    private static final String CACHE_KEY = "car:locations";
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Добавить/обновить координаты машины в Redis
     */
    public void updateCarLocation(UUID carId, double latitude, double longitude) {
        Point point = new Point(longitude, latitude);
        redisTemplate.opsForGeo().add(CACHE_KEY, point, carId.toString());
    }
    
    /**
     * Найти машины в радиусе (Redis GEORADIUS)
     */
    public List<String> findNearbyCars(double latitude, double longitude, double radiusKm) {
        Circle circle = new Circle(
            new Point(longitude, latitude),
            new Distance(radiusKm, Metrics.KILOMETERS)
        );
        
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = 
            redisTemplate.opsForGeo().radius(CACHE_KEY, circle);
        
        return results.getContent().stream()
            .map(result -> result.getContent().getName())
            .toList();
    }
    
    /**
     * Удалить машину из кэша (когда машина неактивна)
     */
    public void removeCarLocation(UUID carId) {
        redisTemplate.opsForGeo().remove(CACHE_KEY, carId.toString());
    }
}
```

---

### 7.2 Стратегия кэширования

**Cache-Aside Pattern:**
1. **Read:** Проверить Redis → если нет → запрос к PostGIS → сохранить в Redis
2. **Write:** Обновить PostGIS → обновить Redis
3. **TTL:** Координаты автомобилей кэшировать на 5 минут (GPS может запаздывать)

```java
@Service
@RequiredArgsConstructor
public class LocationApplicationService {
    
    private final CarLocationRepository repository;
    private final RedisLocationCache cache;
    
    public List<CarLocationDto> findNearbyCars(SearchArea searchArea) {
        // 1. Попытка получить из кэша
        List<String> cachedCarIds = cache.findNearbyCars(
            searchArea.getCenter().getLatitude(),
            searchArea.getCenter().getLongitude(),
            searchArea.getRadius().getMeters() / 1000.0
        );
        
        if (!cachedCarIds.isEmpty()) {
            return fetchCarDetails(cachedCarIds);
        }
        
        // 2. Запрос к PostGIS
        List<CarLocation> locations = repository.findNearby(searchArea);
        
        // 3. Обновить кэш
        locations.forEach(loc -> cache.updateCarLocation(
            loc.getCarId().getValue(),
            loc.getCoordinates().getLatitude(),
            loc.getCoordinates().getLongitude()
        ));
        
        return mapToDto(locations);
    }
}
```

---

## 8. API Endpoints

### 8.1 REST Controller

```java
package org.example.location.api.rest;

import lombok.RequiredArgsConstructor;
import org.example.location.api.rest.dto.*;
import org.example.location.application.LocationApplicationService;
import org.example.location.domain.valueobject.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationController {
    
    private final LocationApplicationService service;
    
    /**
     * Найти машины рядом с пользователем
     * 
     * GET /api/v1/location/nearby?lat=50.4501&lon=30.5234&radius=5000
     */
    @GetMapping("/nearby")
    public ResponseEntity<NearbySearchResponse> findNearbyCars(
        @RequestParam double lat,
        @RequestParam double lon,
        @RequestParam(defaultValue = "5000") int radius
    ) {
        SearchArea searchArea = new SearchArea(
            new Coordinates(lat, lon),
            new Radius(radius)
        );
        
        List<CarLocationDto> cars = service.findNearbyCars(searchArea);
        
        return ResponseEntity.ok(new NearbySearchResponse(
            cars.size(),
            radius,
            cars
        ));
    }
    
    /**
     * Обновить координаты машины (для GPS-трекеров)
     * 
     * PUT /api/v1/location/{carId}
     */
    @PutMapping("/{carId}")
    public ResponseEntity<Void> updateCarLocation(
        @PathVariable UUID carId,
        @RequestBody UpdateLocationRequest request
    ) {
        service.updateCarLocation(
            new CarId(carId),
            new Coordinates(request.latitude(), request.longitude())
        );
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Получить текущие координаты машины
     * 
     * GET /api/v1/location/{carId}
     */
    @GetMapping("/{carId}")
    public ResponseEntity<CarLocationDto> getCarLocation(@PathVariable UUID carId) {
        CarLocationDto location = service.getCarLocation(new CarId(carId));
        return ResponseEntity.ok(location);
    }
}
```

---

### 8.2 DTO

```java
package org.example.location.api.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CarLocationDto(
    UUID carId,
    double latitude,
    double longitude,
    Instant lastUpdated,
    boolean isActive,
    Integer distanceMeters // null если не в контексте поиска
) {}

public record NearbySearchResponse(
    int totalFound,
    int searchRadiusMeters,
    List<CarLocationDto> cars
) {}

public record UpdateLocationRequest(
    double latitude,
    double longitude
) {}
```

---

## 9. Event-Driven Integration

### 9.1 Kafka Events

**CarCreatedEvent** (Fleet Service → Location Service):

```java
package org.example.location.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.example.location.application.LocationApplicationService;
import org.example.location.domain.valueobject.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    
    private final LocationApplicationService service;
    
    /**
     * Слушаем событие создания машины из Fleet Service
     */
    @KafkaListener(topics = "fleet.car.created", groupId = "location-service")
    public void handleCarCreated(CarCreatedEvent event) {
        service.createCarLocation(
            new CarId(event.carId()),
            new Coordinates(event.initialLatitude(), event.initialLongitude())
        );
    }
    
    /**
     * Слушаем изменение статуса машины (AVAILABLE/RENTED)
     */
    @KafkaListener(topics = "fleet.car.status-changed", groupId = "location-service")
    public void handleCarStatusChanged(CarStatusChangedEvent event) {
        if (event.newStatus().equals("RENTED")) {
            service.markCarAsInactive(new CarId(event.carId()));
        } else if (event.newStatus().equals("AVAILABLE")) {
            service.markCarAsActive(new CarId(event.carId()));
        }
    }
}
```

**Event Schema (JSON):**

```json
// fleet.car.created
{
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "initialLatitude": 50.4501,
  "initialLongitude": 30.5234,
  "timestamp": "2025-12-25T12:00:00Z"
}

// fleet.car.status-changed
{
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "oldStatus": "AVAILABLE",
  "newStatus": "RENTED",
  "timestamp": "2025-12-25T12:30:00Z"
}
```

---

### 9.2 Publishing Events from Location Service

```java
package org.example.location.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.example.location.domain.event.CarLocationUpdatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishCarLocationUpdated(CarLocationUpdatedEvent event) {
        kafkaTemplate.send("location.car.updated", event.getCarId().toString(), event);
    }
}
```

---

## 10. Docker Compose

### 10.1 docker-compose.yml для Location Service

```yaml
version: '3.8'

services:
  # PostgreSQL с PostGIS
  location-db:
    image: postgis/postgis:16-3.4
    container_name: location-postgres
    environment:
      POSTGRES_DB: location_db
      POSTGRES_USER: location_user
      POSTGRES_PASSWORD: location_pass
    ports:
      - "5433:5432"
    volumes:
      - location-db-data:/var/lib/postgresql/data
    networks:
      - carsharing-network

  # Redis для кэширования
  location-redis:
    image: redis:7-alpine
    container_name: location-redis
    ports:
      - "6380:6379"
    networks:
      - carsharing-network

  # Kafka (shared с Fleet Service)
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    networks:
      - carsharing-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"
    networks:
      - carsharing-network

  # Location Service
  location-service:
    build: .
    container_name: location-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://location-db:5432/location_db
      SPRING_DATASOURCE_USERNAME: location_user
      SPRING_DATASOURCE_PASSWORD: location_pass
      SPRING_REDIS_HOST: location-redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8081:8080"
    depends_on:
      - location-db
      - location-redis
      - kafka
    networks:
      - carsharing-network

networks:
  carsharing-network:
    driver: bridge

volumes:
  location-db-data:
```

---

## 11. Configuration (application.yml)

```yaml
spring:
  application:
    name: location-service
    
  datasource:
    url: jdbc:postgresql://localhost:5433/location_db
    username: location_user
    password: location_pass
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect
        
  flyway:
    enabled: true
    baseline-on-migrate: true
    
  redis:
    host: localhost
    port: 6380
    
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: location-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

# Настройки кэширования
location:
  cache:
    ttl-minutes: 5
  search:
    max-radius-meters: 50000 # максимальный радиус поиска 50 км
    default-radius-meters: 5000

# Observability (для Phase 7)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0 # 100% для dev, 0.1 для production
```

---

## 12. Тестирование

### 12.1 Integration Test (Testcontainers + PostGIS)

```java
package org.example.location.infrastructure.persistence;

import org.example.location.infrastructure.persistence.entity.CarLocationEntity;
import org.example.location.infrastructure.persistence.repository.JpaCarLocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpatialQueryTest {
    
    @Container
    static PostgisContainer<?> postgis = new PostgisContainer<>("postgis/postgis:16-3.4");
    
    @Autowired
    private JpaCarLocationRepository repository;
    
    @Test
    void shouldFindCarsWithinRadius() {
        // Given: 3 машины в разных точках Киева
        CarLocationEntity car1 = createCar(50.4501, 30.5234); // центр Киева
        CarLocationEntity car2 = createCar(50.4601, 30.5334); // ~1.5 км от центра
        CarLocationEntity car3 = createCar(50.5501, 30.6234); // ~15 км от центра
        
        repository.saveAll(List.of(car1, car2, car3));
        
        // When: ищем машины в радиусе 5 км от центра
        List<CarLocationEntity> result = repository.findNearbyCars(
            50.4501, 30.5234, 5000
        );
        
        // Then: должны найти только car1 и car2
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CarLocationEntity::getCarId)
            .containsExactlyInAnyOrder(car1.getCarId(), car2.getCarId());
    }
    
    private CarLocationEntity createCar(double lat, double lon) {
        CarLocationEntity car = new CarLocationEntity();
        car.setCarId(UUID.randomUUID());
        car.setCoordinates(lat, lon);
        car.setLastUpdated(Instant.now());
        car.setActive(true);
        return car;
    }
}
```

---

### 12.2 Unit Test для Coordinates

```java
package org.example.location.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CoordinatesTest {
    
    @Test
    void shouldCalculateDistanceCorrectly() {
        // Расстояние между Киевом и Львовом ~469 км
        Coordinates kyiv = new Coordinates(50.4501, 30.5234);
        Coordinates lviv = new Coordinates(49.8397, 24.0297);
        
        Distance distance = kyiv.distanceTo(lviv);
        
        assertThat(distance.toKilometers())
            .isCloseTo(469.0, within(5.0)); // погрешность 5 км
    }
    
    @Test
    void shouldRejectInvalidLatitude() {
        assertThatThrownBy(() -> new Coordinates(91.0, 30.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid latitude");
    }
}
```

---

## 13. Метрики для мониторинга

### 13.1 Custom Metrics (Micrometer)

```java
package org.example.location.application;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationApplicationService {
    
    private final CarLocationRepository repository;
    private final MeterRegistry meterRegistry;
    
    public List<CarLocationDto> findNearbyCars(SearchArea searchArea) {
        // Метрика времени выполнения запроса
        return Timer.builder("location.search.duration")
            .tag("radius", String.valueOf(searchArea.getRadius().getMeters()))
            .register(meterRegistry)
            .record(() -> {
                List<CarLocation> locations = repository.findNearby(searchArea);
                
                // Метрика количества найденных машин
                meterRegistry.counter("location.search.results",
                    "count", String.valueOf(locations.size())
                ).increment();
                
                return mapToDto(locations);
            });
    }
}
```

---

### 13.2 Prometheus Queries

```promql
# Среднее время поиска машин за последние 5 минут
rate(location_search_duration_sum[5m]) / rate(location_search_duration_count[5m])

# Количество запросов в секунду
rate(location_search_results_total[1m])

# 95-й перцентиль латентности
histogram_quantile(0.95, location_search_duration_bucket)
```

---

## 14. Roadmap реализации

### Этап 1: Базовая инфраструктура (4-5 часов)
- [x] Создать новый Spring Boot проект
- [x] Настроить PostgreSQL + PostGIS в Docker Compose
- [x] Настроить Flyway migrations
- [x] Создать базовую структуру DDD (domain, infrastructure)

### Этап 2: Domain Model (3-4 часа)
- [x] Создать Value Objects (Coordinates, Distance, SearchArea)
- [x] Создать Aggregate (CarLocation)
- [x] Создать Domain Events
- [x] Unit-тесты для Value Objects

### Этап 3: Persistence (3-4 часа)
- [x] JPA Entity с PostGIS
- [x] Repository с spatial queries
- [x] Integration-тесты с Testcontainers

### Этап 4: API (2-3 часа)
- [x] REST Controller
- [x] DTO mapping
- [x] Validation

### Этап 5: Redis Caching (2-3 часа)
- [x] Redis Geospatial integration
- [x] Cache-Aside pattern
- [x] TTL configuration

### Этап 6: Event-Driven Integration (3-4 часа)
- [x] Kafka listeners (CarCreatedEvent, CarStatusChangedEvent)
- [x] Event publishers (CarLocationUpdatedEvent)
- [x] Integration с Fleet Service

### Этап 7: Observability (реализовать вместе с Phase 7)
- [ ] Prometheus metrics
- [ ] Zipkin distributed tracing
- [ ] Grafana dashboards

---

## 15. Продвинутые фичи (опционально)

### 15.1 GraphQL API

```graphql
type Query {
  nearbyCars(
    latitude: Float!
    longitude: Float!
    radiusMeters: Int = 5000
    limit: Int = 20
  ): [CarLocation!]!
  
  carLocation(carId: ID!): CarLocation
}

type CarLocation {
  carId: ID!
  coordinates: Coordinates!
  lastUpdated: DateTime!
  distance: Distance
}

type Coordinates {
  latitude: Float!
  longitude: Float!
}

type Distance {
  meters: Int!
  kilometers: Float!
}
```

---

### 15.2 WebSocket для Real-Time Updates

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/location").withSockJS();
    }
}

// Client subscribes:
// /topic/location/{carId} -> получает обновления координат в реальном времени
```

---

### 15.3 Route Planning (будущее расширение)

```java
public interface RouteService {
    /**
     * Построить маршрут от точки A до точки B
     * Использует external API (Google Maps, OpenStreetMap)
     */
    Route calculateRoute(Coordinates from, Coordinates to);
    
    /**
     * Рассчитать стоимость поездки на основе маршрута
     */
    Money estimateTripCost(Route route, CarId carId);
}
```

---

## 16. Выводы

### Что демонстрирует Location Service:

✅ **Microservices Architecture:**
- Bounded Context выделен в отдельный сервис
- Event-driven communication (Kafka)
- Independent deployment

✅ **Domain-Driven Design:**
- Aggregate (CarLocation)
- Value Objects (Coordinates, Distance)
- Domain Events

✅ **Performance Optimization:**
- PostGIS spatial indexes
- Redis Geospatial caching
- Efficient queries (ST_DWithin, KNN)

✅ **Production-Ready:**
- Observability (Prometheus, Zipkin)
- Docker Compose infrastructure
- Testcontainers для integration tests

✅ **Backend Skills:**
- PostgreSQL extensions (PostGIS)
- Redis advanced features
- Kafka event streaming

---

### Интеграция с основным приложением:

```
Fleet Service (8080) ──► Location Service (8081)
                         ▲
                         │
                     Kafka Events
                         │
Identity Service ────────┘
```

**API Gateway (опционально):**
```
Client → Gateway (80) ──┬──► Fleet Service (8080)
                        ├──► Location Service (8081)
                        └──► Identity Service (8082)
```

---

## Следующие шаги:

1. **Реализовать Phase 1 + Phase 6** (основное приложение)
2. **Создать Location Service** (этот план)
3. **Реализовать Phase 7** (Observability для обоих сервисов)
4. **Добавить API Gateway** (Spring Cloud Gateway)
5. **Kubernetes deployment** (опционально)

**Общее время:** Phase 1 (14h) + Phase 6 (8h) + Location Service (20h) = **~42 часа** → отличный pet-project для портфолио!
