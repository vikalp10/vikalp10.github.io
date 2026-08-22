package com.vikalp.portfolio.data;

import com.vikalp.portfolio.model.Execution;
import com.vikalp.portfolio.model.Holding;
import com.vikalp.portfolio.model.NewsItem;
import com.vikalp.portfolio.model.Profile;
import com.vikalp.portfolio.model.Quote;
import com.vikalp.portfolio.model.Skill;
import com.vikalp.portfolio.model.Stat;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the whole site.
 *
 * <p>Everything the page shows — HTML panels, {@code /api/*} responses and the exported
 * {@code docs/data/*.json} files — is read from this bean. Change a number here and it
 * changes everywhere on the next build.
 */
@Component
public class PortfolioData {

    // ---------------------------------------------------------------- profile

    private final Profile profile = new Profile(
            "Vikalp Shandilya",
            "VKLP",
            "Software Engineering / Fintech Backend",
            "JP Morgan Chase & Co.",
            "Software Engineer II",
            "Bengaluru, India",
            "2019",
            "THAPAR",
            "STRONG BUY",
            5,
            "HIREABLE",
            "https://github.com/vikalp10",
            // The bare /in/vikalp-shandilya vanity URL is not claimed: LinkedIn silently
            // redirects it to the visitor's own feed. This is the one that resolves, and
            // it is the one on the resume.
            "https://www.linkedin.com/in/vikalp-shandilya-103525198",
            "vikalp09.shandilya@gmail.com",
            // Phone deliberately omitted: this repo is public, so anything here is
            // publicly crawlable and permanent in git history. Email reaches me fine.
            // Relative, so it resolves identically on the local preview and on Pages.
            // The file itself lives at src/main/resources/static/resume.pdf and is
            // copied into docs/ by StaticSiteExporter.
            "./resume.pdf",
            List.of(
                    "AWS Certified Cloud Practitioner (2023)",
                    "Best Performing Intern — Top 5% of cohort",
                    "500+ LeetCode / GFG problems (250+ medium & hard)"
            )
    );

    // ------------------------------------------------------------------ quote

    /**
     * VKLP index levels. Each one is anchored to a real number from the resume so the
     * conceit stays honest — these are not claimed metrics:
     * <ul>
     *   <li>{@code last}/{@code allTimeHigh} 120.00 → $120K annual savings</li>
     *   <li>{@code change} +8.30 → CGPA 8.3/10 (so prevClose is 111.70)</li>
     *   <li>{@code yearLow} 40.00 → 40% faster queries</li>
     *   <li>{@code volume} 500,000 → 500K+ records processed per day</li>
     * </ul>
     * {@code seed}, {@code drift} and {@code volatility} configure the client-side random
     * walk that ticks the price on GitHub Pages, where there is no server to quote from.
     */
    private final Quote quote = new Quote(
            "VKLP",
            "Vikalp Shandilya · Backend & Platform Engineering",
            120.00,
            111.70,
            112.00,
            120.00,
            111.70,
            120.00,
            40.00,
            500_000L,
            "USD",
            "OPEN",
            "GTX · BENGALURU",
            120_500L,
            0.00042,
            0.0021,
            statCards()
    );

    /**
     * The headline metrics, as count-up cards on panel [1].
     *
     * <p>A before→after metric is encoded with the "before" leg in {@code prefix} and the
     * improved figure as {@code value}, so the card still counts up and lands on the number
     * that matters — {@code "3D → " + 6 + "H"} prints {@code 3D → 6H}.
     */
    private static List<Stat> statCards() {
        return List.of(
                new Stat("ANNUAL SAVINGS", "$", 120, 0, "K", "MySQL→PostgreSQL migration"),
                new Stat("RECORDS / DAY", "", 500, 0, "K+", "ETL framework, 15+ vendors"),
                new Stat("PIPELINE UPTIME", "", 99.9, 1, "%", "AWS Step Functions SLA"),
                new Stat("FASTER QUERIES", "", 40, 0, "%", "post-migration"),
                new Stat("FASTER ONBOARDING", "", 85, 0, "%", "vendor feeds: 7 days → 1 day"),
                new Stat("VULNS REMEDIATED", "", 120, 0, "+", "22 repositories"),
                new Stat("LOWER API LATENCY", "", 35, 0, "%", "Kafka event-driven redesign"),
                new Stat("FEEDS ONBOARDED", "", 200, 0, "+", "self-service onboarding platform"),
                new Stat("FEED ONBOARDING", "3D → ", 6, 0, "H", "3 days → under 6 hours"),
                new Stat("ON-CALL TRIAGE", "2H → ", 15, 0, "M", "contract-based monitoring layer"),
                new Stat("ENGINEERS MENTORED", "", 2, 0, "", "1 intern · 1 experienced joiner")
        );
    }

    /** Headline metrics plus supporting numbers, for the scrolling tape in the header. */
    private final List<Stat> tape = List.of(
            new Stat("ANNUAL SAVINGS", "$", 120, 0, "K", ""),
            new Stat("RECORDS/DAY", "", 500, 0, "K+", ""),
            new Stat("UPTIME", "", 99.9, 1, "%", ""),
            new Stat("FASTER QUERIES", "", 40, 0, "%", ""),
            new Stat("FEEDS ONBOARDED", "", 200, 0, "+", ""),
            new Stat("FEED ONBOARDING", "3D → ", 6, 0, "H", ""),
            new Stat("ON-CALL TRIAGE", "2H → ", 15, 0, "M", ""),
            new Stat("FASTER ONBOARDING", "", 85, 0, "%", ""),
            new Stat("VULNS FIXED", "", 120, 0, "+", ""),
            // ▼ rather than -, so a reduction reads as a win on a tape where green is good.
            new Stat("API LATENCY", "▼", 35, 0, "%", ""),
            new Stat("KAFKA EVENTS/DAY", "", 50, 0, "K+", ""),
            new Stat("TABLES MIGRATED", "", 50, 0, "+", ""),
            new Stat("REPOS UPGRADED", "", 22, 0, "", ""),
            new Stat("SERVICES ON JAVA 17", "", 12, 0, "", ""),
            new Stat("PROD INCIDENTS CLOSED", "", 15, 0, "+", ""),
            new Stat("ENGINEERS MENTORED", "", 2, 0, "", ""),
            new Stat("TEST COVERAGE", "", 85, 0, "%", ""),
            new Stat("CGPA", "", 8.3, 1, "/10", "")
    );

    // --------------------------------------------------------- trade blotter

    private final List<Execution> blotter = List.of(
            new Execution(
                    "JAN'26—NOW", "BUY", "JP MORGAN CHASE", "Software Engineer II",
                    List.of(
                            "Self-service feed-onboarding platform: pulls the spec straight from a Jira ticket, generates the Java entity, creates the DB table and closes the ticket — incomplete requests are auto-returned to the requester with the exact missing fields. Feed onboarding cut from 3 days to under 6 hours across 200+ feeds",
                            "Contract-based monitoring layer covering 20 feeds for 4 major trading consumers — scans registered datasets, parses failure logs, surfaces root cause with a recommended action, and escalates SLA-breach risk to consumers. On-call triage cut from 2 hours to 15 minutes",
                            "Agentic tooling in production: an MCP server (JPMC hackathon) where LLM agents vet datasets against configurable data-quality templates, adopted by the Data Quality team to replace manual review — plus a self-improving agent (3 skills, Moderne CLI) driving Spring Boot 2.x→4.x migration, remediating 120+ vulnerabilities across 22 repositories",
                            "Weekly on-call rotation for the platform; onboarded and mentored 2 engineers — an intern and an experienced joiner — on the team's codebase, delivery process and AWS infrastructure"
                    ),
                    "3 DAYS → 6 HRS", "UP"
            ),
            new Execution(
                    "JUL'23—DEC'25", "BUY", "JP MORGAN CHASE", "Software Engineer",
                    List.of(
                            "Zero-downtime MySQL→PostgreSQL migration: 50+ tables, 2TB+, 5 teams, dual-write plus shadow-read — $120K/yr saved, 40% faster queries, adopted as the org-wide playbook",
                            "Python/Java ETL framework at 500K+ records/day (8GB, peaking at 200 rec/sec) across 15+ vendors — onboarding cut from 7 days to 1 (85%), serving 3 teams on AWS Step Functions (Lambda / Glue / S3) at a 99.9% SLA",
                            "Event-driven Spring Boot + Kafka backbone — 8 topics, 3 consumer groups, 50K+ events/day, 35% lower API latency",
                            "Multi-account AWS provisioned with Terraform, plus Spinnaker blue-green CI/CD adopted across 4 teams",
                            "15+ production incidents resolved; the runbooks that came out of them cut recurrence 50%"
                    ),
                    "+$120K SAVED", "UP"
            ),
            new Execution(
                    "JAN'23—JUL'23", "BUY", "JP MORGAN CHASE", "SEP Intern",
                    List.of(
                            "Led the Java 8→17 migration across 12 microservices — 30% less boilerplate, 25% faster startup, and 3 deprecated security libraries flagged in compliance removed",
                            "Python automation to reconcile vendor file backlogs — 80% less manual reconciliation"
                    ),
                    // ▼ not −: these are reductions, and a minus sign in a P&L column
                    // reads as a loss no matter what colour it is printed in.
                    "▼25% STARTUP TIME", "UP"
            ),
            new Execution(
                    "JUN'22—JUL'22", "BUY", "JP MORGAN CHASE", "Summer Analyst Intern",
                    List.of(
                            "Full-stack maker-checker application (Spring Boot + Angular) with a dual-authorization workflow, REST APIs and Swagger documentation",
                            "Integration time down 50%, 85% test coverage"
                    ),
                    "▼50% INTEGRATION TIME", "UP"
            ),
            new Execution(
                    "2019—2023", "IPO", "THAPAR INSTITUTE", "BE Computer Science",
                    List.of(
                            "CGPA 8.3/10",
                            "Merit Scholarship — top 10% of cohort"
                    ),
                    "GPA 8.3", "FLAT"
            )
    );

    // ------------------------------------------------------------- order book

    private final List<Skill> orderBook = List.of(
            new Skill("BID", "Systems & Backend", List.of(
                    "Python 3.x", "Java", "SQL", "Spring Boot",
                    "Spring (Core / MVC / Data JPA / Security)",
                    "RESTful APIs", "Microservices", "GraphQL", "Kafka"
            )),
            new Skill("ASK", "Cloud, AI & Tooling", List.of(
                    "AWS (S3, Lambda, Step Functions, Glue, RDS, EC2, SQS)",
                    "Terraform", "Spinnaker", "Docker", "IaC",
                    "MCP (Model Context Protocol)", "LLM agents", "Moderne CLI"
            )),
            new Skill("ASK", "Databases", List.of(
                    "PostgreSQL", "MySQL", "JPA / Hibernate"
            ))
    );

    // --------------------------------------------------------------- holdings

    /**
     * One position, deliberately. The panel is framed as a concentrated book rather than a
     * list, so the metrics strip gives the single holding depth instead of padding the
     * section with rows. Every figure below is restated from the thesis that follows it.
     */
    private final List<Holding> holdings = List.of(
            new Holding(
                    "KITE", "▲", "QUANT",
                    "KiteTerminal — Algorithmic Trading Terminal",
                    "2025–Present",
                    List.of(
                            new Stat("STRATEGIES", "", 7, 0, "", ""),
                            new Stat("STOCK UNIVERSE", "", 500, 0, "", ""),
                            new Stat("DESKS · INTRADAY / SWING / F&O", "", 3, 0, "", ""),
                            new Stat("LOOK-AHEAD BIAS", "", 0, 0, "", "")
                    ),
                    "Full-stack trading terminal built on the Zerodha Kite Connect REST and WebSocket APIs, with separate intraday, swing and F&O sections and a paper-trading switch that routes simulated and live orders down one auditable path. Runs 7 strategies across equity and index options: swing entries screen a 500-stock universe behind pattern, fundamental and relative-strength gates, while F&O trades multi-leg structures with delta-based strike selection — all validated on a no-look-ahead backtester that models real brokerage costs and slippage.",
                    List.of("PYTHON", "FASTAPI", "NEXT.JS", "TYPESCRIPT", "TAURI"),
                    // Private repo — no link at all beats one that lands somewhere the
                    // position isn't. The capture below is what stands in for it.
                    null,
                    "./img/kite-terminal.png",
                    "KiteTerminal running — intraday, swing and F&O sections with live positions and the paper-trading switch"
            )
    );

    // -------------------------------------------------------------- news wire

    private final List<NewsItem> news = List.of(
            new NewsItem("25", "BREAKING", "VKLP SHIPS ZERO-DOWNTIME 2TB MIGRATION — $120K SAVED, QUERIES UP 40%"),
            new NewsItem("25", "LAUNCH", "VKLP LAUNCHES SELF-SERVICE FEED-ONBOARDING PLATFORM — JIRA-TO-PROD, FULLY AUTOMATED"),
            new NewsItem("25", "PROD", "MCP + LLM AGENTS HIT PROD — DATA-QUALITY REVIEW GOES HANDS-OFF"),
            new NewsItem("24", "SECURITY", "SELF-IMPROVING AI AGENT REMEDIATES 120+ VULNS ACROSS 22 REPOS"),
            new NewsItem("23", "AWARD", "VKLP NAMED BEST PERFORMING INTERN — TOP 5% OF COHORT; AWS CLOUD PRACTITIONER CERTIFIED"),
            new NewsItem("23", "IPO", "GRADUATES THAPAR (CGPA 8.3, MERIT SCHOLARSHIP TOP 10%); ANALYSTS REITERATE STRONG BUY")
    );

    // --------------------------------------------------------------- accessors

    public Profile profile() {
        return profile;
    }

    public Quote quote() {
        return quote;
    }

    public List<Stat> tape() {
        return tape;
    }

    public List<Execution> blotter() {
        return blotter;
    }

    public List<Skill> orderBook() {
        return orderBook;
    }

    public List<Skill> bids() {
        return orderBook.stream().filter(Skill::bid).toList();
    }

    public List<Skill> asks() {
        return orderBook.stream().filter(s -> !s.bid()).toList();
    }

    public List<Holding> holdings() {
        return holdings;
    }

    public List<NewsItem> news() {
        return news;
    }

    /**
     * The datasets published as JSON, keyed by file stem.
     *
     * <p>Used by both {@code DataController} (served at {@code /data/{name}.json} during local
     * preview) and {@code StaticSiteExporter} (written to {@code docs/data/{name}.json} for
     * GitHub Pages), so the browser sees identical bytes in both modes.
     */
    public Map<String, Object> datasets() {
        Map<String, Object> sets = new LinkedHashMap<>();
        sets.put("quote", quote);
        sets.put("blotter", blotter);
        sets.put("orderbook", orderBook);
        sets.put("holdings", holdings);
        sets.put("news", news);
        return sets;
    }

    /** Model attributes shared by the live controller and the static exporter. */
    public Map<String, Object> viewModel() {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("profile", profile);
        model.put("quote", quote);
        model.put("stats", quote.stats());
        model.put("tape", tape);
        model.put("blotter", blotter);
        model.put("bids", bids());
        model.put("asks", asks());
        model.put("holdings", holdings);
        model.put("news", news);
        model.put("buildYear", 2026);
        return model;
    }
}
