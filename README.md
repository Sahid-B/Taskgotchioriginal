# Taskgotchi - Backend REST API

Taskgotchi es una plataforma web orientada a estudiantes que gestiona tareas, hábitos y proyectos grandes (jefes finales) mientras cuidan una mascota virtual en 2D que refleja de forma dinámicamente su nivel de productividad y constancia.

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos previos
- Java 17 o superior
- Apache Maven 3.8+

### Instrucciones
1. Clonar el repositorio y acceder a la carpeta raíz del backend.
2. Ejecutar la aplicación con Maven:
   ```bash
   mvn spring-boot:run
   ```
3. La aplicación se iniciará en `http://localhost:8080`.
4. La consola H2 para desarrollo se encuentra accesible en `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskgotchidb`, usuario: `sa`, contraseña vacía).

### Documentación e Interfaz de Swagger UI
Toda la documentación interactiva OpenAPI v3 y la prueba de los endpoints REST están disponibles en:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 📋 Tabla de Endpoints por Controller

| Controller | Método | Endpoint | Descripción |
| :--- | :--- | :--- | :--- |
| **AuthController** | `POST` | `/api/v1/auth/register` | Registra un nuevo estudiante y le crea su mascota inicial. |
| | `POST` | `/api/v1/auth/login` | Autentica al usuario y devuelve token placeholder JWT. |
| **UserController** | `GET` | `/api/v1/users/me` | Obtiene la información del usuario autenticado. |
| | `PUT` | `/api/v1/users/me` | Actualiza el nombre de usuario o correo. |
| **TaskController** | `GET` | `/api/v1/tasks` | Listado de tareas con paginación y filtros (status, categoryId, priority, dueBefore). |
| | `GET` | `/api/v1/tasks/{id}` | Obtiene el detalle de una tarea. |
| | `POST` | `/api/v1/tasks` | Crea una nueva tarea (201 Created + Location). |
| | `PUT` | `/api/v1/tasks/{id}` | Modifica una tarea existente no completada. |
| | `DELETE` | `/api/v1/tasks/{id}` | Elimina una tarea (204 No Content). |
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
