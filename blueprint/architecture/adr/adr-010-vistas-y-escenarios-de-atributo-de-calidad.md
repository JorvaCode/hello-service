# ADR-010: Vistas, partes interesadas y escenarios de atributo de calidad

## Status
Accepted

## Context
Ámbito: `blueprint/03-architecture.md`, `blueprint/04-design.md`, `blueprint/06-testing.md`, `standards/terms.md`.

`03-architecture.md` listaba diez aspectos que había que considerar: límites del
sistema, componentes, dependencias, APIs, propiedad de datos, patrones de integración,
límites de seguridad, escalabilidad, observabilidad y modelo de despliegue. La lista es
correcta y no suficiente. El problema es que no decía **para quién** era cada decisión ni
**con qué criterio** se juzgaba correcta.

Ese hueco tiene dos consecuencias concretas. La primera: nada impedía que un cambio
declara una arquitectura correcta y, a la vez, que ningun stakeholder cared, porque no
había un lugar donde se registrara quién tenía qué interés. La segunda, más grave: la
fase 01 produce requisitos no funcionales con umbral —«la consulta responde en menos de
200 ms»— y la fase 03 no tenía forma de llevar ese umbral hasta la decisión estructural. El
número existía, pero se perdía en la frontera entre fases. Un umbral que no llega a la
decisión estructural es un deseo, y no hay forma de detectarlo porque nadie lo comprueba.

La ausencia de un vocabulario compartido agravaba el problema. «Arquitectura» en
diez campos distintos significa diez cosas, y dos revisores usando la palabra con
significados distintos llegaban al mismo veredicto solo por casualidad.

## Options considered

**Opción A — ATAM completo.** El método de ATAM (SEI) sería el objetivo: priorizar
concerns, evaluar con un panel, producir un score. Se descartó por desproporción con
mucha diferencia. ATAM presupone un taller con participantes, agendas de días y un
facilitador; la mayoría de los cambios de este repositorio son de un día y no justifican
esa ceremonia. Un blueprint que exige un taller para un cambio pequeño deja de usarse.

**Opción B — solo stakeholders y concerns, sin escenarios.** Es la mitad de lo
necesario y ya habría mejorado la fase. Se descartó porque deja el hueco principal abierto:
el umbral de la fase 01 sigue sin llegar a la decisión estructural, y esa era la
consecuencia grave.

**Opción C — vocabulario 42010 en alcance completo.** Definir vista, modelo y
correspondencia con la generalidad del estándar. Se descartó por el mismo motivo que A:
proporción. Se adopta el vocabulario, recortado a lo que una gate puede comprobar.

**Opción D — nada.** La lista de diez aspectos ya funciona como lista de comprobación.
Se descartó: es exactamente el patrón que este repositorio ya critica en otros sitios, un
checklist que no puede fallar porque no nombra criterio.

## Decision

La fase 03 incorpora el vocabulario de **ISO/IEC/IEEE 42010:2022** (*Software, systems
and enterprise — Architecture description*) con alcance deliberadamente estrecho:

- **Stakeholders y concerns** se declaran antes que la estructura, porque una estructura
  se diseña para satisfacer concerns y un concern sin stakeholder no tiene a nadie que lo
  resuelva.
- **Vistas**: cuando una única descripción no sirve para todos los concerns, la
  descripción se divide en vistas, cada una desde el standpoint de un viewpoint, con la
  correspondencia entre ellas y el rationale declarados.
- **Quality attribute scenarios** de seis campos —source, stimulus, environment,
  artifact, response, response measure— para cada concern con umbral. El campo
  `response measure` es el que hace el escenario falsable; sin él, el escenario repite el
  concern y la fase 01 ya había producido el umbral que se está ignorando.

El vocabulario se declara en `standards/terms.md` en uso estrecho y declarado como tal.
Las definiciones completas viven en 42010; adoptarlas aquí convertiría un blueprint en un
framework de arquitectura, que es un artefacto mucho mayor con su propio ciclo de vida.

La fase 04 deriva sus niveles de prueba de las response measures en lugar de elegirlos
por separado, y la fase 06 registra cuáles response measures están verificadas y por
qué check. Sin esas dos conexiones el mecanismo sería decorativo: la fase 03 produciría
escenarios que nadie ejecutaría.

## Consequences

### Positive

- El umbral de un requisito no funcional tiene ahora un camino hasta la decisión
  estructural, y la fase 06 dice si está verificado. Un concern con umbral y sin escenario
  verificable es detectable.
- Un cambio pequeño no paga el coste de ATAM. Su gate es «cada concern con umbral tiene un
  escenario», y con un solo concern eso es un párrafo.
- Una vista única sigue siendo válida. La división en vistas es para cuando la
  descripción no sirve a todos los concerns, no un objetivo a sí mismo: el
  `blueprint/00-principles.md` principio 10 dice que la ausencia de un patrón no es un
  defecto, y aplica igual aquí.
- Sensibilidad se registra cuando la response measure no es estable en el entorno
  declarado, porque un diseño que cumple su umbral a una carga que nadie ejerce no lo ha
  cumplido.

### Negative / trade-offs

- `standards/terms.md` crece con diez términos. Es un coste real y aceptado: son los
  términos que la gate necesita para ser falsable, y sin ellos volveríamos a «arquitectura»
  como palabra única.
- **No introduce un taller, un score ni un ranking de atributos.** ATAM y similares
  quedan fuera de forma deliberada.
- **No crea una plantilla nueva.** Los escenarios son una tabla de seis campos dentro de
  la fase, no un artefacto que alguien tiene que aprender a rellenar. La plantilla de ADR
  existente no cambia.
- **No exige el registro completo de stakeholders a todo cambio.** Un proyecto sin
  registro declara los stakeholders que conoce y registra el hueco. Pedir un registro
  completo convierte un mecanismo en burocracia, y un mecanismo que se rellena por
  obligación no se lee.
- **No añade requisitos no funcionales al blueprint.** Las fases 04 y 06 solo pasan a
  consumir lo que la 03 ya produce.

## Date
2026-09-29
