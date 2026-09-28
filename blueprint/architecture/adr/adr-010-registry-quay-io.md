# ADR-010: Selección de registro de contenedores (registry OCI) para hello-service — Quay.io

## Status
Accepted

## Context
Las fases 09 (CI), 10 (Packaging) y 12 (Deployment) tienen gate PASS. hello-service se empaqueta como imagen OCI reproducible y ejecutable con Podman: `localhost/hello-service:0.0.1-SNAPSHOT` (ID `0fe6bfd2e77a…`, digest `sha256:f7c0a1ed1cb6099424a689d795796f9eadefa35f100d0ec3f83249f3b27bab7b`). ADR-009 aprobó la plataforma de despliegue (contenedor OCI + runtime Podman) y difirió explícitamente la selección del registry a una decisión posterior. Phase 11 (Continuous Delivery) está bloqueada hasta disponer de un registry para publicar, validar por entorno y promover el mismo artefacto. No existe aún remoto GitHub ni `gh`.

## Options considered
1. GitHub Container Registry (ghcr.io): integración nativa con GitHub Actions vía `GITHUB_TOKEN` (sin secretos en CI), package enlazado al repositorio, imágenes públicas gratuitas con acceso anónimo (las privadas cuentan contra la cuota del plan de GitHub), nomenclatura `ghcr.io/<owner>/hello-service:<tag>`, compatible con Podman, trazabilidad alta mediante label `org.opencontainers.image.source`. Coste: dependencia total de proveedor (concentra código + CI + registry en GitHub).
2. **Quay.io** (seleccionado): imágenes públicas ilimitadas y gratuitas (servicio y almacenamiento), compatibilidad OCI/Podman de primera clase, autenticación mediante robot accounts con opción **OIDC keyless** (federación robot ↔ issuer GitHub, sin credenciales de larga vida) o token de robot, escaneo de vulnerabilidades Clair, nomenclatura `quay.io/<namespace>/hello-service:<tag>`. Coste: requiere cuentas Red Hat + Quay y configuración adicional de autenticación con GitHub Actions; la cuota gratuita no incluye repositorios privados.
3. Docker Hub (citada en ADR-009 como candidata): público ilimitado (fair use), plan Personal gratuito con 1 repositorio privado y rate-limit de pull (100/h autenticado), autenticación mediante token de larga vida (sin OIDC), integración estándar con Podman. Coste: trazabilidad baja (sin vínculo nativo con Git) y límites de pull para automatización.

Fue seleccionada la opción 2, aprobada por el responsable de `hello-service`, por el balance entre desacoplamiento de proveedores, gratuidad pública para portfolio, soporte OCI/Podman y disponibilidad de autenticación keyless.

## Decision
Registry seleccionado: **Quay.io**, como decisión específica del proyecto de referencia `hello-service`; el Blueprint permanece agnóstico de registry (ADR-001, REQ-BP-001) y no se vuelve dependiente de Quay.io.

- Imagen: `quay.io/<quay-namespace>/hello-service:<version>`.
- La etiqueta de versión identifica la release.
- El **digest OCI** es la identidad inmutable del artefacto.
- La promoción entre entornos debe utilizar exactamente el mismo digest.
- Está **prohibido reconstruir la imagen durante una promoción**.
- Podman seguirá siendo el runtime utilizado localmente.
- La autenticación de CI/CD debe utilizar **preferentemente OIDC/keyless authentication** si está disponible y es viable con Quay.io (federación de robot account con el issuer de GitHub Actions).
- Como alternativa documentada, podrá utilizarse un **robot account/token** almacenado exclusivamente como **GitHub Actions Secret**.
- **Ninguna credencial debe almacenarse en el repositorio.**
- La imagen será **pública** para facilitar la demostración del proyecto como portfolio, salvo que exista una restricción técnica que lo impida.
- Deben mantenerse las **etiquetas OCI de procedencia/provenance**, incluyendo posteriormente:
  - `org.opencontainers.image.source`
  - `org.opencontainers.image.revision`

## Consequences
### Positive
- Separación de proveedores: GitHub para repositorio/CI y Quay.io para registry.
- Excelente compatibilidad con Podman/OCI.
- Soporte de imágenes identificables mediante tags y digest.
- Permite promoción del mismo artefacto sin reconstrucción.
- Facilita una futura integración con Kubernetes/OpenShift (la misma imagen OCI se puede desplegar sin reconstruirla).

### Negative / trade-offs
- Requiere configurar autenticación entre GitHub Actions y Quay.io.
- Requiere configurar namespace/repositorio en Quay.io.
- Puede requerir GitHub Secrets si finalmente no es viable OIDC/keyless.
- Añade una dependencia externa respecto a GitHub.
- La cuota gratuita de Quay.io no permite repositorios privados; el repositorio de esta decisión es público.

## Date
2026-09-24