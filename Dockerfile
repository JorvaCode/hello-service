# syntax=docker/dockerfile:1

# Phase 11/12 — Deployment + Continuous Delivery · ADR-009 (OCI + Podman) · ADR-010 (Quay.io).
# Mecanismo: Dockerfile OCI construido con `podman build`. Sin secrets.
# Provenance (ADR-010): org.opencontainers.image.source/revision se inyectan desde el
# contexto real de GitHub/CI como build-args. Si no se pasan (build local sin remoto),
# quedan vacías: no se hardcodea ningún repositorio.
# Base: Eclipse Temurin JRE 21 (Ubuntu 26.04), fijada por digest para reproducibilidad.

ARG APP_VERSION=0.0.1-SNAPSHOT

FROM docker.io/library/eclipse-temurin:21-jre@sha256:49e21e16e3c86eb7816a44a67549910ed090fbeb40c29c525d58bf5e02e91b0f AS base

ARG APP_VERSION
ARG CI_REPOSITORY
ARG CI_SHA

LABEL org.opencontainers.image.title="hello-service" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.base.name="docker.io/library/eclipse-temurin:21-jre" \
      org.opencontainers.image.base.digest="sha256:49e21e16e3c86eb7816a44a67549910ed090fbeb40c29c525d58bf5e02e91b0f" \
      org.opencontainers.image.description="Spring Boot hello-service (REQ-HELLO-001) - ADR-009" \
      org.opencontainers.image.source="${CI_REPOSITORY}" \
      org.opencontainers.image.revision="${CI_SHA}"

WORKDIR /app

RUN useradd --system --uid 10001 --create-home appuser

COPY --chown=appuser:appuser build/libs/hello-service-${APP_VERSION}.jar /app/hello-service.jar

USER appuser

EXPOSE 8080

# Nota (ADR-009 + Phase 12): HEALTHCHECK no se soporta en formato de imagen OCI y
# Podman lo ignora. El health check se define en el runtime con --health-cmd
# (ver reporte blueprint/12-deployment.md): imagen OCI pura, probe en /api/hello.

ENTRYPOINT ["java", "-jar", "/app/hello-service.jar"]