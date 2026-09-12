package com.eventsplatform.events.service;

import com.eventsplatform.events.config.InventoryClient;
import com.eventsplatform.events.dto.CreateInventoryRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySyncService {


    private final InventoryClient inventoryClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public Mono<Boolean> tryCreateInventory(UUID eventId, Integer capacity) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("inventory-service");
        Retry retry = retryRegistry.retry("inventory-service");

        return Mono.fromRunnable(() ->
                        inventoryClient.createInventory(new CreateInventoryRequest(eventId, capacity)))
                .subscribeOn(Schedulers.boundedElastic())
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .thenReturn(true)
                .onErrorResume(ex -> {
                    log.error("Fallo al sincronizar inventario para eventId={}", eventId, ex);
                    return Mono.just(false);
                });
    }
}
