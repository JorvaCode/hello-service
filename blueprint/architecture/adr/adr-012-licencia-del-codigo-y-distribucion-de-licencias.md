# ADR-012: Licencia del código y distribución de las licencias

## Status
Accepted

## Context

ADR-007 aplicó CC BY 4.0 a todo el repositorio y descartó explícitamente una licencia de software con este argumento: "no hay código de aplicación en este repositorio, y añadir una licencia de software para un código hipotético sería complejidad sin problema detrás". Ese argumento era correcto en su momento y hoy es falso.

El repositorio contiene código ejecutable: `scripts/blueprint-init.sh`, `scripts/blueprint-init.ps1`, `scripts/validate-blueprint.sh`, `scripts/test-installers.sh`, `scripts/test-installers.ps1` y las definiciones de workflow de `.github/workflows/`. Todo eso está hoy bajo CC BY 4.0, y bajo CC BY 4.0 se distribuye.

Creative Commons publica su propia recomendación al respecto, y no es ambigua. Su FAQ dice: "We recommend against using Creative Commons licenses for software. Instead, we strongly encourage you to use one of the very good software licenses which are already available." La razón que da es la que aplica aquí: "Unlike software-specific licenses, CC licenses do not contain specific terms about the distribution of source code, which is often important to ensuring the free reuse and modifiability of software. Many software licenses also address patent rights, which are important to software but may not be applicable to other copyrightable works. Additionally, our licenses are currently not compatible with the major software licenses."

Los tres puntos de esa cita tienen un efecto concreto sobre este repositorio:

- **Distribución de código fuente.** Un instalador entrega ficheros de código. La sección 4 de CC BY 4.0 regula la distribución de material, no la distribución de código fuente, y no exige conservar los avisos de copyright, aviso de licencia y fecha al redistribuir. La sección 4.b sí lo exige, pero para "el material"; la lectura que hace la comunidad es que la obligación de atribución se satisface con la licencia y el crédito, no con el aviso por fichero.
- **Patentes.** CC BY 4.0 dice en su sección 2.b.2 que "Patent and trademark rights are not licensed under this Public License". Un ejecutable de CI se ejecuta en la máquina de quien lo usa, y una cesión de patentes es una de las protecciones que un proyecto espera de una licencia de software.
- **Compatibilidad.** La FAQ dice que las licencias CC "are currently not compatible with the major software licenses". Un consumidor que incruste `scripts/blueprint-init.sh` en un proyecto bajo Apache-2.0, MIT o GPL se encuentra con un fichero cuyo único grant procede de una licencia que el propio CC dice incompatible con la suya.

La FAQ también autoriza exactamente el reparto que este ADR adopta: "While we recommend against using a CC license on software itself, CC licenses may be used for software documentation, as well as for separate artistic elements such as game art or music."

A esto se añade un defecto de cumplimiento que el repositorio arrastra desde ADR-007 y que ninguna de sus decisiones subsanó. CC BY 4.0 condiciona la reutilización a la atribución, y los instaladores no distribuían ningún fichero de licencia: un consumidor que ejecutaba `blueprint-init.sh` recibía contenido cuya reutilización condicionada nunca se le entregaba. Un permiso cuyo condicionante no viaja con el permiso no se ha concedido de forma que el destinatario pueda cumplirlo.

ADR-011 establece que la allowlist de instalación "es la definición ejecutable de lo que es el blueprint" y que añadir una entrada es una decisión de distribución que requiere un ADR. Este ADR es ese registro para las licencias, y lo hace de forma explícita: las licencias NO se añaden a la allowlist, porque la allowlist describe directorios de contenido y las licencias no son contenido del proceso. Se distribuyen por una regla distinta, con su propio nombre y su propia evidencia en el manifiesto.

## Options considered

Para la licencia del código:

1. **MIT.** Permisiva, sin copyleft, la más compatible de todas: cualquiera puede incrustarla en un proyecto con cualquier otra licencia. Es la opción por defecto de la industria para utilidades que la gente copia.
2. **Apache-2.0.** Permisiva como MIT, con cesión expresa de patentes que MIT no tiene, y con la obligación de conservar en el código fuente redistribuido los avisos de copyright, patente, marca y atribución.
3. **BSD-3-Clause.** Comparable a MIT, sin cesión de patentes.
4. **MPL-2.0.** Copyleft a nivel de fichero. Obliga a publicar modificaciones de los ficheros del proyecto, que es una carga para un instalador que un consumidor simplemente copia.
5. **GPL-3.0.** Copyleft fuerte. Obliga a que cualquier obra derivada del instalador se publique bajo GPL, lo que impediría a un proyecto propietario usar el instalador.
6. **Mantener CC BY 4.0 para todo.** No cambia nada, y contradice la recomendación explícita de la propia entidad licenciadora, que es el hecho más desfavorable de esta lista.

Para la distribución de las licencias al consumidor:

1. No distribuir nada. Es el estado actual y es un defecto de cumplimiento de CC BY 4.0.
2. Solo un aviso en consola. Informa pero no entrega los términos, y un consumidor que automatiza la instalación no lee la consola.
3. Solo copiar los ficheros. Entrega los términos pero pisa la licencia propia del consumidor.
4. Aviso, registro en el manifiesto y copia de los ficheros solo si el destino no tiene ya un `LICENSE`. Entrega los términos, no pisa nada, y deja constancia por escrito de qué se entregó.

## Decision

**Opción 2 para el código: Apache-2.0**, en `LICENSE-CODE`, con el texto legal completo verbatim. **CC BY 4.0 se mantiene para la documentación**, en `LICENSE`, con su texto completo verbatim. Los dos ficheros declaran su alcance por escrito: `LICENSE` nombra los directorios que cubre y afirma que el código no está cubierto; `LICENSE-CODE` hace lo contrario.

Apache-2.0 sobre MIT no por superioridad de permiso, que es el mismo en lo esencial, sino por las dos cosas que le pedían a esta licencia los tres puntos de la cita de Creative Commons. La cesión de patentes cubre la objeción directa de la sección 2.b.2 de CC BY 4.0, y la sección 4.c - conservar los avisos de copyright, patente, marca y atribución - cubre la objeción de la distribución de código fuente. MIT no resuelve ninguna de las dos. El coste de Apache-2.0 sobre MIT es la longitud del texto y la cláusula 4.d sobre ficheros `NOTICE`, que este proyecto no tiene y que por tanto se traduce en cero trabajo para el mantenedor.

**Opción 4 para la distribución.** Ambos instaladores, después de la allowlist y antes del manifiesto:

- copian `LICENSE` y `LICENSE-CODE` al destino **únicamente si el destino no tiene ya un `LICENSE`**. Un proyecto que ya tiene licencia en su raíz tiene una licencia propia, y pisarla es un defecto, no una cortesía. Si existe, el instalador lo dice y sigue.
- imprimen un aviso con las dos licencias y con el ADR que las fija.
- registran en `.blueprint-install.json` un bloque `licenses` con las dos licencias y la lista de ficheros realmente entregados. La lista puede estar vacía, y lo está cuando el destino ya traía su propio `LICENSE`: un manifiesto que declara ficheros entregados que no se entregaron es exactamente el defecto que corrigió el commit `679dbb4`, y no se reintroduce aquí bajo otro nombre.

La allowlist de ADR-011 no cambia. Se documenta en el propio instalador que las licencias viajan por esta regla y no por la allowlist, para que nadie lea la copia de las licencias como un ensanchamiento silencioso del allowlist.

ADR-007 se resuelve en su párrafo que descarta la opción 5, que queda sustituido por este ADR. No se modifica su Status: un ADR aceptado no se reescribe cuando un decisión posterior lo supera, se le añade una nota que apunta al ADR que lo sustituye. La nota queda bajo `## Consequences` y no altera el texto original de la decisión, porque el valor de un registro histórico está en que dice lo que se pensaba cuando se escribió.

## Consequences
### Positive
- El repositorio deja de estar bajo una licencia que su propia entidad licenciadora desaconseja para software, y pasa a estar bajo una que cubre distribución de código fuente y patentes.
- La reutilización que el proyecto promete llega al consumidor con sus condiciones, lo que cierra el hueco de cumplimiento de CC BY 4.0.
- Un consumidor puede incrustar los instaladores en un proyecto con cualquier licencia sin arrastrar una incompatibilidad entre licencias.
- La instalación sigue siendo no destructiva: un proyecto con su propio `LICENSE` no lo ve modificado.
- El manifiesto dice qué licencias y qué ficheros recibió el consumidor, lo que es verificable desde el propio destino.

### Negative / trade-offs
- Hay dos licencias que leer en lugar de una, y cada cambio de alcance obliga a tocar los dos ficheros para que no se contradigan.
- Apache-2.0 es un texto mucho más largo que MIT y que CC BY 4.0, y ocupa 202 líneas en `LICENSE-CODE` que un lector no consultará.
- El aviso de consola y el bloque `licenses` del manifiesto son superficie nueva que las pruebas de los instaladores tienen que cubrir para que no se degraden en silencio.
- Un consumidor que ya tiene un `LICENSE` recibe el contenido sin los términos de CC BY 4.0, y depende del aviso y del manifiesto para conocerlos. Se acepta: la alternativa es pisar su licencia.

## Date
2026-09-29
