# ADR-004: Modelo de distribución y reutilización del Blueprint

## Status
Accepted

## Context
Los proyectos que quieran aplicar el Blueprint necesitan un mecanismo de consumo. Las opciones principales difieren en el equilibrio entre autonomía local y consistencia de una fuente única de verdad: copiar los directorios por proyecto frente a referenciar un repositorio central, o distribuir como paquete.

## Options considered
1. Copiar los directorios relevantes (blueprint/, templates/, standards/, etc.) dentro de cada proyecto: autonomía total, pero riesgo de divergencia y desincronización entre proyectos.
2. Mantener el Blueprint como repositorio central único referenciado por los proyectos (fuente única de verdad), sin copias.
3. Publicar el Blueprint como paquete versionado (p. ej., npm u otro registro) que los proyectos instalen.

## Decision
Modelo híbrido. README.md en "Quick start" respalda ambas modalidades: "Copy the relevant directories into a project or keep this repository as a centralized blueprint repository". El versionado se hace mediante Git y releases (v1.0.0), y los workflows de GitHub se ofrecen como ejemplos reutilizables. La distribución como paquete se descarta por ahora.

## Consequences
### Positive
- Flexible: cada equipo elige copia local o referencia central según su política.
- El repositorio central puede actuar como fuente única de verdad y de evolución del proceso.
- No se requiere tooling adicional ni instalación para adoptar el Blueprint.
- El versionado por tags/releases permite identificar y reproducir versiones concretas.

### Negative / trade-offs
- Las copias locales pueden divergir del repositorio central al no existir un mecanismo de sincronización automática.
- La trazabilidad entre proyectos y la versión central se debe gestionar manualmente.
- La distribución como paquete versionado queda diferida; si algún equipo lo demanda, decidiría con un ADR nuevo.

## Date
2026-09-22