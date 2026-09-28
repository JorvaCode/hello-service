# ADR-009: Plataforma de despliegue para hello-service — Contenedor OCI con Podman

## Status
Accepted

## Context
hello-service (Java 21 / Spring Boot / Gradle) genera un JAR reproducible (`build/libs/hello-service-0.0.1-SNAPSHOT.jar`) y expone `GET /api/hello` como probe de smoke. Las fases 09 (CI) y 10 (Packaging) están completadas. No existe aún repositorio Git remoto, registry ni entorno de despliegue. Se debe decidir la plataforma de despliegue (Phase 12) de forma repetible, portable, observable y demostrable como portfolio.

Contexto de la máquina de desarrollo: Java 21.0.11, Gradle 8.14.2 (wrapper), Podman 5.8.5 (sin Docker), Minikube 1.38.1, kubectl 1.36.3, git 2.55. La decisión se toma acorde con ADR-001 (agnosticismo de IDE/tecnología): el Blueprint no selecciona plataformas por el proyecto.

## Options considered
1. JVM/JAR directo sobre una VM: proceso `java -jar` sobre un host con Java 21. Máximo simple, mínima infraestructura, pero aislamiento y portabilidad bajos.
2. Contenedor OCI publicado en un registry y ejecutado con Podman: imagen inmutable, portable y reproducible; el registry concreto queda definido en una decisión posterior.
3. Kubernetes (Minikube local o cluster): manifiestos Deployment + Service (+Ingress). Escalabilidad y operaciones declarativas, pero mayor complejidad operativa y dependencia de cluster.

## Decision
Opción 2, aprobada por el responsable de `hello-service`:
- La aplicación se empaquetará como imagen OCI a partir del artefacto de la Fase 10 (JAR Spring Boot reproducible).
- La imagen será ejecutable mediante Podman (runtime OCI disponible en el entorno; no se requiere Docker).
- La imagen será identificable mediante versión/tag y, cuando corresponda, por digest.
- El registry concreto (p. ej. Quay.io, GHCR, Docker Hub u otro compatible con OCI) queda como decisión posterior específica; el Blueprint permanece agnóstico respecto de cualquier registry hasta que exista esa decisión.
- Kubernetes queda fuera de la implementación inmediata de la Phase 12; la misma imagen OCI podrá utilizarse posteriormente en Kubernetes si se decide.

## Consequences
### Positive
- Reproducibilidad alta: la imagen inmoviliza aplicación, runtime y configuración en un único artefacto.
- Portabilidad: la misma imagen puede ejecutarse con cualquier runtime OCI y migrar a Kubernetes sin reconstruir el artefacto.
- Aislamiento: la aplicación se ejecuta con sus propias dependencias y límites, sin ensuciar el host.
- Preparación para CI/CD: la imagen es el artefacto que el pipeline puede construir, publicar y verificar mediante smoke tests.
- Demostrable como portfolio y ejecutable localmente con la herramienta ya disponible (Podman).

### Negative / trade-offs
- Puede añadir complejidad operativa frente a `java -jar` directo: build de imagen, gestión de tags/digests y de registry.
- Requiere una decisión posterior de registry (p. ej. Quay.io, GHCR, Docker Hub) y sus credenciales; hasta esa decisión no hay push a registry.
- La capa de imagen añade tiempo de build y consumo de recursos frente a la ejecución directa del JAR.
- No aporta por sí misma las capacidades de orquestación que ofrecería Kubernetes (autoescalado, scheduling); si se necesitan, esa decisión queda abierta para una fase posterior usando la misma imagen.

## Date
2026-09-24