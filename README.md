# Taskgotchi
Aplicación de productividad para estudiantes: convierte tareas y hábitos en misiones,
gana experiencia y monedas y cuida una mascota virtual. Los proyectos grandes se
representan como jefes cuya vida disminuye al completar las misiones asociadas.

## Ejecutar en Windows
Requisitos: JDK 21, Node.js, Docker Desktop encendido.

Desde la raíz, en una terminal PowerShell:
```powershell
.\scripts\start-backend.ps1
```
El script solicita la clave del contenedor local mediante entrada oculta.
La clave debe coincidir con la empleada al crear el volumen de PostgreSQL.
Las credenciales solo se pasan por variables de entorno. No se incluyen en Git.

En otra terminal:
```powershell
.\scripts\start-web.ps1
```
Interfaz: http://localhost:5173
Swagger: http://localhost:8080/swagger-ui/index.html
Base: PostgreSQL 16, taskgotchidb, usuario taskgotchi, host localhost, puerto 5433.
Datos persistentes: volumen Docker proyecto_taskgotchi-data.
El backend escucha en el puerto 8080 y PostgreSQL en el 5433; se pueden cambiar
con SERVER_PORT y DB_PORT. El frontend usa VITE_API_BASE_URL (por defecto 8080).
Los registros sobreviven al reinicio del backend y del contenedor.

Para una demostración temporal sin PostgreSQL:
```powershell
.\scripts\start-backend.ps1 -Demo
```
El perfil h2 utiliza una base en memoria y pierde sus datos al apagarlo.

## Qué funciona
- Mundo 2D jugable: A/D o flechas para moverse, espacio para saltar y E para
  interactuar cerca de cocina, tablón, taller, fogata o portal. También tiene controles táctiles.
- Misiones: crear, buscar, filtrar, paginar, editar, completar y eliminar.
- Dificultad: fácil 20 XP/10 monedas; media 50/25; difícil 100/60.
- Hábitos diarios: un cumplimiento por día, rachas y bonificación.
- Mascota: saciedad, ánimo, energía, cuidados, evolución y bitácora.
- Jefes: crear proyectos, asociar misiones y recibir 150 XP/100 monedas al derrotarlos.
- Interfaz adaptable con mascota SVG animada y temporizador de enfoque.

El personaje explora el bosque acompañado por Gotchi. El mundo se dibuja con
Canvas; el progreso y las recompensas se consultan y guardan en la API. Caminar
y saltar no otorgan recompensas: el jugador entrega una misión cuando la realizó
en la vida real. Mi progreso conserva el panel de estadísticas y el temporizador.

## Acceso y seguridad
Cada estudiante crea su cuenta en la pantalla inicial y solo ve sus propios datos.
Toda la API bajo `/api/v1/**` exige `Authorization: Bearer <token>` salvo
`/api/v1/auth/register` y `/api/v1/auth/login`; sin token o con uno inválido o
expirado responde 401 con el error uniforme. En Swagger, pulsa **Authorize**.

| Variable | Uso | Por defecto |
|---|---|---|
| `JWT_SECRET` | Clave HS256, mínimo 32 caracteres | Aleatoria en cada arranque (las sesiones caducan al reiniciar) |
| `JWT_EXPIRATION_MINUTES` | Duración del token | 120 |
| `AUTH_RATE_LIMIT_MAX` / `AUTH_RATE_LIMIT_WINDOW_SECONDS` | Intentos de login/registro por IP | 10 cada 60 s (después, 429) |
| `DEV_USER_PASSWORD` | Contraseña de `devstudent` al crearlo | Sin definir: no se puede usar |
| `DEV_USER_RESET_PASSWORD` | `true` aplica `DEV_USER_PASSWORD` a un `devstudent` ya existente (conserva sus datos) | `false` |
| `SLOW_REQUEST_MS` / `SLOW_METHOD_MS` | Umbrales de peticiones y métodos lentos | 1000 / 500 |

## Middlewares
| Tipo | Clase | Función |
|---|---|---|
| Filtro | `config/RequestIdFilter` | ID `X-Request-Id` en cada petición y en los logs |
| Filtro | `config/RequestMonitoringFilter` | Método, URL, estado y tiempo de cada petición |
| Filtro | `security/AuthRateLimitFilter` | Bloquea fuerza bruta en login/registro (429) |
| Filtro | `security/JwtAuthenticationFilter` | Valida el JWT dentro de Spring Security |
| Interceptor | `config/AuditInterceptor` | Auditoría HTTP de acciones que modifican datos |
| Aspecto | `aspect/PerformanceAspect` | Mide los métodos de servicio y avisa de los lentos |
| Aspecto | `aspect/BusinessAuditAspect` + `@Auditable` | Auditoría de negocio: recompensas, jefes, mascota |

Multijugador e IA externa no forman parte de esta versión.

## Laboratorio
Java 21 · Spring Boot 4.1.2 · Spring MVC · Spring Data JPA · PostgreSQL 16 · React/TypeScript/Vite.
Paquete: ec.edu.espe.pa.taskgotchi.
Capas: controller → service → repository; entity, dto, mapper, exception y config.

Contrato principal:
| Método | Ruta | Resultado |
|---|---|---|
| GET | /api/v1/tasks?page=0&size=10&sort=id,asc | 200, content y page |
| GET | /api/v1/tasks/buscar?title=texto | 200, búsqueda sin distinguir mayúsculas |
| GET | /api/v1/tasks/{id} | 200 o 404 |
| POST | /api/v1/tasks | 201 y Location |
| PUT | /api/v1/tasks/{id} | 200; actualización completa |
| DELETE | /api/v1/tasks/{id} | 204, sin cuerpo |
| PATCH | /api/v1/tasks/{id}/complete | 200, recompensas una sola vez |

Errores uniformes: timestamp, status, error, message, path, errors.
Validación 400; credenciales inválidas o falta de token 401; inexistencia 404; método no
soportado 405; duplicado 409; regla de negocio 422; demasiados intentos 429. La fecha límite admite
tareas vencidas y el estado OVERDUE se persiste al arrancar y cada minuto.
No se puede editar una misión completada ni completar una inactiva.

## Verificar
```powershell
# Pruebas backend (incluye procesamiento asíncrono post-commit)
.\mvnw.cmd --batch-mode --no-transfer-progress test

# Pruebas específicas del procesamiento asíncrono
.\mvnw.cmd test "-Dtest=TaskServiceTest,HabitServiceTest,PetServiceTest,AuditInterceptorTest,TaskFlowIntegrationTest,AsyncProcessingIntegrationTest"

# Compilación frontend y laboratorio API (Newman)
npm.cmd --prefix frontend run build
npm.cmd --prefix tools ci --no-audit --no-fund
npm.cmd --prefix tools run lab
```
La última orden usa Newman para ejecutar la misma colección exportable de Postman.
Los resultados quedan en evidence/newman-run.json y evidence/lab-summary.json.
El Collection Runner de Postman usa los archivos de postman/ y el entorno PA-Lab2-local.
La colección controla el orden para ejecutar la eliminación al final.

## Procesamiento asíncrono de misiones
El flujo principal de `PATCH /api/v1/tasks/{id}/complete` conserva en la transacción principal: validación y propiedad, cambio a `COMPLETED`, entrega de XP/monedas, actualización/evolución de la mascota y daño/recompensas de jefe (con protección contra duplicados y `@Version` en tareas).

Tras el commit se procesan en segundo plano (`@TransactionalEventListener(AFTER_COMMIT)` + `@Async("taskgotchiExecutor")`): bitácora informativa `PetEvent`, auditoría de negocio confirmada y notificaciones internas (evolución/jefe derrotado). Los eventos son inmutables, incluyen `eventId`, `userId`, `taskId` (cuando aplica), recompensas, datos de notificación y `requestId`, y solo transportan identificadores/datos simples.

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant T as TaskServiceImpl (tx principal)
    participant DB as PostgreSQL/H2
    participant P as ApplicationEventPublisher
    participant L as DomainEventAsyncListener (tg-async-*)
    participant S as SecondaryEffectProcessor (nueva tx)

    C->>T: PATCH /api/v1/tasks/{id}/complete
    T->>DB: Validar, completar tarea, XP/monedas, evolución, jefe
    T->>P: Publicar evento inmutable (eventId, requestId, userId...)
    T-->>C: 200 TaskResponse (resultado confirmado)
    P-->>L: AFTER_COMMIT
    L->>S: Ejecutar efectos con idempotencia (eventId + tipo)
    S->>DB: PetEvent / auditoría / notificación (tx independiente)
    Note over S,DB: 3 intentos totales, backoff 200ms exponencial; fallos transitorios se registran y pueden reprocesarse
```

Configuración del ejecutor `taskgotchiExecutor`: `corePoolSize=2`, `maxPoolSize=4`, `queueCapacity=100`, prefijo `tg-async-`, `CallerRunsPolicy`, apagado ordenado con espera de hasta 20 s. Se propaga y restaura `MDC`/`SecurityContext` por tarea y se limpia al finalizar.

**Idempotencia y fallos:** cada efecto se registra en `async_processed_effects` con restricción única (`event_id`, `effect_type`) y se procesa de forma atómica. Hasta 3 intentos totales para errores transitorios (200 ms inicial, exponencial); sin reintentos para errores de validación/negocio. Los fallos se registran con `eventId`, `requestId` y `userId`, se persisten y un reprocesador programado los reintenta dentro del límite. Un fallo secundario no revierte recompensas ya confirmadas.

**Limitaciones:** `CallerRunsPolicy` puede ejecutar trabajo secundario en el hilo llamador cuando se satura la cola, aumentando la latencia de la respuesta. Los eventos publicados en memoria no sobreviven a una caída abrupta del proceso antes de persistir/procesar sus efectos; este mecanismo no sustituye un outbox transaccional (el estado de efectos procesados/fallidos sí se persiste para idempotencia y reproceso dentro del sistema).

## Bitácora breve de cambios y validaciones
- **Implementación:** `AsyncConfig` (executor + propagación/restauración de MDC/SecurityContext), eventos de dominio inmutables, listener `AFTER_COMMIT` + `@Async`, coordinador con idempotencia/reintentos/fallos persistidos, procesador secundario con transacciones nuevas, publicación desde `TaskServiceImpl.completeTask` y `HabitServiceImpl.checkHabit`, auditoría de negocio confirmada movida a procesamiento post-commit, adaptación de observabilidad (tiempos principal/secundario, hilo, resultado/errores).
- **Validaciones ejecutadas (entorno local, Windows, perfil H2 en pruebas):** `TaskServiceTest,HabitServiceTest,PetServiceTest,AuditInterceptorTest` (11 pruebas, OK), `AsyncProcessingIntegrationTest` (4 pruebas, OK: recompensas confirmadas + efectos post-commit en otro hilo, rollback sin efectos secundarios, fallo secundario sin pérdida de recompensas y registro de fallo, reintento idempotente con `requestId/userId` preservados), `TaskFlowIntegrationTest` (1 prueba, OK, contrato y no duplicación de recompensas).
- **No ejecutado en este entorno por alcance/tiempo:** suite completa `mvnw test` sin filtro, `frontend build`, Newman/lab (`npm.cmd --prefix tools run lab`). No se afirma que hayan pasado. Pasos para demostración: completar una misión (`PATCH /api/v1/tasks/{id}/complete`) y observar respuesta inmediata con recompensas; revisar logs `Procesamiento secundario`/`BUSINESS`/`NOTIFICATION` y tabla `async_processed_effects` para evidenciar post-commit, hilo `tg-async-*`, `requestId` y estado de efectos.

Consulta de persistencia:
```powershell
docker exec taskgotchi-postgres psql -U taskgotchi -d taskgotchidb -c "SELECT id, code, title FROM tasks ORDER BY id;"
```
La configuración ddl-auto=update se limita al laboratorio. Para producción se
recomiendan migraciones versionadas con Flyway/Liquibase y ddl-auto=validate.

## Cerrar
Ctrl+C en las terminales del backend y la web, después:
```powershell
docker stop taskgotchi-postgres
```
No borrar el volumen. Más detalles de entrega: docs/ENTREGA.md.
