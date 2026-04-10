# 🎯 Development Checklist & Task Board

## 📋 Feature Implementation Tracker

Use this document to track daily progress on feature implementation.

---

## PHASE 2: CORE SERVICES (Target: 75%+ Complete)

### Employee Service - Database & Entities
- [ ] Create Attendance entity with: id, employee_id, punch_in, punch_out, date, status
- [ ] Create LeaveRequest entity with: id, employee_id, type, from_date, to_date, reason, status, approver_id
- [ ] Create LeaveType reference data (Sick, Annual, Casual, Paternity, Maternity)
- [ ] Create Liquibase changelog: 03-create-attendance-table.xml
- [ ] Create Liquibase changelog: 04-create-leave-request-table.xml
- [ ] Create AttendanceRepository with queries:
  - [ ] findByEmployeeIdAndDate()
  - [ ] findByEmployeeIdBetweenDates()
  - [ ] findTodayAttendance()
- [ ] Create LeaveRequestRepository with queries:
  - [ ] findByEmployeeIdAndStatus()
  - [ ] findPendingApprovals()
  - [ ] findByApproverIdAndStatus()
- [ ] Update EmployeeService with methods:
  - [ ] recordAttendance(employeeId, action)
  - [ ] getAttendanceReport(employeeId, month)
  - [ ] requestLeave(leaveRequestDTO)
  - [ ] approveLeave(leaveRequestId, approverId)
  - [ ] rejectLeave(leaveRequestId, approverId, reason)
- [ ] Update EmployeeController with endpoints:
  - [ ] POST /api/employees/{id}/attendance/punch-in
  - [ ] POST /api/employees/{id}/attendance/punch-out
  - [ ] GET /api/employees/{id}/attendance?from=DATE&to=DATE
  - [ ] POST /api/employees/{id}/leave-requests
  - [ ] GET /api/employees/{id}/leave-requests
  - [ ] POST /api/leave-requests/{id}/approve
  - [ ] POST /api/leave-requests/{id}/reject
- [ ] Add Swagger annotations to all endpoints
- [ ] Write unit tests for AttendanceService
- [ ] Write unit tests for LeaveRequestService
- [ ] Write integration tests for new endpoints
- [ ] Test with Postman

**Status**: ⏳ Pending | **Priority**: 🔴 CRITICAL | **ETA**: 3-4 days

---

### Task Service - Database & Entities
- [ ] Create Task entity with full schema
- [ ] Create Subtask entity
- [ ] Create TimeLog entity
- [ ] Create Attachment entity with file path/storage reference
- [ ] Create Liquibase changelog: 01-task-schema.xml
- [ ] Create TaskRepository with queries:
  - [ ] findByTenantIdAndStatus()
  - [ ] findByAssigneeId()
  - [ ] findByCreatedBy()
  - [ ] findOverdueTasks()
- [ ] Create SubtaskRepository
- [ ] Create TimeLogRepository
- [ ] Create AttachmentRepository
- [ ] Implement TaskService with methods:
  - [ ] createTask() - Publish TaskCreated event ⭐
  - [ ] updateTask()
  - [ ] getTasksByStatus()
  - [ ] getTasksByAssignee()
  - [ ] approveTask() - Calculate bonus, Publish TaskApproved event ⭐
  - [ ] logTime()
  - [ ] uploadAttachment()
  - [ ] getTaskComments() - Mock implementation
- [ ] Implement Subtask CRUD
- [ ] Implement TimeLog CRUD with automatic billing to payroll
- [ ] Add RabbitMQ event publisher
  - [ ] Create TaskCreatedEvent
  - [ ] Create TaskApprovedEvent
  - [ ] Publish to task-events exchange
- [ ] Update TaskController with all endpoints
- [ ] Add Swagger annotations
- [ ] Write service tests
- [ ] Write controller tests

**Status**: ⏳ Pending | **Priority**: 🔴 CRITICAL | **ETA**: 5-6 days

---

### Payroll Service - Entities & Tax Calculations
- [ ] Create SalaryStructure entity
- [ ] Create PayrollRun entity
- [ ] Create Payslip entity
- [ ] Create Liquibase changelog: 01-payroll-schema.xml
- [ ] Implement TaxCalculator component:
  - [ ] calculateTDS(grossPay) - India income tax slab
  - [ ] calculatePF(basicPay) - 12% employee contribution
  - [ ] calculateESI(grossPay) - Based on state
  - [ ] calculateProfessionalTax(grossPay) - State-specific
- [ ] Create PayrollCalculationService:
  - [ ] calculateGrossPay(salaryStructure, bonuses)
  - [ ] calculateDeductions(grossPay)
  - [ ] calculateNetPay(grossPay, deductions)
  - [ ] generatePayslip(employee, payrollRun)
- [ ] Implement PayrollRunService:
  - [ ] runMonthlyPayroll(tenantId, periodStart, periodEnd)
  - [ ] runOffCyclePayroll()
  - [ ] updatePayslipWithBonus(taskApprovedEvent) ⭐ - RabbitMQ listener
- [ ] Create PayslipRepository
- [ ] Update PayrollController with endpoints
- [ ] Add Swagger documentation
- [ ] Write PayrollCalculationService tests
- [ ] Write TaxCalculator tests with different salary ranges
- [ ] Test RabbitMQ event consumption

**Status**: ⏳ Pending | **Priority**: 🔴 CRITICAL | **ETA**: 6-7 days

---

### Notification Service - Email/SMS Integration
- [ ] Create NotificationTemplate entity
- [ ] Create NotificationLog entity
- [ ] Create Liquibase changelog: 01-notification-schema.xml
- [ ] Implement EmailService:
  - [ ] Configure SMTP (Gmail, SendGrid, etc.)
  - [ ] Send simple email
  - [ ] Send HTML email
  - [ ] Send with attachments
  - [ ] Send bulk emails
- [ ] Implement SmsService:
  - [ ] Integrate Twilio SDK
  - [ ] Create and send SMS
  - [ ] Handle delivery status
  - [ ] Handle failures/retries
- [ ] Implement WhatsAppService:
  - [ ] Integrate WhatsApp Business API
  - [ ] Send messages with templates
  - [ ] Handle media (images, documents)
- [ ] Create NotificationListener (RabbitMQ):
  - [ ] Listen to task-events (TaskCreated, TaskApproved)
  - [ ] Listen to payroll-events (PayrollCompleted)
  - [ ] Send appropriate notifications
- [ ] Create NotificationTemplateResolver:
  - [ ] Task created → email template
  - [ ] Task approved → email + SMS template
  - [ ] Payroll run → email template
  - [ ] Leave approved → email template
- [ ] Update NotificationController with all endpoints
- [ ] Add retry mechanism (3 retries with backoff)
- [ ] Add notification delivery logging
- [ ] Add Swagger documentation
- [ ] Write EmailService tests
- [ ] Write SmsService tests
- [ ] Write NotificationListener tests
- [ ] Integration test with RabbitMQ

**Status**: ⏳ Pending | **Priority**: 🔴 CRITICAL | **ETA**: 5-6 days

---

### Auth Service - Keycloak Integration
- [ ] Implement KeycloakAuthService:
  - [ ] authenticate(username, password) - Call Keycloak OAuth
  - [ ] validateToken(jwt) - Verify with Keycloak
  - [ ] refreshToken(refreshToken) - Get new JWT
  - [ ] logout(token) - Invalidate token
  - [ ] getUserInfo(token) - Extract claims
- [ ] Create LoginRequest/LoginResponse DTOs
- [ ] Update AuthController with full implementation
- [ ] Add Spring Security filter for token validation
- [ ] Add @PreAuthorize annotations
- [ ] Add CORS configuration
- [ ] Configure Swagger OAuth2 security scheme
- [ ] Implement password reset flow (stub)
- [ ] Add MFA support (optional placeholder)
- [ ] Write AuthService tests
- [ ] Test with real Keycloak instance
- [ ] Add token expiration handling
- [ ] Add rate limiting on login endpoint

**Status**: ⏳ Pending | **Priority**: 🟠 HIGH | **ETA**: 3-4 days

---

## PHASE 3: BUSINESS LOGIC & INTEGRATIONS

### Event-Driven Architecture Setup
- [ ] Configure RabbitMQ in all services (already in docker-compose ✅)
- [ ] Create Event base class
- [ ] Create specific event classes:
  - [ ] TaskCreatedEvent
  - [ ] TaskApprovedEvent
  - [ ] TaskCompletedEvent
  - [ ] PayrollRunEvent
  - [ ] PayrollCompletedEvent
  - [ ] LeaveApprovedEvent
- [ ] Implement EventPublisher (RabbitTemplate wrapper)
- [ ] Create message queues and exchanges in RabbitMQ configuration
- [ ] Implement event listeners in each service
- [ ] Test event publishing and consumption
- [ ] Add dead-letter queue handling

**Priority**: 🟠 HIGH | **ETA**: 3-4 days

---

### Audit Trail & Logging
- [ ] Create AuditLog entity
- [ ] Create AuditLogRepository
- [ ] Create AuditListener (using Spring events)
- [ ] Implement @Audited annotation for entities
- [ ] Track: CREATE, UPDATE, DELETE, APPROVE, REJECT operations
- [ ] Log: who, what, when, old value, new value
- [ ] Create audit endpoints:
  - [ ] GET /api/audit?entity=Employee&id=xxx
  - [ ] GET /api/audit/user?userId=xxx
- [ ] Write audit tests
- [ ] Document audit schema

**Priority**: 🟡 MEDIUM | **ETA**: 2-3 days

---

### Approval Workflows
- [ ] Create ApprovalRequest entity
- [ ] Create ApprovalFlow configuration
- [ ] Implement TaskApprovalService
- [ ] Implement LeaveApprovalService
- [ ] Create approval notifications
- [ ] Add approval history tracking
- [ ] Create approval dashboard endpoints (stub)

**Priority**: 🟡 MEDIUM | **ETA**: 3-4 days

---

## PHASE 4: ADVANCED FEATURES

### Reporting & Exports
- [ ] Create ReportService
- [ ] Implement attendance report (PDF)
- [ ] Implement leave report
- [ ] Implement payroll report
- [ ] Implement task report with metrics
- [ ] Create Excel export functionality
- [ ] Add scheduled report generation
- [ ] Add email delivery of reports

**Priority**: 🟡 MEDIUM | **ETA**: 4-5 days

---

### Caching Layer (Redis - Optional)
- [ ] Configure Spring Cache with Redis
- [ ] Cache employee data (24h TTL)
- [ ] Cache tax rates (30d TTL)
- [ ] Cache leave types (30d TTL)
- [ ] Cache salary structures (7d TTL)
- [ ] Implement cache invalidation on updates
- [ ] Add cache metrics

**Priority**: 🟢 LOW | **ETA**: 2-3 days

---

### API Gateway (Optional)
- [ ] Create API Gateway service
- [ ] Implement service routing
- [ ] Add rate limiting
- [ ] Add request logging
- [ ] Add API versioning (v1/v2)
- [ ] Add request/response transformation
- [ ] Document gateway endpoints

**Priority**: 🟢 LOW | **ETA**: 3-4 days

---

## PHASE 5: TESTING & DEPLOYMENT

### Unit Testing (Target: 80% coverage)
Services to test:
- [ ] UserService
- [ ] EmployeeService
- [ ] AttendanceService
- [ ] LeaveRequestService
- [ ] TaskService
- [ ] PayrollService / TaxCalculator
- [ ] NotificationService
- [ ] AuthService

Each should have:
- [ ] CRUD operation tests
- [ ] Business logic tests
- [ ] Exception handling tests
- [ ] Validation tests

**Priority**: 🔴 CRITICAL | **ETA**: 5-7 days

---

### Integration Testing
- [ ] Controller tests (all endpoints)
- [ ] Database integration tests
- [ ] Service-to-service communication tests
- [ ] RabbitMQ event tests
- [ ] Keycloak integration tests

**Priority**: 🔴 CRITICAL | **ETA**: 3-4 days

---

### CI/CD Pipeline
- [ ] Create GitHub Actions workflow
- [ ] Build job (clean install)
- [ ] Test job (run tests)
- [ ] Docker build job
- [ ] Push to registry
- [ ] Deploy to staging
- [ ] Run smoke tests
- [ ] Deploy to production

**Priority**: 🟠 HIGH | **ETA**: 4-5 days

---

### Performance & Load Testing
- [ ] Identify bottlenecks
- [ ] Load test with 100+ concurrent users
- [ ] Database query optimization
- [ ] Caching strategy optimization
- [ ] Connection pool tuning

**Priority**: 🟡 MEDIUM | **ETA**: 3-4 days

---

### Security Audit
- [ ] SQL injection testing
- [ ] XSS testing
- [ ] CSRF protection verification
- [ ] Rate limiting testing
- [ ] JWT token validation
- [ ] Field-level authorization testing
- [ ] Data encryption verification

**Priority**: 🔴 CRITICAL | **ETA**: 3-4 days

---

## 📊 DAILY STANDUP TEMPLATE

Use this for daily team sync:

```
Date: YYYY-MM-DD
Attendees:

COMPLETED TODAY
- [ ] Feature 1
- [ ] Feature 2

IN PROGRESS
- [ ] Feature 3 (60% done)
- [ ] Feature 4 (40% done)

BLOCKERS
- Issue: ...
  Solution: ...

TOMORROW'S PLAN
- [ ] Feature 5
- [ ] Feature 6

NOTES
- Any risks or dependencies?
- Any help needed?
```

---

## 🎯 WEEKLY GOALS

### Week 1 (Apr 10-14)
**Target**: Employee Service Database + Task Service Schema
- [ ] Attendance table & repository
- [ ] Leave request table & repository
- [ ] Task service database schema
- [ ] Subtask, TimeLog, Attachment entities
- [ ] RabbitMQ event infrastructure

**Success Criteria**:
- All schemas in place
- Services can save/retrieve data
- Events publish successfully
- At least 10 unit tests per service

### Week 2 (Apr 16-21)
**Target**: Business Logic + Event Listeners
- [ ] Attendance tracking logic
- [ ] Leave approval workflow
- [ ] Task approval with bonus calculation
- [ ] Event listeners for all services
- [ ] Tax calculations

**Success Criteria**:
- Full CRUD for all entities
- Events publish and consumed correctly
- Bonuses calculated correctly
- Notifications sent on events

### Week 3 (Apr 23-28)
**Target**: Keycloak + Email/SMS
- [ ] Complete Keycloak integration
- [ ] Email sending with templates
- [ ] SMS sending setup
- [ ] Notification delivery logging
- [ ] User role mapping

**Success Criteria**:
- Users can login with Keycloak
- Emails sent successfully
- SMS sent successfully
- Role-based access control working

### Week 4 (Apr 30-May 5)
**Target**: Testing + Deployment
- [ ] All unit tests (80% coverage)
- [ ] Integration tests
- [ ] CI/CD pipeline
- [ ] Staging deployment
- [ ] Performance baseline

**Success Criteria**:
- All tests pass
- CI/CD pipeline working
- Services deployable
- Performance acceptable

---

## 🚨 CRITICAL PATH ITEMS
(Must be done first)

1. ✅ Exception handling - DONE ✅
2. ✅ Inter-service clients - DONE ✅
3. ⏳ Database schemas (Employee, Task, Payroll)
4. ⏳ Service layer implementation
5. ⏳ RabbitMQ event publishing
6. ⏳ Keycloak authentication
7. ⏳ Unit tests
8. ⏳ CI/CD pipeline

---

## 📞 HELP & ESCALATION

### For Exceptions
- Reference: `common-dto/src/main/java/com/upcraft/exception/`
- Ask: "Which exception should I throw?"

### For Service Calls
- Reference: `common-dto/src/main/java/com/upcraft/client/`
- Ask: "How do I call another service?"

### For Database
- Reference: USER SERVICE Liquibase changelogs
- Ask: "How do I create a new table?"

### For Tests
- Reference: README.md Testing section
- Ask: "How do I write a unit test?"

---

**Last Updated**: April 10, 2026
**Next Review**: Daily during standup
**Maintained By**: Development Team

---

## 📝 NOTES SECTION

Use this space to add team notes, blockers, or important decisions:

```
2026-04-10:
- Exception handling infrastructure complete
- Inter-service clients ready for use
- Ready to start database schemas

TODO:
- Schedule Keycloak setup session
- Arrange RabbitMQ training
- Set up monitoring dashboard
```

---

**How to use this document**:
1. Copy this template to your project management tool (Jira, Monday.com, etc.)
2. Update daily status
3. Review blockers in standup
4. Update completion % weekly
5. Adjust ETA based on actual progress

**Questions?** See LATEST_UPDATES.md or QUICK_REFERENCE.md
