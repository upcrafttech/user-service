# 🚀 DEPLOYMENT GUIDE

**Complete deployment procedures for development, staging, and production environments**

---

## 📋 DEPLOYMENT OVERVIEW

**This guide covers**:
- Local development setup
- Docker containerization
- Kubernetes deployment
- CI/CD pipeline
- Production deployment
- Health checks & monitoring
- Rollback procedures

---

## 1️⃣ LOCAL DEVELOPMENT ENVIRONMENT

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- Java 17
- Maven 3.8+
- Git
- IDE (IntelliJ IDEA recommended)

### Quick Start

```bash
# 1. Clone repository
git clone <repo-url>
cd Upcraft/Product

# 2. Start infrastructure services
docker-compose up -d

# Expected output:
# Creating mysql-db ... done
# Creating rabbitmq ... done
# Creating redis ... done
# Creating keycloak ... done

# 3. Wait for services to be ready (2-3 minutes)
docker-compose ps

# 4. Verify services
curl http://localhost:3306    # MySQL
curl http://localhost:5672    # RabbitMQ
curl http://localhost:6379    # Redis
curl http://localhost:8180    # Keycloak

# 5. Build backend
mvn clean install -DskipTests

# 6. Run services
mvn spring-boot:run -pl auth-service    # Terminal 1
mvn spring-boot:run -pl user-service    # Terminal 2
# ... etc for other services

# 7. Verify all services are running
curl http://localhost:8081/actuator/health (auth-service)
curl http://localhost:8082/actuator/health (user-service)
```

### Docker Compose Configuration

**File**: `docker-compose.yml`

```yaml
version: '3.9'

services:
  # MySQL Database
  mysql-db:
    image: mysql:8.0
    container_name: hrms-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: hrms_db
      MYSQL_USER: hrms_user
      MYSQL_PASSWORD: hrms_pass
    volumes:
      - mysql_data:/var/lib/mysql
      - ./seed-data.sql:/docker-entrypoint-initdb.d/seed-data.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - hrms-network

  # RabbitMQ Message Broker
  rabbitmq:
    image: rabbitmq:3.11-management
    container_name: hrms-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - hrms-network

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: hrms-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - hrms-network

  # Keycloak Identity Provider
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: hrms-keycloak
    ports:
      - "8180:8080"
    environment:
      KC_DB: mysql
      KC_DB_URL: jdbc:mysql://mysql-db:3306/keycloak
      KC_DB_USERNAME: root
      KC_DB_PASSWORD: root123
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin123
    depends_on:
      mysql-db:
        condition: service_healthy
    networks:
      - hrms-network

volumes:
  mysql_data:
  rabbitmq_data:
  redis_data:

networks:
  hrms-network:
    driver: bridge
```

---

## 2️⃣ DOCKER CONTAINERIZATION

### Build Docker Images

**Dockerfile for each service** (same pattern):

```dockerfile
# Build stage
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /app

# Copy pom files
COPY pom.xml .
COPY common-dto/pom.xml common-dto/
COPY user-service/pom.xml user-service/

# Build dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY common-dto/ common-dto/
COPY user-service/ user-service/

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/user-service/target/*.jar app.jar

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build & Push Images

```bash
# Build all images
docker build -t upcraft/user-service:v1.0.0 -f user-service/Dockerfile .
docker build -t upcraft/employee-service:v1.0.0 -f employee-service/Dockerfile .
docker build -t upcraft/task-service:v1.0.0 -f task-service/Dockerfile .

# Tag for registry
docker tag upcraft/user-service:v1.0.0 registry.upcraft.io/user-service:v1.0.0
docker tag upcraft/employee-service:v1.0.0 registry.upcraft.io/employee-service:v1.0.0

# Push to Docker Registry
docker push registry.upcraft.io/user-service:v1.0.0
docker push registry.upcraft.io/employee-service:v1.0.0
```

---

## 3️⃣ KUBERNETES DEPLOYMENT

### Kubernetes architecture

```
Ingress (nginx)
      ↓
Service (LoadBalancer)
      ↓
Pod (Replica Set)
      ↓
Container (Docker image)
```

### Deployment YAML Structure

**File**: `kubernetes/user-service-deployment.yaml`

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: hrms-production

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: hrms-production
  labels:
    app: user-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8082"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      # Service account for RBAC
      serviceAccountName: user-service

      # Init containers (run before app)
      initContainers:
      - name: wait-for-db
        image: busybox:1.28
        command: ['sh', '-c', 'until nslookup mysql.hrms-services; do echo waiting for mysql; sleep 2; done']

      containers:
      - name: user-service
        image: registry.upcraft.io/user-service:v1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8082
          name: http

        # Environment variables
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: user-service-config
              key: db.url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: user-service-secret
              key: db.username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: user-service-secret
              key: db.password

        # Resource limits
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 500m
            memory: 1Gi

        # Health checks
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3

        # Volume mounts
        volumeMounts:
        - name: logs
          mountPath: /app/logs
        - name: config
          mountPath: /app/config

      # Pod security
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000

      # Volumes
      volumes:
      - name: logs
        emptyDir: {}
      - name: config
        configMap:
          name: user-service-config

---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: hrms-production
  labels:
    app: user-service
spec:
  type: ClusterIP
  ports:
  - port: 8082
    targetPort: 8082
    protocol: TCP
    name: http
  selector:
    app: user-service
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 3600

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
  namespace: hrms-production
data:
  db.url: "jdbc:mysql://mysql:3306/hrms_db"
  server.port: "8082"
  spring.jpa.hibernate.ddl-auto: "validate"

---
apiVersion: v1
kind: Secret
metadata:
  name: user-service-secret
  namespace: hrms-production
type: Opaque
data:
  db.username: aHJtc191c2VyCg==  # base64: hrms_user
  db.password: aHJtc19wYXNzCg==  # base64: hrms_pass
```

### Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace hrms-production

# Apply all manifests
kubectl apply -f kubernetes/

# Verify deployment
kubectl get deployments -n hrms-production
kubectl get pods -n hrms-production
kubectl get svc -n hrms-production

# Check logs
kubectl logs -n hrms-production -l app=user-service --tail=100

# Scale service
kubectl scale deployment user-service -n hrms-production --replicas=5

# Monitor rollout
kubectl rollout status deployment/user-service -n hrms-production
```

---

## 4️⃣ CI/CD PIPELINE

### GitLab CI Configuration

**File**: `.gitlab-ci.yml`

```yaml
stages:
  - build
  - test
  - deploy-staging
  - deploy-production

variables:
  REGISTRY: registry.upcraft.io
  IMAGE_TAG: $CI_COMMIT_SHORT_SHA

# Build stage
build:
  stage: build
  image: maven:3.8.1-openjdk-17
  script:
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - "*/target/*.jar"
    expire_in: 1 day
  cache:
    paths:
      - .m2/repository

# Test stage
test:
  stage: test
  image: maven:3.8.1-openjdk-17
  script:
    - mvn clean test
    - mvn sonar:sonar -Dsonar.projectKey=hrms -Dsonar.sources=.
  coverage: '/Total.*?([0-9]{1,3})%/'

# Build Docker images
docker-build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  before_script:
    - echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin $REGISTRY
  script:
    - docker build -t $REGISTRY/user-service:$IMAGE_TAG -f user-service/Dockerfile .
    - docker build -t $REGISTRY/employee-service:$IMAGE_TAG -f employee-service/Dockerfile .
    - docker push $REGISTRY/user-service:$IMAGE_TAG
    - docker push $REGISTRY/employee-service:$IMAGE_TAG
  only:
    - main

# Deploy to staging
deploy-staging:
  stage: deploy-staging
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/user-service user-service=$REGISTRY/user-service:$IMAGE_TAG -n hrms-staging
    - kubectl rollout status deployment/user-service -n hrms-staging --timeout=5m
  only:
    - main
  environment:
    name: staging
    url: https://staging.upcraft.io

# Deploy to production
deploy-production:
  stage: deploy-production
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/user-service user-service=$REGISTRY/user-service:$IMAGE_TAG -n hrms-production
    - kubectl rollout status deployment/user-service -n hrms-production --timeout=10m
  only:
    - tags
  when: manual
  environment:
    name: production
    url: https://api.upcraft.io

# Rollback production
rollback-production:
  stage: deploy-production
  image: bitnami/kubectl:latest
  script:
    - kubectl rollout undo deployment/user-service -n hrms-production
    - kubectl rollout status deployment/user-service -n hrms-production
  when: manual
  environment:
    name: production
```

---

## 5️⃣ PRODUCTION DEPLOYMENT CHECKLIST

### Pre-Deployment (48 hours before)

- [ ] **Code Quality**
  - [ ] Code review completed
  - [ ] All tests passing (80%+ coverage)
  - [ ] SonarQube quality gate passed
  - [ ] No critical security issues

- [ ] **Documentation**
  - [ ] Release notes prepared
  - [ ] API documentation updated
  - [ ] Deployment guide reviewed
  - [ ] Rollback plan documented

- [ ] **Infrastructure**
  - [ ] Production database backed up
  - [ ] Monitoring systems ready
  - [ ] Alert thresholds configured
  - [ ] On-call team assigned

- [ ] **Testing**
  - [ ] Performance testing complete (load: 1000 req/s)
  - [ ] Security testing complete
  - [ ] Smoke test plan prepared
  - [ ] Staging deployment stable

### Deployment Day

**1. Pre-deployment verification** (30 min before)
```bash
# Check production readiness
kubectl get nodes -n hrms-production
kubectl get persistentvolumes
kubectl get configmaps -n hrms-production

# Verify database connectivity
docker exec mysql-prod mysqladmin ping -h localhost -u root -p

# Test service endpoints
curl https://api.upcraft.io/api/v1/health
```

**2. Deployment** (T = 0)
```bash
# 1. Create backup
kubectl exec mysql-prod -- mysqldump hrms_db > backup-$(date +%Y%m%d-%H%M%S).sql

# 2. Trigger deployment
kubectl apply -f kubernetes/production/

# 3. Monitor rollout
kubectl rollout status deployment/user-service -n hrms-production --timeout=10m

# 4. Run smoke tests
pytest tests/smoke_tests/ --env=production

# 5. Monitor metrics (30 min)
- Error rate < 0.1%
- CPU usage < 70%
- Memory usage stable
- Response time < 200ms
```

**3. Post-deployment** (T+60 min)
```bash
# Verify all services
kubectl get pods -n hrms-production -o wide

# Check logs for errors
kubectl logs -n hrms-production -l app=user-service --tail=50

# Run integration tests
pytest tests/integration/ --env=production

# Verify database replication
mysql> SHOW SLAVE STATUS;

# Notify stakeholders
# Send deployment completion email
```

---

## 6️⃣ HEALTH CHECKS & MONITORING

### Kubernetes Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8082
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8082
  initialDelaySeconds: 20
  periodSeconds: 5
  failureThreshold: 3

startupProbe:
  httpGet:
    path: /actuator/health/startup
    port: 8082
  failureThreshold: 30
  periodSeconds: 10
```

### Spring Boot Actuator Endpoints

```
GET /actuator
GET /actuator/health (overall)
GET /actuator/health/liveness (is alive?)
GET /actuator/health/readiness (ready for traffic?)
GET /actuator/health/startup (initialization complete?)
GET /actuator/metrics (JVM metrics)
GET /actuator/prometheus (Prometheus format)
```

---

## 7️⃣ ROLLBACK PROCEDURE

### Immediate Rollback (< 5 minutes problem detection)

```bash
# Check current status
kubectl rollout history deployment/user-service -n hrms-production

# Rollback to previous version
kubectl rollout undo deployment/user-service -n hrms-production

# Monitor rollback
kubectl rollout status deployment/user-service -n hrms-production

# Verify services recovered
curl https://api.upcraft.io/api/v1/health
```

### Database Rollback (if data corruption)

```bash
# 1. Stop all services
kubectl scale deployment/user-service -n hrms-production --replicas=0

# 2. Restore from backup
mysql hrms_db < backup-20260410-143000.sql

# 3. Verify data integrity
mysql> CHECK TABLE user, employee, task, ...;

# 4. Restart services
kubectl scale deployment/user-service -n hrms-production --replicas=3

# 5. Run health checks
pytest tests/smoke_tests/
```

---

## 8️⃣ MONITORING & ALERTING

### Prometheus Configuration

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
            - hrms-production

alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - alertmanager:9093

rule_files:
  - /etc/prometheus/rules/alerts.yml
```

### Alert Rules

```yaml
groups:
  - name: hrms-alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: |
          rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          severity: critical

      - alert: PodCrashLooping
        expr: rate(kube_pod_container_status_restarts_total[30m]) > 0.1
        for: 5m
        annotations:
          summary: "Pod is crash looping"
          severity: critical

      - alert: DatabaseDown
        expr: up{job="mysql"} == 0
        for: 1m
        annotations:
          summary: "Database is down"
          severity: critical
```

---

## 📊 Metrics to Monitor

| Metric | Warning | Critical |
|--------|---------|----------|
| Error Rate | > 1% | > 5% |
| Response Time (p99) | > 500ms | > 2000ms |
| CPU Usage | > 70% | > 90% |
| Memory Usage | > 75% | > 90% |
| Database Connections | > 80% | > 95% |
| Queue Depth | > 1000 | > 5000 |

---

**Deployment Owner**: DevOps Engineer
**Last Updated**: Apr 10, 2026
**Status**: ✅ Ready for Production
