package com.ifba.sipapi.util;

public class HandleString {

    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lower = input.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static String toLowerCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.toLowerCase();
    }
}
