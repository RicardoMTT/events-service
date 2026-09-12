package com.eventsplatform.events.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implementacion temporal: solo loguea el evento de dominio.
 * Reemplazar por un publisher real (RabbitTemplate, KafkaTemplate, etc)
 * cuando se conecte el message broker. El resto del codigo no cambia
 * porque depende de la interfaz EventPublisher.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {


    private static final String CREATED_TOPIC = "event.created";

    private static final String PUBLISHED_TOPIC = "event.published";

    private final KafkaTemplate<String, EventDomainEvent> kafkaTemplate;

    @Override
    public Mono<Void> publishCreated(EventDomainEvent event) {

        return Mono.fromFuture(
                kafkaTemplate.send(
                        CREATED_TOPIC,
                        event.eventId().toString(),
                        event
                )
        ).then();
    }

    @Override
    public Mono<Void> publishPublished(EventDomainEvent event) {

        return Mono.fromFuture(
                kafkaTemplate.send(
                        PUBLISHED_TOPIC,
                        event.eventId().toString(),
                        event
                )
        ).then();
    }
}
