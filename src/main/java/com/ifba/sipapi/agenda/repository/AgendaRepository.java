package com.ifba.sipapi.agenda.repository;

import com.ifba.sipapi.agenda.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgendaRepository extends JpaRepository<Agenda, UUID> {
}
