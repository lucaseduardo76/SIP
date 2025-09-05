package com.ifba.sipapi.util;

public class ItemHelper {
    public static String getOrDefault(String newValue, String currentValue) {
        return (newValue != null && !newValue.isBlank()) ? newValue : currentValue;
    }
}
