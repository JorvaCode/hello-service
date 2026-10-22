# ADR-005: Separación entre skill y agente en el adaptador de OpenCode

## Status
Accepted

## Context
El adaptador de OpenCode (ADR-002) exponía el ciclo de vida completo del Blueprint en un único skill, `blueprint/SKILL.md`, que repetía el contenido normativo de `blueprint/00-principles.md` a `blueprint/14-continuous-improvement.md`. En OpenCode, un skill inyecta conocimiento en la sesión principal, mientras que un agente es un rol delegado con su propio prompt y su propio contexto. El diseño anterior trataba una decisión de rol —orchestrar un ciclo de vida completo— como si fuera contenido de referencia.

Además, el contenido duplicado podía divergir del núcleo normativo, y las referencias a `templates/requirement.md` y `templates/adr.md` no declaraban su base, lo que las hacía resolubles solo por accidente desde el directorio de trabajo.

## Options considered
1. Convertir `blueprint` en un agente y eliminar el skill.
2. Mantener el skill tal cual, asumiendo que la duplicación es un coste aceptable.
3. Reducir el skill a un router que apunta a `blueprint/`, y añadir un subagente `blueprint-orchestrator` para las pasadas autónomas sin usuario interactivo.

## Decision
Opción 3. El ciclo de vida es conocimiento y lo sirve un skill; la orquestación autónoma es un rol y lo sirve un subagente.

- `blueprint/SKILL.md` y los skills de fase quedan como routers: tabla de fases con su documento normativo, rutas de artefactos y procedimiento. No repiten el contenido de `blueprint/`.
- Todas las rutas se declaran explícitamente como relativas a la raíz del proyecto.
- `blueprint-orchestrator` reside en `.opencode/agents/`, con `mode: subagent`, y cubre la pasada autónoma sin usuario interactivo: auditoría de preparación, informe de brechas por fase y generación de artefactos cuando faltan. No modifica código de aplicación.
- El núcleo en `blueprint/`, `templates/` y `standards/` sigue sin depender de OpenCode.

La Fase 01 es deliberadamente interactiva —el blueprint exige no implementar con requisitos ambiguos—, y un subagente no puede iterar con el usuario dentro de su turno. Esa es la razón por la que la ruta interactiva se mantiene como skill y no como agente.

No se añade un agente primario propio. Los instaladores (`scripts/blueprint-init.ps1`, `scripts/blueprint-init.sh`) copian `.opencode/` al proyecto destino, de modo que un primario aparecería en el ciclo de agentes de todo consumidor que instala el blueprint: el adaptador pasaría a imponer un default a equipos que solo pidieron guía, contradiciendo ADR-002. La ruta interactiva usa el primario integrado más el skill `blueprint`, y las reglas de trabajo de este repositorio se declaran en `AGENTS.md`, que no se distribuye a los consumidores y no altera la sesión.

## Consequences
### Positive
- Una sola fuente de verdad: `blueprint/` es normativo, los skills solo enrutan.
- El contexto de la sesión principal deja de cargar las 14 fases para tareas que no las necesitan.
- Rutas no ambiguas: la base es siempre la raíz del proyecto.
- Se cubre el caso no interactivo, que antes no tenía soporte.
- El mecanismo sigue siendo puramente un adaptador opcional (ADR-002).
- El adaptador no impone ningún default a los proyectos consumidores: nada aparece en su ciclo de agentes salvo que lo invoquen.

### Negative / trade-offs
- Un router obliga a una lectura adicional por fase; el intercambio es aceptable porque la fase leída es solo la necesaria.
- El subagente no puede cerrar el bucle de preguntas con el usuario: convierte la ambigüedad en hallazgos en lugar de preguntas.
- `.opencode/agents/` introduce una segunda ubicación que la validación de CI debe cubrir.
- El adaptador no puede forzar el proceso sobre el usuario de una sesión; solo puede guiarlo. La coerción queda fuera del alcance del adaptador por decisión.
- Mantener el adaptador exige que los routers sigan apuntando a `blueprint/` cuando las fases cambien.

## Date
2026-09-29
