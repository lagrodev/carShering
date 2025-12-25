# Фаза 5: Безопасность (Security Hardening)

**Цель фазы:** Внедрить security best practices, защитить от распространённых атак, подготовить к production.

**Время выполнения:** 8-10 часов (6-7 задач по 1-1.5 часа)

**Приоритет:** ⭐⭐ Высокий

**Примечание:** Фаза 5 выполняется ПОСЛЕ Фазы 1, где создается authorization/ bounded context. Многие задачи по security уже реализованы в authorization context (field-level security, permissions). Эта фаза фокусируется на infrastructure security.

---

## 5.1. Externalized Configuration для секретов

### Конкретное действие
Убрать все секреты из application.yaml:
- JWT secret → environment variable
- Database password → environment variable
- MinIO credentials → environment variable
- SMTP credentials → environment variable
- Использовать Spring Cloud Config (опционально) или Docker secrets

### Что нужно изучить
- 12-Factor App: Config
- Spring Boot externalized configuration
- Docker secrets
- Environment variables best practices
- HashiCorp Vault (для продвинутого уровня)

### Возможные сложности
- Локальная разработка требует настройки env vars
- Секреты не должны попасть в Git
- Production secrets management (где хранить?)

### Как проверить результат
```yaml
# application-prod.yaml (NO SECRETS!)
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/car_rental}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  lifetime: ${JWT_LIFETIME:30m}

minio:
  url: ${MINIO_URL:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
```

```.env.example
# .env.example (commit в Git как шаблон)
DATABASE_URL=jdbc:postgresql://postgres:5432/car_rental
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=changeme

JWT_SECRET=generate-long-random-secret-here-at-least-256-bits
JWT_LIFETIME=30m

MINIO_URL=http://minio:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=changeme

SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=user@example.com
SMTP_PASSWORD=changeme
```

```.gitignore
# Добавить
.env
.env.local
**/application-local.yaml
```

Генерация секретов:
```bash
# Генерация JWT secret
openssl rand -base64 64

# Генерация паролей
openssl rand -hex 32
```

Docker Compose с secrets:
```yaml
services:
  backend:
    environment:
      - DATABASE_PASSWORD=/run/secrets/db_password
      - JWT_SECRET=/run/secrets/jwt_secret
    secrets:
      - db_password
      - jwt_secret

secrets:
  db_password:
    file: ./secrets/db_password.txt
  jwt_secret:
    file: ./secrets/jwt_secret.txt
```

Проверка:
```bash
# Проверить, что нет секретов в коде
grep -r "password.*=.*[^$]" src/main/resources/

# Должно найти только ${...} placeholders

# Проверить .gitignore
git status --ignored | grep .env
```

### Как это отразится в Git
```
security: externalize all secrets from configuration

- Removed hardcoded secrets from application.yaml
- All secrets now in environment variables
- Added .env.example template
- Updated .gitignore to exclude .env files
- Documented secret generation in README
- Docker Compose supports Docker secrets

BREAKING CHANGE: Secrets must be provided via environment variables

Closes #50
```

---

## 5.2. Rate Limiting с Bucket4j

### Конкретное действие
Внедрить rate limiting для защиты от abuse:
- API rate limiting (100 requests/minute per IP)
- Auth endpoints — строже (5 login attempts/minute)
- **Favorites API** — rate limit для избежания spam (20 requests/minute)
- **Authorization API** — защита role management endpoints (10 requests/minute для admins)
- Использовать Bucket4j + Spring
- Хранение state в памяти (для начала) или Redis

### Что нужно изучить
- Token Bucket algorithm
- Bucket4j library
- Rate limiting strategies
- HTTP 429 (Too Many Requests)

### Возможные сложности
- In-memory rate limiter не работает при масштабировании (нужен Redis)
- Определение правильных лимитов
- Bypass для healthcheck endpoints

### Как проверить результат
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

```java
// RateLimitingFilter
@Component
@Order(2)
public class RateLimitingFilter implements Filter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = getClientIP(httpRequest);
        
        Bucket bucket = resolveBucket(ip, httpRequest.getRequestURI());
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\": \"Too many requests\"}");
        }
    }
    
    private Bucket resolveBucket(String ip, String uri) {
        return cache.computeIfAbsent(ip, k -> createNewBucket(uri));
    }
    
    private Bucket createNewBucket(String uri) {
        long capacity;
        long refillTokens;
        Duration refillPeriod;
        
        if (uri.startsWith("/api/auth")) {
            // Строже для auth endpoints
            capacity = 5;
            refillTokens = 5;
            refillPeriod = Duration.ofMinutes(1);
        } else {
            // Обычные endpoints
            capacity = 100;
            refillTokens = 100;
            refillPeriod = Duration.ofMinutes(1);
        }
        
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(refillTokens, refillPeriod)
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
```

Тесты:
```java
@Test
void shouldRateLimitExcessiveRequests() throws Exception {
    // given
    String clientIp = "192.168.1.1";
    
    // when - делаем 101 запрос
    for (int i = 0; i < 101; i++) {
        mockMvc.perform(get("/api/car/1")
                .header("X-Forwarded-For", clientIp))
            .andExpect(i < 100 ? status().isOk() : status().isTooManyRequests());
    }
}
```

Проверка:
```bash
# Симуляция множественных запросов
for i in {1..150}; do
  curl -w "%{http_code}\n" http://localhost:8082/api/car/1
done

# Первые 100 должны вернуть 200
# Следующие 50 должны вернуть 429
```

### Как это отразится в Git
```
security: add rate limiting with Bucket4j

- Implemented rate limiting filter
- API endpoints: 100 req/min per IP
- Auth endpoints: 5 req/min per IP
- Returns HTTP 429 when limit exceeded
- In-memory implementation (Redis-ready)
- Tests for rate limiting behavior

Closes #51
```

---

## 5.3. Security Headers

### Конкретное действие
Настроить HTTP security headers:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security (HSTS)
- Content-Security-Policy
- Referrer-Policy

### Что нужно изучить
- OWASP Security Headers
- Spring Security headers configuration
- Content Security Policy
- HSTS best practices

### Возможные сложности
- CSP может блокировать Swagger UI (нужно разрешить)
- HSTS требует HTTPS
- Тестирование headers

### Как проверить результат
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"))
                .xssProtection(xss -> 
                    xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> 
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(referrer -> 
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );
        
        return http.build();
    }
}
```

Проверка:
```bash
# Проверить headers
curl -I http://localhost:8082/api/car/1

# Должны быть:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# X-XSS-Protection: 1; mode=block
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# Content-Security-Policy: ...
# Referrer-Policy: strict-origin-when-cross-origin

# Автоматическая проверка
curl -I http://localhost:8082/api/car/1 | grep -E "X-Content-Type-Options|X-Frame-Options|X-XSS-Protection"
```

Онлайн проверка:
- https://securityheaders.com/

### Как это отразится в Git
```
security: configure HTTP security headers

- Added Content-Security-Policy
- Configured X-Frame-Options: DENY
- Enabled X-XSS-Protection
- Set HSTS with includeSubDomains
- Configured Referrer-Policy
- Headers validated with securityheaders.com

Closes #52
```

---

## 5.4. Input Validation и Sanitization

### Конкретное действие
Усилить валидацию входных данных:
- SQL Injection protection (JPA уже защищает, но проверить)
- XSS protection (sanitize HTML input)
- Path Traversal protection
- Email validation
- Regex для сложных полей (VIN, phone)

### Что нужно изучить
- OWASP Input Validation Cheat Sheet
- Bean Validation constraints
- Custom validators
- OWASP Java HTML Sanitizer

### Возможные сложности
- Баланс между строгостью и UX
- Sanitization может изменить данные пользователя
- Regex может быть сложным

### Как проверить результат
```java
// Custom Email Validator
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface ValidEmail {
    String message() default "Invalid email format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}

// VIN Validator
@Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", 
         message = "VIN must be 17 characters, excluding I, O, Q")
private String vin;

// Sanitization для HTML input
@Component
public class HtmlSanitizer {
    
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    
    public String sanitize(String input) {
        if (input == null) return null;
        return policy.sanitize(input);
    }
}

// В контроллере
@PostMapping("/comments")
public ResponseEntity<?> createComment(@RequestBody CommentRequest request) {
    String sanitized = htmlSanitizer.sanitize(request.comment());
    // ...
}
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.googlecode.owasp-java-html-sanitizer</groupId>
    <artifactId>owasp-java-html-sanitizer</artifactId>
    <version>20220608.1</version>
</dependency>
```

Тесты:
```java
@Test
void shouldRejectSqlInjectionAttempt() {
    var maliciousRequest = new SearchRequest("'; DROP TABLE cars; --");
    
    mockMvc.perform(post("/api/car/search")
            .content(objectMapper.writeValueAsString(maliciousRequest)))
        .andExpect(status().isBadRequest());
}

@Test
void shouldSanitizeXssAttempt() {
    var xssRequest = new CommentRequest("<script>alert('XSS')</script>");
    
    var result = htmlSanitizer.sanitize(xssRequest.comment());
    
    assertThat(result).doesNotContain("<script>");
}

@Test
void shouldRejectInvalidEmail() {
    var request = new RegistrationRequest("invalid-email", "password");
    
    mockMvc.perform(post("/api/registration")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").exists());
}
```

### Как это отразится в Git
```
security: strengthen input validation and sanitization

- Added custom email and VIN validators
- Implemented HTML sanitization for text inputs
- Protection against SQL injection (verified)
- Protection against XSS attacks
- Path traversal protection
- Comprehensive validation tests

Closes #53
```

---

## 5.5. Dependency Scanning с OWASP Dependency Check

### Конкретное действие
Внедрить автоматическую проверку зависимостей на уязвимости:
- OWASP Dependency Check Maven Plugin
- Проверка на CVE в dependencies
- Fail build при критичных уязвимостях
- Регулярное обновление БД уязвимостей

### Что нужно изучить
- OWASP Dependency Check
- CVE (Common Vulnerabilities and Exposures)
- CVSS scoring
- Dependency management best practices

### Возможные сложности
- Сканирование может быть медленным (5-10 минут)
- False positives
- Некоторые уязвимости могут не иметь исправлений

### Как проверить результат
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.7</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
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

```xml
<!-- dependency-check-suppressions.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <!-- Suppress false positives здесь -->
    <suppress>
        <notes>False positive for internal library</notes>
        <cve>CVE-2023-12345</cve>
    </suppress>
</suppressions>
```

Запуск:
```bash
# Сканирование зависимостей
mvn dependency-check:check

# Обновить БД уязвимостей
mvn dependency-check:update-only

# Посмотреть отчёт
open target/dependency-check-report.html

# В CI/CD будет fail при CVSS >= 7
```

Обновление уязвимых зависимостей:
```bash
# Проверить outdated dependencies
mvn versions:display-dependency-updates

# Обновить конкретную зависимость
mvn versions:use-latest-versions -Dincludes=groupId:artifactId
```

### Как это отразится в Git
```
security: add OWASP dependency scanning

- Added OWASP Dependency Check plugin
- Build fails on CVSS >= 7 vulnerabilities
- Created suppressions file for false positives
- Scans all dependencies for known CVEs
- Report generated in target/dependency-check-report.html

Closes #54
```

---

## 5.6. Secure Password Storage

### Конкретное действие
Убедиться, что пароли хранятся безопасно:
- BCrypt encoder (уже используется в Spring Security)
- Cost factor = 12-14 (balance security vs performance)
- Никогда не логировать пароли
- Password strength validation

### Что нужно изучить
- BCrypt algorithm
- Password hashing best practices
- OWASP Password Storage Cheat Sheet
- Password strength policies

### Возможные сложности
- Higher cost factor = slower login
- Миграция существующих паролей (если были небезопасно сохранены)
- Password policies могут раздражать пользователей

### Как проверить результат
```java
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // cost factor
    }
}

// Password Strength Validator
@Component
public class PasswordStrengthValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new WeakPasswordException("Password must be at least " + MIN_LENGTH + " characters");
        }
        
        if (!UPPERCASE.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain uppercase letter");
        }
        
        if (!LOWERCASE.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain lowercase letter");
        }
        
        if (!DIGIT.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain digit");
        }
        
        if (!SPECIAL.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain special character");
        }
    }
}

// Логирование без паролей
@Aspect
@Component
public class SensitiveDataMaskingAspect {
    
    @Around("execution(* org.example.carshering..*(..))")
    public Object maskSensitiveData(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        // Заменить пароли на ****
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof AuthRequest request) {
                log.info("Auth attempt for user: {}", request.username());
                // НЕ логируем password!
            }
        }
        
        return joinPoint.proceed();
    }
}
```

Тесты:
```java
@Test
void shouldHashPasswordWithBCrypt() {
    String rawPassword = "MySecurePassword123!";
    
    String hashed = passwordEncoder.encode(rawPassword);
    
    assertThat(hashed).startsWith("$2a$12$"); // BCrypt with cost 12
    assertThat(hashed).isNotEqualTo(rawPassword);
    assertThat(passwordEncoder.matches(rawPassword, hashed)).isTrue();
}

@Test
void shouldRejectWeakPasswords() {
    assertThatThrownBy(() -> passwordStrengthValidator.validate("weak"))
        .isInstanceOf(WeakPasswordException.class);
    
    assertThatThrownBy(() -> passwordStrengthValidator.validate("noupppercase1!"))
        .isInstanceOf(WeakPasswordException.class);
}

@Test
void shouldNotLogPasswords() {
    // Проверить логи через Logback test appender
    var request = new AuthRequest("user@example.com", "SecretPassword123!");
    
    authController.login(request);
    
    // Логи не должны содержать пароль
    assertThat(testAppender.getEvents())
        .noneMatch(event -> event.getMessage().contains("SecretPassword123!"));
}
```

### Как это отразится в Git
```
security: ensure secure password storage and validation

- Configured BCrypt with cost factor 12
- Implemented password strength validator
- Added min length, uppercase, lowercase, digit, special char requirements
- Ensured passwords never logged
- Tests for password hashing and validation

Closes #55
```

---

## 5.7. Audit Logging для security events

### Конкретное действие
Добавить аудит для security-критичных событий:
- Успешные/неуспешные login попытки
- Password changes
- Role changes (admin actions)
- Document verification
- Account deletion
- Сохранять: timestamp, userId, action, IP address

### Что нужно изучить
- Audit logging patterns
- Spring Security events
- Database audit tables
- GDPR compliance (для audit logs)

### Возможные сложности
- Audit logs могут занимать много места (rotation)
- Performance impact
- GDPR: audit logs могут содержать personal data

### Как проверить результат
```sql
-- Migration: V25_1_20__create_audit_log_table.sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    user_id BIGINT,
    username VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_action ON audit_log(action);
```

```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime timestamp;
    private Long userId;
    private String username;
    private String action;
    private String details;
    private String ipAddress;
    private String userAgent;
    private boolean success;
}

@Service
public class AuditService {
    
    private final AuditLogRepository auditRepo;
    
    public void logLoginAttempt(String username, boolean success, String ip) {
        var audit = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .username(username)
            .action("LOGIN_ATTEMPT")
            .success(success)
            .ipAddress(ip)
            .build();
        
        auditRepo.save(audit);
    }
    
    public void logPasswordChange(Long userId, String ip) {
        var audit = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("PASSWORD_CHANGE")
            .success(true)
            .ipAddress(ip)
            .build();
        
        auditRepo.save(audit);
    }
    
    public void logRoleChange(Long adminId, Long targetUserId, String oldRole, String newRole) {
        var audit = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .userId(adminId)
            .action("ROLE_CHANGE")
            .details(String.format("Changed user %d role from %s to %s", targetUserId, oldRole, newRole))
            .success(true)
            .build();
        
        auditRepo.save(audit);
    }
}

// В AuthController
@PostMapping("/auth")
public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    String ip = getClientIP(httpRequest);
    
    try {
        var response = authService.authenticate(request);
        auditService.logLoginAttempt(request.username(), true, ip);
        return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
        auditService.logLoginAttempt(request.username(), false, ip);
        throw e;
    }
}
```

Тесты:
```java
@Test
void shouldLogSuccessfulLogin() {
    // when
    authController.login(new AuthRequest("user@example.com", "correct-password"), mockRequest);
    
    // then
    var logs = auditRepo.findByAction("LOGIN_ATTEMPT");
    assertThat(logs)
        .hasSize(1)
        .first()
        .satisfies(log -> {
            assertThat(log.getUsername()).isEqualTo("user@example.com");
            assertThat(log.isSuccess()).isTrue();
            assertThat(log.getIpAddress()).isNotNull();
        });
}
```

Проверка:
```sql
-- Последние login попытки
SELECT * FROM audit_log 
WHERE action = 'LOGIN_ATTEMPT' 
ORDER BY timestamp DESC 
LIMIT 10;

-- Неуспешные login за последний час
SELECT username, COUNT(*) as attempts
FROM audit_log
WHERE action = 'LOGIN_ATTEMPT'
  AND success = false
  AND timestamp > NOW() - INTERVAL '1 hour'
GROUP BY username
ORDER BY attempts DESC;
```

### Как это отразится в Git
```
security: implement audit logging for security events

- Created audit_log table
- Audit logs for login attempts (success/fail)
- Audit logs for password changes
- Audit logs for role changes
- Logs include timestamp, user, action, IP, success
- Tests for audit logging

Closes #56
```

---

## Чеклист выполнения Фазы 5

- [ ] 5.1. Все секреты externalized (environment variables)
- [ ] 5.2. Rate limiting защищает от abuse
- [ ] 5.3. Security headers настроены
- [ ] 5.4. Input validation и sanitization усилены
- [ ] 5.5. OWASP Dependency Check в CI
- [ ] 5.6. Пароли хранятся безопасно (BCrypt)
- [ ] 5.7. Audit logging для security events

## Результат Фазы 5

✅ Production-ready security posture
✅ Защита от OWASP Top 10 угроз
✅ Секреты не в коде
✅ Rate limiting защищает API
✅ Зависимости проверяются на уязвимости
✅ Audit trail для compliance

**Следующая фаза:** CI/CD — автоматизация сборки и деплоя
