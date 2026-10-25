# Entrega del Laboratorio 1.2

El proyecto y el laboratorio comparten la entidad Task y la API /api/v1/tasks.

## Verificación técnica

- Java 21 y Spring Boot 4.1.2, Jackson 3 y starter webmvc.
- PostgreSQL 16 en Docker, puerto local 5433, volumen proyecto_taskgotchi-data.
- Capas controller, service, repository, entity, dto, mapper y exception.
- CRUD con DTO, búsqueda derivada, paginación y auditoría automática.
- Código único por usuario, reglas de negocio transaccionales y errores de seis campos.
- Colección Postman reproducible E1-E5, N1-N9, E6 y GET posterior 404.
- Evidencias de SQL, reinicio, pruebas y commits en evidence/.

## Alcance del juego

Mundo 2D con movimiento, salto, compañero e interacción; misiones CRUD con
filtros, dificultad, XP y monedas; hábitos diarios y rachas;
mascota animada con cuidados y evolución; proyectos como jefes finales;
bitácora real y temporizador de enfoque sin recompensas artificiales.

La versión es local de un jugador. DevCurrentUserProvider selecciona el usuario
de desarrollo; no ofrece todavía autenticación JWT ni separación de sesiones
de usuarios. El asistente IA antiguo es un mock y no aparece como IA real en
la interfaz. Estas ampliaciones corresponden al desarrollo posterior del proyecto.

## Antes de subir al aula

1. Revisar nombre, NRC y fecha del informe. El aula muestra NRC 32821 y la guía
   genérica 32808; se empleó el valor del aula para los datos del estudiante.
2. Incluir la URL del repositorio propio que se use para entregar.
3. Importar los dos JSON de postman/ y ejecutar la colección completa con el
   entorno PA-Lab2-local. El orden se controla con Scripts; no usar un orden manual distinto.
4. La ejecución verificada de esta versión utiliza Newman por consola. Si el
   docente requiere además la captura del Runner de escritorio, abrir la misma
   colección en Postman y guardar su ejecución, sin claves.
5. Para apagar al finalizar, detener las dos terminales con Ctrl+C y ejecutar
   docker stop taskgotchi-postgres. No eliminar el volumen ni la base.

El remoto de origen se conserva como referencia. No se publica ningún cambio
automáticamente en GitHub.
