package com.eventsplatform.events.repository;

import com.eventsplatform.events.domain.Event;
import com.eventsplatform.events.domain.EventStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EventRepository extends ReactiveCrudRepository<Event, UUID> {

    Flux<Event> findAllByStatus(EventStatus status);

    Flux<Event> findAllByOrganizerId(UUID organizerId);

    @Query("""
            SELECT * FROM events
            WHERE status = :status
              AND (:category IS NULL OR category = :category)
              AND (:query IS NULL OR title ILIKE CONCAT('%', :query, '%'))
            ORDER BY start_date ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Event> search(String status, String category, String query, int limit, long offset);

    Mono<Boolean> existsByIdAndOrganizerId(UUID id, UUID organizerId);
}
