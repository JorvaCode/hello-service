# ADR-011: Distribución versionada, allowlist de instalación y una sola validación

## Status
Accepted

## Context
Ámbito: `scripts/`, `.github/workflows/`, `.gitignore`, `VERSION`, `README.md`.

El blueprint se distribuye con dos instaladores que copian directorios completos. El
problema no es que copien de más lo que el proyecto necesita; es que copian de más lo que
el mantenedor tiene en su árbol de trabajo.

Concretamente: ambos instaladores copian `.opencode/` entero y luego borran `node_modules`
a posteriori. En la práctica eso significa que un consumidor recibe también el
`package.json`, el lockfile y el `.gitignore` locales de quien ejecutó el instalador. La
limpieza posterior es una lista negra contra un problema de lista blanca, y las listas
negras pierden: cualquier artefacto nuevo que aparezca en `.opencode/` se cuela sin que
nadie se entere hasta que ya está en el repositorio de un consumidor.

El `.opencode/.gitignore` que existía hacía esto más difícil de lo que pareció: se
ignoraba a sí mismo, así que era imposible versionarlo. Cada colaborador debía
reinventarlo localmente y cada uno podía cubrir un conjunto distinto de archivos. La
solución era mover esas reglas al `.gitignore` de la raíz, que sí está versionado.

El segundo problema era la trazabilidad de la distribución. Una copia instalada no
decía qué versión era. El repositorio tenía `[1.0.0]` en el CHANGELOG y `[Unreleased]`
con trabajo sin publicar, pero ningún archivo de la raíz declaraba la versión, así que
un consumidor no podía saber qué había recibido ni si lo había modificado después. Sin
esa información, «actualizar el blueprint» no es una operación: es volver a instalar y
rezar.

El tercero era que la validación vivía duplicada. `ci.yml` tenía el gate completo
inline; `reusable-blueprint-validation.yml` tenía una versión de cinco líneas que
comprobaba `test -d` en tres directorios. Dos gates que dicen cosas distintas no son
redundancia, son dos fuentes de verdad, y la que nadie mira es la que se queda vieja.
La versión de cinco líneas además se ofrecía a los consumidores como si validara el
blueprint, y no validaba casi nada.

## Options considered

**Opción A — mantener la lista negra y añadir más exclusiones.** Se descarta. Añadir
`package.json` al `rm -rf` de ambos instaladores funciona hoy y falla mañana. Es la
opción que ya estaba y su coste es invisible solo hasta la primera fuga.

**Opción B — `git clone` o submódulo para instalar.** Se descarta: exige que el consumidor
tenga git y una conexión, y el compromiso del proyecto es que los instaladores no
necesitan nada externo. Además instalaría la historia completa en repositorios que no la
quieren.

**Opción C — publicar un paquete en un registro.** Es la solución correcta a largo plazo y
la correcta en cuanto exista un mantenedor que pueda asumir el coste de versionar y
publicar. No se adopta ahora porque no hay quien lo mantenga, y un mecanismo de
publicación sin mantenedor se convierte en la parte del proyecto que se rompe primero. El
`VERSION` y el manifiesto dejan el camino abierto sin exigirlo.

**Opción D — dejar dos validaciones.** Se descarta por la razón ya expuesta. La versión de
cinco líneas además prometía a los consumidores una validación que no hacía.

**Opción E — añadir un hash de contenido al manifiesto.** Se descarta, y es la única
decisión de este ADR que se aparta por una restricción técnica y no de criterio. `sha256sum` no
es parte de POSIX —en macOS se llama `shasum`— y los instaladores prometen no depender de
nada externo. Un hash haría que dos consumidores con el mismo número de versión pero
contenido distinto parecieran el mismo, lo cual es peor que no tener hash. El
identificador es la versión, y es la que el proyecto controla.

## Decision

**1. Allowlist en lugar de lista negra.** Los dos instaladores declaran una lista explícita
de entradas —`blueprint`, `templates`, `standards`, `.opencode/agents`, `.opencode/skills`—
y no copian nada más. La limpieza de `node_modules` desaparece: con una allowlist ya no hay
nada que limpiar, y dejarla sería un mensaje contradictorio. El adaptador OpenCode sigue
siendo opcional, porque no es una dependencia (ADR-002): si el origen no lo tiene, se
informa y se sigue.

El motivo de la allowlist es que el coste de añadir una entrada es visible —una línea y un
ADR— mientras que el coste de que una entrada se cuele es invisible hasta que ya
contamina un proyecto ajeno. Merece la pena que lo previsto cueste más que lo accidental.

**2. `VERSION` en la raíz y manifiesto de adopción.** `VERSION` declara la versión que
produce este árbol de trabajo. Los instaladores escriben `.blueprint-install.json` en el
destino con la versión instalada, la fecha, el instalador usado y las entradas que se
aplicaron de verdad. Un consumidor puede responder «qué blueprint tengo» sin buscar en
ningún sitio.

La versión es `1.1.0-dev` mientras el CHANGELOG tenga trabajo sin publicar, y pasa a
`1.1.0` como parte de la publicación. Se declara como `-dev` en vez de afirmar `1.1.0`
porque el árbol ya no es lo que se publicó como `1.0.0`, y un número que miente es peor
que un número con sufijo.

**3. El gate sale del workflow.** El gate se mueve a `scripts/validate-blueprint.sh`,
que `ci.yml` ejecuta. Antes vivía escrito dentro de `ci.yml`, de modo que cada
comprobación nueva había que reescribirla a mano para probarla en local. Ahora se
ejecuta en local sin Actions, que es como se desarrolló esto mismo: cada gate
nuevo se comprueba por mutación antes de commitearse.

**El workflow reutilizable no ejecuta este script, y no puede.** La
documentación de M11 afirmaba que también lo ejecutaba, y era falso. La razón es
estructural, no una omisión: el gate valida *este* repositorio, y los instaladores
no copian `scripts/` a ningún consumidor, de modo que un proyecto consumidor no
tiene ese fichero que ejecutar. Compartirlo exigiría enviar el gate de
mantenimiento del repositorio a cada consumidor y añadir `scripts/` a la
allowlist que este mismo ADR define para excluirlo. El workflow reutilizable
declara entonces su propio suelo estructural, que es lo que un consumidor
necesita de verdad: los directorios instalados existen, las quince fases
existen, cada fase de proceso declara sus information items, y el manifiesto de
adopción registra una versión. Las comprobaciones que solo tienen sentido aquí
—ADR, gobernanza, el `VERSION` de este repositorio, los enlaces de esta prosa—
quedan fuera, porque un consumidor no es este repositorio.

Esa segunda implementación es un coste asumido y declarado, no un descuido: son
dos y van a divergir. Lo que evita que diverjan hacia una afirmación falsa es que
`scripts/validate-blueprint.sh` comprueba que el workflow reutilizable no invoque
el script. La prosa no se comprueba, porque una frase no es un hecho: la
afirmación falsa original ponía el sujeto en una frase y la aserción en la
siguiente, y ningún regex sobre frases la detecta sin disparar también contra la
corrección que dice lo contrario.

**4. La comprobación de enlaces cubre referencias y anclas.** El validador anterior solo
miraba enlaces en línea. Una referencia definida con `[texto][ref]` y sin
definir, o un enlace a `archivo.md#seccion` donde la sección ya no existe, pasaban
silenciosos. Las dos formas son las que más se pudren, y la segunda es la que hace que la
documentación parezca rota.

## Consequences

### Positive

- Un consumidor recibe únicamente lo que el blueprint es. `.opencode/package.json` y los
  lockfiles dejan de propagarse, y `.opencode/.gitignore` desaparece de raíz porque sus
  reglas viven ahora en un archivo versionado.
- La lista de entradas de la allowlist es la definición ejecutable de «qué es el
  blueprint». Añadir una entrada es una decisión de distribución y por tanto necesita un
  ADR, no un commit cualquiera.
- La validación se ejecuta en local sin Actions. Los tres paquetes anteriores de este
  trabajo se desarrollaron con mutaciones negativas contra un gate copiado del workflow; a
  partir de aquí se ejecuta contra el archivo real, y la copia temporal desaparece.

### Negative / trade-offs

- La comprobación de anclas puede fallar sobre enlaces legítimos a secciones que GitHub
  genera de otra forma —código en línea en el encabezado, acentos, títulos duplicados—. Se
  ha limitado a encabezados de una línea, que es donde está el valor, antes que intentar
  replicar el generador de slugs de GitHub completo, que es un proyecto en sí mismo.
- `VERSION` es un archivo más que mantener. El coste es real y pequeño: se actualiza una
  vez por publicación y la validación falla si falta o está vacío.
- **No purga el contenido de los directorios que sí instala.** La allowlist es a nivel
  de directorio: `blueprint/`, `templates/` y `standards/` se copian completos. Un archivo
  suelto que un mantenedor deje dentro de uno de ellos sí llega al consumidor. No se
  atiende con una allowlist por archivo porque habría que enumerar las quince fases, las
  plantillas y los estándares, y esa lista dejaría de estar al día en el primer commit que
  añadiera una fase —fallando en silencio, que es la forma peor de fallar— mientras que
  el problema que resolvería es de baja probabilidad y autocorregible: git muestra un
  archivo suelto en `git status`. El caso que sí era realista, un archivo suelto en
  `.opencode/`, queda cubierto, porque ese directorio lo gestiona la herramienta de
  OpenCode. Las pruebas de `scripts/test-installers.sh` y
  `scripts/test-installers.ps1` fijan este comportamiento conocido como tal, en lugar de
  dejarlo implícito.
- **No publica nada en ningún registro.** No hay npm, ni PyPI, ni un release de GitHub.
  El `VERSION` identifica el árbol, no lo distribuye.
- **No verifica la integridad del contenido instalado.** Se registró por qué no, en la
  Opción E: hacerlo exigiría una dependencia que los instaladores prometen no tener.
- **No fuerza la migración de consumidores existentes.** Un proyecto instalado con la
  versión anterior no recibe `.blueprint-install.json` hasta que reinstale. El
  instalador avisa cuando encuentra un manifiesto previo y no lo sobrescribe, porque el
  manifiesto anterior puede haberse editado a mano.
- **No toca las fases 09 a 14.** La fase 09 describe pipelines y la 14 el bucle de mejora;
  introducir aquí nociones de versionado o de publicación sería duplicar un estándar que
  ya existe en otra parte.

## Verification

La decisión se comprobó ejecutando los dos instaladores contra un destino real y
mutando el validador, no solo leyéndolos. Las pruebas de los instaladores viven
en `scripts/test-installers.sh` y `scripts/test-installers.ps1` y se ejecutan en
CI, porque un script de instalación que nadie ejecuta no es un script probado.

Siete defectos aparecieron al ejecutar en vez de leer, y ninguno era visible en el
código:

- El instalador de PowerShell usaba `Write-Host`, que escribe al host pasando
  por alto el flujo de éxito. `$out = & .\blueprint-init.ps1 C:\x` devolvía
  vacío, lo que hacía el instalador intestable e inservible desde otro script, y
  lo diferenciaba de su hermano de bash, que sí escribe en stdout. Se comprobó
  que `[Console]::Out` tampoco sirve, porque esquiva PowerShell por completo;
  solo `Write-Output` es capturable, y `Write-Host` solo con `6>&1`.
- El validador comprobaba las *definiciones* de enlace pero no sus *usos*. Un
  documento podía definir cada referencia que usaba y aun así tener una
  indefinida, porque son dos comprobaciones separadas: al borrar una definición,
  cada `[texto][ref]` que apuntaba a ella deja de ser un enlace sin que nada lo
  note. Ahora un uso sin definición en el mismo archivo es un defecto.
- El validador buscaba la allowlist en todo el archivo, y ambos instaladores
  nombran cada entrada requerida en el comentario de cabecera. Una comprobación
  que hace `grep` del archivo sigue pasando después de que la allowlist
  ejecutable cambie a copiar `.opencode` entero: es decir, pasaba exactamente la
  regresión que el check existe para detectar. Ahora se parsea la asignación y se
  inspecciona el valor declarado, no el texto que lo rodea.
- Al comprobar usos de enlace aparecieron falsos positivos en documentos que
  explican la sintaxis de los enlaces: el literal va en un `span` de código y no
  es un enlace. El validador descarta código antes de escanear. Descartar solo
  puede ocultar un enlace real, nunca inventar uno, que es la dirección segura en
  la que fallar.
- Una prueba propia de ausencia de BOM daba falso positivo. `ReadAllText`
  decodifica y descarta el BOM, y `String.StartsWith([char]0xFEFF)` resuelve mal
  su sobrecarga en Windows PowerShell: devolvía `True` sobre un archivo que
  empezaba por `7b 0a`. El instalador escribía correctamente sin BOM; la
  comprobación ahora mira los bytes.
- La documentación afirmaba una cosa que el código hacía imposible. Decía, en el
  README, el CHANGELOG, el AGENTS y este ADR, que el workflow reutilizable
  ejecutaba `scripts/validate-blueprint.sh`, y por tanto que había «una
  implementación, tres ejecutores». No lo ejecutaba, y no podía: la allowlist de
  este mismo ADR excluye `scripts/`, así que un consumidor no tiene el fichero.
  Ninguno de los dos lados estaba mal por sí solo —el workflow es correcto, la
  documentación es la que exagera— y ninguno se delata leyendo solo uno de los dos.
  La contradicción era además autoinformante: el CHANGELOG decía que el workflow
  se había puesto a ejecutar el script para no duplicar el gate, y lo único que
  hacía era duplicarlo. Esto apareció al leer la documentación contra la decisión
  de allowlist, que es un cruce que ninguna de las dos mitades hace consigo
  misma. Un gate que hubiera sido insatisfactorio aquí: el validador comprueba que el
  workflow no *invoque* el script, y no puede comprobar que la prosa no lo
  *afirme*, porque la frase original partía el sujeto y la aserción en dos
  frases y la corrección que dice lo contrario dispararía cualquier regex igual.
  Se corrige la prosa y se cubre el comportamiento, y se deja constancia de que
  la prosa no está cubierta.
- El ejemplo de uso del workflow reutilizable fijaba `@v1.0.0`, que es la
  etiqueta anterior a este trabajo: un consumidor que lo siguiera al pie de la
  letra recibía los tres `test -d` originales. El pin por etiqueta es el
  correcto, porque un gate que se mueve solo no es un gate, pero un ejemplo que
  instala un gate más débil del que el repositorio cree tener es peor que no
  documentarlo. El comentario de uso lo dice y el suelo nuevo viaja en la
  siguiente etiqueta.

Se deja constancia de los siete porque son la clase de defecto que aparece al
ejecutar y no al leer, y porque el primero habría sido fácil de declarar
«funciona» sin haberlo intentado nunca. Dos de ellos —el `Write-Host` y la
allowlist mal comprobada— eran la razón entera de este ADR, y ninguno se habría
encontrado leyendo el código. El sexto no es de ejecución sino de
contradicción: apareció al poner la documentación delante de la decisión que
dice lo contrario, que es la única forma de que aparezca.

## Date
2026-09-29
