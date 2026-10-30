# hello-service

PoC mínimo de servicio HTTP usado para recorrer las fases del Software Engineering
Blueprint. No es un servicio de producción ni una publicación remota; la versión `0.1.0`
tiene un registro de release local.

## Alcance de cierre

El cierre aprobado cubre la PoC de `hello-service` con configuración de CI y despliegue
local.

- La publicación remota en Quay queda pospuesta.
- El riesgo residual de `.github/workflows/cd.yml` se documenta y no se modifica el
  workflow.
- La revisión independiente de `REQ-STATUS-001` no se realizó; el cierre de P-07 registra
  una autoevaluación del mantenedor y no una aprobación independiente.
- No se crea `CHANGELOG.md`.
- El Blueprint no se declara formalmente validado.
- Los gates se cierran solo con su evidencia: P-06, P-07, P-10 y P-11 están cerrados;
  P-07 y P-11 registran autoevaluación del mantenedor porque no hay revisor independiente;
  P-02 sigue abierto (ver `blueprint/gates.md`).

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

Las pruebas documentadas no requieren servicios externos, base de datos, secretos ni
configuración de Quay.

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

## Estado de CI y despliegue local

| Área | Evidencia de configuración | Evidencia de ejecución real | Estado |
|---|---|---|---|
| CI | `.github/workflows/ci.yml` define un único job de build/test, construcción de la imagen y registro de su identidad; no contiene ruta de publicación | Ejecución real registrada: run 13 sobre `2e54476`, conclusión `success`, artefactos `artifact-metadata`, `hello-service-0.1.0` y `gitleaks-report` | Configuración validada por ejecución real; CI-02 `VERIFICADO` |
| Packaging | Versión `0.1.0`, JAR ejecutable, imagen OCI con labels de procedencia, identidad y escaneo gitleaks | Run 13: artefactos `artifact-metadata`, `hello-service-0.1.0` y `gitleaks-report`; conclusión `success` | P-10 `CLOSED` |
| Pruebas locales | Gradle Wrapper y suite automatizada | Ejecución registrada de 17 tests sobre `f2b03e5`, `72c2ad5` y `870500c` | Ejecución local documentada y repetida sobre el `HEAD` del cierre |
| Despliegue local | `Dockerfile` OCI, usuario no-root, puerto `8080` y entrypoint del JAR | Procedimiento reproducible verificado el 2026-10-30: build local, imagen `0.1.0`, contenedor como `appuser`, probes en `127.0.0.1:18080` y rollback a `0.0.1-SNAPSHOT` | Procedimiento verificado; registro en `blueprint/deployment/local-0.1.0.md` |
| Release local / P-11 | Versión `0.1.0`, imagen local y rollback a `0.0.1-SNAPSHOT` | Promoción local registrada el 2026-10-30; rollback ejercitado | P-11 `CLOSED` con autoevaluación; sin aprobación independiente |
| CD / Quay | `.github/workflows/cd.yml` contiene disparadores, pasos de OIDC, login y push a Quay | No hay evidencia de ejecución ni de publicación o digest remoto | Publicación remota pospuesta; ruta potencial conservada y riesgo documentado |

## Publicación remota pospuesta

La publicación remota en Quay está pospuesta por decisión de alcance. La promoción local
de `0.1.0` sí está registrada en `blueprint/delivery/release-0.1.0.md` y no se presenta
como publicación remota.

- `ci.yml` no contiene ruta de publicación: construye, prueba y registra la identidad de la
  imagen. No se declara ninguna imagen publicada por ese workflow.
- `cd.yml` contiene una ruta potencial de publicación mediante `push` a `main` y
  `workflow_dispatch`; no se modifica en este cierre.
- No se declara una publicación realizada ni un digest remoto.

## Limitaciones conocidas

- `REQ-STATUS-001` no tiene revisión independiente realizada; la revisión de entrega es
  una autoevaluación, no una aprobación independiente.
- El Blueprint permanece `NO VERIFICADO` en cuanto a validación formal.
- No se crea `CHANGELOG.md`; el registro de release es
  `blueprint/delivery/release-0.1.0.md`.
- P-02 permanece abierto; P-06, P-07, P-10 y P-11 están cerrados; P-07 y P-11 registran
  autoevaluación del mantenedor y no una aprobación independiente (definidos en
  `blueprint/gates.md`).
- `cd.yml` mantiene una ruta potencial de publicación; el riesgo residual se documenta
  sin modificar el workflow.
- La publicación remota no tiene evidencia de ejecución. La promoción local de `0.1.0`
  sí está registrada y su rollback fue ejercitado.
- No hay readiness probe ni despliegue en staging o producción.

## Documentación

- [Requisito `/api/hello`](blueprint/requirements/hello-endpoint.md)
- [Requisito `/api/status`](blueprint/requirements/api-status-endpoint.md)
- [Revisión de `/api/hello`](blueprint/reviews/hello-endpoint.md)
- [Revisión de `/api/status`](blueprint/reviews/api-status-endpoint.md)
- [Evidencia de pruebas de `/api/status`](blueprint/testing/api-status-endpoint.md)
- [Packaging](blueprint/packaging/hello-service.md)
- [Release 0.1.0](blueprint/delivery/release-0.1.0.md)
- [Despliegue local 0.1.0](blueprint/deployment/local-0.1.0.md)
- [Revisión del cierre de entrega](blueprint/reviews/delivery-closure.md)
- [Gates de cierre](blueprint/gates.md)
- [Blueprint — Continuous Delivery](blueprint/11-continuous-delivery.md)
- [Blueprint — Deployment](blueprint/12-deployment.md)
- [Definición de terminado](standards/definition-of-done.md)
- [Mejoras continuas](blueprint/improvements/continuous-improvements.md)
- [Workflow CI](.github/workflows/ci.yml)
- [Workflow CD](.github/workflows/cd.yml)
- [Dockerfile](Dockerfile)
