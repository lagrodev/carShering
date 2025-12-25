# Фаза 4: Инфраструктура и контейнеризация

**Цель фазы:** Упаковать приложение в Docker контейнеры, настроить полное окружение, подготовить к production deployment.

**Время выполнения:** 10-12 часов (6-8 задач по 1.5-2 часа)

**Приоритет:** ⭐⭐⭐ Критический (DevOps showcase)

---

## 4.1. Создание multi-stage Dockerfile

### Конкретное действие
Создать оптимизированный Dockerfile для Spring Boot приложения:
- Stage 1: Maven build (компиляция)
- Stage 2: Runtime (только JRE + jar)
- Использовать Eclipse Temurin JDK 21
- Минимизировать размер образа
- Non-root user для security

### Что нужно изучить
- Docker multi-stage builds
- Java container best practices
- Layer caching optimization
- Docker security (non-root user)

### Возможные сложности
- Maven dependencies кэширование (использовать dependency:go-offline)
- Правильный порядок COPY для максимального кэша
- Timezone в контейнере (может отличаться от хоста)

### Как проверить результат
```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Скачиваем зависимости (будет закэшировано)
RUN ./mvnw dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Создаём non-root пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Копируем только jar из builder stage
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

Сборка и проверка:
```bash
# Сборка
docker build -t car-sharing-backend:latest .

# Проверить размер образа (должен быть < 300MB)
docker images car-sharing-backend

# Запуск
docker run -d -p 8082:8082 --name backend car-sharing-backend:latest

# Проверка health
curl http://localhost:8082/actuator/health

# Проверить логи
docker logs backend

# Проверить, что работает от non-root
docker exec backend whoami
# Должен вывести: spring
```

### Как это отразится в Git
```
feat(docker): add optimized multi-stage Dockerfile

- Created multi-stage build for Spring Boot app
- Build stage: Maven compilation with dependency caching
- Runtime stage: minimal JRE image (< 300MB)
- Added non-root user for security
- Added health check
- JVM tuned for container environment

Closes #40
```

---

## 4.2. Docker Compose для полного окружения

### Конкретное действие
Создать production-ready docker-compose.yml:
- Backend приложение
- PostgreSQL с volume для persistence
- MinIO для хранения файлов
- Mailpit для email (dev)
- Nginx как reverse proxy (опционально)
- Health checks для всех сервисов
- Networks для изоляции

### Что нужно изучить
- Docker Compose v3+
- Docker networks
- Health checks в Compose
- Volumes и persistence
- Environment variables management

### Возможные сложности
- Порядок запуска (backend должен дождаться PostgreSQL)
- Environment variables для разных окружений
- Volumes permissions
- Network connectivity между контейнерами

### Как проверить результат
```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: car-sharing-db
    environment:
      POSTGRES_DB: car_rental
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-postgres}
    ports:
      - "${DB_PORT:-5433}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend-network

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: car-sharing-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/car_rental
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-postgres}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-postgres}
      SPRING_PROFILES_ACTIVE: ${PROFILE:-prod}
      MINIO_URL: http://minio:9000
      SPRING_MAIL_HOST: mailpit
    ports:
      - "${APP_PORT:-8082}:8082"
    depends_on:
      postgres:
        condition: service_healthy
      minio:
        condition: service_healthy
      mailpit:
        condition: service_started
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - backend-network

  minio:
    image: minio/minio:latest
    container_name: car-sharing-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-admin}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD:-admin12345}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
    networks:
      - backend-network

  mailpit:
    image: axllent/mailpit:latest
    container_name: car-sharing-mailpit
    ports:
      - "1025:1025"
      - "8025:8025"
    environment:
      MP_MAX_MESSAGES: 5000
      MP_DATABASE: /data/mailpit.db
    volumes:
      - mailpit_data:/data
    networks:
      - backend-network

volumes:
  postgres_data:
    driver: local
  minio_data:
    driver: local
  mailpit_data:
    driver: local

networks:
  backend-network:
    driver: bridge
```

`.env` файл:
```bash
# .env
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5433
APP_PORT=8082
PROFILE=prod
MINIO_USER=admin
MINIO_PASSWORD=admin12345
```

Запуск и проверка:
```bash
# Запуск одной командой
docker-compose up -d

# Проверить статус всех сервисов
docker-compose ps

# Проверить health
docker-compose ps | grep "(healthy)"

# Логи конкретного сервиса
docker-compose logs -f backend

# Проверить работу приложения
curl http://localhost:8082/actuator/health

# Проверить MinIO
open http://localhost:9001

# Проверить Mailpit
open http://localhost:8025

# Остановка
docker-compose down

# Остановка с удалением volumes
docker-compose down -v
```

### Как это отразится в Git
```
feat(docker): add production-ready docker-compose setup

- Created docker-compose.yml with all services
- PostgreSQL with persistent volume
- MinIO for file storage
- Mailpit for email testing
- Health checks for all services
- Network isolation
- Environment variables via .env file
- One-command startup: docker-compose up

Closes #41
```

---

## 4.3. Настройка Spring Profiles для разных окружений

### Конкретное действие
Создать профили для разных окружений:
- `application.yaml` — дефолтные настройки
- `application-dev.yaml` — локальная разработка
- `application-prod.yaml` — production
- `application-test.yaml` — для тестов
- Использовать environment variables для секретов

### Что нужно изучить
- Spring Profiles mechanism
- Externalized Configuration
- 12-Factor App principles
- Environment variables в Docker

### Возможные сложности
- Случайный commit секретов в git
- Profile не активируется правильно
- Конфликты между настройками разных профилей

### Как проверить результат
```yaml
# application.yaml (базовые настройки)
spring:
  application:
    name: car-sharing-backend
  
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate

server:
  port: 8082
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

---
# application-dev.yaml
spring:
  config:
    activate:
      on-profile: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5433/car_rental
    username: postgres
    password: postgres
  
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  mail:
    host: localhost
    port: 1025

logging:
  level:
    org.example.carshering: DEBUG
    org.hibernate.SQL: DEBUG

---
# application-prod.yaml
spring:
  config:
    activate:
      on-profile: prod
  
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    show-sql: false
  
  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}

logging:
  level:
    root: INFO
    org.example.carshering: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"

jwt:
  secret: ${JWT_SECRET}
  lifetime: ${JWT_LIFETIME:30m}

---
# application-test.yaml
spring:
  config:
    activate:
      on-profile: test
  
  jpa:
    show-sql: false
  
  flyway:
    enabled: true
    clean-disabled: false
```

Запуск с профилем:
```bash
# Dev
java -jar app.jar --spring.profiles.active=dev

# Prod
java -jar app.jar --spring.profiles.active=prod

# С Docker
docker run -e SPRING_PROFILES_ACTIVE=prod car-sharing-backend

# Проверка активного профиля
curl http://localhost:8082/actuator/info | jq .profiles
```

### Как это отразится в Git
```
feat(config): add Spring Profiles for different environments

- Created application-dev.yaml for local development
- Created application-prod.yaml for production
- Created application-test.yaml for testing
- Externalized secrets to environment variables
- Documented required env vars in README
- Updated docker-compose with profile support

Closes #42
```

---

## 4.4. Database migrations с Flyway в Docker

### Конкретное действие
Настроить автоматическое выполнение миграций при старте:
- Flyway baseline для существующей БД
- Миграции в Docker контейнере
- Rollback strategy
- Versioning strategy

### Что нужно изучить
- Flyway в production
- Database migration strategies
- Baseline existing database
- Migration rollback techniques

### Возможные сложности
- Первый запуск в production (базу нужно baseline)
- Миграции могут занимать время и блокировать старт
- Откат миграции (Flyway не поддерживает автоматический rollback)

### Как проверить результат
```yaml
# application-prod.yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    locations: classpath:db/migration
    table: flyway_schema_history
```

Команды:
```bash
# Проверить статус миграций
docker-compose exec backend ./mvnw flyway:info

# Выполнить миграции вручную (если нужно)
docker-compose exec backend ./mvnw flyway:migrate

# Проверить, что миграции выполнены
docker-compose exec postgres psql -U postgres -d car_rental -c \
  "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# Baseline для существующей БД
docker-compose exec backend ./mvnw flyway:baseline

# Validate миграции
docker-compose exec backend ./mvnw flyway:validate
```

Создание миграции для отката (manual):
```sql
-- V25_1_15__add_new_column.sql
ALTER TABLE cars ADD COLUMN new_column VARCHAR(255);

-- R25_1_15__rollback_add_new_column.sql (repeatable, ручной запуск при откате)
ALTER TABLE cars DROP COLUMN IF EXISTS new_column;
```

### Как это отразится в Git
```
feat(flyway): configure database migrations for production

- Flyway auto-runs on application startup
- Baseline strategy for existing databases
- Validated all 30+ existing migrations
- Created rollback scripts for critical migrations
- Documented migration workflow in README

Closes #43
```

---

## 4.5. Health Checks и Graceful Shutdown

### Конкретное действие
Настроить production-ready health checks:
- Actuator health endpoint с детализацией
- Liveness vs Readiness probes (Kubernetes-ready)
- Database health check
- Disk space health check
- Graceful shutdown для завершения requests

### Что нужно изучить
- Spring Boot Actuator Health Indicators
- Liveness vs Readiness probes
- Graceful shutdown в Spring Boot
- Kubernetes probes (для будущего)

### Возможные сложности
- Health check может быть слишком "тяжёлым" (слишком часто опрашивает БД)
- Graceful shutdown timeout нужно настроить правильно
- Различие между liveness и readiness

### Как проверить результат
```java
// Custom Health Indicator
@Component
public class DatabaseConnectionHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
        return Health.down().build();
    }
}
```

```yaml
# application.yaml
management:
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true  # Для Kubernetes
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,db,diskSpace

  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

Проверка:
```bash
# Health endpoint
curl http://localhost:8082/actuator/health | jq .

# Liveness probe
curl http://localhost:8082/actuator/health/liveness

# Readiness probe
curl http://localhost:8082/actuator/health/readiness

# Graceful shutdown test
docker-compose stop backend
# Проверить логи - должны показать graceful shutdown
docker-compose logs backend | grep "Graceful"
```

### Как это отразится в Git
```
feat(health): implement health checks and graceful shutdown

- Added custom health indicators (DB, disk space)
- Configured liveness and readiness probes
- Enabled graceful shutdown with 30s timeout
- Health endpoint shows detailed status
- Kubernetes-ready probe configuration

Closes #44
```

---

## 4.6. Логирование в JSON формате для aggregation

### Конкретное действие
Настроить структурированное логирование:
- JSON формат для production
- Logback configuration с профилями
- Correlation ID для трейсинга requests
- Логирование в stdout для Docker

### Что нужно изучить
- Logback configuration
- JSON logging (Logstash encoder)
- MDC (Mapped Diagnostic Context) для correlation ID
- Log aggregation best practices

### Возможные сложности
- JSON логи сложно читать локально (для dev нужен обычный формат)
- Performance impact от JSON serialization
- Правильная настройка log levels

### Как проверить результат
```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>
    
</configuration>
```

```java
// Request Filter для Correlation ID
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String correlationId = httpRequest.getHeader("X-Correlation-ID");
        
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("traceId", correlationId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

Проверка:
```bash
# Запуск с prod профилем
docker-compose up backend

# Логи должны быть в JSON
docker-compose logs backend | tail -n 1 | jq .

# Пример JSON лога
{
  "@timestamp": "2025-12-22T10:30:00.123Z",
  "level": "INFO",
  "logger_name": "org.example.carshering.service",
  "message": "Car created with ID: 123",
  "thread_name": "http-nio-8082-exec-1",
  "traceId": "abc-123-def",
  "userId": "user@example.com"
}

# Проверка correlation ID
curl -H "X-Correlation-ID: test-123" http://localhost:8082/api/car/1
# В логах должен быть traceId: test-123
```

Зависимости:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Как это отразится в Git
```
feat(logging): implement structured JSON logging

- Added Logstash encoder for JSON logs
- Configured Logback with dev/prod profiles
- Implemented correlation ID filter for request tracing
- MDC includes traceId, userId for context
- Logs to stdout for Docker/Kubernetes

Closes #45
```

---

## 4.7. Resource limits и JVM tuning

### Конкретное действие
Настроить ресурсы контейнера и JVM:
- Docker memory limits
- JVM heap size
- Container-aware JVM flags
- CPU limits
- Monitoring memory usage

### Что нужно изучить
- JVM container support (-XX:+UseContainerSupport)
- JVM memory flags (Xms, Xmx, MaxRAMPercentage)
- Docker resource constraints
- OOMKilled troubleshooting

### Возможные сложности
- Слишком жёсткие limits могут убивать контейнер
- Слишком мягкие limits не защищают хост
- JVM может не видеть container limits без правильных флагов

### Как проверить результат
```yaml
# docker-compose.yml
services:
  backend:
    # ...
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
        reservations:
          cpus: '1.0'
          memory: 512M
    environment:
      JAVA_OPTS: >
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -XX:InitialRAMPercentage=50.0
        -XX:+UseG1GC
        -XX:+HeapDumpOnOutOfMemoryError
        -XX:HeapDumpPath=/tmp/heapdump.hprof
        -Xlog:gc*:file=/tmp/gc.log:time,level,tags
```

```dockerfile
# Dockerfile ENTRYPOINT с tuning
ENTRYPOINT ["sh", "-c", "java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Djava.security.egd=file:/dev/./urandom \
  ${JAVA_OPTS} \
  -jar app.jar"]
```

Проверка:
```bash
# Проверить настройки JVM в контейнере
docker-compose exec backend java -XX:+PrintFlagsFinal -version | grep -i heap

# Проверить потребление памяти
docker stats backend

# Симулировать нагрузку и проверить GC
docker-compose exec backend jstat -gc 1 1000 10

# Проверить, что container limits видны
docker-compose exec backend cat /sys/fs/cgroup/memory/memory.limit_in_bytes
```

### Как это отразится в Git
```
feat(perf): configure JVM and Docker resource limits

- Set Docker memory limit: 1GB, CPU: 2 cores
- JVM uses 75% of container memory (750MB)
- G1GC configured for low-latency
- Heap dump on OOM for debugging
- GC logs enabled for monitoring

Closes #46
```

---

## 4.8. Backup и Restore стратегия

### Конкретное действие
Реализовать автоматический backup PostgreSQL:
- Скрипт для pg_dump
- Cron job в отдельном контейнере
- Ротация бэкапов (хранить 7 дней)
- Restore процедура
- Backup тестирование

### Что нужно изучить
- PostgreSQL pg_dump / pg_restore
- Docker volumes backup
- Cron в Docker
- Backup rotation strategies

### Возможные сложности
- Cron в Docker требует отдельного контейнера или сервиса
- Backup может занимать место
- Нужно тестировать restore регулярно

### Как проверить результат
```bash
# scripts/backup.sh
#!/bin/bash
set -e

BACKUP_DIR="/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.sql.gz"

# Выполнить backup
pg_dump -h postgres -U postgres car_rental | gzip > "$BACKUP_FILE"

# Удалить старые бэкапы (старше 7 дней)
find "$BACKUP_DIR" -name "backup_*.sql.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_FILE"
```

```bash
# scripts/restore.sh
#!/bin/bash
set -e

BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file>"
    exit 1
fi

gunzip < "$BACKUP_FILE" | psql -h postgres -U postgres car_rental

echo "Restore completed from: $BACKUP_FILE"
```

```yaml
# docker-compose.yml
services:
  backup:
    image: postgres:15-alpine
    container_name: car-sharing-backup
    environment:
      PGPASSWORD: postgres
    volumes:
      - ./scripts:/scripts
      - backup_data:/backups
    entrypoint: >
      sh -c "
      apk add --no-cache dcron &&
      echo '0 2 * * * /scripts/backup.sh' > /etc/crontabs/root &&
      crond -f -l 2
      "
    depends_on:
      - postgres
    networks:
      - backend-network

volumes:
  backup_data:
```

Проверка:
```bash
# Выполнить backup вручную
docker-compose exec backup /scripts/backup.sh

# Проверить бэкапы
docker-compose exec backup ls -lh /backups/

# Тест restore
docker-compose exec backup /scripts/restore.sh /backups/backup_20251222_100000.sql.gz

# Автоматический backup через cron (проверить логи)
docker-compose logs -f backup
```

### Как это отразится в Git
```
feat(backup): implement automated database backups

- Added backup container with pg_dump
- Scheduled daily backups at 2 AM via cron
- Backup rotation: keep 7 days
- Restore script for disaster recovery
- Backups stored in persistent volume

Closes #47
```

---

## Чеклист выполнения Фазы 4

- [ ] 4.1. Multi-stage Dockerfile оптимизирован
- [ ] 4.2. Docker Compose запускает всё окружение одной командой
- [ ] 4.3. Spring Profiles для dev/prod настроены
- [ ] 4.4. Flyway миграции работают в Docker
- [ ] 4.5. Health checks и graceful shutdown настроены
- [ ] 4.6. JSON логирование с correlation ID
- [ ] 4.7. Resource limits и JVM tuned
- [ ] 4.8. Автоматический backup БД

## Результат Фазы 4

✅ Production-ready Docker setup
✅ One-command startup (docker-compose up)
✅ Все зависимости контейнеризованы
✅ Health checks и monitoring готовы
✅ Structured logging для aggregation
✅ Автоматический backup данных
✅ Оптимизированные ресурсы

**Следующая фаза:** Security — защита приложения и данных
