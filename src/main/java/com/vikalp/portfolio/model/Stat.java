package com.vikalp.portfolio.model;

/**
 * A headline metric, shaped so the client can count it up on scroll.
 *
 * @param label    caption under the number
 * @param prefix   rendered before the number, e.g. {@code $}
 * @param value    the number itself — the count-up target
 * @param decimals digits to keep while counting (0 for integers)
 * @param suffix   rendered after the number, e.g. {@code K+} or {@code %}
 * @param note     small print / provenance
 */
public record Stat(
        String label,
        String prefix,
        double value,
        int decimals,
        String suffix,
        String note
) {
    /** Fully formatted value, e.g. {@code $120K} — used for the ticker tape and no-JS fallback. */
    public String display() {
        String number = decimals == 0
                ? String.valueOf((long) value)
                : String.format("%." + decimals + "f", value);
        return prefix + number + suffix;
    }
}
