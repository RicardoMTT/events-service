package com.eventsplatform.events.service;

import com.eventsplatform.events.domain.Event;
import com.eventsplatform.events.domain.EventStatus;
import com.eventsplatform.events.dto.EventRequest;
import com.eventsplatform.events.dto.EventResponse;
import com.eventsplatform.events.event.EventDomainEvent;
import com.eventsplatform.events.event.EventPublisher;
import com.eventsplatform.events.exception.EventNotFoundException;
import com.eventsplatform.events.exception.ForbiddenEventActionException;
import com.eventsplatform.events.exception.InvalidEventStateException;
import com.eventsplatform.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;

    public Flux<EventResponse> search(String category, String query, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        long offset = (long) Math.max(page, 0) * safeSize;
        return eventRepository.search(EventStatus.PUBLISHED.name(), category, query, safeSize, offset)
                .map(EventResponse::from);
    }

    public Mono<EventResponse> getById(UUID id) {
        System.out.println("HII");
        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new EventNotFoundException(id)))
                .map(EventResponse::from);
    }

    public Flux<EventResponse> getByOrganizer(UUID organizerId) {
        return eventRepository.findAllByOrganizerId(organizerId)
                .map(EventResponse::from);
    }

    @Transactional
    public Mono<EventResponse> create(UUID organizerId, EventRequest request) {
        validateDateRange(request);

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .organizerId(organizerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .imageUrl(request.getImageUrl())
                .status(EventStatus.DRAFT)
                .build();

        return eventRepository.save(event)
                .flatMap(saved -> eventPublisher
                        .publish(EventDomainEvent.of(EventDomainEvent.CREATED, saved.getId(), organizerId))
                        .thenReturn(saved))
                .map(EventResponse::from);
    }

    public Mono<EventResponse> update(UUID id, UUID organizerId, EventRequest request) {
        validateDateRange(request);

        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new EventNotFoundException(id)))
                .flatMap(event -> assertOwner(event, organizerId))
                .flatMap(event -> {
                    if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
                        return Mono.error(new InvalidEventStateException(
                                "No se puede editar un evento en estado " + event.getStatus()));
                    }
                    event.setTitle(request.getTitle());
                    event.setDescription(request.getDescription());
                    event.setCategory(request.getCategory());
                    event.setLocation(request.getLocation());
                    event.setStartDate(request.getStartDate());
                    event.setEndDate(request.getEndDate());
                    event.setImageUrl(request.getImageUrl());
                    return eventRepository.save(event);
                })
                .flatMap(saved -> eventPublisher
                        .publish(EventDomainEvent.of(EventDomainEvent.UPDATED, saved.getId(), organizerId))
                        .thenReturn(saved))
                .map(EventResponse::from);
    }

    public Mono<EventResponse> publish(UUID id, UUID organizerId) {
        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new EventNotFoundException(id)))
                .flatMap(event -> assertOwner(event, organizerId))
                .flatMap(event -> {
                    if (event.getStatus() != EventStatus.DRAFT) {
                        return Mono.error(new InvalidEventStateException(
                                "Solo un evento en borrador puede publicarse (estado actual: " + event.getStatus() + ")"));
                    }
                    event.setStatus(EventStatus.PUBLISHED);
                    return eventRepository.save(event);
                })
                .flatMap(saved -> eventPublisher
                        .publish(EventDomainEvent.of(EventDomainEvent.PUBLISHED, saved.getId(), organizerId))
                        .thenReturn(saved))
                .map(EventResponse::from);
    }

    public Mono<EventResponse> cancel(UUID id, UUID organizerId) {
        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new EventNotFoundException(id)))
                .flatMap(event -> assertOwner(event, organizerId))
                .flatMap(event -> {
                    if (event.getStatus() == EventStatus.FINISHED || event.getStatus() == EventStatus.CANCELLED) {
                        return Mono.error(new InvalidEventStateException(
                                "El evento ya esta " + event.getStatus() + " y no puede cancelarse"));
                    }
                    event.setStatus(EventStatus.CANCELLED);
                    return eventRepository.save(event);
                })
                .flatMap(saved -> eventPublisher
                        .publish(EventDomainEvent.of(EventDomainEvent.CANCELLED, saved.getId(), organizerId))
                        .thenReturn(saved))
                .map(EventResponse::from);
    }

    private Mono<Event> assertOwner(Event event, UUID organizerId) {
        if (!event.getOrganizerId().equals(organizerId)) {
            return Mono.error(new ForbiddenEventActionException(
                    "El usuario no es el organizador de este evento"));
        }
        return Mono.just(event);
    }

    private void validateDateRange(EventRequest request) {
        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidEventStateException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }
}
