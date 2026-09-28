# ADR-001: Blueprint agnóstico de IDE y tecnología

## Status
Accepted

## Context
Los equipos usan diferentes IDEs (IntelliJ IDEA, Visual Studio, VS Code, etc.), lenguajes y frameworks. Si el proceso de ingeniería queda acoplado a una herramienta o lenguaje concreto, deja de ser reutilizable entre proyectos y equipos, y dificulta la adopción. El Blueprint necesita una regla de arquitectura que garantice reutilización a nivel de proceso sin imponer un stack.

## Options considered
1. Vincular el Blueprint a un IDE, lenguaje o plataforma específicos (p. ej., IntelliJ + Java), ofreciendo guías concretas paso a paso.
2. Mantener el proceso agnóstico de herramientas y tecnología, y proporcionar referencias a herramientas (GitHub Actions, OpenCode) solo como ejemplos o adaptadores opcionales.
3. Parametrizar por completo el Blueprint con plantillas configurables que cada proyecto deba rellenar antes de usarlo.

## Decision
Opción 2: el proceso es agnóstico de IDE, lenguaje y herramientas. README.md define que los proyectos eligen su lenguaje, framework, IDE, cloud, registro y plataforma de despliegue, mientras que el Blueprint define qué debe suceder, qué artefactos se esperan y qué barreras de calidad aplican. Las referencias a herramientas concretas son ejemplos o adaptadores opcionales.

## Consequences
### Positive
- Reutilizable entre proyectos, equipos y organizaciones sin dependencias de stack.
- Sin bloqueo por el proveedor de herramientas (no vendor lock-in).
- Compatible con desarrollo asistido por IA sin exigir una herramienta concreta.
- Núcleo del proceso estable y revisable independientemente de cambios en herramientas.

### Negative / trade-offs
- Los ejemplo concretos deben redactarse con cuidado para que no se interpreten como requisitos obligatorios.
- Cada proyecto adopta la responsabilidad de mapear las herramientas a su propio stack.
- No se ofrece una experiencia "todo incluido" fuera de la caja para cada plataforma.

## Date
2026-09-22