package org.cybersaarthi.analytics.util;

public final class CsvSanitizer {

    private CsvSanitizer() {}

    /**
     * Sanitizes a cell string against CSV formula injection (OWASP CSV Injection defense)
     * and wraps in quotes according to RFC 4180 if needed.
     */
    public static String escapeCsvCell(String value) {
        if (value == null) {
            return "";
        }

        String str = value.trim();

        // Formula injection defense: If starting with =, +, -, @, \t, or \r, prepend single quote
        if (!str.isEmpty()) {
            char firstChar = str.charAt(0);
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
                str = "'" + str;
            }
        }

        // If the value contains commas, double quotes, or newlines, escape internal quotes and enclose in quotes
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = "\"" + str.replace("\"", "\"\"") + "\"";
        }

        return str;
    }
}
