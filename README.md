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

El modo actual es **local de un jugador**; el usuario de desarrollo está identificado
por DevCurrentUserProvider. El login JWT, multijugador y una IA externa no forman
parte de esta versión. La API no debe exponerse públicamente en este modo.

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
Validación 400; credenciales inválidas 401; inexistencia 404; método no
soportado 405; duplicado 409; regla de negocio 422. La fecha límite admite
tareas vencidas y el estado OVERDUE se persiste al arrancar y cada minuto.
No se puede editar una misión completada ni completar una inactiva.

## Verificar
```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress test
npm.cmd --prefix frontend run build
npm.cmd --prefix tools ci --no-audit --no-fund
npm.cmd --prefix tools run lab
```
La última orden usa Newman para ejecutar la misma colección exportable de Postman.
Los resultados quedan en evidence/newman-run.json y evidence/lab-summary.json.
El Collection Runner de Postman usa los archivos de postman/ y el entorno PA-Lab2-local.
La colección controla el orden para ejecutar la eliminación al final.

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
