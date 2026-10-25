# Taskgotchi - Laboratorio 1.2 Programación Avanzada

**Paquete Base**: `ec.edu.espe.pa.taskgotchi`
**Tecnologías**: Java 21 | Spring Boot 3.2.3 | Spring Data JPA | PostgreSQL 16 | React + TypeScript + Vite (`frontend/`)

Taskgotchi es una plataforma web orientada a estudiantes que gestiona tareas, hábitos y proyectos grandes (jefes finales) mientras cuidan una mascota virtual en 2D que refleja dinámicamente su nivel de productividad y constancia.

---

## 🏛️ Arquitectura por Capas (`ec.edu.espe.pa.taskgotchi`)

El backend está organizado de forma desacoplada y estricta:

- `controller`: Controladores REST (`@RestController`) que procesan peticiones/respuestas HTTP usando DTOs.
- `service`: Interfaces y clases de implementación (`service/impl`) con la lógica de negocio y transacciones `@Transactional`.
- `repository`: Repositorios Spring Data JPA con consultas derivadas y soporte de paginación/especificaciones.
- `model`: Entidades JPA (`Task`, `User`, `Pet`, `Habit`, `Boss`, etc.) y enums guardados como texto.
- `dto`: Records DTO para entrada, salida (`TaskResponse`, `PageResponse`) y respuestas de error uniformes (`ApiError`).
- `mapper`: Componentes Spring para conversión bidireccional entre Entidades y DTOs.
- `exception`: Excepciones de dominio (`ResourceNotFoundException`, `DuplicateResourceException`, `BusinessRuleException`) y manejador centralizado `@RestControllerAdvice`.
- `config`: Configuraciones de OpenAPI/Swagger UI, CORS, Web MVC, Security esqueleto e inicializador de desarrollo (`DevDataInitializer`).
- `security`: Abstracción `CurrentUserProvider` / `DevCurrentUserProvider` (ID 1L).

---

## ⚙️ Configuración y Ejecución en Windows

### 1. Preparación de PostgreSQL 16
1. Instalar PostgreSQL 16 y verificar ejecucion en puerto `5432`.
2. Crear la base de datos `taskgotchidb`:
   ```sql
   CREATE DATABASE taskgotchidb;
   ```
3. Configurar variables de entorno (sin exponer contraseñas en código):
   **PowerShell:**
   ```powershell
   $env:DB_HOST="localhost"
   $env:DB_PORT="5432"
   $env:DB_NAME="taskgotchidb"
   $env:DB_USER="postgres"
   $env:DB_PASSWORD="tu_password_aqui"
   ```
   **CMD:**
   ```cmd
   set DB_HOST=localhost
   set DB_PORT=5432
   set DB_NAME=taskgotchidb
   set DB_USER=postgres
   set DB_PASSWORD=tu_password_aqui
   ```

### 2. Ejecutar el Backend (Spring Boot)
En la raíz del proyecto usando el Maven Wrapper:
```cmd
.\mvnw.cmd spring-boot:run
```
*(Para pruebas locales con H2 en memoria, ejecutar: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2`)*

El backend estará disponible en `http://localhost:8080`.

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### 3. Ejecutar el Frontend (React + Vite)
En una nueva terminal, acceder a `frontend/`:
```cmd
cd frontend
npm install
npm run dev
```
El frontend web estará disponible en `http://localhost:5173`.

---

## 📋 Contrato REST de Tareas (`/api/v1/tasks`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/tasks` | Listado paginado de tareas (`page=0`, `size=10`, `sort=id,asc`) | `200 OK` |
| `GET` | `/api/v1/tasks/buscar?title=...` | Búsqueda paginada por título (parámetro `title` obligatorio) | `200 OK` (o `400 Bad Request` si falta `title`) |
| `GET` | `/api/v1/tasks/{id}` | Consulta de tarea por ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/v1/tasks` | Creación de tarea con `code` único, `estimatedHours` y `active` | `201 Created` + `Location` header |
| `PUT` | `/api/v1/tasks/{id}` | Actualización completa con soporte de `bossId` y `parentTaskId` | `200 OK` |
| `DELETE` | `/api/v1/tasks/{id}` | Eliminación de tarea | `204 No Content` |
| `PATCH` | `/api/v1/tasks/{id}/complete` | Completar tarea (otorga XP/Monedas a la mascota y daña al jefe) | `200 OK` / `422 Unprocessable` |

---

## ⚠️ Formato Uniforme de Error (`ApiError`)

Todas las respuestas de error devuelven exactamente **6 claves**:
```json
{
  "timestamp": "2026-10-23T18:00:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos ingresados",
  "path": "/api/v1/tasks",
  "errors": [
    "code: El código de la tarea es obligatorio",
    "estimatedHours: Las horas estimadas deben ser un valor positivo"
  ]
}
```
*(Si no hay errores de campos por Bean Validation, `errors` se devuelve como `[]`)*

### Mapeo de Códigos HTTP
- `400 Bad Request`: Campos inválidos, JSON mal formado, enum inválido o falta de parámetro obligatorio (`title`).
- `404 Not Found`: Tarea o recurso inexistente.
- `405 Method Not Allowed`: Método HTTP no soportado (ej. PATCH en `/api/v1/tasks/{id}`).
- `409 Conflict`: Código de tarea duplicado (`code`) para el mismo usuario.
- `422 Unprocessable Entity`: Violación de regla de negocio (ej. editar tarea ya completada).
- `500 Internal Server Error`: Error inesperado registrado en el log.

---

## 🧪 Verificación con Postman Collection v2.1

Archivos disponibles en `postman/`:
- `postman/taskgotchi.postman_collection.json`
- `postman/taskgotchi.postman_environment.json`

Organizada en dos carpetas principales:
- **`01-Casos exitosos`**:
  - `E1`: Crear tarea válida (`201 Created`, cabecera `Location`, guarda `createdTaskId`).
  - `E2`: Consultar por ID (`200 OK`).
  - `E3`: Listar con paginación y orden (`200 OK`, valida estructura `content` y `page`).
  - `E4`: Buscar por título (`200 OK`).
  - `E5`: Actualizar tarea (`200 OK`, comprueba `updatedAt`).
  - `E6`: Eliminar tarea (`204 No Content` y posterior `404 Not Found`).
- **`02-Casos negativos`**:
  - `N1`: Campos vacíos/fuera de rango (`400 Bad Request` + 6 claves en `ApiError`).
  - `N2`: JSON mal formado / Enum inválido (`400 Bad Request`).
  - `N3`: ID no numérico (`400 Bad Request`).
  - `N4`: ID inexistente (`404 Not Found`).
  - `N5`: Código de tarea duplicado (`409 Conflict`).
  - `N6`: Regla de negocio: Editar tarea completada (`422 Unprocessable Entity`).
  - `N7`: PATCH no soportado a `/api/v1/tasks/{id}` (`405 Method Not Allowed`).

---

## 🔍 Verificación de Persistencia en PostgreSQL

Para verificar que los datos sobreviven al reinicio de la aplicación antes de ser eliminados:
1. Iniciar la aplicación y ejecutar la petición `E1: Crear tarea válida` en Postman.
2. Desde pgAdmin o `psql`, ejecutar:
   ```sql
   SELECT id, code, title, estimated_hours, active, created_at, updated_at FROM tasks;
   ```
3. Detener la aplicación (`Ctrl + C`) y volver a iniciarla (`.\mvnw.cmd spring-boot:run`).
4. Volver a ejecutar la consulta SQL en PostgreSQL y comprobar que la fila permanece intacta.

---

## 📸 Lista de Evidencias para el Informe Académico

1. **Arranque de Backend**: Consola mostrando Spring Boot iniciado en puerto `8080` con la conexión a PostgreSQL.
2. **Estructura de Tabla PostgreSQL**: Captura de pgAdmin/psql describiendo la tabla `tasks` con campos `code`, `estimated_hours`, `active`, `created_at` y `updated_at`.
3. **Persistencia tras Reinicio**: Captura de la consulta `SELECT` antes y después de reiniciar Spring Boot.
4. **Postman Runner**: Ejecución de la colección completa en Postman con todas las pruebas automáticas en verde.
5. **Respuestas de Error Negativas**: Capturas mostrando el formato JSON uniforme `ApiError` de 6 claves para respuestas 400, 404, 405, 409 y 422.
6. **Historial de Commits**: Ejecución en terminal del comando:
   ```bash
   git log --oneline -n 10
   ```

---

## 🔮 Siguiente Unidad (Roadmap de TODOs)

1. **JWT y Autenticación Real**:
   - Reemplazar `DevCurrentUserProvider` (ID 1L) por la extracción del usuario autenticado desde `SecurityContextHolder.getContext().getAuthentication()`.
   - Implementar `JwtTokenProvider` y `JwtAuthenticationFilter` en `config/SecurityConfig.java`.
2. **Interceptores de Auditoría y Monitoreo**:
   - Completar `RequestMonitoringInterceptor.java` y `AuditInterceptor.java` en `config/WebMvcConfig.java`.
3. **Integración con IA Real**:
   - Reemplazar `MockAiAssistantServiceImpl` por integración real de Spring AI con OpenAI/Claude.
