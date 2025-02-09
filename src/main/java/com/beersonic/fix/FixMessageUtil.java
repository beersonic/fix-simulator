package com.beersonic.fix;

public class FixMessageUtil {
    private static final String PIPE = "\\|";
    private static final String SOH = "\u0001";

    public static String convertPipeToSoh(String fixMessage) {
        return fixMessage.replaceAll(PIPE, SOH);
    }
}
