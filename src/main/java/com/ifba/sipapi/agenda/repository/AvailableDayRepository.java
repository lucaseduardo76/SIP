package com.ifba.sipapi.agenda.repository;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AvailableDayRepository extends JpaRepository<AvailableDay, UUID> {

    Optional<AvailableDay> findAllByAvailableDay(DayOfWeekEnum availableDay);
    void deleteByAvailableDay(DayOfWeekEnum dayOfWeek);
}
