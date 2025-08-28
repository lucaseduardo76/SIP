package com.ifba.sipapi.item.api.service;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class ItemApplicationService implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        log.info("[start] ItemApplicationService - createItem");
        String itemCode = generateItemCode(itemRequestDto);
        Item item = new Item(itemRequestDto, itemCode);

        log.debug("[finish] ItemApplicationService - createItem");
        return new ItemResponseDto(itemRepository.save(item));
    }

    private String generateItemCode(ItemRequestDto itemRequestDto) {
        Category category = itemRequestDto.getCategory();

        String prefix = category.name().length() > 4 ? category.name().substring(0, 4) : category.name();
        List<String> codes = itemRepository.findItemCodesByCategory(category);

        int maxNumber = codes.stream().map(code -> code.replace(prefix + "-", ""))
                .mapToInt(numStr -> {
                    try {
                        return Integer.parseInt(numStr);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(999);

        int nextNumber = maxNumber + 1;

        return prefix + "-" + nextNumber;
    }

    @Override
    public List<String> uploadImages(UUID itemId, List<MultipartFile> itemImages) {
        return List.of();
    }
}
