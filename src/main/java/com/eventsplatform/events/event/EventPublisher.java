package com.eventsplatform.events.event;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida hacia el message broker (RabbitMQ, Kafka, etc).
 * La implementacion concreta se conecta en config/broker cuando se
 * decida el proveedor. Por ahora hay una implementacion de logging
 * para no bloquear el desarrollo del resto del servicio.
 */
public interface EventPublisher {

    Mono<Void> publish(EventDomainEvent domainEvent);
}
