package org.cybersaarthi.analytics.util;

public final class SmallCellSuppressionHelper {

    public static final int THRESHOLD = 5;

    private SmallCellSuppressionHelper() {}

    /**
     * Applies k-anonymity small-cell suppression (k=5).
     * Returns "<5" if count is between 1 and 4 inclusive, otherwise the string representation of count.
     */
    public static String maskSmallCell(long count) {
        if (count > 0 && count < THRESHOLD) {
            return "<5";
        }
        return String.valueOf(count);
    }
}
