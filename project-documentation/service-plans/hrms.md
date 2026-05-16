# HRMS App Module Detailed Implementation Plan

## 1. Scope and Current State
- Module: `hrms` (standalone Spring Boot app in repo)
- Current status: scaffold app with minimal test only.
- Observed mismatch risk:
  - Uses different Spring Boot generation baseline than main microservices parent.

## 2. Decision First
- This module needs an explicit role definition:
  1. API gateway shell
  2. local integration test orchestrator
  3. deprecated module to archive

## 3. If Retained: Workstreams

### A. Role Clarification and Architecture
1. Define target purpose in architecture docs.
2. Add boundaries (what goes here vs microservices).
3. Align dependency strategy with root parent.

### B. Version Alignment
1. Align Java version and Spring Boot version with platform policy.
2. Remove conflicting plugin/dependency patterns.
3. Ensure reproducible build in workspace and CI.

### C. Functional Implementation (Only if Needed)
1. If gateway:
   - route aggregation
   - auth token forwarding
   - centralized rate limit
2. If integration harness:
   - smoke test endpoints
   - health check and readiness suite

### D. Testing
1. Module-level smoke tests.
2. Dependency drift checks against root platform.

## 4. Milestones
- M1: Role decision approved
- M2: Build/version alignment
- M3: Purpose-specific implementation
- M4: Documentation and CI validation

## 5. Exit Criteria
- Module purpose is explicit and documented.
- No version conflicts with microservices stack.
- Either actively useful or intentionally retired.

