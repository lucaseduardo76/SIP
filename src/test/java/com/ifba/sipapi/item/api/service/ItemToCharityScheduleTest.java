package com.ifba.sipapi.item.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemToCharitySchedule")
class ItemToCharityScheduleTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemToCharitySchedule schedule;

    @Test
    @DisplayName("refreshItemToCharity delega a chamada ao ItemService")
    void refreshItemToCharity_delegatesToItemService() {
        schedule.refreshItemToCharity();

        verify(itemService).refreshItemToCharity();
    }
}
