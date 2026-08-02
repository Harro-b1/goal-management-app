package com.harro.goaltracker.util;

public class StringUtil {
    private StringUtil(){}

    public static String snakeToCamel(String input) {

        if (input == null || input.indexOf('_') == -1) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean upperNext = false;

        for (char c : input.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                result.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }

        return result.toString();
    }
}
