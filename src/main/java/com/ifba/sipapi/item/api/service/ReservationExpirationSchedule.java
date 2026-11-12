package com.ifba.sipapi.item.api.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rotina diária que encerra as reservas de itens não retirados dentro do prazo configurado
 * (application.time.reservation-expiration), devolvendo os itens à situação disponível.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class ReservationExpirationSchedule {
    private final ItemService itemService;

    @Scheduled(cron = "0 10 0 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void expireReservations() {
        log.info("[start] ReservationExpirationSchedule - expireReservations");
        itemService.expireReservations();
        log.debug("[finish] ReservationExpirationSchedule - expireReservations");
    }
}
