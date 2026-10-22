# REQ-002: API Status Endpoint

## 1. Problem Statement

The service currently exposes a basic hello endpoint, but it does not provide a dedicated endpoint to report its operational status.

## 2. Objective

Expose an HTTP endpoint that allows clients and monitoring systems to check whether the service is running.

## 3. Scope

### In Scope
- Add `GET /api/status`.
- Return HTTP `200 OK` when the service is operational.
- Return a JSON response containing the status.

### Out of Scope
- Adding Spring Boot Actuator.
- Checking external dependencies such as databases.
- Implementing authentication or authorization for this endpoint.

## 4. Functional Requirements

- FR-001: The service SHALL expose `GET /api/status`.
- FR-002: The endpoint SHALL return HTTP `200 OK` when the service is operational.
- FR-003: The response SHALL use `application/json`.
- FR-004: The response SHALL contain a `status` property with the value `UP`.

## 5. Non-Functional Requirements

- NFR-001: The endpoint SHALL NOT introduce unnecessary runtime dependencies.
- NFR-002: The endpoint SHALL follow the existing project architecture and coding standards.
- NFR-003: The implementation SHALL be covered by automated tests.

## 6. Acceptance Criteria

- AC-001: Given the service is running, when a client sends `GET /api/status`, the response status is `200 OK`.
- AC-002: Given the service is running, when a client sends `GET /api/status`, the response content type is `application/json`.
- AC-003: Given the service is running, when a client sends `GET /api/status`, the response body is `{"status":"UP"}`.
- AC-004: Automated tests verify the endpoint's HTTP status, content type and response body.
- AC-005: Existing endpoint behavior and tests remain unchanged.

## 7. Dependencies

- Existing Spring Boot application.
- Existing HTTP endpoint conventions and test infrastructure.

## 8. Assumptions and Risks

- The endpoint reports application availability only; it does not guarantee that external dependencies are healthy.
- The response format and endpoint path are assumed to be acceptable to API consumers.

## 9. Definition of Done

- Implementation and automated tests are complete.
- All relevant tests pass.
- Existing project quality gates pass.
- The change has been reviewed and is documented in Git.
