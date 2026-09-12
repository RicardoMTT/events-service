package com.eventsplatform.events.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 200, message = "El titulo no puede superar los 200 caracteres")
    private String title;

    @Size(max = 10, message = "La descripcion no puede superar los 10 caracteres")
    private String description;

    @NotBlank(message = "La categoria es obligatoria")
    private String category;

    @NotBlank(message = "La ubicacion es obligatoria")
    private String location;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser en el futuro")
    private OffsetDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private OffsetDateTime endDate;

    private String imageUrl;

    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacity;
}
