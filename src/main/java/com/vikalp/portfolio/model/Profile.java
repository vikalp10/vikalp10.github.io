package com.vikalp.portfolio.model;

import java.util.List;

/**
 * The security being traded. One instance, held by {@code PortfolioData}.
 */
public record Profile(
        String name,
        String ticker,
        String sector,
        String company,
        String role,
        String location,
        String listedSince,
        String ipo,
        String rating,
        int stars,
        String status,
        String github,
        String githubRepos,
        String linkedin,
        String email,
        String leetcode,
        List<String> certifications
) {
    /** {@code ★★★★★} for the header badge, built from {@link #stars()}. */
    public String starGlyphs() {
        return "★".repeat(stars) + "☆".repeat(Math.max(0, 5 - stars));
    }

    /** Pre-filled mailto: the "PLACE BUY ORDER" CTA points here. */
    public String buyOrderMailto() {
        return "mailto:" + email + "?subject=BUY%20ORDER%3A%20" + ticker;
    }
}
