package com.vikalp.portfolio.model;

import java.util.List;

/**
 * A position in the portfolio — i.e. a project.
 *
 * @param ticker    short symbol, e.g. {@code KITE}
 * @param direction ▲ or ▼ marker for the position
 * @param tag       strategy label, e.g. {@code QUANT}
 * @param name      full project name
 * @param period    holding period, e.g. {@code 2025–Present}
 * @param metrics   headline numbers for the position, printed as a strip above the
 *                  thesis. Every one restates a figure the thesis already makes —
 *                  this is a tear sheet, not a second set of claims
 * @param thesis    why the position exists — the long-form description
 * @param tech       stack, rendered as chips
 * @param link       repository / demo URL
 * @param screenshot capture of the thing running, relative to the site root; {@code null}
 *                   renders no frame at all. A path that 404s degrades to a "capture
 *                   pending" placeholder rather than a broken-image icon — see
 *                   {@code wireHoldingShots()} in terminal.js
 * @param shotAlt    alt text for {@link #screenshot()}
 */
public record Holding(
        String ticker,
        String direction,
        String tag,
        String name,
        String period,
        List<Stat> metrics,
        String thesis,
        List<String> tech,
        String link,
        String screenshot,
        String shotAlt
) {
}
