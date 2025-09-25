package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ItemRequestReviewDto {
    @Schema(example = "ff2ce525-98f5-418e-84b0-b562ccbc4dba")
    @NotNull(message = "O idRecovery não pode ser nulo")
    private UUID idRecovery;

    @Schema(example = "APPROVED")
    @NotNull(message = "O statusRecovery não pode ser nulo")
    private StatusRecovery statusRecovery;

}
