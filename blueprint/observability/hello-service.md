# Observability record — hello-service 0.1.0

## Operating context

| Field | Value |
| --- | --- |
| System | `hello-service` `0.1.0` — `blueprint/delivery/release-0.1.0.md` |
| Environment | `local` — single-node Podman on Windows/WSL |
| Traffic profile | Manual smoke tests only; no production users, no scheduled traffic |
| Operating model | Single maintainer; no on-call rotation, no paging service, no dashboards |
| Normal state | Container `Up`; `GET /api/hello` -> `200` `Hello, World!`; `GET /api/status` -> `200` `{"status":"UP"}`; `/actuator/health/liveness` and `/actuator/health/readiness` -> `200`; `/actuator/prometheus` -> `200`; one `access method=...` line per request; startup log lines present |
| Scope boundary | Local PoC; remote publications of `0.1.0` were completed on 2026-11-01 by CD runs 41332436297 and 41334455398 (current digest `quay.io/odysseia/hello-service@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`); the observability signals are local and there is no monitoring backend or paging path |

The phase 13 signals are evaluated against this context. The PoC has no production operating
context. The service now emits liveness/readiness probes, Prometheus metrics and a correlated
per-request access log, and `ops/prometheus/hello-service-alerts.yml` defines alert rules for
the failure modes below. There is still no deployed Prometheus, Alertmanager, blackbox exporter,
dashboard or paging path: the rules are reviewable configuration, not a live alerting
capability. The remote publications of `0.1.0` are recorded in
`blueprint/changes/p11-remote-publication.md`; they did not add observability infrastructure.

## Instrumentation

### Logs

- Configuration: `src/main/resources/logback-spring.xml`.
- Pattern: `timestamp level [thread] [requestId=<MDC>] logger - message`.
- Destination: standard output, collected with `podman logs hello-service`.
- Fields: ISO-8601 timestamp with offset, level, thread, `requestId` from the SLF4J MDC
  (`-` when absent), abbreviated logger and message.
- Content: startup and lifecycle logs, framework warnings/errors, application logs and the
  `RequestAccessLogFilter` line. The application does not log request payloads.
- Access log: `src/main/java/com/example/hello/RequestAccessLogFilter.java` emits exactly one
  INFO line per request:
  `access method=<METHOD> path=<getRequestURI()> status=<status> duration_ms=<ms> requestId=<id|->`.
  It runs at `Ordered.HIGHEST_PRECEDENCE + 10`, after `RequestIdFilter`, and logs in a `finally`
  block. The query string, headers, request body and response body are not logged. If the chain
  throws, the line records `status=500` and the original exception is rethrown.
- Limitation: the access log adds one stdout line per request and has no sampling, rotation or
  retention policy. That is acceptable for the local PoC; a production deployment needs a
  logging/retention decision.

### Correlation

- Component: `src/main/java/com/example/hello/RequestIdFilter.java`.
- Incoming `X-Request-ID` is honored when present and non-blank; otherwise a random UUID is
  generated. The same value is echoed in the `X-Request-ID` response header and bound to
  the MDC key `requestId` for the duration of the request. The MDC entry is removed in a
  `finally` block.
- Verified on 2026-10-30: `POST /api/hello` with `X-Request-ID: p13-correlation-test`
  returned `405` with the same response header and produced:
  `WARN ... [requestId=p13-correlation-test] ... HttpRequestMethodNotSupportedException`.
- Residual risk: the client-controlled header is logged without validation, as recorded in
  `blueprint/reviews/api-status-endpoint.md` (log-injection risk). Accepted for the local
  PoC.

### Metrics and traces

- Metrics: Micrometer with the Prometheus registry. `build.gradle.kts` declares
  `spring-boot-starter-actuator` and `micrometer-registry-prometheus`; the versions are pinned
  by the Spring Boot BOM and recorded in `gradle.lockfile`. `/actuator/prometheus` exposes the
  Prometheus text format, including JVM memory metrics and the common tag
  `application="hello-service"`. The endpoint is tested by
  `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics`.
- Traces: none. The service is a single process with no downstream calls, so a distributed
  trace would add infrastructure without a question it can answer. Revisit if the service
  gains dependencies.
- Alert rules: `ops/prometheus/hello-service-alerts.yml` defines eight rules over the
  application, JVM and blackbox signals; `ops/prometheus/README.md` documents the required
  scrape/probe jobs. The rules are configuration evidence only: no Prometheus, Alertmanager,
  blackbox exporter or paging path is deployed.
- The access log increments `hello_service_access_log_missing_request_id_total` when a line is
  emitted without a `requestId`, giving `FM-07` a metric signal. It is exposed through the same
  Prometheus endpoint.
- No metrics backend or dashboard is configured; the endpoint is a pull target for a future
  Prometheus server.

### Health checks

| Check | Endpoint or command | Criteria | Evidence |
| --- | --- | --- | --- |
| Application liveness | `GET /api/status` | `200`, `application/json`, body exactly `{"status":"UP"}` while the process serves HTTP; no external dependency consulted | Verified 2026-10-30 on `127.0.0.1:18080`; pre-existing tests remain green at `1b1db72` |
| Actuator liveness | `GET /actuator/health/liveness` | `200` and a body containing `"status":"UP"` | `ActuatorEndpointTest.livenessProbeReportsUp` at `1b1db72`; jar returned `200 {"status":"UP"}` on `127.0.0.1:18081` on 2026-11-05 |
| Readiness | `GET /actuator/health/readiness` | `200` and a body containing `"status":"UP"` when the context is ready; no external dependency is checked today | `ActuatorEndpointTest.readinessProbeReportsUp` at `1b1db72`; jar returned `200 {"status":"UP"}` on `127.0.0.1:18081` on 2026-11-05 |
| Metrics | `GET /actuator/prometheus` | `200`, Prometheus text containing `jvm_memory_used_bytes` and `application="hello-service"` | `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics` at `1b1db72`; jar returned `200` with 16,179 bytes on 2026-11-05 |
| Disclosure boundary | `GET /actuator/env` and `GET /actuator/health` | `/actuator/env` is `404`; `/actuator/health` has no `components` object | `ActuatorEndpointTest` at `1b1db72` |
| Startup | `podman logs hello-service` | Log contains `Starting HelloApplication v0.1.0`, `Tomcat started on port 8080` and `Started HelloApplication` | Verified 2026-10-30 |
| Correlation and access log | `POST /api/hello` with a known `X-Request-ID` | Response echoes the header and exactly one access-log line carries the same `requestId` | `RequestAccessLogFilterTest` at `1b1db72`; live correlation verified 2026-10-30 with `p13-correlation-test` and 2026-11-05 on the jar with `d-jar-evidence` |
| Missing-correlation counter | `GET /actuator/prometheus` | `hello_service_access_log_missing_request_id_total` is present; normal HTTP traffic resolves an id, so it stays `0.0` | Jar metric on 2026-11-05; non-zero path covered by `RequestAccessLogFilterTest.countsEntriesWithoutRequestId` |

`/api/status` remains the application-level liveness signal only. It is not a readiness check
and it does not report dependency health. `/actuator/health/readiness` is the separate
orchestrator-facing readiness group; because the service has no external dependency, it is
`UP` when the application context is ready.

## Failure modes, detection and response

The design names the startup/registration failure in
`blueprint/design/hello-endpoint.md` (section 9). The remaining failure modes come from
the operating context above. Each row has a manual detection check and a defined alert rule.
The Actuator probes, Prometheus metrics and access log make detection more precise, but no
deployed Prometheus evaluates the rules and no paging path exists.

| ID | Failure mode | Defined alert rule | Detection check (manual signal) | Response | Runbook |
| --- | --- | --- | --- | --- | --- |
| FM-01 | Process or container not running | `HelloServiceDown` | `podman ps --filter name=hello-service` does not list the container, or the Actuator liveness probe gets connection refused | Start the versioned container and verify the probes and both application endpoints | [RB-01](#rb-01) |
| FM-02 | Liveness failure | `HelloServiceLivenessProbeFailing` | `GET /actuator/health/liveness` or `GET /api/status` returns non-`200` or times out while the container is listed | Inspect logs; restart; if persistent, roll back | [RB-02](#rb-02) |
| FM-03 | Hello contract failure | `HelloServiceHelloContractFailing` | `GET /api/hello` returns non-`200` or a body other than `Hello, World!` | Inspect the access log and error logs; restart; if persistent, roll back | [RB-03](#rb-03) |
| FM-04 | Startup or route-registration failure (named in the design) | `HelloServiceRestarting` | Container exits or restarts; logs show a startup exception before `Started HelloApplication` | Read the startup error; correct the configuration or artifact; redeploy; roll back if the release is suspect | [RB-04](#rb-04) |
| FM-05 | `5xx` response | `HelloServiceHigh5xxRate`, `HelloServiceLatencyHigh` | The access log contains `status=5xx`, a probe returns `5xx`, or a consumer reports one | Capture `X-Request-ID`; search the access/error logs; restart if persistent; roll back if the release is suspect | [RB-05](#rb-05) |
| FM-06 | Port conflict or unreachable mapping | `HelloServiceReadinessProbeFailing` | Container is `Up` but `127.0.0.1:18080` is unreachable, or `podman run` fails on the port | Check the port mapping and the host listener; recreate the container on a free port | [RB-06](#rb-06) |
| FM-07 | Correlation failure | `HelloServiceCorrelationMissing` | A known `X-Request-ID` is not echoed, the access/error log does not carry it, or the missing-request-id counter increases | Inspect the filter and logs; restart; if persistent, roll back | [RB-07](#rb-07) |

The phase 13 gate remains met for the local scope with an explicit limitation: every failure
mode above has a detection condition, a defined alert rule, a response and a runbook step, but
the rules are not evaluated or routed by a deployed monitoring stack. The access log also
exposes `hello_service_access_log_missing_request_id_total` for `FM-07`. No dashboard or
paging path exists. See [Gate](#gate).

## Runbook

Runbook commands are written for Windows PowerShell 5.1 with Podman. The image and version
are the promoted local artifacts from `blueprint/delivery/release-0.1.0.md`. The rollback
image is `localhost/hello-service:0.0.1-SNAPSHOT` from commit `f2b03e5`.

<a id="rb-01"></a>
### RB-01 — Container not running

1. Check the container:
   ```powershell
   podman ps -a --filter name=hello-service
   ```
2. If it is absent, start it:
   ```powershell
   podman run -d --name hello-service -p 18080:8080 localhost/hello-service:0.1.0
   ```
3. Wait for startup and verify the probes and application endpoints:
   ```powershell
   Start-Sleep -Seconds 3
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health/liveness' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health/readiness' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -UseBasicParsing
   ```
4. If the container exits immediately, go to [RB-04](#rb-04). If it stays up but the
   probes fail, go to [RB-02](#rb-02) or [RB-03](#rb-03).

<a id="rb-02"></a>
### RB-02 — Liveness failure

1. Confirm the container state:
   ```powershell
   podman ps --filter name=hello-service
   ```
2. Read the recent logs:
   ```powershell
   podman logs --tail 50 hello-service
   ```
3. If the process is running, restart it and re-check both the application and Actuator
   liveness signals:
   ```powershell
   podman restart hello-service
   Start-Sleep -Seconds 3
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health/liveness' -UseBasicParsing
   ```
4. If the restart does not restore `200 {"status":"UP"}`, roll back with
   [RB-08](#rb-08).

<a id="rb-03"></a>
### RB-03 — Hello contract failure

1. Capture the full response:
   ```powershell
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -UseBasicParsing
   ```
2. Read the logs and note the `requestId` if one is present:
   ```powershell
   podman logs --tail 50 hello-service
   ```
3. Restart and repeat the probe. If the body or status is still wrong, roll back with
   [RB-08](#rb-08).

<a id="rb-04"></a>
### RB-04 — Startup or route-registration failure

1. Read the startup logs:
   ```powershell
   podman logs hello-service
   ```
2. Identify the first exception or the last line before exit. A missing
   `Started HelloApplication` line means startup did not complete.
3. If the error is environmental (port, permissions, missing image), correct it and repeat
   [RB-01](#rb-01).
4. If the error is in the artifact, roll back with [RB-08](#rb-08) and record the defect in
   `blueprint/improvements/continuous-improvements.md`.

<a id="rb-05"></a>
### RB-05 — `5xx` response

1. Capture the `X-Request-ID` from the failing response.
2. Search the container logs for that value:
   ```powershell
   podman logs hello-service | Select-String '<request-id>'
   ```
3. Find the `access method=... status=5xx` line for the same request, then use the nearby
   error message and stack trace to identify the cause. If no access line exists, the request
   did not pass through the application filter chain; check the container and port first.
4. Inspect `/actuator/prometheus` for the `http_server_requests` meters if a metrics consumer
   is available, then restart and retry. If the `5xx` persists, roll back with
   [RB-08](#rb-08) and record the incident.

<a id="rb-06"></a>
### RB-06 — Port conflict or unreachable mapping

1. Inspect the port mapping:
   ```powershell
   podman port hello-service
   ```
2. Check the host listener:
   ```powershell
   Get-NetTCPConnection -LocalPort 18080 -ErrorAction SilentlyContinue
   ```
3. Recreate the container on a free host port, for example `18081`:
   ```powershell
   podman stop hello-service
   podman rm hello-service
   podman run -d --name hello-service -p 18081:8080 localhost/hello-service:0.1.0
   ```
4. Verify on the new port and record the change if it becomes permanent.

<a id="rb-07"></a>
### RB-07 — Correlation failure

1. Send a request with a known identifier:
   ```powershell
   try {
     Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -Method Post `
       -Headers @{'X-Request-ID'='runbook-correlation-check'} -UseBasicParsing
   } catch {
     $_.Exception.Response.Headers['X-Request-ID']
   }
   ```
2. Confirm the response header contains `runbook-correlation-check` and both the access line
   and the warning log carry the same `requestId`:
   ```powershell
   podman logs --tail 20 hello-service
   ```
3. If the header is missing, restart the container and repeat. If the value is still not
   echoed, roll back with [RB-08](#rb-08).

<a id="rb-09"></a>
### RB-09 �?" Inspect the observability signals

1. Check the probes and health details:
   ```powershell
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health/liveness' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/health/readiness' -UseBasicParsing
   ```
2. Check the Prometheus exposition and the application tag:
   ```powershell
   (Invoke-WebRequest -Uri 'http://127.0.0.1:18080/actuator/prometheus' -UseBasicParsing).Content | Select-String 'jvm_memory_used_bytes|application="hello-service"'
   ```
3. Inspect the last access lines:
   ```powershell
   podman logs --tail 20 hello-service | Select-String 'access method='
   ```
4. Confirm `/actuator/env` is not exposed (`404`). If any of these signals is missing, use
   [RB-04](#rb-04) for a startup/configuration failure or [RB-08](#rb-08) if the release is
   suspect.

<a id="rb-08"></a>
### RB-08 — Roll back to the previous version

1. Stop and remove the current container:
   ```powershell
   podman stop hello-service
   podman rm hello-service
   ```
2. Start the rollback image:
   ```powershell
   podman run -d --name hello-service -p 18080:8080 localhost/hello-service:0.0.1-SNAPSHOT
   ```
   If the image is not present locally, rebuild it from commit `f2b03e5` using the build
   procedure in `blueprint/deployment/local-0.1.0.md` with the rollback version, then
   confirm the image ID
   `e7eb44f50a87310723f1b90d24f1382462ce02adae64fab59068c0dbd8d9f658` from
   `blueprint/delivery/release-0.1.0.md` before running it. If it cannot be reproduced,
   stop and record the incident; do not run an unverified artifact.
3. Verify both endpoints:
   ```powershell
   Start-Sleep -Seconds 3
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -UseBasicParsing
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
   ```
4. Record the rollback in the incident or change record. The target identity and the
   original rehearsal are in `blueprint/delivery/release-0.1.0.md`.

## Evidence log

| Check | Command or observation | Result |
| --- | --- | --- |
| Container start | `podman run -d --name hello-service -p 18080:8080 localhost/hello-service:0.1.0` | Container `Up`; ID `61047d8d6f7950b0202a73035356dc6d2b6e7b33acc6dd0c76cfeaa0aa43738a` |
| Liveness | `Invoke-WebRequest http://127.0.0.1:18080/api/status` | `200`, `application/json`, `{"status":"UP"}`, `X-Request-ID` present |
| Hello contract | `Invoke-WebRequest http://127.0.0.1:18080/api/hello` | `200`, `text/plain;charset=UTF-8`, `Hello, World!` |
| Unknown path | `Invoke-WebRequest http://127.0.0.1:18080/api/nope` | `404` |
| Unsupported method | `POST /api/hello` | `405`; warning log with generated `requestId` |
| Correlation | `POST /api/hello` with `X-Request-ID: p13-correlation-test` | `405`; response header echoed; log line `[requestId=p13-correlation-test]` |
| Actuator liveness/readiness | `ActuatorEndpointTest` at `1b1db72` | Both `200`, body contains `"status":"UP"` |
| Prometheus metrics | `ActuatorEndpointTest` at `1b1db72` | `200`; `jvm_memory_used_bytes`; `application="hello-service"` |
| Actuator exposure | `ActuatorEndpointTest` at `1b1db72` | `/actuator/env` -> `404`; `/actuator/health` has no `components` |
| Access log | `RequestAccessLogFilterTest` at `1b1db72` | One line with method/path/status/duration/requestId; query omitted; `status=500` and rethrow on failure; missing-id counter increments |
| Alert rules | `AlertRulesTest` at `1b1db72` | Eight rules parse; each has expression, duration, severity, service and runbook |
| Jar liveness/readiness | `java -jar build\libs\hello-service-0.1.0.jar --server.port=18081`; `GET /actuator/health/liveness`, `/readiness` on 2026-11-05 | Both `200`, body `{"status":"UP"}` |
| Jar Prometheus/counter | `GET /actuator/prometheus` on 2026-11-05 | `200`, 16,179 bytes; `jvm_memory_used_bytes`; `application="hello-service"`; `hello_service_access_log_missing_request_id_total 0.0` |
| Jar access log | `GET /api/hello` with `X-Request-ID: d-jar-evidence` on 2026-11-05 | `200`, `Hello, World!`; four access-log lines, the hello line carrying `requestId=d-jar-evidence` |
| Startup log | `podman logs hello-service` | `Starting HelloApplication v0.1.0`; `Tomcat started on port 8080`; `Started HelloApplication` |

The rehearsal container was started from the promoted local image and removed after
verification. The commands above reproduce the checks.

## Gate

**State: CLOSED (local scope; automated probes, metrics, access log and alert definitions;
manual evaluation/paging).**

The phase 13 quality gate is met for the local scope:

- Every failure mode in the table has a detection check, a defined alert rule, a response and a
  runbook step.
- Every detection check names the response it triggers and links to a runbook anchor.
- A responder who was not on call can follow the runbook without asking the author.
- Liveness/readiness probes, the Prometheus endpoint, the correlated access log and eight alert
  rules are implemented and covered by automated tests at `1b1db72`.

Limitations recorded, not hidden:

- No Prometheus, Alertmanager, blackbox exporter, metrics backend, dashboard, trace collector or
  paging path is deployed. The rules are configuration evidence, not live alerts; no rule is
  evaluated or routed.
- No Prometheus server scrapes the endpoint; the local runbook checks it manually.
- Thresholds are local-PoC defaults and have not been tuned against production traffic.
- The access log has no sampling, rotation, shipping or retention policy.
- Actuator endpoints are unauthenticated inside the local boundary; health details are hidden,
  but public exposure still requires authentication or network policy.
- Readiness is `UP` because the service has no external dependency; it must be extended when a
  dependency is added.
- The review is a solo-maintainer self-review, not an independent approval. See
  `blueprint/reviews/observability-improvements.md`; the original phase 13 self-review remains
  in `blueprint/reviews/observability-closure.md`.

This gate does not claim a production monitoring capability. It records what the local PoC
can detect and what it cannot.

## Traceability

| Item | Reference |
| --- | --- |
| Requirements observed | `REQ-HELLO-001`, `REQ-STATUS-001`, `REQ-OBS-001`, `REQ-OBS-002` |
| Requirement for the new signals | `blueprint/requirements/observability-improvements.md` |
| Requirement for alert definitions | `blueprint/requirements/observability-alerting.md` |
| Deployed system | `blueprint/deployment/local-0.1.0.md` |
| Design failure mode | `blueprint/design/hello-endpoint.md` section 9 (startup/registration failure) |
| Observability architecture | `blueprint/architecture/observability-improvements.md`; ADR-014 |
| Existing observability decision | `blueprint/architecture/api-status-endpoint.md` section "Observability" |
| Correlation residual risk | `blueprint/reviews/api-status-endpoint.md` (`X-Request-ID` log injection) |
| Improvement items | `IMP-001` (metrics), `IMP-003` (readiness), `IMP-004` (alert definitions) and `IMP-005` (access logging) implemented; live evaluation/paging remains absent |
| Test evidence | `blueprint/testing/observability-improvements.md` (`1b1db72`; 28 tests) |
| Alert rules | `ops/prometheus/hello-service-alerts.yml`; `ops/prometheus/README.md` |
| Change record | `blueprint/changes/p13-observability.md`; `blueprint/changes/observability-improvements.md` |
| Review record | `blueprint/reviews/observability-closure.md`; `blueprint/reviews/observability-improvements.md` |
