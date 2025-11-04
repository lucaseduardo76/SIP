package com.ifba.sipapi.agenda.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private DayOfWeekEnum availableDays;

    @ManyToMany
    @JoinTable(
            name = "available_day_time",
            joinColumns = @JoinColumn(name = "available_day_id"),
            inverseJoinColumns = @JoinColumn(name = "available_time_id")
    )
    private List<AvailableTime> availableTimeList;

    public AvailableDay(DayOfWeekEnum day) {
        if (day != null) {
            this.availableDays = day;
            availableTimeList = new ArrayList<>();
        }
    }
}

