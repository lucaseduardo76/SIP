package com.ifba.sipapi.agenda.dto;

import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para criação/atualização de uma agenda")
public class AgendaRequestDto {

    @Schema(description = "Identificador único da agenda (opcional para criação, usado em updates)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @NotEmpty(message = "A lista de dias disponíveis não pode estar vazia")
    @Schema(description = "Dias da semana disponíveis para a agenda", example = "[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\"]")
    private List<@NotNull(message = "Cada dia da semana deve ser informado") DayOfWeekEnum> availableDays;

    @NotEmpty(message = "A lista de intervalos não pode estar vazia")
    @Valid
    @Schema(description = "Intervalos de tempo disponíveis para cada dia", example = "[{\"startTime\":\"08:00\",\"endTime\":\"12:00\"}]")
    private List<@NotNull(message = "Cada intervalo de tempo deve ser informado") AvailableTimeSlotRequest> availableTimeSlots;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Intervalo de tempo disponível")
    public static class AvailableTimeSlotRequest {
        @NotNull(message = "A hora de início é obrigatória")
        @Schema(description = "Hora de início do intervalo no formato HH:mm", example = "08:00")
        private LocalTime startTime;

        @NotNull(message = "A hora de término é obrigatória")
        @Schema(description = "Hora de término do intervalo no formato HH:mm", example = "12:00")
        private LocalTime endTime;
    }
}
