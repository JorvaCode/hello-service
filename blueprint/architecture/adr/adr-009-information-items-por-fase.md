# ADR-009: Declarar los information items de cada fase

## Status
Accepted

> **Nota de resolución.** El compromiso de «Negative / trade-offs» de este ADR fue resolver la
> trazabilidad entre fases en un mecanismo distinto, con un coste distinto. Ese mecanismo existe
> y es `ADR-013`. Este registro se conserva tal como se tomó: la limitación que se declara abajo
> era cierta cuando se decidió y `git log` es la evidencia de que lo era.

## Context
Ámbito: `blueprint/00-principles.md` … `blueprint/14-continuous-improvement.md`.

El blueprint define catorce fases y, para la mayoría de ellas, un artefacto. Lo que no
definía era el contenido de ese artefacto. `02-analysis.md` pedía «an analysis
decision» sin decir qué debía contener esa decisión, de modo que dos personas cumpliendo
la fase de la misma forma podían producir documentos incomparables. El problema es
característico: no faltaba rigor, faltaba especificidad.

La segunda mitad del problema eran las quality gates. `01-requirements.md` decía que un
requisito está listo cuando es «clear, testable, feasible» y tiene criterios de aceptación
explícitos. Ninguna de esas condiciones puede fallar, porque ninguna de ellas nombra un
artefacto verificable ni un umbral. `07-code-review.md` preguntaba «Clear and
maintainable?», que es una opinión esperando un sí, y `13-observability.md` pedía que
los fallos operacionalmente significativos pudieran detectarse, sin decir cuáles. Un
criterio que no puede fallar no es una gate: es una afirmación con formato de criterio.

## Options considered

**Opción A — continuar con el estilo actual.** Coste cero, y es lo que hacía el
blueprint legible. Se descartó: la vaguedad era el defecto, no un coste aceptado.

**Opción B — plantillas completas para las catorce fases.** Una plantilla por fase
haría cada artefacto verificable por construcción. Se descartó por desproporción: la
mayoría de los proyectos consume tres o cuatro fases al día y catorce plantillas
obligarían a mantener catorce artefactos que casi nadie rellena, que es un coste fijo
recuperado muy raramente. La sección de information items declara lo que debe contener
el artefacto y remite a la plantilla solo donde la plantilla ya existe.

**Opción C — information items por fase, con el estándar como reglas comunes.**
Adoptada. La declaración vive junto a la fase que la produce, donde el lector ya está;
las reglas transversales viven en `standards/`. La duplicación entre las dos es mínima y
deliberada: la fase declara contenido, el estándar declara método.

## Decision

Cada fase `01` … `14` declara una sección `## Information items` con `### Inputs` y
`### Outputs`. Cada output nombra lo que el artefacto contiene, no sólo cómo se llama.
Las quality gates de las catorce fases se reescribieron para satisfacer las tres condiciones
de `standards/normative-language.md`: nombran el artefacto, el criterio y la evidencia.

El estándar `standards/information-items.md` fija las reglas comunes: qué es un
information item, cuándo su contenido declarado es suficiente, cuándo se retiene, cómo
se versiona y qué no resuelve.

Las reglas siguen a **ISO/IEC/IEEE 15289:2019**, que especifica el propósito y el
contenido de los information items del ciclo de vida y clasifica los documentos en tipos
genéricos (descripción, plan, política, procedimiento, informe, petición,
especificación). Es la misma separación que aplica ISO/IEC/IEEE 12207:2017 al definir un
proceso por propósito, resultados, actividades y tareas, delegando explícitamente el
nombre, formato y contenido de los information items a 15289. Proceso y papeleria se
especifican por separado, y por eso cambiar una no cambia automáticamente la otra.

## Consequences

### Positive

- Un revisor puede decidir si un artefacto de fase es suficiente leyendo la fase, sin
  preguntarle a quien lo escribió. La tercera condición de `standards/normative-language.md`
  deja de ser aspiracional en las catorce fases.
- Las gates de las catorce fases son ahora falsables. `CI` puede comprobar que la
  sección existe y que ninguna gate vuelve a los adjetivos.
- La fase `12` declara explícitamente el caso en que una migración no es reversible.
  Antes, «rollback strategy» aparecía en una lista de consideraciones sin estado terminal.
- Un artefacto de fase que omite exclusiones de alcance ahora es detectable: la segunda
  condición de suficiencia exige declarar lo que se dejó fuera.

### Negative / trade-offs

- **No introduce trazabilidad entre fases.** Un cambio en un requisito no tiene un vínculo
  forzado con el diseño y los tests que lo implementan. Eso es el principio 7 de
  `blueprint/00-principles.md` y sigue siendo aspiracional; resolverlo es un mecanismo
  distinto, con un coste distinto, y se registra como limitación conocida en
  `standards/information-items.md`.
- **No añade artefactos nuevos a ninguna fase.** Cada fase sigue produciendo lo que
  producía. Se especifica el contenido de lo que ya existía; no se crea ningún artefacto
  nuevo ni se subdivide ninguna fase.
- **No fija números.** Ninguna gate inventa un umbral que el proyecto no haya declarado.
  Una gate que exige «el análisis cubre al menos N alternativas» sin que N signifique
  algo para nadie es una gate que se cumple siempre.
- **No aplica a la fase `00`.** `00-principles.md` es una declaración de principios, no
  un proceso, y no produce information items. Es la misma distinción que hace 12207.

## Date
2026-09-29
