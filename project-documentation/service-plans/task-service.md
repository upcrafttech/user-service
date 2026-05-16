# Task Service Detailed Implementation Plan

## 1. Scope and Current State
- Service: `task-service`
- Current status: task/subtask/timelog entities and core CRUD are implemented.
- Pending functional areas:
  - Attachments
  - Comments
  - Workflow guards and advanced approval rules
  - Event publishing and audit trail
  - Templates/recurrence

## 2. Outcomes
- Production-ready task management engine with traceability.
- Event-driven integration with payroll and notification domains.

## 3. Workstreams

### A. Attachments Module
1. Add `Attachment` entity:
   - `id`, `taskId`, `fileName`, `mimeType`, `size`, `storagePath`, `uploadedBy`, timestamps
2. Add migration and repository.
3. Add upload endpoint and storage abstraction:
   - local file storage initially
   - pluggable S3 adapter later
4. Add download endpoint with authorization checks.

### B. Comments Module
1. Add `TaskComment` entity and repository.
2. Add APIs:
   - create/list/delete comments
3. Add mention/notification hook support.

### C. Workflow and Approval Engine
1. Implement valid status transitions:
   - Pending -> InProgress -> Completed -> Approved
   - handle rejection/reopen transitions
2. Validate approver role and assignee constraints.
3. Enforce due-date and overdue policy markers.
4. Add optional bonus business rules on approval.

### D. Event Publishing
1. Add RabbitMQ publisher adapter.
2. Publish:
   - `TaskCreatedEvent`
   - `TaskApprovedEvent`
   - optional `TaskCompletedEvent`
3. Add idempotency key for event emission.
4. Add dead-letter and retry strategy.

### E. Audit Trail
1. Add task audit entity/table.
2. Record operation, actor, timestamp, old/new values.
3. Add audit query endpoint for task history.

### F. Templates and Recurrence
1. Add `TaskTemplate` model.
2. Add recurrence rule fields (daily/weekly/monthly/custom cron).
3. Add scheduler job to materialize recurring tasks.

### G. Testing
1. Unit tests:
   - transition guards
   - bonus calculation logic
2. Integration tests:
   - task + subtask + timelog cascading behavior
   - event publish paths
3. End-to-end tests:
   - task approved -> payroll/notification consumers.

## 4. Milestones
- M1: Attachment + comment features
- M2: Workflow guardrails and approval engine
- M3: Event publishing with retries and idempotency
- M4: Audit trail + template/recurrence support
- M5: Full test hardening

## 5. Deliverables
- New classes:
  - `entity/Attachment.java`
  - `entity/TaskComment.java`
  - `event/*Task events*.java`
  - `service/TaskWorkflowService.java`
  - `service/TaskEventPublisher.java`
- Updated:
  - `TaskService`, `TaskController`, Liquibase changelogs
- Tests:
  - `TaskWorkflowServiceTest`
  - `TaskEventPublisherTest`
  - `TaskModuleIT`

## 6. Exit Criteria
- Task lifecycle is strictly controlled and auditable.
- Events are reliably published and consumed downstream.
- Attachments/comments are secure and production-safe.

