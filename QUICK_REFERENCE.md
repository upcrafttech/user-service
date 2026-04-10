# HRMS Quick Reference Guide

## 📚 Files Location & Purpose

### Exception Handling
```
common-dto/src/main/java/com/upcraft/exception/
├── HrmsException.java                    ← Base exception class
├── ErrorResponse.java                    ← Standard error format
├── ResourceNotFoundException.java        ← 404 errors
├── ValidationException.java              ← Validation errors (400)
├── AuthenticationException.java          ← Auth failures (401)
├── AuthorizationException.java           ← Permission errors (403)
├── BusinessException.java                ← Business rule violations (422)
├── ServiceCommunicationException.java    ← Service failures (503)
└── GlobalExceptionHandler.java           ← Centralized handler
```

### Inter-Service Clients
```
common-dto/src/main/java/com/upcraft/client/
├── BaseServiceClient.java                ← Base HTTP client
├── UserServiceClient.java                ← Call user-service
├── EmployeeServiceClient.java            ← Call employee-service
├── TaskServiceClient.java                ← Call task-service
├── PayrollServiceClient.java             ← Call payroll-service
├── NotificationServiceClient.java        ← Call notification-service
└── config/
    └── RestTemplateConfig.java           ← REST configuration
```

### DTOs
```
common-dto/src/main/java/com/upcraft/dto/
├── TenantDTO.java
├── UserDTO.java
├── EmployeeDTO.java
├── TaskDTO.java
├── SubtaskDTO.java
├── TimeLogDTO.java
├── PayslipDTO.java
├── SalaryStructureDTO.java
└── ApiResponse.java
```

---

## 🚀 Quick Start Commands

```bash
# Build entire project
mvn clean install

# Build specific service
cd user-service && mvn clean package

# Run with Docker
docker-compose up -d

# Stop services
docker-compose down

# Check service health
curl http://localhost:8082/actuator/health

# View logs
docker logs user-service
docker logs -f task-service  # Follow logs

# Access databases
mysql -u root -proot -h localhost

# Keycloak admin
http://localhost:8080
Username: admin
Password: admin
```

---

## 📝 Exception Usage Examples

### ResourceNotFoundException
```java
throw new ResourceNotFoundException("User", userId.toString());
// Response: 404 "User not found with identifier: xxx"
```

### ValidationException
```java
if (email.isEmpty()) {
    throw new ValidationException("email", "Email is required");
}
// Response: 400 "Validation error in field 'email': Email is required"
```

### AuthenticationException
```java
throw AuthenticationException.invalidCredentials();
// Response: 401 "Invalid username or password"
```

### AuthorizationException
```java
if (!user.hasRole("ADMIN")) {
    throw AuthorizationException.roleRequired("ADMIN");
}
// Response: 403 "This operation requires 'ADMIN' role"
```

### BusinessException
```java
if (salary < 0) {
    throw new BusinessException("INVALID_SALARY", "Salary cannot be negative");
}
// Response: 422 "Salary cannot be negative"
```

### ServiceCommunicationException
```java
try {
    userClient.getUser(id, token);
} catch (RestClientException ex) {
    throw new ServiceCommunicationException("user-service", ex.getMessage(), ex);
}
// Response: 503 "Failed to communicate with user-service service: ..."
```

---

## 🔌 Inter-Service Call Examples

### Using UserServiceClient
```java
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final UserServiceClient userClient;

    public void createEmployeeWithUser(EmployeeDTO emp, UserDTO user, String token) {
        // Create user first
        UserDTO createdUser = userClient.createUser(user, token);

        // Get user details
        UserDTO userDetails = userClient.getUserById(createdUser.getId(), token);

        // Use in employee
        emp.setUserId(userDetails.getId());
    }
}
```

### Using EmployeeServiceClient
```java
@Service
@RequiredArgsConstructor
public class TaskService {
    private final EmployeeServiceClient empClient;

    public EmployeeDTO getTaskAssigneeDetails(UUID assigneeId, String token) {
        return empClient.getEmployeeById(assigneeId, token);
    }
}
```

### Using NotificationServiceClient
```java
@Service
@RequiredArgsConstructor
public class PayrollService {
    private final NotificationServiceClient notificationClient;

    public void notifyPayslip(String email, String subject, String body, String token) {
        String result = notificationClient.sendEmail(email, subject, body, token);
        log.info("Payslip notification sent: {}", result);
    }
}
```

---

## 📊 Service Ports Quick Map

```
Auth Service           → 8081   /api/auth/*
User Service          → 8082   /api/users/*
Employee Service      → 8083   /api/employees/*
Task Service          → 8084   /api/tasks/*
Payroll Service       → 8085   /api/payroll/*
Notification Service  → 8086   /api/notifications/*

Keycloak             → 8080   /admin
MySQL                → 3306
RabbitMQ             → 5672 (AMQP), 15672 (Management)
Redis                → 6379
```

---

## 🔧 Configuration (application.yml)

### Add to Each Service
```yaml
service:
  user:
    url: http://user-service:8082
  employee:
    url: http://employee-service:8083
  task:
    url: http://task-service:8084
  payroll:
    url: http://payroll-service:8085
  notification:
    url: http://notification-service:8086
```

### For Docker Compose
```yaml
# Inside container, use service name
service.user.url: http://user-service:8082

# From host machine, use localhost
service.user.url: http://localhost:8082
```

---

## ✅ Checklist for Adding New Features

### 1. Create Entity
```java
@Entity
@Table(name = "new_entity")
@Data
public class NewEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }
}
```

### 2. Create Repository
```java
@Repository
public interface NewEntityRepository extends JpaRepository<NewEntity, UUID> {
    Optional<NewEntity> findByName(String name);
}
```

### 3. Create Service
```java
@Service
@RequiredArgsConstructor
public class NewEntityService {
    private final NewEntityRepository repository;

    @Transactional
    public NewEntity create(NewEntity entity) {
        return repository.save(entity);
    }
}
```

### 4. Create Controller
```java
@RestController
@RequestMapping("/api/new-entities")
@RequiredArgsConstructor
public class NewEntityController {
    private final NewEntityService service;

    @PostMapping
    public ResponseEntity<ApiResponse<NewEntity>> create(@RequestBody NewEntity entity) {
        try {
            NewEntity created = service.create(entity);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), "CREATE_FAILED"));
        }
    }
}
```

### 5. Create Liquibase Changelog
```xml
<!-- src/main/resources/db/changelog/01-create-new-entity-table.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog>
    <changeSet id="1" author="hrms">
        <createTable tableName="new_entity">
            <column name="id" type="VARCHAR(36)" primaryKey="true"/>
            <column name="name" type="VARCHAR(100)"/>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

### 6. Add to Master Changelog
```xml
<!-- db/changelog/db.changelog-master.xml -->
<include file="db/changelog/01-create-new-entity-table.xml"/>
```

---

## 🐛 Common Issues & Solutions

### Issue: Service not found
```
ERROR: Failed to communicate with user-service service
```
**Solution:**
```bash
# Check if service is running
docker ps | grep user-service

# Start missing service
docker-compose up -d user-service

# Check logs
docker logs user-service
```

### Issue: Database connection error
```
WARN: Cannot get a connection, pool error Timeout waiting for idle object
```
**Solution:**
```bash
# Check MySQL
docker ps | grep mysql

# Restart MySQL
docker-compose restart mysql

# Manual repair
mysql -u root -proot -e "SHOW DATABASES;"
```

### Issue: Port already in use
```
ERROR: listen tcp 127.0.0.1:8082: bind: address already in use
```
**Solution:**
```bash
# Kill process on port
lsof -i :8082 | grep -v COMMAND | awk '{print $2}' | xargs kill -9

# Or change port in application.yml
server.port: 9082
```

### Issue: Keycloak not accessible
```
Connection refused when accessing localhost:8080
```
**Solution:**
```bash
# Check Keycloak status
docker logs keycloak-hrms

# Restart Keycloak
docker-compose restart keycloak

# Wait for startup
sleep 30
```

---

## 📈 Current Status at a Glance

```
✅ COMPLETE (Use as-is)
├── Infrastructure & DevOps (100%)
├── Common Libraries (80%)
├── User Service (75%)
└── Docker/K8s (100%)

🔄 IN PROGRESS (Needs work)
├── Employee Service (55%)
├── Task Service (50%)
├── Auth Service (40%)
├── Payroll Service (35%)
└── Notification Service (30%)

⏳ PENDING (Not started)
├── Tests (0%)
├── CI/CD (0%)
├── Advanced Features (20%)
└── Monitoring (15%)
```

---

## 🎯 Where to Find Things

| What | Where |
|------|-------|
| Feature Status | README.md |
| Latest Changes | LATEST_UPDATES.md |
| Extension Guide | SERVICE_GENERATION_GUIDE.md |
| Generation Info | GENERATION_COMPLETE.md |
| Common DTOs | common-dto/src/main/java/com/upcraft/dto/ |
| Exceptions | common-dto/src/main/java/com/upcraft/exception/ |
| Service Clients | common-dto/src/main/java/com/upcraft/client/ |
| User Service | user-service/ |
| Employee Service | employee-service/ |
| Task Service | task-service/ |
| Payroll Service | payroll-service/ |
| Notification Service | notification-service/ |
| Kubernetes | kubernetes/ |
| Docker Compose | docker-compose.yml |

---

## 💡 Pro Tips

### 1. Always include auth token in service calls
```java
String authToken = request.getHeader("Authorization")
    .replace("Bearer ", "");
userClient.getUserById(id, authToken);
```

### 2. Use @Transactional for data operations
```java
@Transactional
public void updateEmployee(UUID id, EmployeeDTO dto) {
    // Changes are automatically committed
}
```

### 3. Handle service communication errors
```java
try {
    EmployeeDTO emp = empClient.getEmployeeById(id, token);
} catch (ServiceCommunicationException ex) {
    log.error("Employee service is down: {}", ex.getMessage());
    // Fallback logic or return error
}
```

### 4. Use Swagger annotations for documentation
```java
@Operation(summary = "Create employee", description = "Create a new employee record")
@ApiResponse(responseCode = "201", description = "Employee created")
@PostMapping
public ResponseEntity<ApiResponse<EmployeeDTO>> create(@RequestBody EmployeeDTO dto) {
    // ...
}
```

### 5. Validate input before processing
```java
if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
    throw new ValidationException("email", "Email is required");
}
```

---

**Last Updated**: April 10, 2026
**Version**: 1.0.0
**For Full Documentation**: See README.md
