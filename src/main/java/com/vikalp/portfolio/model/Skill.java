package com.vikalp.portfolio.model;

import java.util.List;

/**
 * One side of the order book. BID = what I build with day to day,
 * ASK = what I reach for around it.
 *
 * <p>Book depth in the UI is derived from an item's index in {@link #items()} —
 * it is order-book decoration, never a proficiency score.
 *
 * @param side     BID or ASK
 * @param category human label for the group, e.g. {@code Systems & Backend}
 * @param items    the levels, best-priced first
 */
public record Skill(String side, String category, List<String> items) {

    public boolean bid() {
        return "BID".equalsIgnoreCase(side);
    }
}
