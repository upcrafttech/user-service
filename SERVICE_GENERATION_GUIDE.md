# HRMS Microservices - Complete Generation Guide

This guide provides complete instructions for generating the remaining microservices (task-service, payroll-service, notification-service) and deploying the entire HRMS system.

## Completed Services

✅ **parent pom.xml** - Maven parent with dependency management
✅ **common-dto** - Shared DTOs across all services
✅ **keycloak-provider** - Keycloak integration library
✅ **auth-service** - OAuth2 authentication (port 8081)
✅ **user-service** - User & tenant management (port 8082)
✅ **employee-service** - Employee management (port 8083)

## Services to Generate

### 1. Task Service (Port 8084)

**Entities:**
- Task (id, tenant_id, title, description, status, priority, assignee_id, created_by, due_date, bonus_amount, completed_at, created_at)
- Subtask (id, tenant_id, task_id, title, status, assignee_id, due_date, created_at)
- TimeLog (id, tenant_id, task_id, user_id, log_date, hours, created_at)
- Attachment (id, tenant_id, task_id, filename, file_path, uploaded_by, uploaded_at)

**Controllers:**
- TaskController: CRUD operations, list, search, update status
- SubtaskController: CRUD for subtasks
- TimeLogController: Log time for tasks
- AttachmentController: Upload/download attachments

**Database:**
- Database: hrms_tasks
- Tables: task, subtask, timelog, attachment

**Key Features:**
- Task creation and assignment
- Subtask management
- Time tracking
- Task approval workflow
- File attachments
- Publish TaskApproved events

### 2. Payroll Service (Port 8085)

**Entities:**
- SalaryStructure (id, tenant_id, employee_id, basic_pay, hra, other_allowances, created_at)
- PayrollRun (id, tenant_id, period_start, period_end, executed_by, executed_at)
- Payslip (id, payroll_run_id, employee_id, gross_pay, net_pay, created_at)

**Controllers:**
- SalaryStructureController: CRUD salary structures
- PayrollController: Run payroll, generate payslips

**Database:**
- Database: hrms_payroll
- Tables: salary_structure, payroll_run, payslip

**Key Features:**
- Salary structure management
- Monthly payroll execution
- Payslip generation
- Tax calculations (TDS, PF, ESI)
- Listen for TaskApproved events to add billable hours
- Publish PayrollCompleted events

### 3. Notification Service (Port 8086)

**Entities:**
- NotificationTemplate (id, tenant_id, type, subject, body, created_at)
- NotificationLog (id, tenant_id, recipient, type, status, created_at)

**Controllers:**
- NotificationController: Send email, SMS, WhatsApp

**Database:**
- Database: hrms_notifications
- Tables: notification_template, notification_log

**Key Features:**
- Email notifications (via SMTP)
- SMS notifications (via Twilio/MSG91)
- WhatsApp notifications (via WhatsApp Business API)
- Event-driven notifications
- Subscribe to TaskCreated, TaskApproved, PayrollCompleted events
- Retry mechanism
- Rate limiting

## Service Generation Steps

### Step 1: Create Task Service

```bash
# Create module
mkdir task-service
cd task-service

# Create pom.xml (use template below)
# Create src structure
mkdir -p src/main/java/com/upcraft/task/{entity,repository,service,controller}
mkdir -p src/main/resources/db/changelog
mkdir -p src/test/java

# Create files
# - Entity classes
# - Repositories
# - Services
# - Controllers
# - application.yml
# - Liquibase changelog
# - Dockerfile
```

### Step 2: Create Payroll Service

```bash
# Similar steps as Task Service
# Focus on salary calculations and tax deductions
```

### Step 3: Create Notification Service

```bash
# Similar steps as Task Service
# Focus on messaging integrations
```

## Code Templates

### Task Service - Service Implementation

```java
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Task task = new Task();
        // ... set fields
        Task saved = taskRepository.save(task);

        // Publish event
        rabbitTemplate.convertAndSend("task-events",
            new TaskCreatedEvent(saved.getId()));

        return toDTO(saved);
    }

    @Transactional
    public void approveTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow();
        task.setStatus("Completed");
        task.setApprovedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Publish approval event
        rabbitTemplate.convertAndSend("task-events",
            new TaskApprovedEvent(taskId, task.getBonusAmount()));
    }
}
```

### Payroll Service - Tax Calculation

```java
@Component
public class TaxCalculator {
    public TaxDetails calculateTDS(Long grossPay) {
        // India TDS calculation logic
        // Based on slab rate
        return taxDetails;
    }

    public long calculatePF(Long basicPay) {
        // PF = 12% of basic pay (employee contribution)
        return basicPay / 100 * 12;
    }

    public long deductTaxes(Long grossPay) {
        long tds = calculateTDS(grossPay);
        long pf = calculatePF(grossPay);
        long esi = calculateESI(grossPay);
        return tds + pf + esi;
    }
}
```

### Notification Service - Event Listener

```java
@Component
public class TaskEventListener {
    @RabbitListener(queues = "task-events")
    public void handleTaskApproved(TaskApprovedEvent event) {
        // Get task details
        // Get employee details
        // Send notification
        notificationService.sendEmail(
            employee.getEmail(),
            "Task Approved",
            "Your task has been approved. Bonus: " + event.getBonusAmount()
        );
    }
}
```

## Integration Points

### Message Queue (RabbitMQ)

**Topics/Exchanges:**
- `task-events`: TaskCreated, TaskApproved, TaskCompleted
- `payroll-events`: PayrollRun, PayrollCompleted
- `notification-events`: EmailSent, SmsSent, WhatsAppSent

**Message Format:**
```json
{
    "eventId": "uuid",
    "timestamp": "ISO8601",
    "tenantId": "uuid",
    "eventType": "TaskApproved",
    "payload": {
        "taskId": "uuid",
        "bonusAmount": 10000,
        "approvedBy": "uuid"
    }
}
```

### Database Migration Strategy

Each service manages its own Liquibase changelog:

Task Service:
- 01-create-task-table.xml
- 02-create-subtask-table.xml
- 03-create-timelog-table.xml
- 04-create-attachment-table.xml
- 05-add-indexes.xml

Payroll Service:
- 01-create-salary-structure-table.xml
- 02-create-payroll-run-table.xml
- 03-create-payslip-table.xml
- 04-add-constraints.xml

Notification Service:
- 01-create-notification-template-table.xml
- 02-create-notification-log-table.xml

## Configuration

### Environment Variables (for .env or K8s secrets)

```env
# MySQL
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=hrms
MYSQL_PASSWORD=hr_password_123

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin_password_123
KEYCLOAK_REALM=hrms
KEYCLOAK_CLIENT_ID=hrms-app
KEYCLOAK_CLIENT_SECRET=client_secret_123

# Email Service
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=app-password

# SMS Service
SMS_PROVIDER=twilio  # or msg91
SMS_ACCOUNT_SID=xxxxx
SMS_AUTH_TOKEN=xxxxx

# WhatsApp
WA_BUSINESS_ACCOUNT_ID=xxxxx
WA_BUSINESS_PHONE_NUMBER=xxxxx
WA_API_TOKEN=xxxxx

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

## Testing Strategy

### Unit Tests
- Service layer tests with mocked repositories
- DTO mapping tests
- Business logic tests

### Integration Tests
- Controller tests with TestRestTemplate
- Database integration with Testcontainers
- Event messaging tests

### Example Test

```java
@SpringBootTest
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    public void testCreateTask() {
        TaskDTO dto = new TaskDTO();
        // ... set fields

        TaskDTO result = taskService.createTask(dto);

        assertNotNull(result.getId());
        verify(taskRepository).save(any());
    }
}
```

## Deployment Checklist

- [ ] All 6 services built and tested
- [ ] Docker images created and pushed to registry
- [ ] Kubernetes manifests created
- [ ] Database migrations validated
- [ ] Keycloak realm and clients configured
- [ ] SSL certificates configured
- [ ] Logging and monitoring configured
- [ ] Backup strategy implemented
- [ ] Load testing completed
- [ ] Security audit completed
- [ ] Documentation updated
- [ ] Runbooks created

## Quick Start Commands

```bash
# Build entire project
mvn clean install

# Build specific service
cd task-service && mvn clean package

# Run with Docker Compose
docker-compose up -d

# Check service health
curl http://localhost:8084/actuator/health

# View Swagger docs
open http://localhost:8084/swagger-ui.html

# Run migrations
# Liquibase automatically runs on application startup

# Check logs
docker logs task-service
docker logs payroll-service
docker logs notification-service
```

## Monitoring & Observability

### Metrics
- Request latency
- Error rates
- Database query performance
- Queue depths (for RabbitMQ)
- Service uptime

### Logs
- Structured JSON logging
- Request IDs for tracing
- Separate logs for each service
- Centralized log aggregation (ELK stack)

### Alerts
- Service down (no heartbeat)
- Error rate > threshold
- Database connectivity issues
- Queue backlog issues

## Next Steps

1. **Implement remaining services** following this guide
2. **Set up CI/CD pipeline** for automated builds and deployments
3. **Configure monitoring** with Prometheus/Grafana
4. **Set up centralized logging** with ELK stack
5. **Implement API Gateway** for request routing
6. **Configure service mesh** (optional, with Istio)
7. **Set up automated backups** for database
8. **Document runbooks** for operations team

---

**Last Updated**: April 2026
**Status**: Partially Complete (3 of 6 services)
**Maintainer**: Development Team
