package com.ifba.sipapi.util;

import java.util.Random;

public class GenerateNumber {
    public static String generateCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}