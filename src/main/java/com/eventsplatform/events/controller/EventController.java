package com.eventsplatform.events.controller;

import com.eventsplatform.events.dto.EventRequest;
import com.eventsplatform.events.dto.EventResponse;
import com.eventsplatform.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * El API gateway valida el JWT y reenvia el user_id autenticado en el
 * header X-User-Id. Este servicio confia en esa cabecera y no vuelve
 * a validar el token (evita acoplarse al servicio de Auth).
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final EventService eventService;

    @GetMapping
    public Flux<EventResponse> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return eventService.search(category, q, page, size);
    }

    @GetMapping("/{id}")
    public Mono<EventResponse> getById(@PathVariable UUID id) {
        System.out.println("TEEE");
        return eventService.getById(id);
    }

    @GetMapping("/mine")
    public Flux<EventResponse> getMine(@RequestHeader(USER_ID_HEADER) UUID organizerId) {
        return eventService.getByOrganizer(organizerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EventResponse> create(
            @RequestHeader(USER_ID_HEADER) UUID organizerId,
            @Valid @RequestBody EventRequest request) {
        return eventService.create(organizerId, request);
    }

    @PutMapping("/{id}")
    public Mono<EventResponse> update(
            @PathVariable UUID id,
            @RequestHeader(USER_ID_HEADER) UUID organizerId,
            @Valid @RequestBody EventRequest request) {
        return eventService.update(id, organizerId, request);
    }

    @PatchMapping("/{id}/publish")
    public Mono<EventResponse> publish(
            @PathVariable UUID id,
            @RequestHeader(USER_ID_HEADER) UUID organizerId) {
        return eventService.publish(id, organizerId);
    }

    @PatchMapping("/{id}/cancel")
    public Mono<EventResponse> cancel(
            @PathVariable UUID id,
            @RequestHeader(USER_ID_HEADER) UUID organizerId) {
        return eventService.cancel(id, organizerId);
    }
}
