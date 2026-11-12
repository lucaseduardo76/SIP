package com.ifba.sipapi.item.api.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rotina diária que apaga as fotografias dos itens já entregues ou com prazo de guarda encerrado,
 * depois do número de dias configurado (application.time.pictures-retention).
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class ItemPictureCleanupSchedule {
    private final ItemService itemService;

    @Scheduled(cron = "0 20 0 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void removeExpiredPictures() {
        log.info("[start] ItemPictureCleanupSchedule - removeExpiredPictures");
        itemService.removeExpiredPictures();
        log.debug("[finish] ItemPictureCleanupSchedule - removeExpiredPictures");
    }
}
