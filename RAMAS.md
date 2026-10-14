# RAMAS.md — Guía de ramas y reparto de trabajo (Rapidos)

Equipo: **5 desarrolladores (Dev 1 … Dev 5)**.
Regla de reparto: **un módulo lo trabajan 2 personas; los otros módulos, una persona cada uno.**

---

## 1. Reparto de módulos

| Módulo | Personas | Por qué |
|--------|----------|---------|
| `pedidos` (orquestador) | **Dev 1 + Dev 5** (en pareja) | Es el módulo más grande y el corazón del flujo. Dev 1 guía; Dev 5 toma tareas pequeñas y bien acotadas |
| `catalogo` | **Dev 2** | Independiente, con poco riesgo. Menú + caché |
| `despacho` | **Dev 3** | Estados con llave única, worker que avisa, asignación de repartidor |
| `ubicacion` | **Dev 4** | GPS en memoria con TTL + regla de degradación |

> Cambia "Dev 1..5" por los nombres reales. Si Dev 5 prefiere otro módulo, el que importa es que **el módulo en pareja sea el de mayor tamaño**.

### Quién depende de quién

```
despacho  ──► pedidos ──► catalogo
ubicacion ──► pedidos
ubicacion ──► despacho
```

Esto define el **orden natural de trabajo**: `catalogo` y `pedidos` son la base; `despacho` y `ubicacion` se apoyan en ellos.

### Tareas por persona

**Dev 1 (pedidos, líder del módulo)**
- `PedidosApi`, `PedidoResumen`, eventos `PedidoConfirmado` y `ConfirmacionMedida`.
- Crear pedido: validar contra `CatalogoApi`, calcular total, cobro simulado, idempotencia (`Idempotency-Key`).
- Publicar `PedidoConfirmado` en la misma transacción que guarda el pedido (cola persistente con `spring-modulith-starter-jdbc`) y medir la duración de la confirmación (`ConfirmacionMedida`).
- `GET /api/cola`: cantidad de eventos pendientes (usa `IncompleteEventPublications` de Spring Modulith).
- Controlador `POST /api/pedidos` y `GET /api/pedidos/{id}`.
- Test de integración `PedidoFlujoIT`.

**Dev 5 (pedidos, en pareja con Dev 1)**
Tareas pensadas para aprender sin bloquear a nadie:
1. DTOs de entrada/salida (`CrearPedidoRequest`, validaciones `@NotNull`, `@Min`).
2. `schema.sql` y `data.sql` de la tabla de pedidos (con orientación de Dev 1).
3. Pruebas unitarias de las reglas simples (total del pedido, cantidad mínima).
4. Mantener el ejemplo de `curl` del README funcionando.
5. Revisar el `Dockerfile` ya incluido: construir la imagen (`docker build -t rapidos .`) y comprobar que la app arranca en contenedor.
6. Revisar los Pull Requests de sus compañeros (aprende leyendo código).

**Dev 2 (catalogo)**
- `CatalogoApi`, `ItemMenu`.
- Tablas `restaurante` y `item_menu` + datos de ejemplo.
- Caché en memoria del menú (ADR-001, complemento C).
- `GET /api/restaurantes/{id}/menu`.
- **Entregar la interfaz `CatalogoApi` el primer día**, porque `pedidos` la necesita.

**Dev 3 (despacho)**
- `DespachoApi`, `EstadoEntrega` (`ASIGNADO`, `RECOGIDO`, `ENTREGADO`).
- `@ApplicationModuleListener` de `PedidoConfirmado` (worker): asigna repartidor y "avisa" por consola (restaurante + repartidor).
- El worker debe ser **idempotente**: guarda una llave única por pedido; si el evento llega dos veces, no procesa doble.
- Configurar `@EnableAsync` y un `ThreadPoolTaskExecutor` acotado con los valores `rapidos.workers.*` de `application.yml` (el "tope de instancias" del ADR-001).
- Prueba: forzar un fallo en el worker y comprobar que el evento se reintenta y el pedido se procesa una sola vez.
- Estados con llave única: la misma llave no se procesa dos veces (ADR-003).
- `POST /api/repartidores/{id}/estado`.

**Dev 4 (ubicacion)**
- `UbicacionApi`, `UbicacionActual`.
- Almacén en memoria con vencimiento (TTL): solo la última posición por repartidor, **sin base de datos de pedidos**.
- `ReglaDegradacionGps`: escucha `ConfirmacionMedida`; 5 s → 10 s → 15 s y regreso a 5 s.
- `PUT /api/repartidores/{id}/ubicacion` y `GET /api/pedidos/{id}/ubicacion`.
- Prueba de carga con `carga/pedidos.js` (k6): validar p95 < 2 s y 0 % de rechazos, y comprobar que la frecuencia del GPS sube a 10 s bajo presión. Reporta los resultados a Dev 1.

---

## 2. Flujo de ramas

Todo el código del equipo **vive en GitHub** en ramas remotas (`origin`). La regla simple:

1. **`main`** — solo entregas estables (demo lista, tags de entrega).
2. **`develop`** — **aquí se integra todo el trabajo del día a día.** Nadie acumula el proyecto solo en `main`.
3. **Tu rama personal** — partes de `develop`, trabajas, abres PR **hacia `develop`**, y cuando se aprueba borras o sigues en la misma rama para la siguiente tarea.

```
main          ●────────────────────────────●  (release / entrega)
               \                          /
develop         ●──●──●──●──●──●──●──●──●  (integración continua)
                     \    /      /
{tu-nombre}           ●──●──●──●           (rama personal de cada dev)
feature/...              ●──●              (tareas cortas, opcional)
```

### Ramas permanentes (en GitHub)

| Rama | Dónde existe | Uso |
|------|----------------|-----|
| `main` | `origin/main` | Estable y entregable. Solo recibe merges desde `develop` (o release), no commits directos |
| `develop` | `origin/develop` | **Rama de integración.** Todo el equipo sube trabajo aquí vía Pull Request |

> En GitHub conviene marcar **`develop` como rama por defecto** mientras construyen la demo, y dejar `main` para hitos. Se cambia en *Settings → General → Default branch*.

### Rama personal: `{tu-nombre}`

Cada desarrollador tiene **su propia rama en GitHub**, con un nombre corto y reconocible (por ejemplo el primer nombre: `Maria`, `Juan`, `Nicolas`). Sustituye `{tu-nombre}` por el tuyo en todos los comandos.

La rama personal **siempre se crea a partir de `develop`**, no de `main`:

```bash
git fetch origin
git checkout develop
git pull origin develop
git checkout -b {tu-nombre}    # primera vez; ej.: git checkout -b Maria
# o, si ya existe en GitHub:
git checkout {tu-nombre}
git pull origin {tu-nombre}
```

Desde tu rama `{tu-nombre}` puedes:

- Acumular varios commits de tu módulo antes de integrar.
- Abrir PR **`{tu-nombre}` → `develop`** cuando quieras mezclar un bloque de cambios.
- O usar además ramas `feature/...` por tarea (abajo); lo importante es que **todo termina en `develop` por PR**.

> **Convención del equipo:** una rama `{tu-nombre}` por persona en `origin`. No compartáis la misma rama personal entre dos devs.

### Ramas de trabajo (una por persona o por tarea)

Formato recomendado para tareas acotadas: `feature/<modulo>-<nombre>-<descripcion-corta>`

| Persona | Rama en GitHub (ejemplo) |
|---------|---------------------------|
| Cualquier dev | `{tu-nombre}` — rama personal (ej. `Nicolas`, `Maria`, `Pedro`) |
| Dev 1 | `{tu-nombre}` y/o `feature/pedidos-dev1-crear-pedido` |
| Dev 5 | `{tu-nombre}` y/o `feature/pedidos-dev5-dtos-y-validaciones` |
| Dev 2 | `{tu-nombre}` y/o `feature/catalogo-dev2-menu-y-cache` |
| Dev 3 | `{tu-nombre}` y/o `feature/despacho-dev3-estados` |
| Dev 4 | `{tu-nombre}` y/o `feature/ubicacion-dev4-gps-y-degradacion` |

Otros prefijos: `fix/` correcciones, `docs/` documentación, `chore/` configuración.

**Flujo estándar (todos):**

```text
develop  →  tu-rama  →  commits + push  →  Pull Request → develop  →  (más adelante) main
```

- **No** hagas `push` directo a `develop` ni a `main` si el equipo usa PR (regla en sección 4).
- **Sí** haz `git push -u origin <tu-rama>` para que la rama exista en GitHub.

### Paso 0: esqueleto común (antes de que todos empiecen)

Una sola persona (Dev 1) crea la rama `chore/esqueleto` con:
- `pom.xml` (Spring Boot, Spring Modulith, PostgreSQL, Validation, Test).
- `docker-compose.yml` y `application.yml`.
- `RapidosApplication` y los **4 paquetes de módulo vacíos**.
- Las **interfaces públicas** (`CatalogoApi`, `PedidosApi`, `DespachoApi`, `UbicacionApi`) y los records/eventos, aunque sin implementación.
- `ModulosTest` con `verify()`.

Se abre PR hacia `develop` (desde `{tu-nombre}` o `chore/esqueleto`) y, al mezclarse, **solo entonces** el resto crea o actualiza su rama desde `develop`. Así nadie pelea por los mismos archivos y los contratos entre módulos quedan claros desde el inicio.

---

## 3. Comandos paso a paso

### Primera vez (clonar y quedarte en `develop`)

```bash
git clone https://github.com/Nicolas-12000/Rapidos.git
cd rapidos
git fetch origin
git checkout develop
git pull origin develop
```

Si `develop` aún no existe en tu clon (solo `main`), pide a quien administre el repo que haya hecho `git push -u origin develop`, o créala tú desde `main` actualizado (ver abajo).

### Crear `develop` en GitHub (una sola vez, desde `main`)

Quien tenga el repo local al día en `main`:

```bash
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
```

A partir de aquí **todo el equipo trabaja contra `origin/develop`**.

### Crear tu rama `{tu-nombre}` desde `develop` (una vez por persona)

```bash
git checkout develop
git pull origin develop
git checkout -b {tu-nombre}          # ej.: git checkout -b Maria
git push -u origin {tu-nombre}       # ej.: git push -u origin Maria
```

### Crear tu rama de feature (cualquier dev)

```bash
git checkout develop
git pull origin develop
git checkout -b feature/catalogo-dev2-menu-y-cache
git push -u origin feature/catalogo-dev2-menu-y-cache
```

### Trabajar y guardar cambios

```bash
git status                      # ver qué cambió
git add src/main/java/com/rapidos/catalogo/
git commit -m "feat(catalogo): agrega caché en memoria del menú"
git push -u origin feature/catalogo-dev2-menu-y-cache
```

### Mantener tu rama al día (hazlo cada día)

```bash
git checkout develop
git pull origin develop
git checkout feature/catalogo-dev2-menu-y-cache
git merge develop
```

Si hay conflictos, Git marca los archivos con `<<<<<<<`. Se abren, se decide qué queda, se hace `git add <archivo>` y `git commit`. **Si no entiendes un conflicto, pregunta antes de borrar nada.**

### Abrir un Pull Request

1. Sube tu rama (`git push`).
2. En GitHub/GitLab: *New Pull Request* → de tu rama **hacia `develop`**.
3. Pide revisión al menos a **una persona** (Dev 5 también puede revisar).
4. Cuando lo aprueban y las pruebas pasan, se mezcla. Después se puede borrar la rama.

### Cuando una tarea termina

```bash
git checkout develop
git pull origin develop
git branch -d feature/catalogo-dev2-menu-y-cache
# y se crea una rama nueva para la siguiente tarea
```

---

## 4. Convenciones

### Commits (formato `tipo(modulo): mensaje`)

| Tipo | Cuándo |
|------|--------|
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `test` | Pruebas |
| `docs` | README, RAMAS, comentarios |
| `chore` | Configuración, dependencias |
| `refactor` | Cambio interno sin cambiar el comportamiento |

Ejemplos: `feat(pedidos): valida idempotencia por llave`, `test(ubicacion): prueba degradación a 10 s`.

### Reglas de oro del equipo

1. **Trabaja solo dentro de tu módulo.** Si necesitas algo de otro módulo, usa su `XxxApi`.
2. **Nunca importes `*.internal.*` de otro módulo.** `verify()` fallará.
3. **Si necesitas cambiar una API pública**, avisa al dueño del módulo y haz un PR **pequeño solo con ese cambio**.
4. **No hagas push directo a `develop` ni `main`.** Todo entra por Pull Request.
5. **Antes de abrir un PR corre `./mvnw test`.** Si no pasa en tu máquina, no pasará en el equipo.
6. **PR pequeños y frecuentes** (menos de ~300 líneas) son más fáciles de revisar que uno gigante.
7. **No subas secretos reales.** Para la demo, las credenciales locales en `application.yml` están bien.

### Lista de verificación antes de pedir revisión

- [ ] `./mvnw test` pasa (incluido `ModulosTest`).
- [ ] No hay imports a `internal` de otros módulos.
- [ ] Solo toqué archivos de mi módulo (o lo hablé con el dueño).
- [ ] El mensaje de commit sigue el formato.
- [ ] Actualicé el README si cambié un endpoint.

---

## 5. Orden de trabajo sugerido

| Fase | Qué pasa | Quién |
|------|----------|-------|
| 0 | Esqueleto + interfaces públicas + `ModulosTest` | Dev 1 |
| 1 | `catalogo` completo; `pedidos` con DTOs y validaciones | Dev 2; Dev 1 + Dev 5 |
| 2 | `pedidos` crea pedido, publica `PedidoConfirmado` | Dev 1 + Dev 5 |
| 3 | `despacho` escucha el evento y maneja estados; `ubicacion` guarda GPS | Dev 3; Dev 4 |
| 4 | Worker idempotente con reintento (`despacho`), degradación del GPS, `PedidoFlujoIT` | Dev 3; Dev 4; Dev 1 |
| 5 | Prueba de carga (`carga/pedidos.js`), `Dockerfile` verificado, README final, revisión conjunta | Dev 4 + Dev 5; todos |

Mientras una dependencia no esté lista, usa un **stub** de su interfaz pública (por ejemplo un `CatalogoApi` falso en tus tests) y sigue avanzando.

---

## 6. Guía para Dev 5

- Empieza leyendo el **README.md** y los **ADR** para entender el *porqué* antes del *cómo*.
- Ejecuta el proyecto y prueba el `curl` de ejemplo antes de escribir código.
- Trabaja siempre en pareja con Dev 1 las primeras tareas (*pair programming*): uno escribe, el otro lee y pregunta.
- Haz commits pequeños; es más fácil deshacer un error pequeño.
- **Preguntar es parte del trabajo.** Si llevas 30 minutos atascado, pide ayuda.
- Revisar PRs de otros es una de las mejores formas de aprender; no hace falta encontrar errores, basta con preguntar "¿qué hace esto?".
- Si algo sale mal con Git, no borres la carpeta: pide ayuda. Casi todo se puede recuperar (`git status`, `git log`, `git reflog`).

### Comandos de rescate

```bash
git status                       # dónde estoy y qué cambió
git log --oneline -10            # últimos commits
git restore <archivo>            # descartar cambios no guardados de un archivo
git stash                        # guardar cambios a un lado temporalmente
git stash pop                    # recuperarlos
git merge --abort                # cancelar un merge con conflictos
```

---

## 7. Definición de "terminado" del proyecto

- [ ] `./mvnw test` pasa, incluido `verify()` de Modulith.
- [ ] Ningún import entre módulos apunta a `*.internal.*`.
- [ ] El caso de uso principal funciona contra PostgreSQL local (`docker compose up`).
- [ ] Idempotencia, degradación del GPS, prioridad de estados y reintento de la cola tienen al menos una prueba cada una.
- [ ] `docker build -t rapidos .` funciona y `k6 run carga/pedidos.js` cumple p95 < 2 s y 0 % de rechazos.
- [ ] El README y el diagrama generado en `target/spring-modulith-docs/` se entienden en 5 minutos.