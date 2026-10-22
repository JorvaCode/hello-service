# ADR-007: Licencia y gobernanza del repositorio

## Status
Accepted

## Context
El repositorio se distribuye con dos instaladores (`scripts/blueprint-init.ps1` y `scripts/blueprint-init.sh`) que copian `blueprint/`, `templates/`, `standards/` y `.opencode/` dentro de proyectos de terceros. ADR-001 declara el Blueprint reutilizable entre proyectos, equipos y organizaciones, y el CHANGELOG ya publica la versión 1.0.0 como "Initial reusable release".

Aun así, el repositorio no contenía ningún fichero `LICENSE`, ni `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, `CODEOWNERS` ni plantillas de issue y pull request. Tampoco contenía `.editorconfig` ni configuración de linting de Markdown.

Esto produce tres defectos concretos:

- **Legal.** Sin licencia explícita, opera el derecho de autor por defecto y todos los derechos quedan reservados. Un consumidor que copia el contenido con los instaladores no tiene concedido legalmente ese derecho, aunque el README se lo invite a hacerlo. La premisa central del proyecto —la reutilización— no está refrendada.
- **De proceso.** `standards/git.md` define commits, ramas y pull requests, pero no existe un `CONTRIBUTING.md` que declare el orden de autoridad entre `blueprint/`, `standards/`, `templates/` y `.opencode/`. Esa regla solo vive hoy dentro de `AGENTS.md`, que es específico de asistentes de IA, y por tanto no llega a un colaborador humano.
- **De automatización.** El principio 5 del Blueprint es "Automation first", pero el propio repositorio no está protegido por esa automatización. El CI valida enlaces internos con un `grep` propio que no cubre enlaces por referencia ni anclas, y nada valida la estructura de la prosa.

La elección de licencia es una decisión legal y estratégica que no puede derivarse del contenido del repositorio, y por eso se registra aquí con sus alternativas en lugar de deducirse.

## Options considered
1. No añadir licencia. Mantener el repositorio sin uso externo más allá de la lectura.
2. Licencia de software permisiva clásica (MIT o Apache-2.0) aplicada al repositorio completo.
3. Licencia de contenido Creative Commons Attribution 4.0 (CC BY 4.0) aplicada al repositorio completo.
4. Open Government Licence v3.0 (OGL v3.0), el modelo que usa GDS Way.
5. Licencia doble: contenido bajo CC BY 4.0 y cualquier código futuro bajo una licencia de software.

Para la gobernanza se consideraron: (a) no añadir nada y confiar en GitHub; (b) añadir únicamente `CONTRIBUTING.md`; (c) añadir el conjunto completo de ficheros de gobernanza más `.editorconfig` y linting de Markdown.

Para el linting se consideraron tres herramientas: `markdownlint-cli` (Node, sin dependencias de red más allá de la instalación), `lychee` (binario Rust, descarga una release) y Vale (prosa, requiere paquetes de estilos externos). Se descartaron `lychee` y Vale, y el motivo queda registrado en `CONTRIBUTING.md`.

## Decision
Opción 3 para la licencia: **CC BY 4.0**, con el texto legal completo verbatim en `LICENSE`.

CC BY 4.0 es una licencia de contenido, no de software, y es la que corresponde a un repositorio cuyo producto es un proceso documentado. Concede cualquier uso, incluida la adaptación y el uso comercial, con una única condición: la atribución. No impone copyleft, de modo que un proyecto que quiera publicar su propio proceso derivado no queda obligado a reabrirlo. Exige atribución de forma expresa, lo que resulta apropiado para un estándar que otros proyectos Adoptan citando su origen.

Se descarta la opción 5: no hay código de aplicación en este repositorio, y añadir una licencia de software para un código hipotético sería complejidad sin problema detrás. Si algún día el repositorio incluye código ejecutable, esa decisión se tomará en un ADR propio.

Opción (c) para la gobernanza: se añade el conjunto completo — `LICENSE`, `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, `.github/CODEOWNERS`, plantillas de issue y pull request, `.editorconfig` y `.markdownlint.json`.

Para el linting se adopta únicamente `markdownlint-cli`, con versión fijada. Se descartan `lychee` y Vale por proporcionalidad, conforme al principio 8 del Blueprint: `lychee` añade una descarga de binario externo para comprobar la vivacidad de URLs externas, y Vale requiere paquetes de estilos que en un repositorio bilingüe producirían falsos positivos sobre el texto en español. La comprobación de vivacidad de URLs externas queda fuera de alcance y se revisa aparte.

La configuración de `markdownlint-cli` desactiva `MD013` (longitud de línea), `MD022` (líneas en blanco alrededor de encabezados) y `MD032` (líneas en blanco alrededor de listas). Esto no es una concesión de calidad: documenta el estilo de la casa, que es compacto y deliberado, y evita un reformateo de más de treinta documentos normativos que sería refactoring no relacionado.

## Consequences
### Positive
- La reutilización que el repositorio promete queda legalmente concedida y sus condiciones son explícitas.
- Un colaborador humano recibe el orden de autoridad, las reglas de escritura y el proceso de pull request sin depender de `AGENTS.md`.
- Existe un canal privado y con plazos para reportar vulnerabilidades, lo que es obligatorio en un repositorio con workflows que se ejecutan en cada push.
- Los documentos se validan contra un linter estándar de facto, con la configuración versionada y la versión fijada.
- El linting detectó y corrigió defectos reales: `<name>` sin marcar como código en cinco documentos, y seis ficheros sin salto de línea final.
- La validación de enlaces internos permanece sin cambios en este ADR. Se unifica y se amplía junto con el workflow reutilizable, para no tocar dos veces la misma validación.

### Negative / trade-offs
- CC BY 4.0 exige atribución en cada reutilización. Un proyecto que quiera incorporar el proceso sin citar el origen no puede hacerlo.
- La configuración de `markdownlint-cli` desactiva tres reglas. Un repositorio con reglas más estrictas no coincidirá con esta configuración.
- `SECURITY.md` y `CODE_OF_CONDUCT.md` requieren una dirección de contacto mantenida que este cambio deja marcada como pendiente de completar por el propietario del repositorio.
- `.github/CODEOWNERS` requiere que el equipo `@JorvaCode/blueprint-maintainers` exista en la organización. Una entrada que no resuelve a ningún usuario o equipo se ignora en silencio, y por eso también queda marcada como pendiente.
- La plantilla de pull request es más larga. Es intencionado: obliga a declarar la fase y a enumerar las comprobaciones antes de que un revisor las pida.

Resuelto el 2026-09-29. Los tres puntos pendientes de este ADR se cerraron sin inventar un
mantenimiento que nadie se comprometa a asumir:

- Los dos ficheros de reporte ya no piden un buzón. Ambos enrutan por canales privados de
  GitHub —el botón *Report a vulnerability* y *Report abuse* de la organización—, que no
  requieren que una dirección esté vigilada. La ausencia del botón en `SECURITY.md` se
  declara como un hueco de configuración del mantenedor, no como una invitación a publicar
  el reporte.
- `.github/CODEOWNERS` se eliminó. Con un solo mantenedor no hay una segunda cuenta que
  nombrar, y un fichero de propietarios que no resuelve a nadie se parece a un control de
  revisión y no protege de nada. Regresa como un fichero de una línea cuando exista otra
  persona que nombrar.

Resuelto el 2026-09-29, segunda parte. El párrafo de `## Decision` que descarta la opción 5
—"no hay código de aplicación en este repositorio"— ha quedado superado por
[ADR-012](adr-012-licencia-del-codigo-y-distribucion-de-licencias.md). La premisa era
correcta cuando se escribió y hoy es falsa: el repositorio tiene cinco scripts ejecutables y
dos definiciones de workflow, y CC BY 4.0 los distribuye como si fueran contenido. La
documentación sigue bajo CC BY 4.0 y el código pasa a Apache-2.0.

El `## Status` de este ADR no cambia a `Superseded`, y esa es la decisión. Un ADR aceptado
cuyo texto se reescribe deja de ser un registro: pasa a ser una afirmación sobre lo que el
mantenedor cree hoy. La mitad de gobernanza de este ADR sigue vigente y no se toca; la mitad
de licencia queda ampliada y no corregida, y ADR-012 dice cuál es la parte que manda. Un
lector que encuentre las dos referencias sabe cuál es cuál.

## Date
2026-09-29
