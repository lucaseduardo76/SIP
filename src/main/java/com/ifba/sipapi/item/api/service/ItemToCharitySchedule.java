package com.ifba.sipapi.item.api.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class ItemToCharitySchedule {
    private final ItemService itemService;

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void refreshItemToCharity() {
        log.info("[start] ItemToCharitySchedule - refreshItemToCharity");
        itemService.refreshItemToCharity();
        log.debug("[finish] ItemToCharitySchedule - refreshItemToCharity");
    }

}
