package com.eventsplatform.events.service;


import com.eventsplatform.events.domain.Event;
import com.eventsplatform.events.exception.EventNotFoundException;
import com.eventsplatform.events.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    // Crea un mock(objeto falso) del EventRepository
    @Mock
    private EventRepository eventRepository;

    // Crea una instancia real del EventService, pero inyecta en su constructor el mock de EventRepository
    @InjectMocks
    private EventService eventService;

    private UUID eventId;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        event = new Event(); // ajusta según los campos reales de tu entidad
        event.setId(eventId);
    }

    @Test
    void getById_deberiaRetornarEventResponse_cuandoExisteElEvento() {
        when(eventRepository.findById(eventId)).thenReturn(Mono.just(event));

        StepVerifier.create(eventService.getById(eventId))
                .expectNextMatches(response -> response.getId().equals(eventId))
                .verifyComplete();
    }

    @Test
    void getById_deberiaLanzarEventNotFoundException_cuandoNoExisteElEvento() {
        when(eventRepository.findById(eventId)).thenReturn(Mono.empty());

        StepVerifier.create(eventService.getById(eventId))
                .expectErrorMatches(throwable ->
                        throwable instanceof EventNotFoundException)
                .verify();
    }

}
