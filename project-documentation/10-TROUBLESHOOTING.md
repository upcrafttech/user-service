# 🚨 TROUBLESHOOTING GUIDE

**Common issues, error messages, and solutions for the HRMS project**

---

## 🔧 QUICK SOLUTIONS BY SYMPTOM

### Service Won't Start

**Error**: `Unable to acquire JDBC Connection from DataSource`

**Solution**:
```bash
# 1. Check database is running
docker ps | grep mysql

# 2. Check database credentials
docker logs hrms-mysql | grep -i error

# 3. Verify connection
mysql -h localhost -u hrms_user -p hrms_db

# 4. Check application.yml credentials match
grep spring.datasource application.yml

# 5. Increase connection timeout
# In application.yml:
spring.datasource.hikari.connection-timeout: 40000
```

---

### High Memory Usage

**Symptom**: OOMKilled error, memory usage > 90%

**Solution**:
```bash
# 1. Check current memory
docker stats hrms-user-service

# 2. Increase heap size
# In Dockerfile:
ENV JAVA_OPTS="-Xmx1024m -Xms512m"

# 3. Check for memory leaks
# In application.yml:
management.endpoints.web.exposure.include: heapdump

# 4. Download and analyze heap dump
curl http://localhost:8082/actuator/heapdump > heap.hprof

# 5. Check for excessive logging
logging.level.com.upcraft: INFO  # Not DEBUG
```

---

### Request Timeout / Slow Response

**Error**: `Read timed out: connect timed out`

**Solution**:
```bash
# 1. Check service CPU usage
top | grep java

# 2. Check database query performance
EXPLAIN SELECT * FROM task WHERE tenant_id = 'xyz' AND status = 'OPEN';

# 3. Add missing indexes
CREATE INDEX idx_tenant_status ON task(tenant_id, status);

# 4. Increase timeout values
# In application.yml:
server.tomcat.connection-timeout: 60000
spring.datasource.hikari.maximum-pool-size: 30

# 5. Check RabbitMQ queue depth
# Go to http://localhost:15672 (admin/admin)
```

---

### Keycloak Authentication Failing

**Error**: `401 Unauthorized` or `Invalid token`

**Solution**:
```bash
# 1. Check Keycloak is running
curl http://localhost:8180

# 2. Verify token endpoint
curl -X POST http://localhost:8180/auth/realms/hrms/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=hrms-client&client_secret=xxx&username=user&password=pass"

# 3. Check token expiration
jq -R 'split(".")[1] | @base64d | fromjson' <<< "eyJhbGc..."

# 4. Verify realm configuration
# Go to http://localhost:8180 → Manage → Import

# 5. Check service is using correct realm
# In application.yml:
spring.security.oauth2.resourceserver.jwt.issuer-uri: http://keycloak:8080/auth/realms/hrms
```

---

### Database Connection Pool Exhausted

**Error**: `Cannot get a connection, pool error Timeout waiting for an idle object`

**Solution**:
```bash
# 1. Check active connections
mysql> SHOW PROCESSLIST;

# 2. Check connection pool config
# In application.yml:
spring.datasource.hikari.maximum-pool-size: 20
spring.datasource.hikari.minimum-idle: 5

# 3. Kill zombie connections
mysql> KILL <connection_id>;

# 4. Check for connection leaks
# Ensure all ResultSet/Statement/Connection closed

# 5. Increase pool size (temporary fix)
spring.datasource.hikari.maximum-pool-size: 50
```

---

### RabbitMQ Queue Overflow

**Symptom**: Queue depth > 10,000, notifications delayed

**Solution**:
```bash
# 1. Check queue depth
# Go to http://localhost:15672 → Queues

# 2. Check consumer count
curl -s http://guest:guest@localhost:15672/api/queues | jq '.[] | {name, consumers}'

# 3. Scale consumer services
kubectl scale deployment notification-service -n hrms-production --replicas=5

# 4. Purge stale messages
curl -i -u guest:guest -X DELETE http://localhost:15672/api/queues/%2F/task-approved/contents

# 5. Check for processing errors
kubectl logs -l app=notification-service --tail=100 | grep -i error
```

---

### Task/Leave Approval Not Working

**Symptom**: Status stuck at PENDING, no event published

**Solution**:
```bash
# 1. Check event listener is running
kubectl logs -l app=notification-service | grep "listening"

# 2. Check queue bindings
curl http://localhost:15672/api/bindings | jq '.[] | select(.source == "task-approved")'

# 3. Verify rabbitmq exchange is created
curl http://localhost:15672/api/exchanges/%2F | jq '.[] | select(.name == "task-approved")'

# 4. Check service has permission to publish
# In application.yml, ensure host IP is correct:
spring.rabbitmq.host: rabbitmq

# 5. Test event publishing
curl -X POST http://localhost:8084/api/v1/tasks/{id}/approve
# Check RabbitMQ logs:
docker logs hrms-rabbitmq | tail -20
```

---

### Database Migrations Failing

**Error**: `Liquibase migration failed` or `Column already exists`

**Solution**:
```bash
# 1. Check migration status
mysql> SELECT * FROM databasechangelog;

# 2. List all changesets
mysql> SELECT * FROM databasechangelog ORDER BY dateexecuted DESC;

# 3. Identify failed changeset
# Run: mysql> SELECT * FROM databasechangelog WHERE exectype = 'FAILED';

# 4. Manual fix (if changeset is irreversible)
# 1. Identify the issue in db/changelog/XXX.xml
# 2. Manually apply SQL fix
# 3. Update databasechangelog:
   INSERT INTO databasechangelog VALUES (...);

# 5. Resume migrations
# Restart service - Liquibase will continue from last successful

# 6. Prevention
# - Always test migrations locally first
# - Use rollbackTag for reversible changes
```

---

### Payslip PDF Generation Failing

**Error**: `PDF generation failed` or `Timeout generating document`

**Solution**:
```bash
# 1. Check iText/PDF library is included
mvn dependency:tree | grep pdf

# 2. Check template file exists
ls -la payroll-service/src/main/resources/templates/payslip.html

# 3. Check free disk space (PDF needs temp space)
df -h /tmp

# 4. Check font file access
# If fonts missing:
docker cp fonts/: hrms-payroll-service:/usr/share/fonts/

# 5. Increase memory for payroll service
# In Dockerfile:
ENV JAVA_OPTS="-Xmx2048m"

# 6. Test PDF generation
curl -X POST http://localhost:8085/api/v1/payroll/payslips/generate \
  -H "Authorization: Bearer <token>"
```

---

### Email Notifications Not Sending

**Error**: `Failed to send email` or `SMTP connection refused`

**Solution**:
```bash
# 1. Check SMTP configuration
# In application.yml:
spring.mail.host: smtp.gmail.com
spring.mail.port: 587
spring.mail.properties.mail.smtp.starttls.enable: true

# 2. Test connection
telnet smtp.gmail.com 587

# 3. Verify credentials
# Check environment variables:
echo $SPRING_MAIL_USERNAME
echo $SPRING_MAIL_PASSWORD

# 4. Check notification service is running
kubectl get pods -l app=notification-service

# 5. Check RabbitMQ has messages
curl http://guest:guest@localhost:15672/api/queues/%2F/email-notification

# 6. View error logs
kubectl logs -l app=notification-service | grep -i email | tail -20

# 7. Test sending email directly
curl -X POST http://localhost:8086/api/v1/notifications/email \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test",
    "message": "Hello"
  }'
```

---

### High CPU Usage in Task Service

**Symptom**: CPU > 80%, server unresponsive

**Solution**:
```bash
# 1. Identify hot methods
jcmd <pid> JFR.start duration=60s filename=profile.jfr
jcmd <pid> JFR.dump filename=profiling.jfr

# 2. Analyze profile
jfr dump profiling.jfr --output profile.json

# 3. Common culprits:
   - N+1 query problem (fix: use JOIN FETCH)
   - Inefficient algorithm (fix: optimize logic)
   - Excessive logging (fix: reduce log level)

# 4. Check for infinite loops
# Search code for loops without proper exit condition

# 5. Example N+1 fix:
   // INEFFICIENT
   List<Task> tasks = taskRepository.findAll();
   for (Task task : tasks) {
     Assignee assignee = employeeRepository.findById(task.getAssigneeId()); // N queries!
   }

   // EFFICIENT
   List<Task> tasks = taskRepository.findAllWithAssignees(); // 1 query with JOIN FETCH
```

---

### Kubernetes Pod Stuck in Pending

**Symptom**: Pod never transitions to Running

**Solution**:
```bash
# 1. Check pod status
kubectl describe pod <pod-name> -n hrms-production

# 2. Common causes:
   # - Insufficient resources
   kubectl describe nodes

   # - Image not found
   kubectl logs <pod> | grep "ImagePullBackOff"

   # - Init container failed
   kubectl logs <pod> -c <init-container-name>

   # - Node not ready
   kubectl get nodes

# 3. Fix insufficient resources
# Increase node capacity or decrease pod requests

# 4. Fix image not found
docker push registry.upcraft.io/user-service:v1.0.0
kubectl rollout restart deployment/user-service -n hrms-production

# 5. Debugging logs
kubectl logs <pod> --all-containers=true
kubectl events <pod>
```

---

## 📊 PERFORMANCE TUNING

### Disable Debug Logging
```yaml
# application.yml
logging:
  level:
    root: INFO
    com.upcraft: INFO           # Not DEBUG
    org.springframework.web: WARN  # Not DEBUG
    org.hibernate.SQL: WARN     # Not DEBUG
```

### Increase Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Enable Response Compression
```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024
    mime-types: application/json,text/html,text/xml,text/plain
```

### Cache Configuration
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      cache-names: users,employees,tasks
```

---

## 🔍 DEBUG MODE

### Enable Debug Logging
```bash
# In application.yml
logging.level.com.upcraft: DEBUG
logging.level.org.hibernate.SQL: DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Enable Spring Boot DevTools
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Remote Debugging
```bash
# Add flag to startup
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar

# Connect from IntelliJ
Run → Debug 'RemoteJVM'
```

---

## 📋 COMMON ERROR CODES

| Error Code | Meaning | Action |
|-----------|---------|--------|
| 400 | Bad Request (validation failed) | Check request body |
| 401 | Unauthorized (missing/invalid token) | Get new token from /auth/login |
| 403 | Forbidden (insufficient permission) | Check user roles in Keycloak |
| 404 | Not Found (resource doesn't exist) | Check ID/tenant-id in URL |
| 422 | Unprocessable (business rule violation) | Check error message for details |
| 503 | Service Unavailable (external service down) | Check RabbitMQ, MySQL, Keycloak status |
| 504 | Gateway Timeout (request took too long) | Check database performance, increase timeout |

---

## 📞 ESCALATION PATH

**Priority 1 (Critical)**: Respond in 15 minutes
- Production completely down
- Data loss risk
- Security breach

**Priority 2 (High)**: Respond in 1 hour
- Service degradation
- High error rate (> 5%)
- Performance issues

**Priority 3 (Medium)**: Respond in 4 hours
- Single feature not working
- Minor performance issues
- Non-critical bugs

**Priority 4 (Low)**: Respond in 24 hours
- UI improvements
- Documentation updates
- Optimization requests

---

**Owner**: DevOps & Support Team
**Last Updated**: Apr 10, 2026
**Status**: ✅ Complete
