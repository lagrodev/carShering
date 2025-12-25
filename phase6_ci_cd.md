# Фаза 6: CI/CD Pipeline

**Цель фазы:** Автоматизировать сборку, тестирование, проверки качества и деплой через GitHub Actions.

**Время выполнения:** 10-12 часов (7-9 задач по 1-1.5 часа)

**Приоритет:** ⭐⭐⭐ Критический (DevOps showcase)

---

## 6.1. GitHub Actions: Basic CI Pipeline

### Конкретное действие
Создать базовый CI pipeline в GitHub Actions:
- Trigger на push в main и pull requests
- Checkout кода
- Setup Java 21
- Maven build
- Unit тесты
- Cache Maven dependencies для скорости

### Что нужно изучить
- GitHub Actions YAML syntax
- GitHub Actions runners
- Maven caching strategies
- CI/CD best practices

### Возможные сложности
- Настройка кэширования (правильный cache key)
- Secrets для CI (если нужно)
- Разные окружения (CI vs local)

### Как проверить результат
```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-
      
      - name: Build with Maven
        run: mvn -B clean compile
      
      - name: Run unit tests
        run: mvn -B test
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports/
      
      - name: Upload build artifact
        uses: actions/upload-artifact@v3
        with:
          name: jar-file
          path: target/*.jar
```

Проверка:
```bash
# Создать commit и push
git add .github/workflows/ci.yml
git commit -m "ci: add basic CI pipeline"
git push

# Проверить статус в GitHub
# Actions → CI Pipeline → должен быть зелёный

# Локальная эмуляция (act)
brew install act  # или другой менеджер пакетов
act -j build
```

### Как это отразится в Git
```
ci: add basic GitHub Actions CI pipeline

- Created ci.yml workflow
- Runs on push to main/develop and PRs
- Java 21 setup with Temurin distribution
- Maven build and unit tests
- Caches Maven dependencies for faster builds
- Uploads test results and JAR artifacts

Closes #60
```

---

## 6.2. Integration Tests в CI

### Конкретное действие
Добавить integration тесты в CI с Testcontainers:
- Docker-in-Docker для Testcontainers
- Запуск integration тестов
- Отдельный job или stage
- Parallel execution тестов (опционально)

### Что нужно изучить
- Testcontainers в CI
- Docker-in-Docker в GitHub Actions
- GitHub Actions services (альтернатива Testcontainers)
- Test parallelization

### Возможные сложности
- Testcontainers требует Docker daemon
- CI runner может быть медленным
- Testcontainers может не работать на некоторых CI платформах

### Как проверить результат
```yaml
# .github/workflows/ci.yml (добавить job)
  integration-tests:
    runs-on: ubuntu-latest
    needs: build
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: car_rental_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Run integration tests
        run: mvn -B verify -DskipUnitTests
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/car_rental_test
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: postgres
      
      - name: Upload integration test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: target/failsafe-reports/
```

Альтернатива с Testcontainers (без services):
```yaml
  integration-tests-testcontainers:
    runs-on: ubuntu-latest
    needs: build
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
      
      - name: Run integration tests with Testcontainers
        run: mvn -B verify -Pintegration-tests
```

### Как это отразится в Git
```
ci: add integration tests to CI pipeline

- Created separate job for integration tests
- Uses PostgreSQL service in GitHub Actions
- Runs integration tests with mvn verify
- Uploads test results as artifacts
- Integration tests run after successful build

Closes #61
```

---

## 6.3. Code Quality Checks (Checkstyle, SpotBugs, PMD)

### Конкретное действие
Добавить статический анализ кода в CI:
- Checkstyle для code style
- SpotBugs для bugs detection
- PMD для code quality
- Fail build при критичных проблемах

### Что нужно изучить
- Maven Checkstyle Plugin
- SpotBugs Maven Plugin
- PMD Maven Plugin
- Quality gates configuration

### Возможные сложности
- Огромный список warnings на первом запуске
- False positives
- Баланс между строгостью и pragmatism

### Как проверить результат
```xml
<!-- pom.xml -->
<build>
    <plugins>
        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <failOnViolation>true</failOnViolation>
                <violationSeverity>warning</violationSeverity>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- SpotBugs -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.2.0</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Medium</threshold>
                <failOnError>true</failOnError>
                <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.21.2</version>
            <configuration>
                <failOnViolation>true</failOnViolation>
                <printFailingErrors>true</printFailingErrors>
                <rulesets>
                    <ruleset>/rulesets/java/quickstart.xml</ruleset>
                </rulesets>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

```yaml
# .github/workflows/ci.yml (добавить step)
      - name: Run code quality checks
        run: |
          mvn checkstyle:check
          mvn spotbugs:check
          mvn pmd:check
      
      - name: Upload code quality reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: code-quality-reports
          path: |
            target/checkstyle-result.xml
            target/spotbugsXml.xml
            target/pmd.xml
```

### Как это отразится в Git
```
ci: add static code analysis to pipeline

- Added Checkstyle with Google Java Style
- Added SpotBugs for bug detection
- Added PMD for code quality
- All checks run in CI pipeline
- Build fails on violations
- Reports uploaded as artifacts

Closes #62
```

---

## 6.4. Security Scanning (OWASP, Trivy)

### Конкретное действие
Добавить security scanning в CI:
- OWASP Dependency Check
- Trivy для Docker image scanning
- Vulnerability reporting
- Fail на критичных уязвимостях

### Что нужно изучить
- OWASP Dependency Check в CI
- Trivy container scanner
- CVE databases
- Security automation

### Возможные сложности
- Dependency Check очень медленный (можно кэшировать БД)
- False positives
- Может блокировать деплой при новых CVE

### Как проверить результат
```yaml
# .github/workflows/security.yml
name: Security Scan

on:
  push:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * 1'  # Еженедельно по понедельникам в 2 AM

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Cache OWASP DB
        uses: actions/cache@v3
        with:
          path: ~/.m2/repository/org/owasp
          key: ${{ runner.os }}-owasp-db-${{ github.run_id }}
          restore-keys: |
            ${{ runner.os }}-owasp-db-
      
      - name: Run OWASP Dependency Check
        run: mvn org.owasp:dependency-check-maven:check
      
      - name: Upload OWASP report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: owasp-report
          path: target/dependency-check-report.html
  
  trivy-scan:
    runs-on: ubuntu-latest
    needs: build-docker
    
    steps:
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: car-sharing-backend:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'
      
      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
      
      - name: Fail on critical vulnerabilities
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: car-sharing-backend:${{ github.sha }}
          format: 'table'
          exit-code: '1'
          severity: 'CRITICAL'
```

### Как это отразится в Git
```
ci: add security scanning to pipeline

- Added OWASP Dependency Check job
- Added Trivy Docker image scanning
- Scans run on push and weekly schedule
- Results uploaded to GitHub Security
- Build fails on CRITICAL vulnerabilities
- OWASP DB cached for faster scans

Closes #63
```

---

## 6.5. Build и Push Docker Image

### Конкретное действие
Автоматизировать сборку и публикацию Docker образа:
- Build Docker image в CI
- Tag с version и latest
- Push в Docker Hub или GitHub Container Registry
- Multi-platform builds (amd64, arm64)

### Что нужно изучить
- Docker Buildx
- GitHub Container Registry (GHCR)
- Docker Hub
- Semantic versioning

### Возможные сложности
- Аутентификация в registry
- Секреты для Docker Hub
- Multi-platform builds могут быть медленными

### Как проверить результат
```yaml
# .github/workflows/ci.yml (добавить job)
  build-and-push-docker:
    runs-on: ubuntu-latest
    needs: [build, integration-tests]
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
      
      - name: Login to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ghcr.io/${{ github.repository }}
          tags: |
            type=ref,event=branch
            type=sha,prefix={{branch}}-
            type=semver,pattern={{version}}
            type=raw,value=latest,enable={{is_default_branch}}
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          platforms: linux/amd64,linux/arm64
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
      
      - name: Image digest
        run: echo ${{ steps.docker_build.outputs.digest }}
```

Альтернатива с Docker Hub:
```yaml
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
```

Настройка секретов:
```bash
# GitHub Settings → Secrets → Actions
# Add secrets:
# - DOCKERHUB_USERNAME
# - DOCKERHUB_TOKEN
```

Проверка:
```bash
# После push в main, проверить в GitHub Packages
# https://github.com/YOUR_USERNAME/YOUR_REPO/pkgs/container/YOUR_REPO

# Pull image
docker pull ghcr.io/your-username/car-sharing-backend:latest

# Проверить теги
docker images | grep car-sharing-backend
```

### Как это отразится в Git
```
ci: add Docker build and push to pipeline

- Builds Docker image on push to main
- Tags with branch name, SHA, and latest
- Pushes to GitHub Container Registry
- Multi-platform builds (amd64, arm64)
- Uses GitHub Actions cache for faster builds

Closes #64
```

---

## 6.6. Code Coverage Reporting (Codecov)

### Конкретное действие
Интегрировать Codecov для визуализации покрытия:
- JaCoCo XML report generation
- Upload в Codecov
- Badge в README
- Pull Request comments с coverage diff

### Что нужно изучить
- Codecov integration
- JaCoCo XML format
- Coverage badges
- PR comments

### Возможные сложности
- Codecov token (для private repos)
- Codecov configuration (codecov.yml)

### Как проверить результат
```xml
<!-- pom.xml - уже должен быть JaCoCo -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

```yaml
# .github/workflows/ci.yml (добавить step)
      - name: Generate JaCoCo report
        run: mvn verify
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          token: ${{ secrets.CODECOV_TOKEN }}
          files: ./target/site/jacoco/jacoco.xml
          flags: unittests
          name: codecov-umbrella
          fail_ci_if_error: true
```

```yaml
# codecov.yml (в корне проекта)
coverage:
  status:
    project:
      default:
        target: 80%
        threshold: 1%
    patch:
      default:
        target: 80%

comment:
  layout: "header, diff, files"
  require_changes: true
```

```markdown
<!-- README.md - добавить badge -->
[![codecov](https://codecov.io/gh/YOUR_USERNAME/YOUR_REPO/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/YOUR_REPO)
```

Настройка:
```bash
# 1. Зарегистрироваться на codecov.io
# 2. Добавить репозиторий
# 3. Получить token
# 4. Добавить в GitHub Secrets → CODECOV_TOKEN
```

### Как это отразится в Git
```
ci: add Codecov integration for coverage reporting

- JaCoCo generates XML report
- Coverage uploaded to Codecov on every push
- Added codecov.yml configuration
- Target: 80% coverage, 1% threshold
- Added coverage badge to README

Closes #65
```

---

## 6.7. Automated Deployment (CD)

### Конкретное действие
Настроить автоматический деплой в staging:
- Deploy на успешную сборку из main
- SSH deploy на VPS / cloud
- Docker Compose deploy
- Smoke tests после деплоя
- Rollback mechanism

### Что нужно изучить
- GitHub Actions deployment
- SSH key management
- Docker Compose remote deploy
- Blue-Green deployment (advanced)
- Rollback strategies

### Возможные сложности
- SSH ключи в secrets
- Downtime при деплое (нужен rolling update)
- Secrets на production сервере

### Как проверить результат
```yaml
# .github/workflows/deploy.yml
name: Deploy to Staging

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment:
      name: staging
      url: https://staging.car-sharing.example.com
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup SSH
        uses: webfactory/ssh-agent@v0.8.0
        with:
          ssh-private-key: ${{ secrets.DEPLOY_SSH_KEY }}
      
      - name: Add server to known hosts
        run: |
          mkdir -p ~/.ssh
          ssh-keyscan -H ${{ secrets.DEPLOY_HOST }} >> ~/.ssh/known_hosts
      
      - name: Deploy with Docker Compose
        run: |
          ssh ${{ secrets.DEPLOY_USER }}@${{ secrets.DEPLOY_HOST }} << 'EOF'
            cd /opt/car-sharing
            git pull origin main
            docker-compose pull
            docker-compose up -d --force-recreate
            docker-compose ps
          EOF
      
      - name: Wait for application to start
        run: sleep 30
      
      - name: Run smoke tests
        run: |
          curl -f https://staging.car-sharing.example.com/actuator/health || exit 1
          curl -f https://staging.car-sharing.example.com/api/car | grep -q "\"content\"" || exit 1
      
      - name: Notify deployment
        if: success()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: 'Deployment to staging successful! :rocket:'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

Подготовка сервера:
```bash
# На staging сервере
sudo mkdir -p /opt/car-sharing
cd /opt/car-sharing
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git .

# Создать .env с production secrets
nano .env

# Первый запуск
docker-compose up -d
```

GitHub Secrets:
```
DEPLOY_SSH_KEY - приватный SSH ключ
DEPLOY_HOST - IP или hostname сервера
DEPLOY_USER - пользователь для SSH
SLACK_WEBHOOK - (опционально) для уведомлений
```

Rollback:
```yaml
# .github/workflows/rollback.yml
name: Rollback Deployment

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Docker image tag to rollback to'
        required: true

jobs:
  rollback:
    runs-on: ubuntu-latest
    
    steps:
      - name: Setup SSH
        uses: webfactory/ssh-agent@v0.8.0
        with:
          ssh-private-key: ${{ secrets.DEPLOY_SSH_KEY }}
      
      - name: Rollback to previous version
        run: |
          ssh ${{ secrets.DEPLOY_USER }}@${{ secrets.DEPLOY_HOST }} << EOF
            cd /opt/car-sharing
            docker-compose pull car-sharing-backend:${{ github.event.inputs.version }}
            docker-compose up -d --force-recreate
          EOF
```

### Как это отразится в Git
```
ci: add automated deployment to staging

- Deploys on push to main branch
- Uses SSH to deploy to staging server
- Docker Compose pulls and recreates containers
- Smoke tests verify deployment
- Slack notifications on success/failure
- Manual rollback workflow available

Closes #66
```

---

## Чеклист выполнения Фазы 6

- [ ] 6.1. Basic CI pipeline с тестами
- [ ] 6.2. Integration тесты в CI
- [ ] 6.3. Code quality checks (Checkstyle, SpotBugs, PMD)
- [ ] 6.4. Security scanning (OWASP, Trivy)
- [ ] 6.5. Docker build и push автоматизированы
- [ ] 6.6. Codecov интегрирован
- [ ] 6.7. Automated deployment в staging

## Результат Фазы 6

✅ Полноценный CI/CD pipeline
✅ Автоматические тесты и quality checks
✅ Security scanning в каждом build
✅ Автоматический деплой в staging
✅ Coverage tracking и reporting
✅ One-click rollback mechanism
✅ DevOps best practices

**Следующая фаза:** Observability — мониторинг и логирование
