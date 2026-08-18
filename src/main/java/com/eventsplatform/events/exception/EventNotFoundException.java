package com.eventsplatform.events.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(UUID id) {
        super("No se encontro el evento con id " + id);
    }
}
