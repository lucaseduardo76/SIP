package com.ifba.sipapi.util;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.dto.ItemRequestDto;

import java.util.List;

public class GenerateItemCode {
    public static String generateItemCode(ItemRequestDto itemRequestDto, List<String> itemCodes) {
        Category category = itemRequestDto.getCategory();

        String prefix = category.name().length() > 4 ? category.name().substring(0, 4) : category.name();

        int maxNumber = itemCodes.stream().map(code -> code.replace(prefix + "-", ""))
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
}
