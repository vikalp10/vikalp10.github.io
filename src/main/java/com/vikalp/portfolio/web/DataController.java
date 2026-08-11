package com.vikalp.portfolio.web;

import com.vikalp.portfolio.data.PortfolioData;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stands in for the static {@code docs/data/*.json} files while running locally, so the
 * front end can fetch the same relative {@code ./data/quote.json} path in both modes and
 * nothing has to be branched in JavaScript.
 */
@RestController
public class DataController {

    private final PortfolioData data;

    public DataController(PortfolioData data) {
        this.data = data;
    }

    @GetMapping(value = "/data/{name}.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> dataset(@PathVariable String name) {
        Object dataset = data.datasets().get(name);
        return dataset == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dataset);
    }
}
