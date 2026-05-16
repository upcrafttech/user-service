# Keycloak Provider Detailed Implementation Plan

## 1. Scope and Current State
- Module: `keycloak-provider`
- Current status: config + admin service exist; some operations return `null` on failure.
- Purpose: shared adapter for Keycloak admin and identity operations.

## 2. Outcomes
- Stable integration layer used by auth and user services.
- Typed, reliable contract with explicit error handling.

## 3. Workstreams

### A. API and Error Contract Hardening
1. Replace `null` return values with typed responses or exceptions.
2. Introduce module-level exception hierarchy:
   - `KeycloakClientException`
   - `KeycloakResourceNotFoundException`
   - `KeycloakConflictException`
3. Standardize result object for create/update/delete role operations.

### B. Service Capability Expansion
1. Add methods:
   - update user attributes
   - enable/disable user
   - get user roles
   - sync role set
2. Add search methods by email and external id.

### C. Reliability
1. Add timeout settings and connection pool tuning.
2. Add retry for transient failures.
3. Add correlation-id logging for cross-service debugging.

### D. Security
1. Ensure secrets are loaded via env/secret manager only.
2. Prevent sensitive data logging.
3. Add optional realm/client validation on startup.

### E. Testing
1. Unit tests for mapper and response parsing.
2. Integration tests against Keycloak test container.
3. Failure-mode tests for non-2xx responses.

## 4. Milestones
- M1: Remove null returns and add typed errors
- M2: Expand user/role lifecycle methods
- M3: Reliability and logging improvements
- M4: Integration tests and hardening

## 5. Deliverables
- Updated:
  - `service/KeycloakAdminService.java`
  - `config/KeycloakConfig.java` (if needed for timeout/pool)
- New:
  - `exception/* keycloak exceptions */`
  - response DTOs for operation status
- Tests:
  - `KeycloakAdminServiceIT`

## 6. Exit Criteria
- No silent failures or null ambiguity.
- Consumers can handle typed success/failure reliably.

