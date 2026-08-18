package com.eventsplatform.events.exception;

public class ForbiddenEventActionException extends RuntimeException {

    public ForbiddenEventActionException(String message) {
        super(message);
    }
}
