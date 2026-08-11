package com.vikalp.portfolio.web;

import com.vikalp.portfolio.data.PortfolioData;
import com.vikalp.portfolio.model.Execution;
import com.vikalp.portfolio.model.Holding;
import com.vikalp.portfolio.model.NewsItem;
import com.vikalp.portfolio.model.Profile;
import com.vikalp.portfolio.model.Quote;
import com.vikalp.portfolio.model.Skill;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * JSON API for local development — poke at the data with curl while iterating.
 *
 * <p>The page itself never calls these: it reads {@code ./data/*.json} so the identical
 * JavaScript works both here and on GitHub Pages, where no JVM exists. See
 * {@link DataController} for those files during preview.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final PortfolioData data;

    public ApiController(PortfolioData data) {
        this.data = data;
    }

    @GetMapping("/profile")
    public Profile profile() {
        return data.profile();
    }

    @GetMapping("/quote")
    public Quote quote() {
        return data.quote();
    }

    @GetMapping("/blotter")
    public List<Execution> blotter() {
        return data.blotter();
    }

    @GetMapping("/orderbook")
    public List<Skill> orderBook() {
        return data.orderBook();
    }

    @GetMapping("/holdings")
    public List<Holding> holdings() {
        return data.holdings();
    }

    @GetMapping("/news")
    public List<NewsItem> news() {
        return data.news();
    }
}
