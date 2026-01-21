package com.ifba.sipapi.agenda.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum DayOfWeekEnum {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;


    public static String nextAvailableDay(LocalDateTime dateTime, List<AvailableDay> allAvailableDay) {
        if (allAvailableDay == null || allAvailableDay.isEmpty())
            return "Nenhum dia disponivel";

        DayOfWeekEnum currentDay = DayOfWeekEnum.valueOf(dateTime.getDayOfWeek().name());
        Set<DayOfWeekEnum> availableDays = allAvailableDay.stream().map(AvailableDay::getAvailableDay).collect(Collectors.toSet());
        DayOfWeekEnum nextDay = currentDay;

        for (int i = 0; i < 7; i++) {
            nextDay = DayOfWeekEnum.values()[(nextDay.ordinal() + 1) % DayOfWeekEnum.values().length];

            if (availableDays.contains(nextDay)) {
                return availableToString(nextDay);
            }
        }

        return "";
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

}

