package com.eventsplatform.events.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado al message broker cuando cambia el estado
 * de un evento del catalogo. Otros servicios (Inventario, Notificaciones,
 * Busqueda) se suscriben a estos eventos por su "type".
 */
public record EventDomainEvent(
        String type,
        UUID eventId,
        UUID organizerId,
        Instant occurredAt
) {
    public static final String CREATED = "event.created";
    public static final String PUBLISHED = "event.published";
    public static final String UPDATED = "event.updated";
    public static final String CANCELLED = "event.cancelled";

    public static EventDomainEvent of(String type, UUID eventId, UUID organizerId) {
        return new EventDomainEvent(type, eventId, organizerId, Instant.now());
    }
}
