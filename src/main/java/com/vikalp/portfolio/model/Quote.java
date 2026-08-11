package com.vikalp.portfolio.model;

import java.util.List;

/**
 * The VKLP quote.
 *
 * <p>These are index levels for the career conceit, not resume claims. Every level is
 * anchored to a real number from the profile so nothing here is arbitrary — see
 * {@code PortfolioData#quote()} for the mapping. The only genuinely live thing is the
 * tick, and on GitHub Pages that is a client-side random walk seeded from {@link #seed()}.
 *
 * @param symbol       VKLP
 * @param name         long name of the security
 * @param last         last traded level
 * @param prevClose    previous close, drives change / changePercent
 * @param open         session open
 * @param dayHigh      session high
 * @param dayLow       session low
 * @param allTimeHigh  record level — VKLP prints at ATH
 * @param yearLow      52-week low
 * @param volume       shares done today
 * @param currency     quote currency
 * @param session      OPEN / CLOSED
 * @param exchange     listing venue
 * @param seed         seed for the client-side random walk, so the tape is deterministic per load
 * @param drift        per-tick upward drift applied by the walk
 * @param volatility   per-tick noise applied by the walk
 * @param stats        headline metrics shown as cards and on the ticker tape
 */
public record Quote(
        String symbol,
        String name,
        double last,
        double prevClose,
        double open,
        double dayHigh,
        double dayLow,
        double allTimeHigh,
        double yearLow,
        long volume,
        String currency,
        String session,
        String exchange,
        long seed,
        double drift,
        double volatility,
        List<Stat> stats
) {

    public double change() {
        return round(last - prevClose);
    }

    public double changePercent() {
        return round((last - prevClose) / prevClose * 100.0);
    }

    public boolean up() {
        return last >= prevClose;
    }

    /** True when the last print is the record print — VKLP is at an all-time high. */
    public boolean atAllTimeHigh() {
        return last >= allTimeHigh;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
