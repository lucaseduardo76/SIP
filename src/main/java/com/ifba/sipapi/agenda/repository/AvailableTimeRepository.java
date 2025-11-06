package com.ifba.sipapi.agenda.repository;

import com.ifba.sipapi.agenda.domain.AvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, UUID> {
}
