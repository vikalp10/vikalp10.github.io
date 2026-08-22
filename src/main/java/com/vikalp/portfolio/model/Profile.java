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
        String linkedin,
        String email,
        String resume,
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

    /** {@code linkedin.com/in/vikalp-shandilya-103525198} — display form of {@link #linkedin()}. */
    public String linkedinHandle() {
        return displayHandle(linkedin);
    }

    /** {@code github.com/vikalp10} — display form of {@link #github()}. */
    public String githubHandle() {
        return displayHandle(github);
    }

    /**
     * Strips the scheme and {@code www.} so a URL can be printed as a handle.
     *
     * <p>Derived rather than written out again in the template: the panel [6] contact grid
     * used to carry its own hardcoded copy of each handle, which is precisely how it came
     * to advertise a LinkedIn URL that no longer matched the one it linked to.
     */
    private static String displayHandle(String url) {
        return url.replaceFirst("^https?://(www\\.)?", "").replaceFirst("/$", "");
    }
}
