# 🗄️ DATABASE DESIGN & SCHEMA

**Complete database schema, relationships, migrations, and data integrity strategy**

---

## 📊 DATABASE OVERVIEW

- **Type**: Relational (MySQL 8.0+)
- **Isolation Model**: Multi-tenant (Shared database, separate data per tenant)
- **Connection Pool**: HikariCP (20 connections)
- **DDL Management**: Liquibase
- **Transactions**: ACID compliance on all critical operations

---

## 🏗️ SCHEMA ARCHITECTURE

### Multi-Tenant Isolation Strategy

```
┌─────────────────────────────────────┐
│      SHARED MYSQL DATABASE          │
├─────────────────────────────────────┤
│                                     │
│  Tenant A        │    Tenant B      │
│  tenant_id=A     │    tenant_id=B   │
│  ─────────────   │    ─────────────  │
│  10 users        │    5 users       │
│  25 employees    │    15 employees  │
│  100 tasks       │    50 tasks      │
│                                     │
│  Query: WHERE tenant_id = 'A'       │
│                                     │
└─────────────────────────────────────┘
```

### Key Principles

1. **Every table must have `tenant_id`** (except lookup tables)
2. **All queries must filter by `tenant_id`**
3. **Database constraints enforced via foreign keys**
4. **Indexes on tenant_id + frequently queried columns**
5. **Application layer also validates tenant isolation**

---

## 📋 ENTITY-RELATIONSHIP DIAGRAM

```
TENANT (Root)
  │
  ├── USER (1:∞)
  │   └── EMPLOYEE (1:1)
  │       ├── ATTENDANCE (1:∞)
  │       ├── LEAVE_REQUEST (1:∞)
  │       ├── SALARY_STRUCTURE (1:1)
  │       └── PAYSLIP (1:∞)
  │
  ├── TASK (1:∞)
  │   ├── SUBTASK (1:∞)
  │   └── TIME_LOG (1:∞)
  │
  ├── PAYROLL_RUN (1:∞)
  │
  └── NOTIFICATION_LOG (1:∞)
```

---

## 📄 TABLE DEFINITIONS

### 1. TENANT (Multi-tenancy root)

```sql
CREATE TABLE tenant (
    tenant_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    max_users INT DEFAULT 1000,
    subscription_plan ENUM('BASIC', 'PRO', 'ENTERPRISE') DEFAULT 'BASIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Purpose**: Root entity for multi-tenancy. Every tenant gets isolated data.
**Partition Strategy**: None (expected <10K tenants)

---

### 2. USER (Authentication & Identity)

```sql
CREATE TABLE user (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    keycloak_id VARCHAR(255),
    password_hash VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,

    UNIQUE KEY uk_username_tenant (username, tenant_id),
    UNIQUE KEY uk_email_tenant (email, tenant_id),
    UNIQUE KEY uk_keycloak_tenant (keycloak_id, tenant_id),

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Purpose**: User accounts, authentication
**Indexes**: Composite (tenant_id, status), single (email)
**Constraints**: Unique username per tenant, unique email per tenant

---

### 3. EMPLOYEE (HR Entity)

```sql
CREATE TABLE employee (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    department_id VARCHAR(36),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    employee_code VARCHAR(50),
    designation VARCHAR(100),
    join_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_employee_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,

    UNIQUE KEY uk_employee_code_tenant (employee_code, tenant_id),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_department (department_id),
    INDEX idx_email (email),
    INDEX idx_join_date (join_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 4. DEPARTMENT (Lookup)

```sql
CREATE TABLE department (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    head_id VARCHAR(36),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_department_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_department_head
        FOREIGN KEY (head_id) REFERENCES employee(id) ON DELETE SET NULL,

    UNIQUE KEY uk_department_name_tenant (name, tenant_id),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 5. ATTENDANCE (Time Tracking)

```sql
CREATE TABLE attendance (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    attendance_date DATE NOT NULL,
    punch_in_time TIMESTAMP,
    punch_out_time TIMESTAMP,
    punch_in_location VARCHAR(255),
    punch_out_location VARCHAR(255),
    worked_hours DECIMAL(5,2),
    status ENUM('PRESENT', 'ABSENT', 'HALF_DAY', 'LEAVE') DEFAULT 'PRESENT',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,

    UNIQUE KEY uk_attendance_emp_date (employee_id, attendance_date),
    INDEX idx_tenant_date (tenant_id, attendance_date),
    INDEX idx_employee_date (employee_id, attendance_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Design**: One row per employee per day
**Query Pattern**: Range queries on date

---

### 6. LEAVE_REQUEST (Leave Management)

```sql
CREATE TABLE leave_request (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    leave_type ENUM('CASUAL', 'SICK', 'EARNED', 'MATERNITY', 'SABBATICAL') NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    number_of_days INT NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by VARCHAR(36),
    approved_on TIMESTAMP NULL,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_employee_dates (employee_id, from_date, to_date),
    INDEX idx_leave_type (leave_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraint**: Prevent overlapping leave dates (enforced in application)
**Query Pattern**: Date range, employee-specific

---

### 7. TASK (Work Items)

```sql
CREATE TABLE task (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assignee_id VARCHAR(36),
    reporter_id VARCHAR(36),
    status ENUM('OPEN', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED', 'CANCELLED') DEFAULT 'OPEN',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    due_date DATE,
    estimated_hours INT,
    actual_hours INT,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    bonus_percentage INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,

    CONSTRAINT fk_task_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assignee
        FOREIGN KEY (assignee_id) REFERENCES employee(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_reporter
        FOREIGN KEY (reporter_id) REFERENCES employee(id) ON DELETE SET NULL,

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_assignee (assignee_id),
    INDEX idx_due_date (due_date),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 8. SUBTASK (Task Decomposition)

```sql
CREATE TABLE subtask (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'OPEN',
    assigned_to VARCHAR(36),
    estimated_hours INT,
    actual_hours INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subtask_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_subtask_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    INDEX idx_task (task_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 9. TIME_LOG (Time Tracking per Task)

```sql
CREATE TABLE time_log (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    hours_logged DECIMAL(5,2) NOT NULL,
    log_date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_timelog_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_timelog_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_timelog_employee
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,

    INDEX idx_task (task_id),
    INDEX idx_employee_date (employee_id, log_date),
    INDEX idx_log_date (log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 10. SALARY_STRUCTURE

```sql
CREATE TABLE salary_structure (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    hra DECIMAL(12,2) DEFAULT 0,
    dearness_allowance DECIMAL(12,2) DEFAULT 0,
    conveyance_allowance DECIMAL(12,2) DEFAULT 0,
    other_allowances DECIMAL(12,2) DEFAULT 0,
    gross_salary DECIMAL(12,2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_salary_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_salary_employee
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,

    UNIQUE KEY uk_salary_emp_period (employee_id, effective_from),
    INDEX idx_tenant (tenant_id),
    INDEX idx_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 11. PAYROLL_RUN

```sql
CREATE TABLE payroll_run (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    status ENUM('DRAFT', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'DRAFT',
    total_amount DECIMAL(15,2),
    employees_processed INT DEFAULT 0,
    process_date DATE,
    processed_at TIMESTAMP NULL,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payrun_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,

    UNIQUE KEY uk_payrun_period (tenant_id, month, year),
    INDEX idx_status (status),
    INDEX idx_period (year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 12. PAYSLIP

```sql
CREATE TABLE payslip (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    payroll_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    gross_pay DECIMAL(12,2) NOT NULL,
    basic_salary DECIMAL(12,2),
    hra DECIMAL(12,2),
    dearness_allowance DECIMAL(12,2),
    other_earnings DECIMAL(12,2),
    income_tax DECIMAL(12,2),
    professional_tax DECIMAL(12,2),
    pf_deduction DECIMAL(12,2),
    esi_deduction DECIMAL(12,2),
    other_deductions DECIMAL(12,2),
    net_pay DECIMAL(12,2) NOT NULL,
    bonus_amount DECIMAL(12,2) DEFAULT 0,
    status ENUM('DRAFT', 'FINALIZED', 'SENT') DEFAULT 'DRAFT',
    pdf_generated_at TIMESTAMP NULL,
    email_sent_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payslip_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_payslip_payrun
        FOREIGN KEY (payroll_id) REFERENCES payroll_run(id) ON DELETE CASCADE,
    CONSTRAINT fk_payslip_employee
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,

    UNIQUE KEY uk_payslip_emp_period (employee_id, month, year),
    INDEX idx_status (status),
    INDEX idx_period (year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 13. NOTIFICATION_LOG

```sql
CREATE TABLE notification_log (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    type ENUM('EMAIL', 'SMS', 'WHATSAPP', 'IN_APP') NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    template_id VARCHAR(100),
    status ENUM('PENDING', 'SENT', 'FAILED', 'DELIVERED') DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_retry (status, next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 14. AUDIT_LOG (Compliance)

```sql
CREATE TABLE audit_log (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'APPROVE') NOT NULL,
    user_id VARCHAR(36),
    old_values JSON,
    new_values JSON,
    changes_summary TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,

    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_tenant_date (tenant_id, created_at),
    INDEX idx_action (action),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🔐 INDEXES OPTIMIZATION STRATEGY

### Composite Indexes (B+ Tree)

```sql
-- Most important queries
CREATE INDEX idx_tenant_status_date ON attendance(tenant_id, status, attendance_date);
CREATE INDEX idx_emp_leave_types ON leave_request(employee_id, leave_type, from_date);
CREATE INDEX idx_task_assignee_status ON task(assignee_id, status, due_date);
CREATE INDEX idx_payslip_emp_period ON payslip(employee_id, year, month);
```

### Cardinality Consideration

```
High Cardinality (Good for indexing):
- id, email, username, employee_code

Medium Cardinality:
- tenant_id, status, department_id

Low Cardinality (Avoid indexing alone):
- gender, status (when only 3-4 values)
```

---

## 📊 Liquibase Migration Structure

```
db/changelog/
├── 001_create_tenant_user_tables.xml
├── 002_create_employee_department_tables.xml
├── 003_create_attendance_leave_tables.xml
├── 004_create_task_tables.xml
├── 005_create_payroll_tables.xml
├── 006_create_notification_tables.xml
├── 007_create_audit_tables.xml
├── 008_add_indexes.xml
└── 009_add_constraints.xml
```

---

## 💾 BACKUP & RECOVERY STRATEGY

### Backup Frequency
- **Full backup**: Daily at 2 AM
- **Incremental**: Every 6 hours
- **Transaction log**: Continuous

### Recovery RTO/RPO
- **RTO** (Recovery Time Objective): 1 hour
- **RPO** (Recovery Point Objective): 15 minutes

### Retention Policy
- Daily backups: 7 days
- Weekly backups: 4 weeks
- Monthly backups: 12 months

---

## 📈 PERFORMANCE TUNING

### Connection Pool
```
HikariCP Configuration:
- Maximum Pool Size: 20
- Minimum Idle: 5
- Connection Timeout: 20s
- Idle Timeout: 10m
- Max Lifetime: 30m
- Leak Detection: 15m
```

### Query Optimization

```sql
-- Good: Uses covering index
SELECT id, status FROM task
WHERE tenant_id = ? AND due_date > ?
ORDER BY due_date;

-- Bad: Full table scan
SELECT * FROM task
WHERE status = ? OR status = ?;

-- Better: Union
SELECT * FROM task WHERE tenant_id = ? AND status = 'OPEN'
UNION
SELECT * FROM task WHERE tenant_id = ? AND status = 'IN_PROGRESS';
```

---

## 🔄 TRANSACTION MANAGEMENT

### Critical Transactions

```java
@Transactional
public PayslipResponse generatePayslip(PayslipRequest request) {
    // 1. Lock payroll_run
    PayrollRun payroll = payrollRunRepository
        .findByIdAndTenant(id, tenantId);

    // 2. Calculate components (no external calls)
    PayslipDetails details = calculateSalary(payroll);

    // 3. Save payslip (committed once)
    Payslip payslip = payslipRepository.save(payslipEntity);

    // 4. Update audit log
    auditLogRepository.save(auditEntry);

    // Rollback entire transaction if any step fails
}
```

### Isolation Level
- **Default**: READ_COMMITTED
- **Sensitive Operations**: SERIALIZABLE (payroll, leaves)

---

## 📝 Data Integrity Rules

### Cascade Rules

| Operation | User | Employee | Attendance | Leave | Task |
|-----------|------|----------|-----------|-------|------|
| Tenant Delete | CASCADE | CASCADE | CASCADE | CASCADE | CASCADE |
| User Delete | - | CASCADE | - | - | - |
| Employee Delete | - | - | CASCADE | CASCADE | CASCADE |
| Task Delete | - | - | - | - | CASCADE |

---

## 🚀 Scalability Considerations

### Partitioning Strategy (Future)

```sql
-- When attendance grows large (>100M records)
-- Partition by month
ALTER TABLE attendance
PARTITION BY RANGE (YEAR_MONTH(attendance_date)) (
    PARTITION p202601 VALUES LESS THAN (202602),
    PARTITION p202602 VALUES LESS THAN (202603),
    ...
);
```

### Sharding Strategy (If needed)

```
Shard Key: tenant_id
Shard 1: Tenant A, C, E (odd)
Shard 2: Tenant B, D, F (even)
Shard Router: Hash(tenant_id) % number_of_shards
```

---

**Database Owner**: Database Engineer
**Last Updated**: Apr 10, 2026
**Status**: ✅ Ready for Implementation
