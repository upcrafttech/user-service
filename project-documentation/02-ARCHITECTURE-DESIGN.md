# 🏗️ ARCHITECTURE & SYSTEM DESIGN

**Complete system architecture, microservices design, and technology decisions**

---

## 📐 SYSTEM ARCHITECTURE OVERVIEW

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY / LOAD BALANCER                │
│                    (Nginx / Spring Cloud Gateway)               │
└──────────────────────────────┬──────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
   ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
   │  AUTH       │      │  USER       │      │  EMPLOYEE   │
   │  SERVICE    │      │  SERVICE    │      │  SERVICE    │
   └─────────────┘      └─────────────┘      └─────────────┘
        │                      │                      │
        ├──────────────────────┼──────────────────────┤
        │                      │                      │
        ▼                      ▼                      ▼
   ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
   │  TASK       │      │  PAYROLL    │      │ NOTIFICATION│
   │  SERVICE    │      │  SERVICE    │      │  SERVICE    │
   └─────────────┘      └─────────────┘      └─────────────┘
        │                      │                      │
        └──────────────────────┼──────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
              ┌──────────────┐      ┌──────────────┐
              │  RabbitMQ    │      │  MySQL DB    │
              │  (Events)    │      │  (Shared)    │
              └──────────────┘      └──────────────┘
                    │
                    ▼
              ┌──────────────┐
              │  Service     │
              │  Listeners   │
              └──────────────┘
```

---

## 🔐 KEYCLOAK INTEGRATION

### Keycloak as Authentication Authority

```
┌─────────────┐
│   Client    │
│ (Web/Mobile)│
└──────┬──────┘
       │ 1. Login Request
       ▼
┌─────────────────────┐
│    KEYCLOAK         │
│  (Authentication)   │
├─────────────────────┤
│ • User Management   │
│ • Token Issuance    │
│ • RBAC              │
│ • 2FA               │
└──────┬──────────────┘
       │ 2. JWT Token
       ▼
┌─────────────┐
│  API        │
│  Gateway    │
├─────────────┤
│ Token Valid?│
│ • Verify    │
│ • Decode    │
│ • Check exp │
└──────┬──────┘
       │ 3. Allow/Deny
       ▼
   Microservices
```

### Token Structure (JWT)

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-123",
    "name": "John Doe",
    "email": "john@upcraft.com",
    "roles": ["EMPLOYEE", "MANAGER"],
    "tenant_id": "tenant-456",
    "iat": 1640000000,
    "exp": 1640003600
  },
  "signature": "HMACSHA256(...)"
}
```

---

## 🗄️ DATABASE ARCHITECTURE

### Multi-Tenant Design

```
┌──────────────────────────────────────────────┐
│         SHARED MYSQL DATABASE                │
├──────────────────────────────────────────────┤
│                                              │
│  Tenant A Data    │    Tenant B Data        │
│  (tenant_id: A)   │    (tenant_id: B)       │
│  ─────────────    │    ─────────────        │
│  • Users          │    • Users              │
│  • Employees      │    • Employees          │
│  • Tasks          │    • Tasks              │
│  • Payroll        │    • Payroll            │
│                                              │
└──────────────────────────────────────────────┘

All rows filtered by: WHERE tenant_id = ?
```

### Key Tables & Relationships

```
TENANT
  ├── tenant_id (PK)
  ├── name
  ├── status
  └── created_at

USER
  ├── user_id (PK)
  ├── tenant_id (FK)
  ├── username
  ├── email
  ├── keycloak_id
  └── is_active

EMPLOYEE
  ├── employee_id (PK)
  ├── tenant_id (FK)
  ├── user_id (FK)
  ├── department
  ├── designation
  └── salary

TASK
  ├── task_id (PK)
  ├── tenant_id (FK)
  ├── assignee_id (FK to EMPLOYEE)
  ├── title
  ├── status
  └── due_date

ATTENDANCE
  ├── attendance_id (PK)
  ├── tenant_id (FK)
  ├── employee_id (FK)
  ├── punch_in
  ├── punch_out
  └── date

LEAVE_REQUEST
  ├── leave_id (PK)
  ├── tenant_id (FK)
  ├── employee_id (FK)
  ├── leave_type
  ├── from_date
  ├── to_date
  └── status

PAYROLL_RUN
  ├── payroll_id (PK)
  ├── tenant_id (FK)
  ├── month
  ├── year
  ├── status
  └── processed_date

PAYSLIP
  ├── payslip_id (PK)
  ├── tenant_id (FK)
  ├── employee_id (FK)
  ├── payroll_id (FK)
  ├── gross_pay
  ├── deductions
  └── net_pay
```

---

## 🔄 EVENT-DRIVEN ARCHITECTURE

### Event Flow with RabbitMQ

```
SERVICE A          RabbitMQ          SERVICE B
  │                  │                  │
  ├─ Publish ────→ Exchange           │
  │ Event: "User   ├─→ Queue A      ←─┤
  │ Created"       │                  │
  │                ├─→ Queue B      ←─┤
  │                │                Consume
  │                │              & Process
  └─ Process ←──── Response ────────→ ┘
    Immediately
```

### Event Types

1. **UserCreated** → Trigger: Employee creation form
2. **UserActivated** → Trigger: Admin activation
3. **EmployeeAdded** → Trigger: New hire onboarding
4. **AttendancePunched** → Trigger: Punch in/out
5. **LeaveRequested** → Trigger: Leave application
6. **LeaveApproved** → Trigger: Manager approval
7. **TaskCreated** → Trigger: New task assignment
8. **TaskApproved** → Trigger: Task completion
9. **PayrollRun** → Trigger: Monthly processing
10. **PayslipGenerated** → Trigger: Salary computation complete

---

## 🔌 SERVICE COMMUNICATION PATTERNS

### Synchronous (REST)

```
User Service
    │
    ├─→ GET /employees/{id} → Employee Service
    │
    ├─→ GET /tasks?assignee=emp-123 → Task Service
    │
    └─→ POST /notifications/send → Notification Service
```

### Asynchronous (Event-Based)

```
Task Service
    │
    └─→ PUBLISH: TaskApproved
         │
         ├─→ Payroll Service (Calculate bonus)
         │
         ├─→ Employee Service (Update status)
         │
         └─→ Notification Service (Send email)
```

---

## 🏢 MICROSERVICES STRUCTURE

### 1. Auth Service
```
auth-service/
├── src/main/java/com/upcraft/auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   └── AuthService.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java
│   └── config/
│       └── KeycloakConfig.java
└── application.yml
```

### 2. User Service
```
user-service/
├── src/main/java/com/upcraft/user/
│   ├── entity/
│   │   ├── User.java
│   │   └── Tenant.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── TenantRepository.java
│   ├── service/
│   │   └── UserService.java
│   ├── controller/
│   │   └── UserController.java
│   └── dto/
│       ├── UserDTO.java
│       └── TenantDTO.java
├── db/changelog/
│   └── 001_create_user_table.xml
└── application.yml
```

### 3. Employee Service
```
employee-service/
├── src/main/java/com/upcraft/employee/
│   ├── entity/
│   │   ├── Employee.java
│   │   ├── Attendance.java
│   │   ├── LeaveRequest.java
│   │   └── Department.java
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── event/ (listener for UserCreated)
│   └── dto/
├── db/changelog/
└── application.yml
```

### 4. Task Service
```
task-service/
├── src/main/java/com/upcraft/task/
│   ├── entity/
│   │   ├── Task.java
│   │   ├── Subtask.java
│   │   ├── TimeLog.java
│   │   └── Attachment.java
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── event/
│   │   ├── TaskCreatedPublisher.java
│   │   └── TaskApprovedPublisher.java
│   └── dto/
├── db/changelog/
└── application.yml
```

### 5. Payroll Service
```
payroll-service/
├── src/main/java/com/upcraft/payroll/
│   ├── entity/
│   │   ├── SalaryStructure.java
│   │   ├── PayrollRun.java
│   │   └── Payslip.java
│   ├── service/
│   │   ├── PayrollService.java
│   │   ├── TaxCalculationService.java
│   │   └── PayslipGeneratorService.java
│   ├── event/ (listener for TaskApproved)
│   └── dto/
├── db/changelog/
└── application.yml
```

### 6. Notification Service
```
notification-service/
├── src/main/java/com/upcraft/notification/
│   ├── entity/
│   │   ├── NotificationTemplate.java
│   │   └── NotificationLog.java
│   ├── service/
│   │   ├── EmailService.java
│   │   ├── SmsService.java
│   │   └── WhatsAppService.java
│   ├── event/ (listener for all events)
│   └── config/
│       ├── SmtpConfig.java
│       └── TwilioConfig.java
├── db/changelog/
└── application.yml
```

---

## 🛡️ SECURITY ARCHITECTURE

### Layer 1: API Gateway
- Request validation
- Rate limiting
- HTTPS enforcement
- IP whitelisting

### Layer 2: Authentication
- Keycloak token validation
- JWT expiry check
- Token refresh logic

### Layer 3: Authorization
- Role-based access control (RBAC)
- Tenant isolation verification
- Permission matrix checking

### Layer 4: Data Layer
- Field-level encryption (PII)
- Query parameter validation
- SQL injection prevention (Prepared statements)
- Audit logging on sensitive operations

---

## 📊 CACHING STRATEGY

### Redis Cache Layers

```
Request
  │
  ├─→ Cache: userById:{userId}
  │         userByEmail:{email}
  │
  ├─→ Cache: employeeById:{empId}
  │
  ├─→ Cache: tasksByAssignee:{empId}
  │
  ├─→ Cache: rolePermissions:{roleName}
  │
  └─→ Database (if cache miss)
```

### Cache Invalidation Strategy

```
USER_UPDATED
  └─→ Invalidate: userById:{id}, userByEmail:{email}

EMPLOYEE_UPDATED
  └─→ Invalidate: employeeById:{id}

ROLE_MODIFIED
  └─→ Invalidate: rolePermissions:*

TASK_UPDATED
  └─→ Invalidate: tasksByAssignee:{empId}
```

---

## 🔗 INTEGRATION POINTS

### External Systems

1. **Keycloak**
   - User registration & login
   - Token issuance & validation
   - RBAC management

2. **RabbitMQ**
   - Event publishing
   - Event consumption
   - Dead letter queue (DLQ)

3. **MySQL**
   - All business data
   - Transactions
   - Liquibase migrations

4. **SMTP Server**
   - Email notifications
   - Payslip distribution

5. **Twilio**
   - SMS notifications
   - WhatsApp messages

---

## 📈 SCALABILITY DESIGN

### Horizontal Scaling
- Stateless microservices design
- Database replication (Read replicas)
- Kubernetes load balancing
- RabbitMQ message queues

### Vertical Scaling
- Increased pod resources (CPU/Memory)
- Database query optimization
- Caching (Redis)
- Connection pooling

---

## 🚀 DEPLOYMENT ARCHITECTURE

### Kubernetes Clusters

```
┌─────────────────────────────────────────┐
│         KUBERNETES CLUSTER              │
├─────────────────────────────────────────┤
│                                         │
│  Namespace: production                  │
│  ├─ Auth Service Pod(s)                │
│  ├─ User Service Pod(s)                │
│  ├─ Employee Service Pod(s)            │
│  ├─ Task Service Pod(s)                │
│  ├─ Payroll Service Pod(s)             │
│  └─ Notification Service Pod(s)        │
│                                         │
│  Namespace: monitoring                  │
│  ├─ Prometheus                          │
│  ├─ Grafana                             │
│  └─ ELK Stack                           │
│                                         │
│  Shared Resources:                      │
│  ├─ MySQL PersistentVolume              │
│  ├─ RabbitMQ StatefulSet                │
│  └─ Redis Cache                         │
│                                         │
└─────────────────────────────────────────┘
```

---

## 💾 DATA CONSISTENCY

### ACID Compliance
- MySQL transactions for critical operations
- Liquibase for schema migration control
- Foreign key constraints enforced

### Event Sourcing Strategy
- All domain events logged to database
- Audit trail maintained automatically
- Event replay capability for debugging

---

## 🔍 MONITORING & OBSERVABILITY

### Metrics Collected
- Request count & latency (per endpoint)
- Error rates (by type)
- Database query performance
- Cache hit/miss ratio
- Queue depth (RabbitMQ)
- Service availability (uptime %)

### Log Aggregation
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Structured JSON logging
- Correlation IDs for request tracing
- Alert triggers on error thresholds

---

## 🎯 Design Principles

1. **Separation of Concerns** - Each service has single responsibility
2. **Loose Coupling** - Services communicate via events, not direct calls
3. **High Cohesion** - Related functionality grouped in same service
4. **Stateless Services** - No session state in pods
5. **Cross-Cutting Concerns** - Exception handling, logging, auth centralized
6. **API Versioning** - v1, v2, etc. for backward compatibility
7. **Idempotency** - All operations safe to retry

---

## 📝 Architecture Decision Log

| Decision | Rationale | Alternatives |
|----------|-----------|---------------|
| Spring Boot Microservices | Industry standard, mature, excellent Spring Cloud ecosystem | Node.js, Go, .NET |
| MySQL (Shared DB) | Multi-tenant, easier initial scaling, ACID compliance | Separate DB per tenant, NoSQL |
| Keycloak | Open source, OIDC/OAuth2 complaint, RBAC native | Custom JWT, Auth0, Okta |
| RabbitMQ | Reliable message broker, AMQP protocol, good documentation | Kafka, AWS SQS |
| Docker in Kubernetes | Industry standard container orchestration | Docker Swarm, Nomad |
| Redis Caching | In-memory, fast, distributed caching | Memcached, Hazelcast |

---

**Architecture Owner**: Tech Lead
**Last Updated**: Apr 10, 2026
**Status**: ✅ Finalized
