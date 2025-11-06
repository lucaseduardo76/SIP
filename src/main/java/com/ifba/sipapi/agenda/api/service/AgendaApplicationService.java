package com.ifba.sipapi.agenda.api.service;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.dto.AgendaEditRequestDto;
import com.ifba.sipapi.agenda.repository.AvailableDayRepository;
import com.ifba.sipapi.agenda.repository.AvailableTimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
public class AgendaApplicationService implements AgendaService {

    private final AvailableDayRepository availableDayRepository;
    private final AvailableTimeRepository availableTimeRepository;

    @Transactional
    @Override
    public void createOrEditAgenda(AgendaEditRequestDto agendaEditRequestDto) {
        log.info("[start] AgendaApplicationService - editAgenda");

        agendaEditRequestDto.getAvailableDayTimeSlotRequest().forEach(availableTimeSlot ->{
            AvailableDay availableDays = availableDayRepository.findAllByAvailableDays(availableTimeSlot.getAvailableDay())
                    .orElse(new AvailableDay(availableTimeSlot.getAvailableDay()));

            availableDays.changeTimeList(availableTimeSlot.getAvailableTimeSlotRequest());
            availableDayRepository.save(availableDays);
        });

        cleanupOrphanTimes();
        log.debug("[finish] AgendaApplicationService - editAgenda");
    }

    private void cleanupOrphanTimes() {
        availableTimeRepository.findAll().forEach(time -> {
            if (time.getAvailableDays() == null || time.getAvailableDays().isEmpty()) {
                availableTimeRepository.delete(time);
            }
        });
    }



}
