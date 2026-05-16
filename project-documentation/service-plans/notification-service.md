# Notification Service Detailed Implementation Plan

## 1. Scope and Current State
- Service: `notification-service`
- Current status: endpoints exist but send logic is placeholder.
- Pending core:
  - Provider integrations (email/SMS/WhatsApp)
  - Templates and delivery logs
  - Event listeners and retry strategy

## 2. Outcomes
- Reliable multi-channel notifications with delivery observability.
- Event-driven delivery for task, payroll, and leave workflows.

## 3. Workstreams

### A. Domain and Persistence
1. Add entities:
   - `NotificationTemplate`
   - `NotificationLog`
   - optional `NotificationPreference`
2. Add migrations with indexes:
   - `status`, `channel`, `nextRetryAt`, `tenantId`, `recipient`
3. Add repositories and service APIs.

### B. Provider Adapters
1. Define channel interface:
   - `NotificationProvider.send(request)`
2. Implement:
   - SMTP Email provider
   - Twilio SMS provider
   - WhatsApp provider adapter
3. Add provider-specific response parsing and error classification.

### C. Template Engine
1. Add template resolver:
   - keyed by event type + channel + locale
2. Support placeholders:
   - employee name
   - task title
   - payroll month
3. Add HTML + plain text email rendering.

### D. Event Consumers
1. Add RabbitMQ listeners for:
   - task created/approved
   - payroll completed
   - leave approved
2. Map events -> channels -> templates -> send requests.

### E. Retry and Delivery Tracking
1. Add retry policy:
   - max attempts
   - exponential backoff
2. Persist each attempt in `NotificationLog`.
3. Add dead-letter handling for poison messages.
4. Add manual requeue endpoint for failed notifications.

### F. API and Operational Endpoints
1. Keep direct send APIs for admin operations.
2. Add query APIs:
   - list by status/channel/date
   - get failed notifications
3. Add health endpoint for provider readiness diagnostics.

### G. Security and Controls
1. Add rate limiting by tenant/channel.
2. Add do-not-disturb and preference checks.
3. Add PII-safe logging and secret masking.

### H. Testing
1. Unit tests:
   - template rendering
   - retry/backoff logic
2. Integration tests:
   - provider stubs/mocks
   - Rabbit listeners + DLQ flow
3. Contract tests:
   - event payload to notification mapping.

## 4. Milestones
- M1: Domain schema + template engine
- M2: Email provider end-to-end
- M3: SMS/WhatsApp provider adapters
- M4: Event listeners + retry + DLQ
- M5: Operational APIs and test suite

## 5. Deliverables
- New:
  - `entity/NotificationTemplate.java`
  - `entity/NotificationLog.java`
  - `provider/* channel adapters */`
  - `listener/NotificationEventListener.java`
  - `service/RetrySchedulerService.java`
- Updated:
  - `controller/NotificationController.java`
  - `application.yml` provider sections
- Tests:
  - `TemplateResolverTest`
  - `NotificationRetryServiceTest`
  - `NotificationListenerIT`

## 6. Exit Criteria
- Event-driven notifications are sent and tracked.
- Failures are retried with clear status visibility.
- Email/SMS/WhatsApp channels are production-configurable.

