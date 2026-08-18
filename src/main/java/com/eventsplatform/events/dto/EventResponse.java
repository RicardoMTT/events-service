package com.eventsplatform.events.dto;

import com.eventsplatform.events.domain.Event;
import com.eventsplatform.events.domain.EventStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class EventResponse {

    UUID id;
    UUID organizerId;
    String title;
    String description;
    String category;
    String location;
    OffsetDateTime startDate;
    OffsetDateTime endDate;
    EventStatus status;
    String imageUrl;
    Instant createdAt;
    Instant updatedAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizerId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .imageUrl(event.getImageUrl())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
