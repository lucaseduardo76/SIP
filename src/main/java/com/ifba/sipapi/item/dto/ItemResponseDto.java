package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.item.*;
import com.ifba.sipapi.user.dto.UserDetailsResponseDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ItemResponseDto {
    @Schema(example = "ff2ce525-98f5-418e-84b0-b562ccbc4dba")
    private UUID id;

    @Schema(example = "Descrição do item perdido")
    private String description;

    @Schema(example = "29-04-2025")
    private LocalDate findingAt;

    @Schema(example = "29-10-2025")
    private LocalDate donationDate;

    @Schema(example = "DISPONIBLE")
    private Status status;

    @Schema(example = "AFTERNOON")
    private DayPeriod dayPeriod;

    @Schema(example = "DOCUMENT")
    private Category category;

    @Schema(example = "RC")
    private Area area;

    @Schema(example = "29-04-2025")
    private LocalDate dateReturned;

    @Schema(example = "DOCU-1001")
    private String code;

    @ArraySchema(schema = @Schema(implementation = PictureResponseDto.class))
    private List<PictureResponseDto> pictures;

    @Schema(implementation = UserDetailsResponseDto.class)
    private UserDetailsResponseDto owner;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.description = item.getDescription();
        this.findingAt = item.getFindingAt();
        this.status = item.getStatus();
        this.dayPeriod = item.getDayPeriod();
        this.category = item.getCategory();
        this.area = item.getArea();
        this.dateReturned = item.getDateReturned();
        this.code = item.getCode();
        this.donationDate = item.getDonationDate();

        if (item.getPictures() != null)
            this.pictures = item.getPictures().stream()
                    .map(p -> new PictureResponseDto(p.getId(), p.getUrl()))
                    .toList();

        if (item.getOwner() != null)
            this.owner = new UserDetailsResponseDto(item.getOwner());
    }

}
