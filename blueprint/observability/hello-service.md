# Observability record — hello-service 0.1.0

## Operating context

| Field | Value |
| --- | --- |
| System | `hello-service` `0.1.0` — `blueprint/delivery/release-0.1.0.md` |
| Environment | `local` — single-node Podman on Windows/WSL |
| Traffic profile | Manual smoke tests only; no production users, no scheduled traffic |
| Operating model | Single maintainer; no on-call rotation, no paging service, no dashboards |
| Normal state | Container `Up`; `GET /api/hello` -> `200` `Hello, World!`; `GET /api/status` -> `200` `{"status":"UP"}`; startup log lines present |
| Scope boundary | Local PoC; remote publication to Quay remains postponed and is not claimed |

The phase 13 signals are evaluated against this context. The PoC has no production
operating context, so detection is manual and the runbook is the response path. This record
does not claim an automated monitoring system.

## Instrumentation

### Logs

- Configuration: `src/main/resources/logback-spring.xml`.
- Pattern: `timestamp level [thread] [requestId=<MDC>] logger - message`.
- Destination: standard output, collected with `podman logs hello-service`.
- Fields: ISO-8601 timestamp with offset, level, thread, `requestId` from the SLF4J MDC
  (`-` when absent), abbreviated logger and message.
- Content: startup and lifecycle logs, framework warnings/errors, and any application log.
  The application does not log request payloads.
- Limitation: there is no per-request access log. A successful request emits no
  application log line, so correlation is observable on error paths, not on every request.

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

- Metrics: none. No metrics endpoint, registry or exporter is configured. Deferred as
  `IMP-001`.
- Traces: none. The service is a single process with no downstream calls, so a distributed
  trace would add infrastructure without a question it can answer. Revisit if the service
  gains dependencies.

### Health checks

| Check | Endpoint or command | Criteria | Evidence |
| --- | --- | --- | --- |
| Liveness | `GET /api/status` | `200`, `application/json`, body exactly `{"status":"UP"}` while the process serves HTTP; no external dependency consulted | Verified 2026-10-30 on `127.0.0.1:18080` |
| Startup | `podman logs hello-service` | Log contains `Starting HelloApplication v0.1.0`, `Tomcat started on port 8080` and `Started HelloApplication` | Verified 2026-10-30 |
| Correlation | `POST /api/hello` with a known `X-Request-ID` | Response echoes the header and the resulting warning log carries the same `requestId` | Verified 2026-10-30 with `p13-correlation-test` |
| Readiness | Not implemented | The service has no database, broker, cache or downstream dependency; startup work is the HTTP listener. Liveness is sufficient for the local PoC. Orchestrator-level readiness is deferred as `IMP-003`. | This record |

`/api/status` is a liveness signal only. It is not a readiness check and it does not report
dependency health.

## Failure modes, detection and response

The design names the startup/registration failure in
`blueprint/design/hello-endpoint.md` (section 9). The remaining failure modes come from
the operating context above. Each row is a manual detection check, not an automated alert.

| ID | Failure mode | Detection check (manual alert) | Response | Runbook |
| --- | --- | --- | --- | --- |
| FM-01 | Process or container not running | `podman ps --filter name=hello-service` does not list the container, or the probe gets connection refused | Start the versioned container and verify both endpoints | [RB-01](#rb-01) |
| FM-02 | Liveness failure | `GET /api/status` returns non-`200` or times out while the container is listed | Inspect logs; restart; if persistent, roll back | [RB-02](#rb-02) |
| FM-03 | Hello contract failure | `GET /api/hello` returns non-`200` or a body other than `Hello, World!` | Inspect logs; restart; if persistent, roll back | [RB-03](#rb-03) |
| FM-04 | Startup or route-registration failure (named in the design) | Container exits or restarts; logs show a startup exception before `Started HelloApplication` | Read the startup error; correct the configuration or artifact; redeploy; roll back if the release is suspect | [RB-04](#rb-04) |
| FM-05 | `5xx` response | A probe returns `5xx`, or a `5xx` is reported by a consumer | Capture `X-Request-ID`; search the logs; restart if persistent; roll back if the release is suspect | [RB-05](#rb-05) |
| FM-06 | Port conflict or unreachable mapping | Container is `Up` but `127.0.0.1:18080` is unreachable, or `podman run` fails on the port | Check the port mapping and the host listener; recreate the container on a free port | [RB-06](#rb-06) |
| FM-07 | Correlation failure | A known `X-Request-ID` is not echoed, or the matching error log does not carry it | Inspect the filter and logs; restart; if persistent, roll back | [RB-07](#rb-07) |

The phase 13 gate is met for the local scope: every failure mode above has a detection
condition, a response and a runbook step. The limitation is that detection is manual and no
automated alert fires. See [Gate](#gate).

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
3. Wait for startup and verify:
   ```powershell
   Start-Sleep -Seconds 3
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
3. If the process is running, restart it:
   ```powershell
   podman restart hello-service
   Start-Sleep -Seconds 3
   Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
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
3. If a matching error is found, use its message and stack trace to identify the cause.
   If no matching log exists, note the absence: successful requests are not access-logged.
4. Restart and retry the request. If the `5xx` persists, roll back with
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
2. Confirm the response header contains `runbook-correlation-check` and the warning log
   carries `[requestId=runbook-correlation-check]`:
   ```powershell
   podman logs --tail 20 hello-service
   ```
3. If the header is missing, restart the container and repeat. If the value is still not
   echoed, roll back with [RB-08](#rb-08).

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
| Startup log | `podman logs hello-service` | `Starting HelloApplication v0.1.0`; `Tomcat started on port 8080`; `Started HelloApplication` |

The rehearsal container was started from the promoted local image and removed after
verification. The commands above reproduce the checks.

## Gate

**State: CLOSED (local manual detection; no automated alerting).**

The phase 13 quality gate is met for the local scope:

- Every failure mode in the table has a detection check, a response and a runbook step.
- Every detection check names the response it triggers and links to a runbook anchor.
- A responder who was not on call can follow the runbook without asking the author.

Limitations recorded, not hidden:

- No automated alerting, metrics backend, dashboard, trace collector or paging path exists.
- No per-request access log exists; correlation is demonstrated on error paths.
- No readiness check exists; liveness is sufficient because the service has no external
  dependencies.
- The review is a solo-maintainer self-review, not an independent approval. See
  `blueprint/reviews/observability-closure.md`.

This gate does not claim a production monitoring capability. It records what the local PoC
can detect and what it cannot.

## Traceability

| Item | Reference |
| --- | --- |
| Requirements observed | `REQ-HELLO-001`, `REQ-STATUS-001` |
| Deployed system | `blueprint/deployment/local-0.1.0.md` |
| Design failure mode | `blueprint/design/hello-endpoint.md` section 9 (startup/registration failure) |
| Existing observability decision | `blueprint/architecture/api-status-endpoint.md` section "Observability" |
| Correlation residual risk | `blueprint/reviews/api-status-endpoint.md` (`X-Request-ID` log injection) |
| Improvement candidates | `IMP-001` (metrics), `IMP-003` (readiness), `IMP-004` (automated alerting), `IMP-005` (access logging) |
| Change record | `blueprint/changes/p13-observability.md` |
| Review record | `blueprint/reviews/observability-closure.md` |
