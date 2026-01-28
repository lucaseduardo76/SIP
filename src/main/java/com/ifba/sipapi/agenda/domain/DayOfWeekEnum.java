package com.ifba.sipapi.agenda.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum DayOfWeekEnum {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;


    public static String nextAvailableDay(LocalDateTime dateTime, List<AvailableDay> allAvailableDay) {
        if (allAvailableDay == null || allAvailableDay.isEmpty()) {
            return "Nenhum dia disponível";
        }

        Set<DayOfWeekEnum> availableDays = allAvailableDay.stream()
                .map(AvailableDay::getAvailableDay)
                .collect(Collectors.toSet());

        LocalDateTime nextDate = dateTime;

        for (int i = 0; i < 7; i++) {
            nextDate = nextDate.plusDays(1);

            DayOfWeekEnum nextDay = DayOfWeekEnum.valueOf(nextDate.getDayOfWeek().name());

            if (availableDays.contains(nextDay))
                return availableToString(nextDay) + " " + formatDate(nextDate);
        }

        return "Nenhum dia disponível";
    }
    private static String availableToString(DayOfWeekEnum nextDay) {
        if (nextDay == null) {
            return "";
        }

        switch (nextDay) {
            case MONDAY:
                return "Segunda-feira";
            case TUESDAY:
                return "Terça-feira";
            case WEDNESDAY:
                return "Quarta-feira";
            case THURSDAY:
                return "Quinta-feira";
            case FRIDAY:
                return "Sexta-feira";
            case SATURDAY:
                return "Sábado";
            case SUNDAY:
                return "Domingo";
            default:
                return "";
        }
    }

    private static String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

}

