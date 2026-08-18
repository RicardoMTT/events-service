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
    private static final String TOPIC = "event.created";
    private final KafkaTemplate<String, EventDomainEvent> kafkaTemplate;

    @Override
    public Mono<Void> publish(EventDomainEvent event) {

        return Mono.fromFuture(
                kafkaTemplate.send(
                        TOPIC,
                        event.eventId().toString(),
                        event
                )
        ).then();
    }
}
