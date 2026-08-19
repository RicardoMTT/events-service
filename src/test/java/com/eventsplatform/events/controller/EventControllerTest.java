package com.eventsplatform.events.controller;

import com.eventsplatform.events.dto.EventResponse;
import com.eventsplatform.events.exception.EventNotFoundException;
import com.eventsplatform.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = EventController.class)
public class EventControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    // No es el servicio real, es un mock
    @MockBean
    private EventService eventService;

    @Test
    void getById_deberiaRetornar200YElEvento_cuandoExiste() {
        UUID eventId = UUID.randomUUID();
        EventResponse response = EventResponse.builder()
                .id(eventId)
                .build();

        // cuando alguien llame a getById(eventId), no ejecutes lógica real, simplemente devuelve un Mono que emite response".
        // Esto aísla el test: solo se está probando el controller, no el servicio.
        when(eventService.getById(eventId)).thenReturn(Mono.just(response));

        // webTestClient simula un cliente HTTP real, pero sin levantar un servidor de verdad
        webTestClient.get()
                .uri("/events/{id}", eventId)
                .exchange()// Ejecuta la petición
                .expectStatus().isOk()
                .expectBody(EventResponse.class)
                .value(body -> assertEquals(eventId, body.getId()));
    }

    @Test
    void getById_deberiaRetornar404_cuandoNoExisteElEvento() {
        UUID eventId = UUID.randomUUID();

        when(eventService.getById(eventId))
                .thenReturn(Mono.error(new EventNotFoundException(eventId)));

        webTestClient.get()
                .uri("/events/{id}", eventId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
