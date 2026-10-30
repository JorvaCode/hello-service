# hello-service

PoC mínimo de servicio HTTP usado para recorrer las fases del Software Engineering
Blueprint. No es un servicio de producción ni un release formal.

## Alcance de cierre

El cierre aprobado cubre la PoC de `hello-service` con configuración de CI y despliegue
local.

- La publicación remota en Quay queda pospuesta.
- El riesgo residual de `.github/workflows/cd.yml` se documenta y no se modifica el
  workflow.
- La revisión independiente de `REQ-STATUS-001` debe realizarse antes del cierre; el
  registro actual no es independiente.
- No se crea `CHANGELOG.md`.
- El Blueprint no se declara formalmente validado.
- P-02, P-06, P-07, P-10 y P-11 no se cierran por posponer la publicación.

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

Limitación actual: el repositorio no contiene un comando de arranque local documentado
y reproducible. Este README no prescribe uno para no presentar como soportado algo que
no está verificado.

Evidencia existente:

- `blueprint/testing/api-status-endpoint.md` registra una ejecución de la aplicación en
  `127.0.0.1:18080` y probes HTTP directos.
- `blueprint/analisys/api-status-endpoint.md` registra además una observación en
  `127.0.0.1:8080`.
- `Dockerfile` expone el puerto `8080` y define el entrypoint del JAR.

No se registra si esas ejecuciones usaron el JAR directamente o un contenedor. El
procedimiento local reproducible sigue pendiente como limitación conocida.

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
- Base: commit `f2b03e5f8d8306192922051dbf6359e23a9a0ee2`.
- Fecha: 2026-10-27.

La suite no se volvió a ejecutar sobre el `HEAD` actual durante la preparación del
cierre.

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
| CI | `.github/workflows/ci.yml` define un único job de build/test, construcción de la imagen y registro de su identidad; no contiene ruta de publicación | No hay evidencia en el repositorio de una ejecución real en GitHub Actions | Configuración presente; CI-02 `NO VERIFICADO` |
| Pruebas locales | Gradle Wrapper y suite automatizada | Ejecución registrada de 17 tests sobre `f2b03e5` | Ejecución local documentada; no repetida sobre el `HEAD` actual |
| Despliegue local | `Dockerfile` OCI, usuario no-root, puerto `8080` y entrypoint del JAR | Probe local registrado en `127.0.0.1:18080` | Ejecución observada; procedimiento reproducible pendiente |
| CD / Quay | `.github/workflows/cd.yml` contiene disparadores, pasos de OIDC, login y push a Quay | No hay evidencia de ejecución ni de publicación o digest remoto | Publicación remota pospuesta; ruta potencial conservada y riesgo documentado |

## Publicación remota pospuesta

La publicación remota en Quay está pospuesta por decisión de alcance.

- `ci.yml` no contiene ruta de publicación: construye, prueba y registra la identidad de la
  imagen. No se declara ninguna imagen publicada por ese workflow.
- `cd.yml` contiene una ruta potencial de publicación mediante `push` a `main` y
  `workflow_dispatch`; no se modifica en este cierre.
- No se declara una publicación realizada ni un digest remoto.

## Limitaciones conocidas

- No existe un comando de arranque local reproducible documentado.
- `REQ-STATUS-001` no tiene revisión independiente realizada.
- El Blueprint permanece `NO VERIFICADO` en cuanto a validación formal.
- No se crea release ni `CHANGELOG.md` en este cierre.
- Los gates P-02, P-06, P-07, P-10 y P-11 permanecen abiertos o no verificados (definidos
  en `blueprint/gates.md`).
- `cd.yml` mantiene una ruta potencial de publicación; el riesgo residual se documenta
  sin modificar el workflow.
- No hay evidencia de una ejecución real de CI en GitHub.

## Documentación

- [Requisito `/api/hello`](blueprint/requirements/hello-endpoint.md)
- [Requisito `/api/status`](blueprint/requirements/api-status-endpoint.md)
- [Revisión de `/api/hello`](blueprint/reviews/hello-endpoint.md)
- [Revisión de `/api/status`](blueprint/reviews/api-status-endpoint.md)
- [Evidencia de pruebas de `/api/status`](blueprint/testing/api-status-endpoint.md)
- [Gates de cierre](blueprint/gates.md)
- [Blueprint — Continuous Delivery](blueprint/11-continuous-delivery.md)
- [Blueprint — Deployment](blueprint/12-deployment.md)
- [Definición de terminado](standards/definition-of-done.md)
- [Mejoras continuas](blueprint/improvements/continuous-improvements.md)
- [Workflow CI](.github/workflows/ci.yml)
- [Workflow CD](.github/workflows/cd.yml)
- [Dockerfile](Dockerfile)
