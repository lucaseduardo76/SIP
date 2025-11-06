package com.ifba.sipapi.agenda.dto;

import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para criação/atualização de uma agenda")
public class AgendaEditRequestDto {

    @NotEmpty(message = "A lista de intervalos não pode estar vazia")
    @Valid
    @Schema(description = "Intervalos de tempo disponíveis para cada dia", example = "[{\"startTime\":\"08:00\",\"endTime\":\"12:00\"}]")
    private List<@NotNull(message = "Cada intervalo de tempo deve ser informado") AvailableDayTimeSlotRequest> availableDayTimeSlotRequest;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Intervalo de tempo disponível")
    public static class AvailableDayTimeSlotRequest {

        @NotNull(message = "Dia disponível não pode ser nulo")
        private DayOfWeekEnum availableDay;

        @NotEmpty(message = "A lista de intervalos não pode estar vazia")
        @Valid
        @Schema(description = "Intervalos de tempo disponíveis para cada dia", example = "[{\"startTime\":\"08:00\",\"endTime\":\"12:00\"}]")
        private List<@NotNull(message = "Cada intervalo de tempo deve ser informado") AvailableTimeSlotRequest> availableTimeSlotRequest;
    }
}
