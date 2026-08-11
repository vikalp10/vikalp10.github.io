package com.vikalp.portfolio.model;

/**
 * A headline on the news wire.
 *
 * @param year     two-digit year the story broke, e.g. {@code 25}
 * @param tag      wire desk label used for colour, e.g. {@code BREAKING}
 * @param headline the story, as it prints on the tape
 */
public record NewsItem(String year, String tag, String headline) {

    public boolean breaking() {
        return "BREAKING".equalsIgnoreCase(tag);
    }
}
