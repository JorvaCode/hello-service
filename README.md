# hello-service

PoC mínimo de servicio HTTP usado para recorrer las fases del Software Engineering
Blueprint. No es un servicio de producción; la versión `0.1.0` tiene un registro de
release local y dos publicaciones remotas en Quay registradas el 2026-11-01.

## Alcance de cierre

El cierre aprobado cubre la PoC de `hello-service` con configuración de CI, despliegue
local y publicaciones remotas mediante `cd.yml`.

- Las publicaciones remotas en Quay se realizaron el 2026-11-01 con `cd.yml` (runs
  41332436297 y 41334455398); el digest actual es
  `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`.
- El workflow `cd.yml` se ejecutó al fusionar los PR #5 y #6; ahora solo admite
  `workflow_dispatch` para evitar publicaciones automáticas.
- La revisión independiente de `REQ-STATUS-001` no se realizó; el cierre de P-07 registra
  una autoevaluación del mantenedor y no una aprobación independiente.
- No se crea `CHANGELOG.md`.
- El Blueprint no se declara formalmente validado.
- Los gates se cierran solo con su evidencia: P-06, P-07, P-10, P-11, P-13 y P-14
  están cerrados; P-07, P-11, P-13 y P-14 registran autoevaluación del mantenedor porque
  no hay revisor independiente; P-02 sigue abierto (ver `blueprint/gates.md`).

## Qué es y relación con el Blueprint

`hello-service` demuestra la implementación y verificación de dos requisitos:

- `REQ-HELLO-001`: `GET /api/hello`.
- `REQ-STATUS-001`: `GET /api/status` como señal de liveness.

El Blueprint se conserva como metodología reutilizable en `blueprint/`. Esta PoC no
demuestra por sí sola que el Blueprint esté formalmente validado.

## Requisitos previos

Entorno registrado para la evidencia actual:

- Windows y PowerShell 5.1.
- Java 21.
- Gradle Wrapper 8.14.2 incluido en el repositorio.
- Caché local de dependencias si se ejecuta Gradle con `--offline`.

Las pruebas locales documentadas no requieren servicios externos, base de datos, secretos
ni configuración de Quay; la publicación remota usa la configuración de Quay del
repositorio.

## Ejecución local

Procedimiento verificado el 2026-10-30 sobre `870500c` con Java 21 y Podman. Desde la
raíz del repositorio en Windows PowerShell 5.1:

```powershell
.\gradlew.bat clean build --offline --no-daemon --console=plain
$sha = (git rev-parse HEAD).Trim()
podman build --build-arg APP_VERSION=0.1.0 --build-arg CI_REPOSITORY=JorvaCode/hello-service --build-arg CI_SHA=$sha -t localhost/hello-service:0.1.0 .
podman run -d --name hello-service -p 18080:8080 localhost/hello-service:0.1.0
Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -UseBasicParsing
Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
podman stop hello-service
podman rm hello-service
```

Prerrequisitos: Java 21, una máquina Podman en ejecución y las dependencias de Gradle en
la caché local para `--offline`.

Evidencia: `blueprint/deployment/local-0.1.0.md` registra el despliegue, la verificación
posterior y el rollback ejercitado. `blueprint/testing/api-status-endpoint.md` conserva
la ejecución histórica en `127.0.0.1:18080`.

## Pruebas

Comando registrado para Windows PowerShell 5.1:

```powershell
.\gradlew.bat clean test --offline --no-daemon --console=plain
```

El flag `--offline` requiere que las dependencias estén disponibles en la caché local.
No se documenta una variante equivalente para otros shells que incluya los mismos
parámetros que el comando de PowerShell.

Resultado registrado en `blueprint/testing/api-status-endpoint.md`:

- `BUILD SUCCESSFUL`.
- 17 tests, 0 fallos, 0 errores, 0 omitidos.
- Base histórica: commit `f2b03e5f8d8306192922051dbf6359e23a9a0ee2`, fecha 2026-10-27.
- Repetición: 17 tests sobre `870500c` el 2026-10-30, como parte de la promoción local.

## Endpoints implementados

| Endpoint | Comportamiento documentado | Evidencia |
|---|---|---|
| `GET /api/hello` | `200`, `text/plain`, cuerpo `Hello, World!`; métodos no soportados `405`; ruta desconocida `404` | `blueprint/requirements/hello-endpoint.md`, `blueprint/reviews/hello-endpoint.md` |
| `GET /api/status` | `200`, `application/json`, cuerpo exacto `{"status":"UP"}`, `X-Request-ID`; `HEAD`/`OPTIONS` permitidos; métodos no soportados `405` con `Allow` que contiene `GET` | `blueprint/requirements/api-status-endpoint.md`, `blueprint/testing/api-status-endpoint.md` |

`/api/status` es una señal de liveness, no un readiness check ni un health check de
dependencias.

`REQ-HELLO-001` y `REQ-STATUS-001` tienen registro de revisión. La revisión de
`REQ-STATUS-001` la realizó el asistente OpenCode, no un revisor humano independiente;
esa aprobación sigue pendiente y no debe presentarse como obtenida.

## Estado de CI, despliegue local y publicación remota

| Área | Evidencia de configuración | Evidencia de ejecución real | Estado |
|---|---|---|---|
| CI | `.github/workflows/ci.yml` define un único job de build/test, construcción de la imagen y registro de su identidad; no contiene ruta de publicación | Ejecución real registrada: run 13 sobre `2e54476`, conclusión `success`, artefactos `artifact-metadata`, `hello-service-0.1.0` y `gitleaks-report` | Configuración validada por ejecución real; CI-02 `VERIFICADO` |
| Packaging | Versión `0.1.0`, JAR ejecutable, imagen OCI con labels de procedencia, identidad y escaneo gitleaks | Run 13: artefactos `artifact-metadata`, `hello-service-0.1.0` y `gitleaks-report`; conclusión `success` | P-10 `CLOSED` |
| Pruebas locales | Gradle Wrapper y suite automatizada | Ejecución registrada de 17 tests sobre `f2b03e5`, `72c2ad5` y `870500c` | Ejecución local documentada y repetida sobre el `HEAD` del cierre |
| Despliegue local | `Dockerfile` OCI, usuario no-root, puerto `8080` y entrypoint del JAR | Procedimiento reproducible verificado el 2026-10-30: build local, imagen `0.1.0`, contenedor como `appuser`, probes en `127.0.0.1:18080` y rollback a `0.0.1-SNAPSHOT` | Procedimiento verificado; registro en `blueprint/deployment/local-0.1.0.md` |
| Release local / P-11 | Versión `0.1.0`, imagen local y rollback a `0.0.1-SNAPSHOT` | Promoción local registrada el 2026-10-30; rollback ejercitado | P-11 `CLOSED` con autoevaluación; publicaciones remotas registradas; sin aprobación independiente |
| Observabilidad / P-13 | Logs estructurados con `requestId`, `/api/status` liveness y runbook manual; sin métricas, trazas ni alertas automatizadas | Registro en `blueprint/observability/hello-service.md`; prueba de correlación y runbook el 2026-10-30; 17 tests verdes en la revisión | P-13 `CLOSED` con detección manual; sin aprobación independiente |
| Mejora continua / P-14 | Registro de mejoras con propietario, artefacto y fuente de feedback; `IMP-001` … `IMP-005` diferidos | Registro y cierre en `blueprint/improvements/continuous-improvements.md`; revisión en `blueprint/reviews/continuous-improvement-closure.md` | P-14 `CLOSED` para el registro; bucles de mejora abiertos; sin aprobación independiente |
| CD / Quay | `.github/workflows/cd.yml` contiene pasos de OIDC, login y push a Quay; el único disparador es `workflow_dispatch` | Runs 41332436297 sobre `c7d3a86` y 41334455398 sobre `21772cc`: `success`; imagen `quay.io/odysseia/hello-service:0.1.0`, digest actual `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`, deploy por digest y smoke test `GET /api/hello` -> `200` | Publicación remota completada; publicación automática deshabilitada; promoción a segundo entorno no ejecutada; sin rollback remoto |

## Publicación remota

La publicación remota de `0.1.0` se realizó dos veces el 2026-11-01. La primera al
fusionar el PR #5, que disparó `cd.yml` sobre `c7d3a86`; la segunda al fusionar el PR #6
sobre `21772cc`, después de que el intento de deshabilitar el workflow no impidiera la
ejecución. El workflow usó autenticación OIDC y publicó en el namespace `odysseia`:

- Imagen: `quay.io/odysseia/hello-service:0.1.0`.
- Digest actual: `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`.
- Primer digest, superado: `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`.
- Deploy por digest y smoke test `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`,
  `Hello, World!`.
- CI runs 41332436321 y 41334455181; CD runs 41332436297 y 41334455398, ambos `success`.
- Promoción a un segundo entorno: mecanismo preparado, no ejecutado.
- Rollback remoto: no ejercitado; no hay release remoto anterior registrado.
- El registro local de `0.1.0` sigue en `blueprint/delivery/release-0.1.0.md`; los eventos
  remotos se registran en `blueprint/changes/p11-remote-publication.md`.
- `cd.yml` ahora solo admite `workflow_dispatch`; un push a `main` no publica.

El tag `0.1.0` es mutable: una ejecución manual posterior puede republicarlo con un digest
nuevo. El digest actual es la identidad inmutable del artefacto remoto actual.

## Observabilidad

El registro de observabilidad de la PoC local está en
`blueprint/observability/hello-service.md`. Documenta el contexto de operación, los logs
estructurados, la correlación por `X-Request-ID`, la señal de liveness `/api/status`, siete
modos de fallo y un runbook con ocho procedimientos.

- La detección es manual: no hay alertas automatizadas, métricas, trazas ni readiness probe.
- La correlación se verificó en una ruta de error (`POST /api/hello` -> `405`) con
  `X-Request-ID: p13-correlation-test`; el mismo valor aparece en la cabecera de respuesta y
  en el log.
- El runbook cubre arranque, liveness, contrato, `5xx`, conflicto de puerto, correlación y
  rollback a `0.0.1-SNAPSHOT`.
- La revisión es una autoevaluación del mantenedor en
  `blueprint/reviews/observability-closure.md`; no es una aprobación independiente.

## Mejora continua

El registro de mejora continua está en
`blueprint/improvements/continuous-improvements.md`. Cada uno de los cinco items
(`IMP-001` … `IMP-005`) nombra un propietario, el artefacto que cambiaría y la fuente de
feedback que lo originó.

- Los cinco items siguen `Deferred`; no se implementó ninguno en esta fase.
- No hay revisión de requisito ni versión nueva; `0.1.0` sigue siendo la versión
  entregada, ahora también publicada en Quay.
- El cierre de la fase 14 está en el registro y en
  `blueprint/reviews/continuous-improvement-closure.md`; es una autoevaluación del
  mantenedor, no una aprobación independiente.
- Los bucles de mejora de `IMP-001` … `IMP-005` permanecen abiertos.

## Limitaciones conocidas

- `REQ-STATUS-001` no tiene revisión independiente realizada; la revisión de entrega es
  una autoevaluación, no una aprobación independiente.
- El Blueprint permanece `NO VERIFICADO` en cuanto a validación formal.
- No se crea `CHANGELOG.md`; el registro de release es
  `blueprint/delivery/release-0.1.0.md`.
- P-02 permanece abierto; P-06, P-07, P-10, P-11, P-13 y P-14 están cerrados; P-07, P-11,
  P-13 y P-14 registran autoevaluación del mantenedor y no una aprobación independiente
  (definidos en `blueprint/gates.md`).
- `cd.yml` se ejecutó al fusionar el PR #5 y publicó `0.1.0`; el workflow no se modifica en
  este cambio.
- La publicación remota tiene evidencia de ejecución y digest registrado. La promoción
  local de `0.1.0` sigue registrada y su rollback fue ejercitado.
- No se ejecutó promoción remota a un segundo entorno ni rollback remoto; el tag `0.1.0`
  es mutable.
- No hay readiness probe ni despliegue en staging o producción; `/api/status` es una señal
  de liveness.
- No hay alertas automatizadas, métricas, trazas ni access log por petición; la detección de
  la fase 13 es manual y el runbook está en `blueprint/observability/hello-service.md`.
- Los items de mejora `IMP-001` … `IMP-005` siguen diferidos; sus bucles de mejora no están
  cerrados y no se declara ninguna mejora entregada.

## Documentación

- [Requisito `/api/hello`](blueprint/requirements/hello-endpoint.md)
- [Requisito `/api/status`](blueprint/requirements/api-status-endpoint.md)
- [Revisión de `/api/hello`](blueprint/reviews/hello-endpoint.md)
- [Revisión de `/api/status`](blueprint/reviews/api-status-endpoint.md)
- [Evidencia de pruebas de `/api/status`](blueprint/testing/api-status-endpoint.md)
- [Packaging](blueprint/packaging/hello-service.md)
- [Release 0.1.0](blueprint/delivery/release-0.1.0.md)
- [Despliegue local 0.1.0](blueprint/deployment/local-0.1.0.md)
- [Cambio de publicación remota](blueprint/changes/p11-remote-publication.md)
- [Revisión de publicación remota](blueprint/reviews/remote-publication.md)
- [Observabilidad local](blueprint/observability/hello-service.md)
- [Revisión de observabilidad](blueprint/reviews/observability-closure.md)
- [Cambio de observabilidad](blueprint/changes/p13-observability.md)
- [Revisión del cierre de entrega](blueprint/reviews/delivery-closure.md)
- [Gates de cierre](blueprint/gates.md)
- [Blueprint — Continuous Delivery](blueprint/11-continuous-delivery.md)
- [Blueprint — Deployment](blueprint/12-deployment.md)
- [Blueprint — Observability](blueprint/13-observability.md)
- [Definición de terminado](standards/definition-of-done.md)
- [Mejoras continuas](blueprint/improvements/continuous-improvements.md)
- [Revisión de mejora continua](blueprint/reviews/continuous-improvement-closure.md)
- [Cambio de mejora continua](blueprint/changes/p14-continuous-improvement.md)
- [Blueprint — Feedback and Continuous Improvement](blueprint/14-continuous-improvement.md)
- [Workflow CI](.github/workflows/ci.yml)
- [Workflow CD](.github/workflows/cd.yml)
- [Dockerfile](Dockerfile)
