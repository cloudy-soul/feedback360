package com.feedback.feedback360.util;

/**
 * Single source of truth for how person names are displayed across the app:
 * "LASTNAME Firstname" — last name upper-cased, first name capitalized.
 */
public final class NameFormatter {

    private NameFormatter() {}

    public static String display(String firstName, String lastName) {
        String last = capitalizeFirst(lastName).toUpperCase();
        String first = capitalizeFirst(firstName);
        return (last + " " + first).trim();
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isBlank()) return "";
        String lower = s.trim().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
