package com.ifba.sipapi.agenda.domain;

import com.ifba.sipapi.agenda.dto.AvailableTimeSlotRequest;
import com.ifba.sipapi.config.handler.APIException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "availableDay")
public class AvailableDay {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeekEnum availableDay;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "available_day_time",
            joinColumns = @JoinColumn(name = "available_day_id"),
            inverseJoinColumns = @JoinColumn(name = "available_time_id")
    )
    private List<AvailableTime> availableTimeList;

    public AvailableDay(DayOfWeekEnum day) {
        if (day != null) {
            this.availableDay = day;
            availableTimeList = new ArrayList<>();
        }
    }

    public void changeTimeList(List<AvailableTimeSlotRequest> availableTimeSlotRequest) {
        this.availableTimeList.clear();
        availableTimeSlotRequest.forEach(this::handleTimeAssert);
    }

    private void handleTimeAssert(AvailableTimeSlotRequest availableTimeSlotRequest) {
        if (availableTimeList.isEmpty()) {
            availableTimeList.add(new AvailableTime(availableTimeSlotRequest));
            return;
        }

        boolean areThereConflicts = availableTimeList.stream().anyMatch(availableTime ->
                availableTimeSlotRequest.getStartTime().isBefore(availableTime.getEndTime()) &&
                        availableTimeSlotRequest.getEndTime().isAfter(availableTime.getStartTime())
        );

        if (areThereConflicts) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Horários conflitantes, verifique a requisição e tente novamente");
        }

        availableTimeList.add(new AvailableTime(availableTimeSlotRequest));
    }

}

