package com.ifba.sipapi.agenda.domain;

import com.ifba.sipapi.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "agenda")
@NoArgsConstructor
@AllArgsConstructor
public class Agenda extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private List<LocalDateTime> busyTime;
    private List<Integer> availableDays;
    private LocalTime startTime;
    private LocalTime endTime;

}
