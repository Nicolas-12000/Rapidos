# RAMAS.md — Arquitectura hexagonal y ramas (Rapidos)

Equipo: **5 personas**. Cada una trabaja en su rama hexagonal. El código de Spring Modulith que ya está en `main`, `develop` y `Nicolas` no se toca.

| Persona | Rol | Rama | PR hacia |
|---------|-----|------|----------|
| Nicolás | Dev 1, orquestador de `pedidos` | `Nicolas-Hexagonal` | `develop-Hexagonal` |
| Esteban | Dev 2, menú dentro de `pedidos` | `esteban-Hexagonal` | `develop-Hexagonal` |
| Matheu | Dev 3, `despacho` | `matheu-Hexagonal` | `develop-Hexagonal` |
| Pipe | Dev 4, `ubicacion` | `pipe-Hexagonal` | `develop-Hexagonal` |
| Gio | Dev 5, DTOs, SQL y pruebas del pedido | `Gio-Hexagonal` | `develop-Hexagonal` |

Esta guía es la **línea hexagonal**. Vive en `Hexagonal-Main`. El trabajo del día se junta en `develop-Hexagonal`. De ahí sale la rama de cada persona. Esas ramas ya existen: no se crean otra vez desde `main`.

El ejemplo es `carnaval-tickets-st`. En la rama `02-dominio` había varios módulos (`inventario`, `pagos`, `notificaciones`, `pedidos`) que solo eran pasos de una misma compra. En `03-adaptadores` se unieron en `reservas`: cada paso quedó como puerto (`InventarioRepository`, `PasarelaDePagos`, `Notificador`) y hay un controlador. Eso mismo hacemos con lo que es un paso de confirmar el pedido. No copiamos el salto a un solo módulo: despacho y ubicación tienen otra consistencia y se quedan aparte.

---

## 1. Ramas

```
main, develop, Nicolas   (Modulith; esta línea no vuelve ahí)
        \
Hexagonal-Main        ●────────────────────────────●   entrega estable
                       \                          /
develop-Hexagonal       ●──●──●──●──●──●──●──●──●      aquí se junta el trabajo
                             \    \    \    \    \
Nicolas-Hexagonal            ●    ·    ·    ·    ·     Nicolás, Dev 1
esteban-Hexagonal                 ●    ·    ·    ·     Esteban, Dev 2
matheu-Hexagonal                       ●    ·    ·     Matheu, Dev 3
pipe-Hexagonal                              ●    ·     Pipe, Dev 4
Gio-Hexagonal                                    ●     Gio, Dev 5
```

| Rama | De dónde sale | Hacia dónde va el PR |
|------|----------------|----------------------|
| `Hexagonal-Main` | Una vez, desde `main` | Solo recibe `develop-Hexagonal` cuando hay entrega |
| `develop-Hexagonal` | Desde `Hexagonal-Main` | Recibe el PR de cada persona |
| `Nicolas-Hexagonal` | Desde `develop-Hexagonal` | PR hacia `develop-Hexagonal` |
| `esteban-Hexagonal` | Desde `develop-Hexagonal` | PR hacia `develop-Hexagonal` |
| `matheu-Hexagonal` | Desde `develop-Hexagonal` | PR hacia `develop-Hexagonal` |
| `pipe-Hexagonal` | Desde `develop-Hexagonal` | PR hacia `develop-Hexagonal` |
| `Gio-Hexagonal` | Desde `develop-Hexagonal` | PR hacia `develop-Hexagonal` |

Una rama por persona. No se comparte. No se abre desde `main` ni desde el `develop` de Modulith. No se hace push directo a `develop-Hexagonal` ni a `Hexagonal-Main`.

`develop` y `Nicolas`, sin el sufijo `-Hexagonal`, son la línea Modulith. Un push a esas dos pisa el trabajo anterior.

---

## 2. Tres módulos

| Módulo | Qué incluye | Por qué es un módulo |
|--------|-------------|----------------------|
| `pedidos` | Confirmar el pedido. El menú y el cobro son puertos de salida, como inventario y pagos en carnaval | Misma transacción, misma base, el caso de uso que tiene que responder ya |
| `despacho` | Asignar repartidor, avisar, estados con llave única | Canal asíncrono. Un fallo aquí no deshace el pedido (ADR-001, ADR-003) |
| `ubicacion` | Última posición en memoria y degradación del GPS | Otra consistencia: se puede descartar y no puede tumbar el pedido (ADR-002) |

No existe el módulo `catalogo`. El menú vive dentro de `pedidos`. No existe un módulo de cobro: `Cobro` es un puerto de salida con un adaptador simulado.

```
cliente ──HTTP──► entrada de pedidos
                      │
                      ▼
                 CrearPedidoService          ← orquesta
                   │         │          │
              MenuPort     Cobro    PublicadorDeEventos
                   │         │          │
                   ▼         ▼          ├──► despacho (listener, no HTTP)
              adaptador   adaptador     └──► ubicacion (listener, no HTTP)
              menú+caché  simulado

repartidor ──HTTP──► DespachoController      solo para reportar el estado
repartidor ──HTTP──► UbicacionController     solo para mandar el GPS
cliente    ──HTTP──► UbicacionController     solo para leer la última posición
```

`pedidos` no importa clases de `despacho` ni de `ubicacion`. Publica `PedidoConfirmado` y `ConfirmacionMedida`. Esos dos escuchan. `ubicacion`, para saber qué repartidor lleva un pedido, usa un puerto hacia `despacho`, no un `RestController`.

`CrearPedidoService` tampoco llama al HTTP del menú. Pregunta `MenuPort`. El `GET` del menú existe porque el cliente lo consulta antes de pedir, y ese adaptador de entrada también está en `pedidos`.

### Controladores que sí existen

Uno por actor de afuera. Varios métodos en el mismo controlador están bien (en carnaval, crear y pagar viven en `ReservaController`). Un controlador para que un módulo llame a otro, no.

| Controlador | Dónde vive | Métodos | Quién lo llama | Quién lo escribe |
|-------------|------------|---------|----------------|------------------|
| `PedidosController` | `pedidos` | `POST /api/pedidos`, `GET /api/pedidos/{id}`, `GET /api/cola`, `GET /api/restaurantes/{id}/menu` | El cliente y la alarma de la cola | Nicolás los tres primeros. Esteban el `GET` del menú, en este mismo controlador |
| `DespachoController` | `despacho` | `POST /api/repartidores/{id}/estado` | El repartidor | Matheu |
| `UbicacionController` | `ubicacion` | `PUT /api/repartidores/{id}/ubicacion`, `GET /api/pedidos/{id}/ubicacion` | El repartidor y el cliente | Pipe |

El menú no tiene controlador propio ni módulo propio. `GET /api/cola` tampoco. Los dos métodos van en `PedidosController`.

### Lo que no es un controlador

| Qué pasa | Cómo entra | Quién |
|----------|------------|-------|
| Validar plato, precio y restaurante activo | Puerto de salida `MenuPort` | Esteban lo implementa. Nicolás solo lo llama |
| Cobrar | Puerto de salida `Cobro` | Adaptador simulado dentro de `pedidos` |
| Avisar al restaurante y asignar repartidor | Listener de `PedidoConfirmado` | Matheu |
| Bajar la frecuencia del GPS | Listener de `ConfirmacionMedida` | Pipe |
| Saber el repartidor de un pedido | Puerto de salida hacia `despacho` | Pipe declara el puerto; Matheu lo cubre con su caso de uso |

Si el único que va a llamar tu código es otro módulo, o el propio orquestador, no crees un controlador. Crea el puerto y el adaptador.

---

## 3. Forma de cada módulo

Igual que `reservas` en carnaval:

```
com.rapidos.pedidos
├── dominio/
│   ├── pedido/                        Nicolás y Gio. Pedido, LineaPedido, total
│   └── menu/                          Esteban. ItemMenu, restaurante activo
├── aplicacion/
│   ├── servicios/                     CrearPedidoService, ConsultarMenuService
│   └── puertos/
│       ├── entrada/                   CrearPedidoUseCase, ConsultarPedidoUseCase, ConsultarMenuUseCase
│       └── salida/                    PedidoRepository, MenuPort, Cobro, PublicadorDeEventos, Reloj
└── infraestructura/
    ├── entrada/                       PedidosController
    ├── salida/
    │   ├── pedido/                    JDBC del pedido
    │   ├── menu/                      tablas, datos y caché con TTL
    │   ├── cobro/                     cobro simulado
    │   └── eventos/                   cola detrás de PublicadorDeEventos
    └── config/

com.rapidos.despacho
com.rapidos.ubicacion
├── dominio/
├── aplicacion/servicios + puertos/entrada + puertos/salida
└── infraestructura/entrada + salida + config
```

1. `dominio` no importa `aplicacion` ni `infraestructura`.
2. El servicio solo ve interfaces y dominio. `CrearPedidoService` ve `MenuPort`, no el adaptador de la caché.
3. El adaptador implementa el puerto. La config los une, como `ReservasConfig`.
4. Entre los tres módulos solo se cruza un puerto o un evento.
5. Las pruebas del dominio y del servicio usan dobles en memoria, como `ReservarBoletasServiceTest`, sin Spring.

---

## 4. De quién es cada parte

| Parte | Persona | Rama | Qué toca |
|-------|---------|------|----------|
| Caso de uso de `pedidos` | **Nicolás, Dev 1** | `Nicolas-Hexagonal` | Orquesta. Dominio del pedido, `CrearPedidoService`, `PedidosController` (pedido y cola), JDBC del pedido, publicación de eventos |
| Menú, dentro de `pedidos` | **Esteban, Dev 2** | `esteban-Hexagonal` | `dominio/menu`, `ConsultarMenuUseCase`, `MenuPort`, `ConsultarMenuService`, adaptador con caché, y el `GET` del menú en `PedidosController` |
| `despacho` | **Matheu, Dev 3** | `matheu-Hexagonal` | Estados con llave única y el listener del pedido confirmado |
| `ubicacion` | **Pipe, Dev 4** | `pipe-Hexagonal` | Última posición en memoria y la regla de degradación |
| Piezas acotadas de `pedidos` | **Gio, Dev 5** | `Gio-Hexagonal` | DTO, validaciones, SQL de pedidos, pruebas del total, `curl`, imagen Docker. No abre otro controlador |

Nicolás, Esteban y Gio comparten el módulo `pedidos` y no los mismos paquetes. Esteban no entra a `dominio/pedido` ni al JDBC del pedido. Nicolás no entra a `infraestructura/salida/menu`. Si hace falta un método nuevo en `MenuPort`, lo acuerdan en un PR pequeño.

---

## 5. Paso a paso de cada persona

Todos arrancan en la rama que ya tienen. No se crea otra.

```bash
git fetch origin
git checkout develop-Hexagonal
git pull origin develop-Hexagonal
git checkout esteban-Hexagonal
git merge develop-Hexagonal
git push
```

Cambia `esteban-Hexagonal` por la tuya: `Nicolas-Hexagonal`, `matheu-Hexagonal`, `pipe-Hexagonal` o `Gio-Hexagonal`.

Cada paso es un commit en tu rama y, cuando el paso cierra algo revisable, un PR a `develop-Hexagonal`. Antes del PR: `./mvnw test`.

### Nicolás — Dev 1, orquestador (`pedidos`), rama `Nicolas-Hexagonal`

1. El esqueleto va directo en `Hexagonal-Main`, sin rama aparte: carpetas de **tres** módulos (`pedidos`, `despacho`, `ubicacion`), con los subpaquetes de menú y pedido dentro de `pedidos`. Puertos `CrearPedidoUseCase`, `MenuPort`, `Cobro` y `PublicadorDeEventos`. Un controlador por módulo, solo el que llama alguien de afuera.
2. Dominio en `dominio/pedido`: `Pedido`, `LineaPedido`, total, cantidad mínima.
3. Puertos de entrada `CrearPedidoUseCase` y `ConsultarPedidoUseCase`. El servicio depende de `MenuPort` y de `Cobro`; no de clases de menú ni de un módulo `catalogo`.
4. `CrearPedidoService`: valida con `MenuPort`, calcula el total, cobra, aplica la `Idempotency-Key`, guarda y publica `PedidoConfirmado` en la misma transacción. Mide `ConfirmacionMedida` y la publica por el mismo puerto de eventos.
5. `PedidosController` con `POST /api/pedidos`, `GET /api/pedidos/{id}` y `GET /api/cola`. Deja el método del menú para Esteban, en este mismo controlador. No crees `CatalogoController` ni `ColaController`.
6. JDBC detrás de `PedidoRepository`. La cola (`spring-modulith-starter-jdbc`) detrás de `PublicadorDeEventos`. El cobro simulado detrás de `Cobro`.
7. Cuando los demás ya están en `develop-Hexagonal`, `PedidoFlujoIT`: el POST usa `MenuPort`, publica el evento y el listener de despacho avisa. Si el test pide el menú por HTTP para poder crear el pedido, está mal.

### Gio — Dev 5, contigo en `pedidos`, rama `Gio-Hexagonal`

Trabajas en pareja con Nicolás, en los paquetes del pedido. Tu rama es `Gio-Hexagonal` y sale de `develop-Hexagonal`. No abras un controlador ni un módulo nuevo.

1. Lee el README (el `curl` del caso de uso) y, en carnaval, `ReservarBoletasUseCase`, `ReservarBoletasService` y `ReservaController`. El controlador no reserva: llama a `reservar`. Inventario y pagos entran como puertos, no como módulos.
2. Arranca la app y ejecuta el `curl` de crear pedido. Anota qué JSON entra y qué JSON sale.
3. `CrearPedidoRequest` y la línea del body, con `@NotNull` y `@Min`. Viven al lado del controlador. El dominio no lleva anotaciones de Bean Validation. Nicolás usa tu request dentro de `PedidosController`.
4. `schema.sql` y `data.sql` de la tabla de pedidos. El menú (`restaurante`, `item_menu`) lo escribe Esteban. Revísalo con Nicolás antes del PR.
5. Pruebas del dominio, sin Spring: el total de dos líneas y el rechazo cuando la cantidad es menor que 1. El menú, en esas pruebas, es un `MenuPort` falso.
6. Deja el `curl` del README funcionando después de cada PR.
7. `docker build -t rapidos .` y comprueba que el contenedor arranca.
8. En los PR de los demás, mira una sola cosa: ¿apareció un módulo `catalogo`, o un controlador cuyo único cliente es otro módulo? Si sí, coméntalo.

Commits pequeños. Si llevas un rato atascado, pregúntale a Nicolás antes de borrar nada.

### Esteban — Dev 2, menú dentro de `pedidos`, rama `esteban-Hexagonal`

Tu código va en `com.rapidos.pedidos`, paquetes `dominio/menu`, `ConsultarMenu*`, `MenuPort` e `infraestructura/salida/menu`. No crees `com.rapidos.catalogo`.

1. Dominio: `ItemMenu` y si el restaurante está activo.
2. `MenuPort` (`restauranteActivo`, `obtenerItem`) y `ConsultarMenuUseCase` (`menu`). **Mézclalos a `develop-Hexagonal` lo primero**, con métodos que ya se puedan llamar. Nicolás trabaja contra `MenuPort` aunque tu adaptador todavía no exista: mientras tanto usa un doble.
3. `ConsultarMenuService`. Puertos de salida `MenuRepository` y `Reloj`.
4. Tablas `restaurante` e `item_menu`, datos de ejemplo, y la caché en memoria con TTL detrás del repositorio (ADR-001). El servicio no sabe si el dato vino de caché o de la tabla.
5. Agrega `GET /api/restaurantes/{id}/menu` a `PedidosController`. Ese método llama a `ConsultarMenuUseCase`. `CrearPedidoService` no pasa por ahí.
6. Prueba del servicio con un repositorio falso: menú vacío, plato de otro restaurante, restaurante inactivo.

### Matheu — Dev 3, `despacho`, rama `matheu-Hexagonal`

1. Dominio: `EstadoEntrega` (`ASIGNADO`, `RECOGIDO`, `ENTREGADO`) y la regla de llave única (ADR-003). La misma llave no se aplica dos veces.
2. Puerto de entrada `RegistrarEstadoUseCase`. Puertos de salida `DespachoRepository` y `Avisador`.
3. Servicio: asignar repartidor al confirmar; registrar el estado que manda el repartidor.
4. Entrada interna, sin HTTP: listener de `PedidoConfirmado`. Traduce el evento y llama al caso de uso. Si el worker falla, el evento se reintenta y el pedido queda procesado una sola vez. Prueba ese caso.
5. Un controlador, `POST /api/repartidores/{id}/estado`, porque el repartidor está afuera. El aviso al restaurante y al repartidor es el adaptador de `Avisador` (consola), no un endpoint.
6. En config: `@EnableAsync` y el `ThreadPoolTaskExecutor` con `rapidos.workers.*`.

### Pipe — Dev 4, `ubicacion`, rama `pipe-Hexagonal`

1. Dominio: `UbicacionActual` y `ReglaDegradacionGps` (5 s → 10 s → 15 s, y vuelta a 5 s).
2. Puerto de entrada `ReportarUbicacionUseCase`. Puertos de salida: almacén en memoria, `Reloj`, y la lectura del repartidor del pedido (puerto hacia `despacho`, no HTTP).
3. Servicio: solo la última posición por repartidor, con TTL. Sin tabla de pedidos (ADR-002).
4. Entrada interna, sin HTTP: listener de `ConfirmacionMedida`, que mueve la frecuencia.
5. Un controlador con dos métodos: `PUT /api/repartidores/{id}/ubicacion` (lo manda el repartidor) y `GET /api/pedidos/{id}/ubicacion` (lo lee el cliente).
6. Con la app integrada, `k6 run carga/pedidos.js`: p95 < 2 s, 0 % de rechazos, y la frecuencia sube a 10 s bajo presión. Pásale los números a Nicolás.

---

## 6. Puesta en marcha de las ramas

`Hexagonal-Main`, `develop-Hexagonal` y las cinco ramas personales ya están creadas. No se crea otra desde `main`.

```bash
git clone https://github.com/Nicolas-12000/Rapidos.git
cd rapidos
git fetch origin
git checkout develop-Hexagonal
git pull origin develop-Hexagonal
git checkout esteban-Hexagonal
git merge develop-Hexagonal
```

Esteban cambia el último `checkout` por su rama. Lo mismo Nicolás, Matheu, Pipe y Gio.

El `develop` y el `Nicolas` que ya están en GitHub son Modulith. No se les hace push desde esta línea.

Comprobar: `https://github.com/Nicolas-12000/Rapidos/branches`

En `develop-Hexagonal` ya se ve que el menú es un paquete de `pedidos`, no un cuarto módulo.

### Día a día

```bash
git checkout develop-Hexagonal
git pull origin develop-Hexagonal
git checkout esteban-Hexagonal
git merge develop-Hexagonal

git add src/main/java/com/rapidos/pedidos/dominio/menu/
git commit -m "feat(pedidos): publica MenuPort para el orquestador"
git push
```

El PR en GitHub va de tu rama **hacia `develop-Hexagonal`**. Por ejemplo, `esteban-Hexagonal` → `develop-Hexagonal`.

Cuando una fase queda estable, Nicolás mezcla `develop-Hexagonal` en `Hexagonal-Main` y lo empuja. Ese merge no reemplaza los PR de cada tarea.

### Si Git se enreda

```bash
git status
git log --oneline -10
git restore <archivo>          # deshace cambios que aún no están en un commit
git stash                      # aparta cambios un momento
git stash pop
git merge --abort              # cancela un merge con conflictos
```

No borres la carpeta del proyecto. Casi todo se recupera.

---

## 7. Cuando ya está todo: la orquestación

El flujo lo cierra `CrearPedidoService`, no un controlador de integración. Con los PR en `develop-Hexagonal`, este es el camino de la demo:

1. El cliente pide el menú con `GET /api/restaurantes/{id}/menu`. Responde `PedidosController` llamando a `ConsultarMenuUseCase`. La caché está detrás de `MenuRepository`.
2. El cliente hace `POST /api/pedidos` con `Idempotency-Key`. El mismo controlador llama a `CrearPedidoUseCase`.
3. El servicio pregunta el plato a `MenuPort`, cobra por `Cobro`, guarda y publica `PedidoConfirmado`. La respuesta sale ya, sin esperar al repartidor.
4. El listener de Matheu asigna repartidor y avisa por consola. Si llega el mismo evento otra vez, no duplica.
5. `ConfirmacionMedida` llega al listener de Pipe y, si la confirmación se pone lenta, la frecuencia del GPS pasa a 10 s o 15 s.
6. El repartidor hace `POST .../estado` y `PUT .../ubicacion` en los controladores de `despacho` y `ubicacion`.
7. El cliente lee `GET /api/pedidos/{id}` y `GET /api/pedidos/{id}/ubicacion`.
8. Repetir el POST con la misma llave devuelve el mismo pedido.

`PedidoFlujoIT` recorre los pasos 2 a 4 por `PedidosController`, `MenuPort` y el listener de despacho. No aparece un módulo `catalogo` ni un controlador puente.

Si algo no cuadra al juntar, se arregla en un PR chico del dueño del puerto.

---

## 8. Convenciones

Commits: `tipo(modulo): mensaje`. Tipos: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`.

El módulo del commit es `pedidos`, `despacho` o `ubicacion`. El menú se commitea como `pedidos`.

Ejemplo: `feat(pedidos): publica MenuPort para confirmar el pedido`.

### Reglas

1. Hay tres módulos. El menú y el cobro son puertos de `pedidos`.
2. Otro módulo se usa por su puerto de entrada o por un evento, nunca por su controlador ni por su paquete de infraestructura.
3. Dentro de `pedidos`, Nicolás no importa `infraestructura/salida/menu`. Usa `MenuPort`.
4. No agregues un controlador si nadie de afuera del proceso lo va a llamar. El `GET` del menú y el de la cola viven en `PedidosController`.
5. Un cambio de `MenuPort` se avisa y va en un PR pequeño. Esteban lo publica antes de que Nicolás lo conecte.
6. Nada de push directo a `develop-Hexagonal`, `Hexagonal-Main`, `develop`, `Nicolas` o `main`.
7. `./mvnw test` antes de pedir revisión.
8. PR por debajo de ~300 líneas.
9. Sin secretos reales. Las credenciales locales de la demo pueden quedarse en `application.yml`.

### Antes de pedir revisión

- [ ] `./mvnw test` pasa.
- [ ] `dominio` y `aplicacion` no importan `infraestructura`.
- [ ] No apareció un módulo `catalogo` ni un controlador fuera de la tabla de la sección 2.
- [ ] El controlador solo traduce HTTP y llama al caso de uso.
- [ ] Solo toqué mis paquetes, o lo hablé con el dueño.
- [ ] El PR apunta a `develop-Hexagonal`.

---

## 9. Orden

| Fase | Qué queda en `develop-Hexagonal` | Quién |
|------|------------------------|-------|
| 0 | `Hexagonal-Main`, `develop-Hexagonal`, esqueleto de tres módulos y puertos (`MenuPort`, `Cobro`) | Nicolás |
| 1 | `MenuPort` y `ConsultarMenuUseCase` usables. En el pedido: request, SQL y pruebas de total con un `MenuPort` falso | Esteban; Gio con Nicolás |
| 2 | Crear pedido, publicar `PedidoConfirmado`, `PedidosController` (pedido, cola y, cuando Esteban lo agregue, el menú) | Nicolás; Gio deja el `curl` vivo |
| 3 | Listener de despacho y estados. GPS en memoria y su controlador | Matheu; Pipe |
| 4 | Reintento idempotente, degradación, `PedidoFlujoIT` | Matheu; Pipe; Nicolás |
| 5 | k6, Dockerfile, README. Merge de `develop-Hexagonal` a `Hexagonal-Main` | Pipe y Gio; cierra Nicolás |

Mientras `MenuPort` no tenga adaptador, usa un doble en las pruebas y sigue. No desbloquees el pedido con un HTTP al menú.

---

## 10. Terminado

- [ ] `./mvnw test` pasa, incluido el `verify()` de los tres módulos.
- [ ] `dominio` y `aplicacion` no importan `infraestructura`.
- [ ] Los módulos son `pedidos`, `despacho` y `ubicacion`. No existe `catalogo`.
- [ ] `CrearPedidoService` habla con el menú por `MenuPort` y con el cobro por `Cobro`.
- [ ] Despacho y ubicación entran por eventos. Sus controladores solo los usa alguien de afuera.
- [ ] `GET` del menú y `GET /api/cola` están en `PedidosController`.
- [ ] El caso de uso corre contra PostgreSQL (`docker compose up`) y el `curl` del README responde.
- [ ] Idempotencia, degradación del GPS, llave única de estados y reintento de la cola tienen cada uno una prueba.
- [ ] `docker build -t rapidos .` funciona y `k6 run carga/pedidos.js` cumple p95 < 2 s y 0 % de rechazos.
- [ ] `develop-Hexagonal` está mezclado en `Hexagonal-Main`.
