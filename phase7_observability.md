# Фаза 7: Observability (Мониторинг и трейсинг)

**Цель фазы:** Внедрить полный observability stack (logs, metrics, traces) для production monitoring.

**Время выполнения:** 8-10 часов (6-7 задач по 1-1.5 часа)

**Приоритет:** ⭐⭐ Высокий (DevOps showcase)

---

## 7.1. Prometheus Metrics с Micrometer

### Конкретное действие
Настроить экспорт метрик в формате Prometheus:
- Spring Boot Actuator metrics endpoint
- Micrometer registry для Prometheus
- Custom metrics для бизнес-метрик
- JVM metrics, HTTP metrics, DB metrics

### Что нужно изучить
- Micrometer metrics library
- Prometheus data model
- Spring Boot Actuator metrics
- Custom metrics (@Timed, Counter, Gauge)

### Возможные сложности
- Слишком много метрик (cardinality explosion)
- Naming conventions для метрик
- Performance impact

### Как проверить результат
```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:dev}
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

```java
// Custom business metrics
@Component
public class ContractMetrics {
    
    private final Counter contractsCreated;
    private final Counter contractsCompleted;
    private final Gauge activeContracts;
    
    public ContractMetrics(MeterRegistry registry, ContractRepository contractRepo) {
        this.contractsCreated = Counter.builder("contracts.created")
            .description("Total number of contracts created")
            .tag("type", "rental")
            .register(registry);
        
        this.contractsCompleted = Counter.builder("contracts.completed")
            .description("Total number of contracts completed")
            .register(registry);
        
        this.activeContracts = Gauge.builder("contracts.active", contractRepo,
                repo -> repo.countByStatus(ContractStatus.ACTIVE))
            .description("Number of currently active contracts")
            .register(registry);
    }
    
    public void incrementContractsCreated() {
        contractsCreated.increment();
    }
    
    public void incrementContractsCompleted() {
        contractsCompleted.increment();
    }
}

// В сервисе
@Service
public class ContractApplicationService {
    
    private final ContractMetrics metrics;
    
    @Timed(value = "contract.create", description = "Time to create a contract")
    public ContractId createContract(CreateContractRequest request) {
        // ... logic
        metrics.incrementContractsCreated();
        return contractId;
    }
}
```

Проверка:
```bash
# Проверить metrics endpoint
curl http://localhost:8082/actuator/prometheus

# Должны быть метрики:
# - jvm_memory_used_bytes
# - http_server_requests_seconds
# - contracts_created_total
# - contracts_active

# Проверить конкретную метрику
curl http://localhost:8082/actuator/metrics/contracts.created | jq .
```

### Как это отразится в Git
```
feat(observability): add Prometheus metrics

- Configured Micrometer Prometheus registry
- Exposed /actuator/prometheus endpoint
- Added custom business metrics (contracts created/active)
- JVM, HTTP, and DB metrics enabled
- @Timed annotation for method duration tracking

Closes #70
```

---

## 7.2. Prometheus + Grafana Stack

### Конкретное действие
Поднять Prometheus и Grafana для сбора и визуализации метрик:
- Prometheus в Docker Compose
- Scraping metrics из приложения
- Grafana для dashboards
- Алерты (опционально)

### Что нужно изучить
- Prometheus configuration
- PromQL query language
- Grafana dashboards
- Alerting rules

### Возможные сложности
- Prometheus не видит targets (network issues)
- Retention policy (сколько хранить метрики)
- Dashboard design

### Как проверить результат
```yaml
# docker-compose.yml (добавить)
  prometheus:
    image: prom/prometheus:latest
    container_name: car-sharing-prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - backend-network
  
  grafana:
    image: grafana/grafana:latest
    container_name: car-sharing-grafana
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./grafana/datasources:/etc/grafana/provisioning/datasources
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
    networks:
      - backend-network

volumes:
  prometheus_data:
  grafana_data:
```

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8082']
        labels:
          application: 'car-sharing'
          environment: 'prod'
```

```yaml
# grafana/datasources/prometheus.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
```

Grafana Dashboard JSON (базовый):
```json
{
  "dashboard": {
    "title": "Car Sharing Metrics",
    "panels": [
      {
        "title": "HTTP Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Active Contracts",
        "targets": [
          {
            "expr": "contracts_active"
          }
        ]
      }
    ]
  }
}
```

Проверка:
```bash
# Запустить весь stack
docker-compose up -d

# Проверить Prometheus
open http://localhost:9090
# Targets → должен быть зелёный spring-boot-app

# Проверить Grafana
open http://localhost:3000
# Login: admin/admin
# Datasources → Prometheus должен быть активен
# Create Dashboard → Add panel
# Query: rate(http_server_requests_seconds_count[5m])
```

### Как это отразится в Git
```
feat(observability): add Prometheus and Grafana

- Added Prometheus to Docker Compose
- Configured scraping from Spring Boot app
- Added Grafana with pre-configured datasource
- Created dashboard for HTTP and business metrics
- Retention: 30 days

Closes #71
```

---

## 7.3. Distributed Tracing с OpenTelemetry

### Конкретное действие
Внедрить distributed tracing для отслеживания requests:
- OpenTelemetry Java Agent
- Jaeger для UI
- Trace context propagation
- Span annotations для бизнес-операций

### Что нужно изучить
- OpenTelemetry concepts (traces, spans)
- Jaeger backend
- Context propagation
- Trace sampling strategies

### Возможные сложности
- Performance overhead
- Trace sampling (не все requests трейсить)
- Корреляция с logs

### Как проверить результат
```yaml
# docker-compose.yml (добавить Jaeger)
  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: car-sharing-jaeger
    environment:
      - COLLECTOR_ZIPKIN_HOST_PORT=:9411
    ports:
      - "5775:5775/udp"
      - "6831:6831/udp"
      - "6832:6832/udp"
      - "5778:5778"
      - "16686:16686"  # Jaeger UI
      - "14268:14268"
      - "14250:14250"
      - "9411:9411"
    networks:
      - backend-network
  
  backend:
    # ...
    environment:
      - OTEL_SERVICE_NAME=car-sharing-backend
      - OTEL_TRACES_EXPORTER=jaeger
      - OTEL_EXPORTER_JAEGER_ENDPOINT=http://jaeger:14250
      - OTEL_METRICS_EXPORTER=none
```

```dockerfile
# Dockerfile (добавить OpenTelemetry agent)
FROM eclipse-temurin:21-jre-alpine

# Download OpenTelemetry Java Agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", \
  "-javaagent:/app/opentelemetry-javaagent.jar", \
  "-jar", "app.jar"]
```

Custom spans:
```java
@Service
public class ContractApplicationService {
    
    @WithSpan("create-contract")  // OpenTelemetry annotation
    public ContractId createContract(CreateContractRequest request) {
        var span = Span.current();
        span.setAttribute("contract.car.id", request.carId());
        span.setAttribute("contract.client.id", request.clientId());
        
        // ... logic
        
        span.addEvent("contract.created");
        return contractId;
    }
}
```

Проверка:
```bash
# Запустить с Jaeger
docker-compose up -d

# Сделать несколько requests
curl http://localhost:8082/api/car/1
curl http://localhost:8082/api/contracts

# Открыть Jaeger UI
open http://localhost:16686

# Service: car-sharing-backend
# Operation: GET /api/car/{id}
# Find Traces → должны быть traces
```

### Как это отразится в Git
```
feat(observability): add distributed tracing with OpenTelemetry

- Added OpenTelemetry Java Agent to Docker image
- Configured Jaeger backend for trace collection
- Custom spans for business operations
- Trace attributes include contract and car IDs
- Jaeger UI available at :16686

Closes #72
```

---

## 7.4. Centralized Logging с ELK Stack (опционально: Loki)

### Конкретное действие
Настроить централизованное логирование:
- JSON logs из приложения
- Filebeat или Fluentd для сбора логов
- Elasticsearch для хранения
- Kibana для визуализации
- (Альтернатива: Loki + Promtail)

### Что нужно изучить
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Log aggregation patterns
- Loki как lightweight альтернатива
- Log retention policies

### Возможные сложности
- ELK Stack тяжёлый (много RAM)
- Elasticsearch кластер для production
- Log volume может быть большим

### Как проверить результат

**Вариант 1: Loki (легковеснее)**
```yaml
# docker-compose.yml
  loki:
    image: grafana/loki:latest
    container_name: car-sharing-loki
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    networks:
      - backend-network
  
  promtail:
    image: grafana/promtail:latest
    container_name: car-sharing-promtail
    volumes:
      - /var/log:/var/log
      - ./promtail-config.yml:/etc/promtail/config.yml
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
    command: -config.file=/etc/promtail/config.yml
    depends_on:
      - loki
    networks:
      - backend-network
  
  grafana:
    # ... добавить Loki datasource
    environment:
      - GF_INSTALL_PLUGINS=grafana-loki-datasource
```

```yaml
# promtail-config.yml
server:
  http_listen_port: 9080

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    static_configs:
      - targets:
          - localhost
        labels:
          job: dockerlogs
          __path__: /var/lib/docker/containers/*/*-json.log
    
    pipeline_stages:
      - json:
          expressions:
            stream: stream
            log: log
      - labels:
          stream:
```

**Вариант 2: ELK Stack**
```yaml
# docker-compose.yml
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: car-sharing-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - backend-network
  
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: car-sharing-kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - backend-network
  
  filebeat:
    image: docker.elastic.co/beats/filebeat:8.11.0
    container_name: car-sharing-filebeat
    user: root
    volumes:
      - ./filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    depends_on:
      - elasticsearch
    networks:
      - backend-network
```

Проверка:
```bash
# С Loki
open http://localhost:3000
# Grafana → Explore → Loki datasource
# Query: {job="dockerlogs"}

# С ELK
open http://localhost:5601
# Kibana → Discover → создать index pattern
# Logs должны быть видны
```

### Как это отразится в Git
```
feat(observability): add centralized logging with Loki

- Added Loki for log aggregation
- Configured Promtail to collect Docker logs
- JSON logs from application parsed automatically
- Logs queryable in Grafana
- Lightweight alternative to ELK stack

Closes #73
```

---

## 7.5. Application Performance Monitoring (APM)

### Конкретное действие
Внедрить APM для мониторинга производительности:
- Slow query detection
- Transaction tracing
- Error tracking
- Database query analysis

(Elastic APM или New Relic, или Datadog — для demo можно Elastic APM)

### Что нужно изучить
- APM concepts
- Elastic APM Java Agent
- Transaction sampling
- Performance bottleneck identification

### Возможные сложности
- Performance overhead от APM agent
- Commercial APM solutions ($$)
- Настройка sampling

### Как проверить результат
```yaml
# docker-compose.yml
  apm-server:
    image: docker.elastic.co/apm/apm-server:8.11.0
    container_name: car-sharing-apm-server
    ports:
      - "8200:8200"
    environment:
      - output.elasticsearch.hosts=["elasticsearch:9200"]
    depends_on:
      - elasticsearch
    networks:
      - backend-network
  
  backend:
    environment:
      - ELASTIC_APM_SERVICE_NAME=car-sharing-backend
      - ELASTIC_APM_SERVER_URL=http://apm-server:8200
      - ELASTIC_APM_ENVIRONMENT=production
      - ELASTIC_APM_APPLICATION_PACKAGES=org.example.carshering
```

```dockerfile
# Dockerfile
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.43.0/elastic-apm-agent-1.43.0.jar /app/elastic-apm-agent.jar

ENTRYPOINT ["java", \
  "-javaagent:/app/elastic-apm-agent.jar", \
  "-jar", "app.jar"]
```

Проверка:
```bash
# Сделать несколько запросов
for i in {1..100}; do
  curl http://localhost:8082/api/car/$((RANDOM % 100 + 1))
done

# Открыть Kibana APM
open http://localhost:5601/app/apm

# Должны быть:
# - Transaction timeline
# - Slow queries
# - Errors (если были)
# - Database queries
```

### Как это отразится в Git
```
feat(observability): add Elastic APM for performance monitoring

- Added Elastic APM Server to stack
- Configured Java APM agent
- Transaction tracking for all endpoints
- Slow query detection
- Error tracking and analysis
- APM dashboard in Kibana

Closes #74
```

---

## 7.6. Alerting с Prometheus Alertmanager

### Конкретное действие
Настроить алерты для критичных метрик:
- High error rate
- Low available cars
- High response time
- Database connection issues
- Alertmanager для routing
- Notifications (email, Slack, webhook)

### Что нужно изучить
- Prometheus alerting rules
- Alertmanager configuration
- Alert routing and grouping
- Notification channels

### Возможные сложности
- Alert fatigue (слишком много алертов)
- Правильные thresholds
- False positives

### Как проверить результат
```yaml
# prometheus-alerts.yml
groups:
  - name: application_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors/sec"
      
      - alert: HighResponseTime
        expr: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "P99 latency is {{ $value }}s"
      
      - alert: LowAvailableCars
        expr: cars_available < 5
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Low number of available cars"
          description: "Only {{ $value }} cars available"
      
      - alert: DatabaseConnectionIssues
        expr: up{job="spring-boot-app"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Application is down"
          description: "Spring Boot app has been down for 2 minutes"
```

```yaml
# docker-compose.yml
  alertmanager:
    image: prom/alertmanager:latest
    container_name: car-sharing-alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
    networks:
      - backend-network
  
  prometheus:
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--alertmanager.url=http://alertmanager:9093'
    volumes:
      - ./prometheus-alerts.yml:/etc/prometheus/alerts.yml
```

```yaml
# alertmanager.yml
global:
  resolve_timeout: 5m
  slack_api_url: 'YOUR_SLACK_WEBHOOK_URL'

route:
  receiver: 'default'
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  routes:
    - match:
        severity: critical
      receiver: 'critical-alerts'
    - match:
        severity: warning
      receiver: 'warning-alerts'

receivers:
  - name: 'default'
    slack_configs:
      - channel: '#alerts'
        title: 'Alert: {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
  
  - name: 'critical-alerts'
    slack_configs:
      - channel: '#critical-alerts'
        title: '🚨 CRITICAL: {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
  
  - name: 'warning-alerts'
    slack_configs:
      - channel: '#alerts'
        title: '⚠️ WARNING: {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

Проверка:
```bash
# Запустить Alertmanager
docker-compose up -d alertmanager

# Проверить UI
open http://localhost:9093

# Симулировать alert (создать много ошибок)
for i in {1..100}; do
  curl http://localhost:8082/api/car/999999  # Not found
done

# Проверить алерт в Prometheus
open http://localhost:9090/alerts

# Проверить Slack (если настроен)
```

### Как это отразится в Git
```
feat(observability): add alerting with Prometheus Alertmanager

- Created alerting rules for critical metrics
- Alerts: high error rate, high latency, low cars, downtime
- Configured Alertmanager with Slack notifications
- Separate channels for critical vs warning alerts
- Alert grouping and deduplication

Closes #75
```

---

## 7.7. Custom Grafana Dashboards

### Конкретное действие
Создать production-ready Grafana dashboards:
- Application Overview Dashboard
- Business Metrics Dashboard (contracts, cars, users)
- Infrastructure Dashboard (CPU, memory, DB)
- Error Dashboard
- Export dashboards в JSON для версионирования

### Что нужно изучить
- Grafana dashboard design
- PromQL queries
- Dashboard variables
- Dashboard export/import

### Возможные сложности
- Dashboard design (UX)
- PromQL queries могут быть сложными
- Dashboard JSON большой и сложно читать

### Как проверить результат
```json
// grafana/dashboards/application-overview.json
{
  "dashboard": {
    "title": "Car Sharing - Application Overview",
    "tags": ["car-sharing", "overview"],
    "timezone": "browser",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])"
          }
        ]
      },
      {
        "title": "Response Time (P99)",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))"
          }
        ]
      },
      {
        "title": "Active Contracts",
        "type": "stat",
        "targets": [
          {
            "expr": "contracts_active"
          }
        ]
      },
      {
        "title": "Available Cars",
        "type": "stat",
        "targets": [
          {
            "expr": "cars_available"
          }
        ]
      }
    ]
  }
}
```

Provisioning:
```yaml
# grafana/dashboards/dashboard-provider.yml
apiVersion: 1

providers:
  - name: 'Car Sharing Dashboards'
    folder: 'Car Sharing'
    type: file
    options:
      path: /etc/grafana/provisioning/dashboards
```

Проверка:
```bash
# Запустить Grafana
docker-compose up -d grafana

# Открыть Grafana
open http://localhost:3000

# Dashboards → Car Sharing folder
# Должны быть:
# - Application Overview
# - Business Metrics
# - Infrastructure

# Экспорт dashboard
# Dashboard → Share → Export → Save to file
```

### Как это отразится в Git
```
feat(observability): add Grafana dashboards

- Created Application Overview dashboard
- Created Business Metrics dashboard (contracts, cars)
- Created Infrastructure dashboard (CPU, memory, DB)
- Dashboards provisioned automatically
- Exported to JSON for version control

Closes #76
```

---

## Чеклист выполнения Фазы 7

- [ ] 7.1. Prometheus metrics экспортируются
- [ ] 7.2. Prometheus + Grafana работают
- [ ] 7.3. Distributed tracing с OpenTelemetry/Jaeger
- [ ] 7.4. Centralized logging (Loki или ELK)
- [ ] 7.5. APM для performance monitoring
- [ ] 7.6. Alerting настроен
- [ ] 7.7. Custom Grafana dashboards созданы

## Результат Фазы 7

✅ Полный Observability Stack (logs, metrics, traces)
✅ Real-time мониторинг через Grafana
✅ Distributed tracing для debugging
✅ Centralized logging для анализа
✅ Alerting для proactive response
✅ Production-ready dashboards
✅ DevOps best practices

**Итоговое состояние проекта:**

🎯 Production-ready монолит с:
- ✅ Чистая DDD архитектура
- ✅ 80%+ test coverage
- ✅ CI/CD pipeline
- ✅ Full observability
- ✅ Security hardening
- ✅ Docker/Kubernetes ready
- ✅ Готовность к микросервисам

**Готовность к демонстрации работодателю: 100%** 🚀
