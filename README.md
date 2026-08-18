# events-service

Microservicio de catálogo de eventos para la plataforma de eventos.
Responsabilidad única: CRUD y ciclo de vida de eventos (`DRAFT` → `PUBLISHED` → `CANCELLED`/`FINISHED`).
No maneja inventario de tickets ni pagos — eso vive en otros microservicios.

## Stack

- Spring Boot 3.3 (WebFlux, reactivo de punta a punta)
- Spring Data R2DBC + PostgreSQL
- Flyway para migraciones (usa JDBC solo para migrar; la app corre 100% reactiva)
- Bean Validation
- Spring Boot Actuator
- Lombok
- Testcontainers para tests de integración

## Estructura

```
src/main/java/com/eventsplatform/events/
├── config/       # Config de R2DBC, transacciones y auditoría
├── controller/    # Endpoints REST
├── domain/        # Entidad Event + EventStatus
├── dto/           # Request/Response
├── event/         # Publicación de eventos de dominio al broker
├── exception/      # Excepciones de negocio + manejador global
├── repository/     # EventRepository (ReactiveCrudRepository)
└── service/        # Lógica de negocio y transiciones de estado
```

## Cómo levantarlo

```bash
docker compose up --build
```

Esto levanta Postgres + el servicio en `http://localhost:8081`. Flyway corre la migración automáticamente al iniciar.

Para desarrollo local sin Docker, corré Postgres aparte y ajustá las variables de entorno
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (ver `application.yml` para los defaults).

## Autenticación

Este servicio **no valida JWT**. Asume que el API gateway ya validó el token
y reenvía el usuario autenticado en el header `X-User-Id`. Así se evita
acoplar Eventos al servicio de Auth.

## Endpoints

| Método | Ruta                     | Descripción                                  | Requiere `X-User-Id` |
|--------|--------------------------|-----------------------------------------------|:---------------------:|
| GET    | `/events`                | Lista eventos publicados (filtros: `category`, `q`, `page`, `size`) | No |
| GET    | `/events/{id}`           | Detalle de un evento                          | No |
| GET    | `/events/mine`           | Eventos del organizador autenticado           | Sí |
| POST   | `/events`                | Crea un evento en estado `DRAFT`              | Sí |
| PUT    | `/events/{id}`           | Edita un evento (solo el dueño, no si está cancelado/finalizado) | Sí |
| PATCH  | `/events/{id}/publish`   | `DRAFT` → `PUBLISHED`                          | Sí |
| PATCH  | `/events/{id}/cancel`    | Cancela el evento                              | Sí |

Ejemplo de creación:

```bash
curl -X POST http://localhost:8081/events \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 3f1a9c2e-6b7d-4e3a-9c1a-2f8e7d6b5a4c" \
  -d '{
    "title": "Concierto de rock",
    "description": "Una noche de rock en vivo",
    "category": "music",
    "location": "Estadio Nacional, Lima",
    "startDate": "2026-12-01T20:00:00-05:00",
    "endDate": "2026-12-01T23:00:00-05:00"
  }'
```

## Eventos publicados al broker

Cuando cambia el estado de un evento, se publica un `EventDomainEvent` con uno de estos tipos:
`event.created`, `event.published`, `event.updated`, `event.cancelled`.

Por ahora `LoggingEventPublisher` solo loguea el evento — reemplazalo por una implementación
real de `EventPublisher` (RabbitMQ/Kafka) cuando se conecte el broker. El resto del código
no necesita cambiar porque depende de la interfaz.

## Tests

```bash
./mvnw test
```

Usa Testcontainers, así que necesita Docker corriendo localmente.

## Actuator

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`

## Próximos pasos sugeridos

- Reemplazar `LoggingEventPublisher` por la integración real con el broker.
- Agregar rate limiting / circuit breaker si otros servicios llaman síncronamente a este.
- Sumar un endpoint de búsqueda más rico o delegarlo por completo al servicio de Búsqueda (Elasticsearch) consumiendo `event.published`.
