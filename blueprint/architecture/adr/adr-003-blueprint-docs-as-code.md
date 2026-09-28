# ADR-003: Blueprint como documentación estructurada y Docs-as-Code

## Status
Accepted

## Context
Los artefactos del proceso (fases, requisitos, ADR, cambios, estándares) necesitan un formato que sea revisable, versionable, diferenciable en cambios (diff), trazable y agnóstico de herramientas, coherente con el estándar de Git del propio Blueprint.

## Options considered
1. Wiki (p. ej., Confluence o GitHub Wiki): edición fácil, pero con control de versiones y revisión por pull request débiles.
2. Datos estructurados (YAML/JSON): muy legibles por máquina, pero con baja legibilidad humana y peor experiencia de autoría.
3. Documentación en Markdown gestionada como código (Docs-as-Code): organizada en blueprint/, templates/, standards/ y examples/, con plantillas y validación por CI.

## Decision
Opción 3: Docs-as-Code. El Blueprint se distribuye como ficheros Markdown versionados en Git, con estructura fija (blueprint/00-14, templates/, standards/, examples/, .github/workflows/), plantillas para cada artefacto y validación de estructura mediante CI (.github/workflows/ci.yml).

## Consequences
### Positive
- Revisable mediante pull requests y diferenciable en el historial (diff).
- Portátil y editable con cualquier IDE, coherente con ADR-001.
- CI puede validar la estructura automáticamente y evolucionar hacia validación de contenido.
- Alineado con standards/git.md y con la trazabilidad exigida por el proceso.

### Negative / trade-offs
- No hay esquema forzado; la conformidad con plantillas depende de convenciones y revisión.
- La calidad de contenido depende de la revisión humana, no de un compilador.
- El CI actual solo valida presencia de archivos (estructura), no la calidad del contenido; mejorar esa validación queda como mejora pendiente.

## Date
2026-09-22