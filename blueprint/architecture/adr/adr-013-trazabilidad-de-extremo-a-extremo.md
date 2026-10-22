# ADR-013: Trazabilidad de extremo a extremo con un campo por information item

## Status
Accepted

## Context
Ámbito: `blueprint/00-principles.md` … `blueprint/14-continuous-improvement.md` y
`standards/information-items.md`.

`ADR-009` declaró los information items de las catorce fases y dejó constancia
expresa de lo que no resolvía: un cambio en un requisito no tenía vínculo forzado
con el diseño y los tests que lo implementan, ni con la release que lo entregó. Esa
limitación viven en `standards/information-items.md`, que la declara en su propio
texto. Es el principio 7 de `blueprint/00-principles.md` declarado como aspiracional
por un ADR que, sin embargo, no lo había resuelto.

La auditoría encontró además que la cadena no estaba rota en un punto sino en dos,
y que el segundo era invisible porque nadie lo buscaba:

- La fase `04` ya exige que cada elemento del diseño nombre el requisito que
  satisface. La fase `05` produce la implementación y no lo nombra. El eslabón
  diseño→requisito existía; el de requisito→implementación, no.
- La fase `11` produce la release record con la versión del artefacto y los entornos
  que alcanzó. No dice qué requisitos entrega. Una release puede describir el
  artefacto con precisión absoluta y ser imposible de responder a la pregunta «¿el
  requisito FR-07 salió en producción y quién lo verificó?». La cadena terminaba en
  el artefacto, que es exactamente el punto donde deja de ser útil: el artefacto es
  lo que existe, no lo que se pidió.

El problema de fondo no es que falte un documento. Es que las preguntas que la
trazabilidad pretende responder no tienen hoy ninguna fuente, y por tanto ninguna
respuesta puede declararse incorrecta.

El segundo defecto era de otra naturaleza. `blueprint/requirements/software-engineering-blueprint.md`
se titula `REQ-BP-001 — Software Engineering Blueprint v1.0.0`, su FR-08 declara
«v1.0.0, initial reusable release» y su supuesto afirma que «el contenido actual del
repositorio v1.0.0 es la línea base». Mientras `VERSION` dice `1.1.0-dev`, ese
documento describe un estado que ya no existe: no menciona el juego de gobernanza,
el reparto de licencias de `ADR-012`, este mismo estándar de information items, los
quality attribute scenarios, la modalidad normativa ni el manifiesto de adopción. Sus
criterios de aceptación no tienen método de verificación, de modo que **incumple el
Definition of Done que este mismo repositorio se escribió**. Un registro de
requisitos que viola el DoD del proyecto no puede sostener una cadena de trazabilidad,
por muy bien resuelta que esté la cadena.

## Options considered

**Opción A — solo plantillas y registro, sin tocar las fases.** Arregla el
registro obsoleto y engorda las plantillas sin cerrar el hueco de `ADR-009`. Es la
opción barata, y su desventaja es que deja la limitación declarada con una
limitación recién escrita encima: el texto de `standards/information-items.md`
diría que la trazabilidad no está resuelta mientras las plantillas Creed que sí.
Descartada por incoherente consigo misma.

**Opción B — borrar el registro y tratarlo como artefacto histórico.** `git log`
conserva la v1.0.0 igual que cualquier otra historia; un documento que describe una
línea base muerta no aporta información que el historial no dé mejor. Es la opción
más limpia en número de archivos, y su desventaja es que el repositorio quedaría sin
ningún lugar donde tranquillemente se declare a qué está obligado. `blueprint/` es
una plantilla que los consumidores instalan; un consumidor que pregunta «¿qué
promete este repositorio?» obtendría como respuesta un changelog. Descartada.

**Opción C — matriz de trazabilidad requirements↔tests↔releases.** Es la solución
que la literatura estándar llama traceability matrix, y es la que casi siempre
aparece escrita. Descartada por desproporción en la forma en que ADR-009 descartó
las catorce plantillas: una matriz es un artefacto nuevo que mantener que no
contiene información que la regla de un campo no contenga ya, y que además
introduce un problema de sincronización propio. Una matriz de trazabilidad que se
desincroniza no es trazabilidad: es un segundo lugar donde mentir con estructura
convincente. Violaría el principio 8, que pide el mecanismo más sencillo que
resuelva el problema.

**Opción D — un campo por information item, dos anclas y un extremo de entrega.**
Adoptada. La regla es que un output que satisface, implementa o verifica un requisito
nombra el identificador, y que un output que no satisface ninguno lo dice
explícitamente. Eso más un identificador estable para los criterios de aceptación y
un extremo de entrega en la fase `11` cierra la cadena sin añadir ningún artefacto
nuevo a ninguna fase.

## Decision

El mecanismo es un campo, no un documento. Tres piezas:

**1. El requisito nombra su verificación y su implementación.** Cada entrada del
registro en `blueprint/requirements/` declara, además del enunciado, el método con el
que un tercero decide si se cumple y el artefacto que la satisface. El registro pasa
a ser un documento vivo, actualizado en el mismo cambio que altera lo que el
repositorio sostiene, según el Definition of Done.

**2. Los criterios de aceptación tienen identificador estable.** La fase `01` emite
`AC-nn` además de `FR-nn` y `NFR-nn`. Sin un identificador que nombrar, «el criterio
que dice X» es una referencia que se rompe en cuanto alguien reescribe el texto. Con
identificador, la prueba y la release nombran la misma cosa y la pregunta «¿este
criterio salió?» tiene una respuesta.

**3. El extremo de entrega está en la fase `11`.** La release record nombra los
identificadores de requisitos y cambios que entrega. Es el punto donde la cadena se
cierra, y es el que faltaba por completo.

La granularidad es el requisito, no la línea de código. Un proyecto con doscientos
requisitos no obtiene doscientas filas: traza desde el cambio al requisito que
satisface, y del requisito a la release que lo entregó. Un requisito sin ninguna
release que lo entregue es un requisito declarado y no entregado, y la fase lo
declara en vez de dejarlo abierto.

El caso de «este cambio no satisface ningún requisito» es una **declaración**, no un
campo ausente. Una corrección de errata en prosa no inventa un `FR-nn` para quedar
bien ante un validador, y una función nueva que nadie pidió sí necesita uno. La
distinción es la misma que hace `standards/code-design.md` con la ausencia de un
patrón: la ausencia no es un hallazgo, la ausencia no declarada sí lo es.

`standards/information-items.md` declara esta regla junto a las que ya gobiernan los
information items, y su sección de limitaciones deja de declarar esta. Lo que sigue
sin resolverse se declara en su lugar.

## Consequences

### Positive

- «¿Este requisito salió y cómo se verificó?» tiene una respuesta con fuente, y esa
  respuesta es el release record de la fase `11`.
- El registro deja de mentir: pasa de describir `v1.0.0` a describir lo que el
  repositorio sostiene hoy, y su propia regla de mantenimiento impide que vuelva a
  quedarse atrás.
- Las plantillas quedan alineadas con los estándares que ya las gobernaban.
  `templates/requirement.md` exige el método de verificación que el Definition of Done
  exige, y `templates/change.md` exige la justificación de patrones que
  `ADR-006` y `standards/code-design.md` ya exigían.
- Se cierra la limitación que ADR-009 declaró, sin editar ADR-009 para que parezca
  que nunca existió. Se le añade una nota de resolución, que es lo que se hizo con
  ADR-007 en ADR-012.

### Negative / trade-offs

- **No hay enforcement automático de completitud.** Una release puede omitir un
  requisito que sí entrega y ninguna herramienta lo detecta. La regla es
  declarativa y depende de la revisión. Cubrirla con tooling es un proyecto
  distinto, con su propio coste, y no se ha disfrazado de parte de este.
- **Aumenta el trabajo documental de cada cambio.** Cada cambio pasa a declarar los
  identificadores que satisface. Es un campo, pero es un campo que hay que rellenar y
  que un revisor tiene que comprobar.
- **Una errata en un requisito existente cambia su texto, no su identificador.** El
  identificador sobrevive a la reescritura; por eso `AC-nn` debe ser un identificador
  y no una cita del enunciado.
- **El registro vivo acumula histórico de cambios de requisitos.** Un registro que
  nunca se vacía crece. Se asume: el detalle del cambio de un enunciado pertenece a
  `git log` y al changelog, no al registro, que declara lo vigente y lo entregado.

## Date
2026-09-30
