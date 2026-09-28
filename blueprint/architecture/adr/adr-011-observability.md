# ADR-011: Observabilidad mínima operativa para hello-service (Phase 13) sin nuevas dependencias

## Status
Accepted

## Context
Phase 13 (Observability) exige las señales Logs, Metrics, Traces y Alerts, con un gate definido como: *"Operationally significant failures can be detected and investigated."* `hello-service` arrastra además la restricción NFR-02 (sin dependencias de runtime más allá del stack HTTP existente), aplicada como convención del proyecto ya en ADR-008, Phase 08 (C-1) y Phase 12 (explícitamente: "no Actuator added"). El classpath de runtime actual incluye SLF4J 2.0.17 y Logback 1.5.18 (vía `spring-boot-starter-logging`) y `micrometer-observation/commons` 1.15.1 únicamente transitivo; **no existe** registry de métricas, ni Actuator, ni backend/collector de tracing (OTel/Micrometer Tracing), ni plataforma/canal de alertas. Phase 12 ya demostró un health check funcional usando `GET /api/hello` como `--health-cmd` de Podman (contenedor `healthy`), sin añadir Actuator. Configured la imagen y el contenedor no están hoy desplegados, pero la imagen es reproducible (Phase 12 V-6) y el patrón de desplegado ya está validado.

## Options considered
1. **Observabilidad mínima operativa sin nuevas dependencias** (seleccionada): reutilizar el health check de Phase 12, añadir logs estructurados con Logback (ya presente), request-id mediante MDC (filtro `Servlet` propio, sin dependencias) y demostrar la detección e investigación de un fallo real (gate). Coste: las señales avanzadas quedan fuera de esta versión.
2. Añadir Spring Boot Actuator (con su registry de Micrometer): habilita endpoints de health/metrics inmediatamente, pero introduce una dependencia de runtime nueva → contradice NFR-02 y la práctica del proyecto (ADR-008, Phase 08/12) salvo enmienda explícita del requisito.
3. Adoptar trazas distribuidas (Micrometer Tracing/OTel) + plataforma de alertas (Prometheus/Grafana/Alertmanager): completitud total de señales, pero exige múltiples dependencias nuevas y, además, infraestructura externa (collector, almacenamiento, canal de alertas) que no existe; sobre-dimensiona un endpoint único y stateless.

Fue seleccionada la opción 1, aprobada por el responsable de `hello-service`, por respetar NFR-02, mantener la portabilidad del proyecto y permitir demostrar el gate de Phase 13 de forma local y reproducible.

## Decision
Para esta versión de `hello-service` se implementará **observabilidad mínima operativa sin nuevas dependencias de runtime**, alineada con el gate de Phase 13 y con NFR-02:

- **Health check existente**: `GET /api/hello` como probe (patrón Phase 12), sin Actuator.
- **Logs estructurados**: configuración de Logback (ya presente en el stack) con formato consistente y campos útiles; no se añade encoder JSON ni dependencias externas.
- **Request-id mediante MDC**: un `Filter` propio asigna un identificador por petición y lo propaga en los logs para correlación de errores.
- **Evidencia reproducible del gate**: un fallo operativo significativo debe detectarse (health no healthy + probe externo falla) e investigarse en los logs (causa identificable).
- **Métricas: DEFERRED** — no existe registry ni backend, y añadirlo implicaría dependencias/infraestructura adicional.
- **Trazas: DEFERRED** — no existe infraestructura OTel/collector, y añadirla implicaría dependencias/infraestructura adicional.
- **Alertas: DEFERRED** — no existe plataforma ni canal de alerting.

Las señales diferidas se documentan como evolución futura y no forman parte del gate de Phase 13 de esta versión.

## Consequences
### Positive
- El gate de Phase 13 puede demostrarse localmente con el stack actual.
- No se introduce ninguna dependencia de runtime nueva (NFR-02 respetado).
- Los fallos operativos son detectables (health + probe) e investigables (logs + request-id).
- Las señales avanzadas quedan preparadas como evolución futura sobre una base sin deuda técnica.
- Mantiene coherencia con ADR-008 y con las fases 08 y 12.

### Negative / trade-offs
- No hay métricas técnicas/de negocio ni trazas distribuidas en esta versión (DEFERRED).
- No hay alertas activas; la detección queda limitada al health/probe y a los logs locales y del despliegue.
- La correlación es a nivel de petición (request-id), no de trace distribuido entre servicios.
- Obtener métricas/trazas/alertas completas requeriría enmendar NFR-02 y añadir dependencias e infraestructura.

## Relación con decisiones previas
- **NFR-02**: la decisión no incorpora dependencias de runtime nuevas; el alcance mínimo está condicionado por él.
- **Phase 08**: mantiene la inspección de dependencias (C-1) y la regla "sin deps nuevas".
- **Phase 12**: reutiliza el health check `GET /api/hello` y el patrón de despliegue OCI + Podman validado.
- **ADR-003 (Docs-as-Code)**: esta decisión se registra como ADR en la documentación del Blueprint.
- **ADR-008**: refuerza la restricción de dependencias del endpoint (sin Actuator).
- **ADR-009**: la plataforma OCI + Podman es la base sobre la que se ejecuta el health check y se recogen los logs.
- **ADR-010**: el registry Quay.io y la publicación/promoción por digest no se ven alterados por esta decisión.

## Date
2026-09-28