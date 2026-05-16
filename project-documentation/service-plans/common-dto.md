# Common DTO Module Detailed Implementation Plan

## 1. Scope and Current State
- Module: `common-dto`
- Current status: shared DTOs, exception hierarchy, and REST client base exist.
- Pending platform needs:
  - event contract models
  - helper utilities
  - test utility support

## 2. Outcomes
- Stable, versioned shared contracts across all services.
- Reduced duplication in validation, mapping, and event payload handling.

## 3. Workstreams

### A. Event Contract Package
1. Add shared event base:
   - `eventId`, `eventType`, `occurredAt`, `tenantId`, `sourceService`, `version`
2. Add specific events:
   - `TaskCreatedEvent`
   - `TaskApprovedEvent`
   - `PayrollCompletedEvent`
   - `LeaveApprovedEvent`
3. Add schema versioning policy.

### B. Validation and Mapping Utilities
1. Add common validation helpers:
   - UUID validation
   - date range validation
   - required field checks
2. Add mapping helpers for repetitive DTO transformations.

### C. Shared API Response Standards
1. Review `ApiResponse<T>` usage consistency.
2. Add standardized pagination wrapper (if needed).
3. Add error code registry reference constants.

### D. Test Utilities
1. Add test factory classes for DTOs and common payloads.
2. Add JSON serialization helpers for event testing.
3. Add shared assertion helpers for `ApiResponse`.

### E. Backward Compatibility Process
1. Add semver guideline for module.
2. Add deprecation process for DTO fields.
3. Add changelog note requirements for contract changes.

## 4. Milestones
- M1: Event contracts and version policy
- M2: Validation/mapping helpers
- M3: Test utility package
- M4: Compatibility and usage documentation

## 5. Deliverables
- New packages:
  - `com.upcraft.event.*`
  - `com.upcraft.util.validation.*`
  - `com.upcraft.testutil.*`
- Updated:
  - DTO docs and examples
- Tests:
  - serialization/compatibility tests for events and DTOs

## 6. Exit Criteria
- All event-capable services reuse shared event classes.
- DTO contract changes are versioned and documented.

