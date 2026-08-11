package com.vikalp.portfolio.web;

import com.vikalp.portfolio.data.PortfolioData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Local preview only. On GitHub Pages the same template has already been rendered to
 * {@code docs/index.html} by the exporter, so nothing here runs in production.
 */
@Controller
public class PageController {

    private final PortfolioData data;

    public PageController(PortfolioData data) {
        this.data = data;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAllAttributes(data.viewModel());
        return "index";
    }
}
