package com.ifba.sipapi.agenda.api.service;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.domain.AvailableTime;
import com.ifba.sipapi.agenda.dto.AgendaRequestDto;
import com.ifba.sipapi.agenda.repository.AvailableDayRepository;
import com.ifba.sipapi.agenda.repository.AvailableTimeRepository;
import com.ifba.sipapi.config.handler.APIException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
@Service
public class AgendaApplicationService implements AgendaService {

    private final AvailableDayRepository availableDayRepository;
    private final AvailableTimeRepository availableTimeRepository;

    @Transactional
    @Override
    public void receiveFromRequest(AgendaRequestDto agendaRequestDto) {
        log.info("[start] AgendaApplicationService - receiveFromRequest");
        verifyIfRequestIsEditOrCreation(agendaRequestDto);
        log.debug("[end] AgendaApplicationService - receiveFromRequest");
    }

    private void verifyIfRequestIsEditOrCreation(AgendaRequestDto agendaRequestDto) {

        if (agendaRequestDto.getId() != null){
            editAgenda(agendaRequestDto);
            return;
        }

        createAgenda(agendaRequestDto);
    }

    private void editAgenda(AgendaRequestDto agendaRequestDto) {
        log.info("[start] AgendaApplicationService - editAgenda");


        log.debug("[end] AgendaApplicationService - editAgenda");
    }

    private void createAgenda(AgendaRequestDto agendaRequestDto) {
        log.info("[start] AgendaApplicationService - createAgenda");
        agendaRequestDto.getAvailableDays().forEach(day ->{
            if(day != null && !availableDayRepository.existsAvailableDaysByAvailableDays(day))
                availableDayRepository.save(new AvailableDay(day));
        });

        agendaRequestDto.getAvailableTimeSlots().forEach(this::handleTimeManagement);
        log.debug("[end] AgendaApplicationService - createAgenda");
    }

    private void handleTimeManagement(AgendaRequestDto.AvailableTimeSlotRequest time) {
        log.info("[start] AgendaApplicationService - verifyIntervalTime");
        List<AvailableTime> timeRepositoryAll = availableTimeRepository.findAll();

        if(timeRepositoryAll.isEmpty()) {
            includeTimeToDay(availableTimeRepository.save(new AvailableTime(time)));
            return;
        }

        timeRepositoryAll.forEach(availableTime -> {
            if(!time.getStartTime().isBefore(availableTime.getEndTime()) || !time.getEndTime().isAfter(availableTime.getStartTime()))
                includeTimeToDay(availableTimeRepository.save(new AvailableTime(time)));
            else
                throw APIException.build(HttpStatus.BAD_REQUEST, "Horarios conflitantes, verifique a requisição e tente novamente");
        });

        log.debug("[end] AgendaApplicationService - verifyIntervalTime");
    }

    private void includeTimeToDay(AvailableTime time) {
        log.info("[start] AgendaApplicationService - includeTimeToDay");

        List<AvailableDay> availableDays = availableDayRepository.findAll();
        availableDays.forEach(days -> {
            if(days == null)
                throw  APIException.build(HttpStatus.BAD_REQUEST, "Horario não pode ser nulo");
            days.getAvailableTimeList().add(time);
        });

        availableDayRepository.saveAll(availableDays);
        log.debug("[end] AgendaApplicationService - includeTimeToDay");
    }

}
