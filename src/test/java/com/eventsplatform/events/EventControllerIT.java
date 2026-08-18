package com.eventsplatform.events;

import com.eventsplatform.events.domain.EventStatus;
import com.eventsplatform.events.dto.EventRequest;
import com.eventsplatform.events.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("events_db")
            .withUsername("events_user")
            .withPassword("events_pass");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":"
                + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }


    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createAndPublishEvent_happyPath() {
        UUID organizerId = UUID.randomUUID();

        EventRequest request = new EventRequest();
        request.setTitle("Concierto de rock");
        request.setDescription("Una noche de rock en vivo");
        request.setCategory("music");
        request.setLocation("Estadio Nacional, Lima");
        request.setStartDate(OffsetDateTime.now().plusDays(30));
        request.setEndDate(OffsetDateTime.now().plusDays(30).plusHours(3));

        EventResponse created = webTestClient.post().uri("/events")
                .header("X-User-Id", organizerId.toString())
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EventResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(EventStatus.DRAFT);

        webTestClient.patch().uri("/events/{id}/publish", created.getId())
                .header("X-User-Id", organizerId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PUBLISHED");

        webTestClient.get().uri("/events?category=music")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EventResponse.class)
                .value(list -> assertThat(list).extracting(EventResponse::getId).contains(created.getId()));
    }

    @Test
    void publish_byNonOwner_isForbidden() {
        UUID organizerId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();

        EventRequest request = new EventRequest();
        request.setTitle("Charla tech");
        request.setCategory("tech");
        request.setLocation("Online");
        request.setStartDate(OffsetDateTime.now().plusDays(10));
        request.setEndDate(OffsetDateTime.now().plusDays(10).plusHours(1));

        EventResponse created = webTestClient.post().uri("/events")
                .header("X-User-Id", organizerId.toString())
                .bodyValue(request)
                .exchange()
                .expectBody(EventResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.patch().uri("/events/{id}/publish", created.getId())
                .header("X-User-Id", otherUser.toString())
                .exchange()
                .expectStatus().isForbidden();
    }
}
