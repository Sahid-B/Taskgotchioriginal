# Taskgotchi - Backend REST API & Frontend Base

Taskgotchi es una plataforma web orientada a estudiantes que gestiona tareas, hábitos y proyectos grandes (jefes finales) mientras cuidan una mascota virtual en 2D que refleja dinámicamente su nivel de productividad y constancia.

---

## 🏛️ Arquitectura por Capas (`com.taskgotchi`)

El backend está diseñado siguiendo una arquitectura en capas limpia y desacoplada:

- `controller`: Controladores REST (`@RestController`) que gestionan solicitudes/respuestas HTTP mediante DTOs.
- `service`: Interfaces y clases de implementación (`service/impl`) con la lógica de negocio y transacciones `@Transactional`.
- `repository`: Repositorios Spring Data JPA que extienden `JpaRepository` con consultas derivadas para aislamiento de datos.
- `model`: Entidades JPA y enums del dominio.
- `dto`: Records DTO para peticiones, respuestas e informes de error uniformes.
- `mapper`: Componentes Spring para la conversión entre entidades y DTOs.
- `exception`: Excepciones de dominio personalizadas y manejador global `@RestControllerAdvice`.
- `config`: Configuraciones de OpenAPI/Swagger UI, CORS, Web MVC y Security esqueleto.
- `security`: Abstracción para la gestión e inyección del usuario autenticado actual.

---

## 🛠️ Tecnologías y Requisitos

- **Java**: Java 21
- **Framework**: Spring Boot 3.2.x / 4.x
- **Persistencia**: Spring Data JPA + PostgreSQL 16 (H2 disponible en perfil dev/test)
- **Documentación API**: Springdoc OpenAPI v3 / Swagger UI
- **Frontend**: React + TypeScript + Vite (`frontend/`)

---

## 🚀 Guía de Inicio Rápido en Windows

### 1. Configuración de PostgreSQL

1. Instalar PostgreSQL 16 y asegurar que el servicio esté corriendo en el puerto `5432`.
2. Crear la base de datos desde `psql` o pgAdmin:
   ```sql
   CREATE DATABASE taskgotchidb;
   ```
3. Configurar las variables de entorno en Windows (CMD o PowerShell):
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

En la carpeta raíz del proyecto:
```bash
mvn spring-boot:run
```
*(Para desarrollo local con base de datos H2 en memoria, ejecutar: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`)*

El backend estará disponible en `http://localhost:8080`.

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### 3. Ejecutar el Frontend (React + Vite)

En una nueva terminal, acceder a la carpeta `frontend`:
```bash
cd frontend
npm install
npm run dev
```

El frontend estará disponible en `http://localhost:5173`.

---

## 📋 Tabla de Endpoints por Controller

| Controller | Método | Endpoint | Descripción |
| :--- | :--- | :--- | :--- |
| **AuthController** | `POST` | `/api/v1/auth/register` | Registra un nuevo estudiante y le crea su mascota inicial. |
| | `POST` | `/api/v1/auth/login` | Autentica al usuario y devuelve token placeholder JWT. |
| **UserController** | `GET` | `/api/v1/users/me` | Obtiene la información del usuario autenticado. |
| | `PUT` | `/api/v1/users/me` | Actualiza el nombre de usuario o correo. |
| **TaskController** | `GET` | `/api/v1/tasks` | Listado de tareas paginado y ordenable (`page=0`, `size=10`, `sort=id,asc`) con búsqueda por `title`. |
| | `GET` | `/api/v1/tasks/{id}` | Obtiene el detalle de una tarea por ID. |
| | `POST` | `/api/v1/tasks` | Crea una nueva tarea (`201 Created` + cabecera `Location`). |
| | `PUT` | `/api/v1/tasks/{id}` | Modifica una tarea existente no completada (`200 OK`). |
| | `DELETE` | `/api/v1/tasks/{id}` | Elimina una tarea (`204 No Content`). |
| | `PATCH` | `/api/v1/tasks/{id}/complete` | Marca tarea como completada, suma XP y monedas a la mascota y registra evento. |
| **CategoryController** | `GET` | `/api/v1/categories` | Lista todas las categorías del usuario. |
| | `GET` | `/api/v1/categories/{id}` | Detalle de categoría. |
| | `POST` | `/api/v1/categories` | Crea una nueva categoría. |
| | `PUT` | `/api/v1/categories/{id}` | Actualiza nombre o color de categoría. |
| | `DELETE` | `/api/v1/categories/{id}` | Elimina categoría. |
| **PetController** | `GET` | `/api/v1/pet` | Estado actual de la mascota (calcula desgaste por tiempo). |
| | `POST` | `/api/v1/pet/feed` | Alimenta a la mascota (+Hambre, +Ánimo) usando monedas. |
| | `POST` | `/api/v1/pet/play` | Juega con la mascota (+Ánimo, -Energía). |
| | `POST` | `/api/v1/pet/rest` | Pone a descansar a la mascota (+Energía). |
| | `GET` | `/api/v1/pet/events` | Historial paginado de eventos de la mascota. |
| **HabitController** | `GET` | `/api/v1/habits` | Lista de hábitos del usuario. |
| | `GET` | `/api/v1/habits/{id}` | Detalle de un hábito. |
| | `POST` | `/api/v1/habits` | Crea un nuevo hábito. |
| | `PUT` | `/api/v1/habits/{id}` | Actualiza hábito. |
| | `DELETE` | `/api/v1/habits/{id}` | Elimina hábito. |
| | `POST` | `/api/v1/habits/{id}/check` | Registra el cumplimiento de hoy, actualiza racha con bonificación y otorga XP. |
| | `GET` | `/api/v1/habits/{id}/logs` | Obtiene el historial de cumplimientos. |
| **BossController** | `GET` | `/api/v1/bosses` | Lista todos los jefes activos y derrotados. |
| | `GET` | `/api/v1/bosses/{id}` | Detalle del jefe con barra de vida (HP) y subtareas. |
| | `POST` | `/api/v1/bosses` | Crea un nuevo jefe a partir de una tarea/proyecto grande. |
| | `POST` | `/api/v1/bosses/{id}/subtasks` | Agrega una nueva subtarea al jefe. |
| | `PATCH` | `/api/v1/bosses/{id}/subtasks/{subId}/complete` | Completa subtarea, reduce vida al jefe y al llegar a 0 HP lo derrota con gran recompensa. |
| **AssistantController** | `POST` | `/api/v1/assistant/pet-message` | Genera un mensaje contextual con personalidad según el estado de la mascota. |
| | `POST` | `/api/v1/assistant/split-task` | Descompone un objetivo complejo proponiendo subtareas paso a paso. |
| | `GET` | `/api/v1/assistant/daily-missions` | Devuelve misiones diarias recomendadas por la IA MOCK. |
| **DashboardController** | `GET` | `/api/v1/dashboard/summary` | Resumen de pendientes, vencidas, racha máxima y estado de mascota. |
| | `GET` | `/api/v1/dashboard/weekly-stats` | Estadísticas semanales de tareas y hábitos por día. |
| | `GET` | `/api/v1/dashboard/pet-history` | 10 eventos más recientes de la mascota. |

---

## 🧪 Pruebas con Postman

En la carpeta `postman/` se incluyen los archivos listos para importar en Postman:
1. `postman/taskgotchi.postman_collection.json`: Colección v2.1 con 12 solicitudes de verificación.
2. `postman/taskgotchi.postman_environment.json`: Entorno local con la variable `baseUrl` (`http://localhost:8080`).

### 12 Escenarios Cubiertos
1. **Crear tarea válida**: `201 Created` con cabecera `Location` e ID almacenado dinámicamente.
2. **Consultar tarea creada**: `200 OK` por ID.
3. **Listar tareas con paginación y orden**: `200 OK` (`page=0`, `size=10`, `sort=id,asc`).
4. **Buscar tarea por título**: `200 OK` (`?title=Examen`).
5. **Actualizar tarea**: `200 OK` comprobando `actualizadoEn` (`updatedAt`).
6. **Validación de campos obligatorios vacíos**: `400 Bad Request` con DTO de error uniforme.
7. **JSON mal formado / enum inválido**: `400 Bad Request`.
8. **Identificador inexistente**: `404 Not Found`.
9. **Duplicado / Conflicto**: `409 Conflict` (o `422`).
10. **Incumplimiento de regla de negocio**: `422 Unprocessable Entity`.
11. **Método no admitido**: `405 Method Not Allowed`.
12. **Eliminar tarea**: `204 No Content` y verificación posterior en `404 Not Found`.

---

## 📸 Lista de Evidencias para el Estudiante

Para la entrega académica del laboratorio, el estudiante debe tomar y adjuntar las siguientes capturas de pantalla:
1. **Arranque del Backend**: Consola de Spring Boot con inicio exitoso en puerto `8080` conectado a PostgreSQL.
2. **Base de Datos PostgreSQL**: Consulta SQL en pgAdmin/psql mostrando la tabla `tasks` con los campos `created_at` y `updated_at`.
3. **Swagger UI**: Interfaz gráfica en `/swagger-ui.html` mostrando los controladores y endpoints desplegados.
4. **Resultados de Postman**: Ejecución del Runner de Postman con los 12 casos en verde.
5. **Errores Negativos**: Captura de respuesta HTTP 400, 404, 405 y 422 con formato JSON `ApiError`.
6. **Historial Git**: Ejecución en consola del comando:
   ```bash
   git log --oneline -n 10
   ```

---

## 🔮 Siguiente Unidad (Roadmap de TODOs)

El proyecto queda preparado con los siguientes puntos de extensión expresamente estructurados:

1. **JWT y Autenticación**:
   - Reemplazar la implementación temporal `DevCurrentUserProvider` por la extracción del usuario autenticado desde `SecurityContextHolder.getContext().getAuthentication()`.
   - Implementar `JwtTokenProvider` y `JwtAuthenticationFilter` en `config/SecurityConfig.java`.
   - Reemplazar los tokens placeholder devueltos en `AuthServiceImpl.java` (`TODO_JWT_TOKEN_FOR_USER_...`) por tokens JWT reales firmados.
   - Proteger los endpoints `/api/v1/**` requiriendo autenticación JWT en lugar de `.permitAll()`.

2. **Interceptores HTTP**:
   - Completar `RequestMonitoringInterceptor.java` en `WebMvcConfig.java` para registrar métricas de tiempo de ejecución de cada petición HTTP.
   - Completar `AuditInterceptor.java` para registrar eventos de auditoría (completación de tareas, derrotas de jefes, evoluciones).

3. **Integración con IA Real**:
   - Sustituir `MockAiAssistantServiceImpl` por una implementación que consuma la API de OpenAI / Claude mediante Spring AI sin alterar las firmas ni los controladores REST.

---

## 📌 Supuestos de Diseño

1. **Desgaste Dinámico de la Mascota**:
   - El desgaste de hambre, ánimo y energía se calcula de forma lazy al consultar la mascota mediante la diferencia en horas desde `lastUpdatedAt`.
2. **Recompensas y Dificultad**:
   - Dificultades de Tareas: `EASY` (+20 XP, +10 monedas), `MEDIUM` (+50 XP, +25 monedas), `HARD` (+100 XP, +60 monedas).
3. **Aislamiento por Usuario**:
   - Todos los repositorios y servicios filtran estrictamente los recursos pertenecientes a `userId`. Si se intenta acceder a un recurso ajeno, la API responde con un error uniforme `404 Not Found`.
