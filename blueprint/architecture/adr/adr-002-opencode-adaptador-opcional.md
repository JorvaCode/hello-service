# ADR-002: OpenCode como adaptador opcional

## Status
Accepted

## Context
El Blueprint debe ser compatible con el desarrollo asistido por IA. OpenCode puede ejecutar skills que inyectan la guía del ciclo de vida en el asistente. Sin embargo, si OpenCode fuera un requisito, el Blueprint pasaría a depender de una herramienta de IA concreta y dejaría de ser agnóstico de herramientas (ADR-001).

## Options considered
1. Hacer de OpenCode el runtime principal y obligatorio para operar el Blueprint.
2. Mantener el núcleo del Blueprint agnóstico y proporcionar los skills de OpenCode como adaptador opcional, descubrible desde .opencode/skills/<name>/SKILL.md.
3. Soportar desde el inicio adaptadores paralelos para múltiples herramientas de IA (Cursor, Copilot, etc.).

## Decision
Opción 2: OpenCode es un adaptador opcional, no una dependencia. Los skills residen en .opencode/skills/<name>/SKILL.md y son descubribles automáticamente cuando se usa OpenCode. El núcleo del Blueprint en blueprint/, templates/ y standards/ no requiere OpenCode para aplicarse.

## Consequences
### Positive
- El núcleo permanece libre de dependencias de herramientas de IA.
- Quienes usen OpenCode obtienen guía del ciclo de vida sin configuración adicional.
- La puerta queda abierta a añadir adaptadores para otras herramientas en el futuro sin cambiar el núcleo.
- README.md lo declara explícitamente: "OpenCode integration is provided as an adapter, not as a dependency".

### Negative / trade-offs
- Los equipos que no usen OpenCode no disfrutan de la experiencia guiada.
- El soporte multiplataforma de IA queda diferido hasta que un segundo adaptador se necesite.
- El adaptador puede separarse del núcleo si no se mantiene alineado con las fases del Blueprint.

## Date
2026-09-22