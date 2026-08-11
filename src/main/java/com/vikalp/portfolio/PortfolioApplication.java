package com.vikalp.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Map;

/**
 * Two run modes, one jar:
 *
 * <pre>
 *   mvn spring-boot:run                    # live preview on :8080
 *   java -jar target/vklp-portfolio.jar --export   # render docs/ and exit
 * </pre>
 *
 * With {@code --export} there is nothing to serve, so the web server is switched off and
 * {@code StaticSiteExporter} runs instead: Thymeleaf renders the page to {@code docs/index.html}
 * and Jackson writes {@code docs/data/*.json}. GitHub Pages then serves that directory.
 */
@SpringBootApplication
public class PortfolioApplication {

    public static final String EXPORT_FLAG = "--export";

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PortfolioApplication.class);
        if (Arrays.asList(args).contains(EXPORT_FLAG)) {
            app.setWebApplicationType(WebApplicationType.NONE);
            app.setDefaultProperties(Map.of("portfolio.export.enabled", "true"));
        }
        app.run(args);
    }
}
