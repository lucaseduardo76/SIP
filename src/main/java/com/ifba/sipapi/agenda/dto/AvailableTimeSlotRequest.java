package com.ifba.sipapi.agenda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Intervalo de tempo disponível")
public class AvailableTimeSlotRequest {

    @NotNull(message = "A hora de início é obrigatória")
    @Schema(description = "Hora de início do intervalo no formato HH:mm", example = "08:00")
    private LocalTime startTime;

    @NotNull(message = "A hora de término é obrigatória")
    @Schema(description = "Hora de término do intervalo no formato HH:mm", example = "12:00")
    private LocalTime endTime;
}
