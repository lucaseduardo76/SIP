package com.ifba.sipapi.agenda.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ifba.sipapi.agenda.dto.AvailableTimeSlotRequest;
import com.ifba.sipapi.config.handler.APIException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "availableTime")
public class AvailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @JsonIgnore
    @ManyToMany(mappedBy = "availableTimeList")
    private List<AvailableDay> availableDays;

    public AvailableTime(AvailableTimeSlotRequest time) {
        if(time == null || !time.getStartTime().isBefore(time.getEndTime()))
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Horário não pode ser nulo, e horario inicial não pode ser posterior a horario final");

        this.startTime = time.getStartTime();
        this.endTime = time.getEndTime();

    }
}

