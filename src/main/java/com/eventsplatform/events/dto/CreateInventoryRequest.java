package com.eventsplatform.events.dto;

import java.util.UUID;

public record CreateInventoryRequest(UUID eventId, Integer capacity) {
}
