package com.ifba.sipapi.agenda.dto;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.domain.AvailableTime;
import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class AvailableDayResponse {

    private final UUID id;
    private final DayOfWeekEnum availableDay;
    private final List<AvailableTime> availableTimeList;


    public AvailableDayResponse(AvailableDay availableDay) {
        this.id = availableDay.getId();
        this.availableDay = availableDay.getAvailableDay();
        this.availableTimeList = availableDay.getAvailableTimeList();
    }
}
