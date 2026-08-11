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
 * @param thesis    why the position exists — the long-form description
 * @param tech      stack, rendered as chips
 * @param link      repository / demo URL
 */
public record Holding(
        String ticker,
        String direction,
        String tag,
        String name,
        String period,
        String thesis,
        List<String> tech,
        String link
) {
}
