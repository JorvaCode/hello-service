# ADR-008: Terminología y modalidad del texto normativo

## Status
Accepted

## Context
El Blueprint es la definición normativa de un proceso de ingeniería, y sin embargo su propio texto normativo no distinguía entre una obligación y una recomendación. Esto producía tres defectos verificables.

**Modalidad mezclada sin convención.** El texto usaba `shall`, `should`, `must`, `Prefer` y `may` sin declarar qué fuerza tenía cada uno. `standards/git.md:7` decía "Prefer small, meaningful commits", que es una instrucción sin fuerza definida. `blueprint/06-testing.md:15` decía "must have automated verification", mientras el mismo conjunto de documentos usaba `shall` en otras frases. Un revisor no puede saber si una frase es exigible.

**Una contradicción interna verificable.** `blueprint/03-architecture.md:18` decía que las decisiones importantes "should be captured", mientras que la quality gate de la línea 21 decía que "important decisions are documented". Una era recomendación y la otra obligación, en el mismo documento, a tres líneas de distancia. Ninguna lectura de la fase puede satisfacer ambas con seguridad.

**Vocabulario sin definiciones.** Ninguno de los ficheros versionados contenía una sección de términos y definiciones, y el Blueprint usaba "quality gate", "artifact", "phase" y "decision" como si estuvieran definidos. El caso más grave era "important", en la contradicción anterior: sin umbral definido, dos revisores pueden discrepar sobre si un ADR era obligatorio y ambos tener razón. La quality gate no era falsable porque el término que la sostenía no lo era.

La fuente correcta para el vocabulario no es una decisión de estilo sino un estándar publicado. **RFC 2119** (Bradner, 1997) y su actualización **RFC 8174** (Leiba, 2017), que actualizan BCP 14, definen las palabras clave que usan las especificaciones de Internet. Tres propiedades de esa fuente condicionan la decisión: las palabras clave solo tienen fuerza normativa en el convenio de énfasis del idioma, que en RFC 2119 son mayúsculas; `MUST` y `SHALL` son sinónimos exactos, de modo que elegir uno u otro no significa nada; y RFC 2119 §4 desaconseja usar esas palabras para imponer un método en lugar de expresar un requisito de interoperabilidad.

Esa última advertencia se aplica a las especificaciones de protocolo, donde lo que importa es que implementaciones independientes interoperen. No se transfiere a un blueprint de proceso, cuyo propósito es precisamente imponer un proceso. Por eso este ADR no importa esa prohibición: el Blueprint puede y debe usar `shall` para exigir un proceso. La pregunta útil es otra, y es la que este cambio responde: si lo que se exige es comprobable.

## Options considered
1. No cambiar nada. El texto normativo conserva su vocabulario actual.
2. Adoptar RFC 2119 literalmente, con `MUST`, `SHOULD` y `MAY` en mayúsculas.
3. Adoptar el vocabulario de RFC 2119 con las palabras en minúsculas, declarando en el repositorio que tienen fuerza normativa, y acompañarlo de un glosario.
4. Declarar la convención en `CONTRIBUTING.md` sin un estándar propio.
5. Alinear el texto con ISO/IEC/IEEE 15289:2019 e ISO/IEC/IEEE 24748, incluyendo tipos de documento y procesos de gestión del ciclo de vida.

## Decision
Opción 3 para la modalidad, con un estándar propio. `standards/normative-language.md` declara el vocabulario, la desviación respecto de RFC 2119 y el test de falsabilidad de una quality gate. `standards/terms.md` recoge el vocabulario, indicando en cada entrada si la definición procede de un estándar externo o es local.

La desviación de RFC 2119 es deliberada y queda declarada, no pasada por alto. El estándar exige mayúsculas; este Blueprint escribe en minúsculas siguiendo el convenio de ISO, y es el propio `standards/normative-language.md` el que les confiere fuerza dentro del repositorio. La consecuencia es real y se acepta: quien no conozca la convención no puede distinguir una obligación de una preferencia.

Se descarta la opción 2. Escribir `MUST` en mayúsculas en cuarenta documentos en inglés habría sido una modificación del texto normativo mucho más disruptiva que una declaración de ocho párrafos, y el resultado habría sido indistinguible de un estándar de protocolo, que no es lo que este repositorio es.

Se descarta la opción 4. Una convención declarada únicamente en el fichero de contribución para colaboradores humanos no alcanza a un asistente de IA que lee `AGENTS.md`, ni a un lector del repositorio como repositorio, ni a un consumidor que instale solo `blueprint/`. La convención tiene que vivir en la capa normativa.

La opción 5 no se descarta, se divide. La alineación de los *information items* de cada fase con ISO/IEC/IEEE 15289:2019 es una decisión mayor, con su propio ADR, porque afecta a los catorce documentos de fase y al modo en que un proyecto consumidor verifica una fase. Mezclarla aquí habría producido un cambio que toca todo el repositorio y no se puede revisar por partes.

Como parte de este cambio, el uso existente se normaliza: `must` pasa a `shall` en los ocho lugares donde era modalidad, y `Prefer` pasa a una construcción con `should` en los cuatro documentos normativos afectados. Se conservan sin tocar los dos casos en que `must` es un sintagma nominal y no una modalidad ("defining for each phase what must happen") y la cita textual de `blueprint/05-implementation.md` que ADR-006 conserva como registro del estado anterior.

Se corrige la contradicción de `blueprint/03-architecture.md`: las decisiones significativas se registran según los criterios de `standards/terms.md`, y la quality gate nombra el artefacto, el criterio y la evidencia.

## Consequences
### Positive
- Una quality gate puede ser refutada por un tercero: nombre el artefacto, el criterio y la evidencia.
- La contradicción interna de la fase 03 desaparece, y "importante" pasa a tener un umbral verificable con cinco criterios objetivos.
- El vocabulario del repositorio deja de depender de que cada lector adivine la intención de quien escribió la frase.
- Un asistente de IA que lea `AGENTS.md` tiene una referencia única y no necesita inferir la modalidad.
- Registrar un ADR para una decisión no significativa deja de ser una obligación implícita, lo que reduce el ruido en la práctica de ADRs ya existente.

### Negative / trade-offs
- El Blueprint se desvía de RFC 2119 en el uso de mayúsculas. Un lector que aplique el estándar por inercia puede suponer que `shall` en minúsculas es texto decorativo.
- Añadir `standards/normative-language.md` y `standards/terms.md` aumenta la superficie normativa en dos documentos que deben mantenerse sincronizados con el resto.
- Normalizar `must` a `shall` cambia texto normativo ya publicado, lo que obliga a releer documentos que la mayoría de los proyectos considera estables.
- La redefinición de "significant decision" puede volver obligatorios algunos ADRs que antes se consideraban opcionales, y opcionales otros que se hacían por rutina. Es el efecto buscado, pero es un cambio de comportamiento.
- La reescritura completa de las quality gates de las catorce fases se aplaza al trabajo de *information items*. Hasta entonces, varias gates siguen sin ser falsables: este ADR mejora el marco, no las gates concretas que aún no se han reescrito.

## Date
2026-09-29
