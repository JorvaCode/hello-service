# ADR-006: SOLID, Clean Code y uso justificado de patrones de diseño

## Status
Accepted

## Context

Los principios de `blueprint/00-principles.md` son principios de ciclo de vida —requisitos antes que implementación, arquitectura intencionada, cambios pequeños, calidad continua, automatización, seguridad por diseño, trazabilidad, bucle de mejora e independencia de herramienta—, pero ninguno cubre cómo debe escribirse el código. La consecuencia es que las fases 04 a 08 guían el proceso sin ningún criterio de calidad de diseño que sea verificable:

- `blueprint/04-design.md` incluye la activity "Select appropriate patterns", pero no nombra ningún patrón, no da criterios de selección y no impide la adopción por costumbre. Es una instrucción inaccionable.
- `blueprint/05-implementation.md` exige "Follow project coding standards" y "Prefer simple solutions", pero no define esos estándares. El resultado es que la calidad de diseño queda delegada al criterio de cada autor y no es auditable.
- `blueprint/07-code-review.md` solo tiene la puerta genérica "Clear and maintainable?", que no distingue entre una decisión de diseño justificada y una no justificada.
- `standards/` solo contiene Definition of Done y estándar de Git. No existe ningún estándar de diseño de código.

El riesgo no es únicamente la omisión: añadir SOLID, Clean Code y patrones como exigencia obligatoria produciría el fallo opuesto. El diseño por encima de las necesidades genera abstracciones que nadie pidió, capas que nadie usa y patrones aplicados donde el problema real no existe. Un blueprint que empuje a "aplicar patrones" sin exigir justificación produce sobre-ingeniería, y uno que empuje a "aplicar SOLID" sin diagnóstico produce refactors que rompen comportamiento para corregir problemas que no existían.

Ambos extremos son fallos de un blueprint de proceso: el primero es una brecha de calidad sin criterio verificable, el segundo es un coste de proceso que se paga en cada cambio.

## Options considered

1. No añadir nada. El blueprint es agnóstico de tecnología y los principios de diseño pertenecen al proyecto consumidor.
2. Exigir SOLID, Clean Code y un catálogo de patrones como norma obligatoria, verificable en cada cambio entre las fases 04 y 08.
3. Añadirlos como lentes de diagnóstico con justificación obligatoria para los patrones, en un estándar separado, y hacerlos exigibles en las fases 04, 05, 07 y 08.
4. Añadirlos como documentación opcional, sin ninguna puerta de calidad asociada.

La opción 1 es incorrecta porque el repositorio ya es normativo sobre artefactos y puertas de calidad, y la calidad de diseño es una puerta como cualquier otra: dejarlo sin criterio equivale a delegar una decisión de proceso en cada autor, que es exactamente el problema que el blueprint existe para resolver.

La opción 2 aplica la presión en sentido contrario y es peor que la ausencia de criterio: "aplicar SOLID" no es un objetivo, es un diagnóstico. Forzarlo produce cambios de comportamiento en código que ya funcionaba, y "usar un patrón" deja de exigir que exista un problema, con lo que la instrucción degenera en hábito. Además convierte el blueprint en un catálogo de patrones, lo que contradice NFR-02 (agnóstico de lenguaje): cada lenguaje tiene sus propios patrones idiomáticos y la lista sería arbitraria.

La opción 4 es equivalente a la opción 1 con más páginas.

## Decision

Opción 3. `standards/code-design.md` se convierte en el estándar que define los criterios, y las fases 04, 05, 07 y 08 lo referencian como puerta. El contenido normativo vive una sola vez, en `standards/`; los skills y el subagente solo lo enrutan (ADR-005).

Los tres elementos se tratan de forma deliberadamente distinta, porque su naturaleza es distinta:

- **SOLID es una lente de diagnóstico, no un objetivo.** Ninguna de sus cinco propiedades es un fin. Cada una es una pregunta que se hace al código y solo se convierte en hallazgo cuando la respuesta produce un problema concreto y declarado: por ejemplo, un módulo que debe cambiarse junto con tres no relacionados.
- **Clean Code es el coste de cambio, no una estética.** Sus criterios se expresan como preguntas sobre el coste de entender y modificar el cambio, no como preferencias de estilo.
- **Los patrones de diseño son soluciones con un coste de entrada alto y un problema concreto detrás.** Un patrón solo puede introducirse si el diseño declara el problema que resuelve, la evidencia de que el problema es real —no hipotético—, el coste de la alternativa más simple y el alcance de aplicación. La ausencia de un patrón nunca es un hallazgo por sí misma.

El principio que gobierna los tres es la proporcionalidad ya declarada en `blueprint/08-security-quality.md`: los controles deben ser proporcionales al riesgo. Un cambio trivial no justifica un análisis de diseño; una decisión de arquitectura sí. Ninguno de estos criterios exime del resto del proceso: son preguntas adicionales, no sustitutivas.

El blueprint no se convierte en un catálogo de patrones ni en un manual de SOLID. El estándar define cómo se decide y cómo se verifica, no qué se aplica. Cualquier ejemplo concreto pertenece al proyecto consumidor, no a este repositorio, para no romper NFR-02.

## Consequences

### Positive

- Las fases 04 a 08 dejan de depender del criterio individual del autor: "Select appropriate patterns" pasa a tener un criterio verificable.
- La puerta de la fase 07 puede distinguir entre un patrón justificado y uno por costume, en lugar de limitarse a "Clear and maintainable?".
- Se cierra el riesgo en sentido contrario: la exigencia de justificación impide la sobre-ingeniería tanto como la omisión de los principios.
- El contenido normativo tiene una única ubicación, coherente con ADR-005: los skills no lo repiten.
- Se mantiene NFR-02: el estándar define preguntas y pruebas de justificación, no patrones de un lenguaje concreto.
- La proporcionalidad mantiene el coste de proceso bajo para cambios triviales, alineado con "Small, verifiable changes" en `blueprint/00-principles.md`.

### Negative / trade-offs

- Añade un estándar más que mantener y una puerta más que cerrar en cada cambio.
- Los criterios de SOLID y Clean Code no son automatizables en general; dependen de revisión, con lo que el coste recae en el revisor. La puerta de la fase 08 solo cubre la parte automatizable.
- "Problema concreto" admite interpretación: dos revisores pueden discrepar sobre si una justificación es suficiente. La mitigación es exigir que la justificación quede escrita en el diseño, donde es discutible.
- Introducir un estándar de diseño de código aumenta la presión de revisión sobre bases de código existentes. El estándar no crea hallazgos retrospectivos: solo aplica a los cambios que se producen bajo él.
- Un estándar que nombra principios puede leerse como catálogo. Mitigación: el estándar declara explícitamente que nombra lentes, no patrones a aplicar, y que la lista de patrones aplicables pertenece al proyecto.

## Date
2026-09-29
