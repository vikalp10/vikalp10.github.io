package com.vikalp.portfolio.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikalp.portfolio.data.PortfolioData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Renders the running application into a directory of static files.
 *
 * <p>GitHub Pages cannot run a JVM, so the JVM runs at build time instead: Thymeleaf renders
 * the same template the live controller uses, Jackson serialises the same beans the API
 * returns, and the CSS/JS/favicon are copied alongside. The result in {@code docs/} is a
 * byte-for-byte equivalent of the local preview with the server removed.
 *
 * <p>Activated by {@code --export} on the command line; see {@code PortfolioApplication}.
 */
@Component
@ConditionalOnProperty(name = "portfolio.export.enabled", havingValue = "true")
public class StaticSiteExporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StaticSiteExporter.class);

    /** Where the exported site is published — used for canonical / sitemap URLs. */
    private static final String SITE_URL = "https://vikalp10.github.io";

    /**
     * Assets copied verbatim from {@code src/main/resources/static} into the export directory.
     * Listed explicitly rather than glob-scanned so the copy behaves identically whether the
     * app runs from a jar or from exploded classes in CI.
     */
    private static final List<String> ASSETS = List.of(
            "css/terminal.css",
            "js/terminal.js",
            "favicon.svg",
            "robots.txt"
    );

    private final PortfolioData data;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private final Path exportDir;

    public StaticSiteExporter(PortfolioData data,
                              TemplateEngine templateEngine,
                              ObjectMapper objectMapper,
                              @org.springframework.beans.factory.annotation.Value("${portfolio.export.dir:docs}") String exportDir) {
        this.data = data;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
        this.exportDir = Path.of(exportDir).toAbsolutePath().normalize();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Exporting static site to {}", exportDir);
        Files.createDirectories(exportDir);

        writePage();
        writeDatasets();
        copyAssets();

        // Tells GitHub Pages to publish the directory as-is instead of running it through Jekyll.
        Files.writeString(exportDir.resolve(".nojekyll"), "");
        writeSitemap();

        log.info("Export complete — open {}", exportDir.resolve("index.html"));
    }

    /** Renders {@code templates/index.html} with the live view model. */
    private void writePage() throws IOException {
        Context context = new Context(Locale.ENGLISH);
        context.setVariables(data.viewModel());
        String html = templateEngine.process("index", context);
        Files.writeString(exportDir.resolve("index.html"), html, StandardCharsets.UTF_8);
        log.info("  index.html ({} KB)", html.getBytes(StandardCharsets.UTF_8).length / 1024);
    }

    /** Serialises every dataset the API exposes into {@code data/<name>.json}. */
    private void writeDatasets() throws IOException {
        Path dataDir = exportDir.resolve("data");
        Files.createDirectories(dataDir);
        for (Map.Entry<String, Object> dataset : data.datasets().entrySet()) {
            Path target = dataDir.resolve(dataset.getKey() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), dataset.getValue());
            log.info("  data/{}.json", dataset.getKey());
        }
    }

    /** One page, one entry — keeps the {@code Sitemap:} line in robots.txt honest. */
    private void writeSitemap() throws IOException {
        String today = java.time.LocalDate.now().toString();
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                  <url>
                    <loc>%s</loc>
                    <lastmod>%s</lastmod>
                  </url>
                </urlset>
                """.formatted(SITE_URL, today);
        Files.writeString(exportDir.resolve("sitemap.xml"), xml, StandardCharsets.UTF_8);
    }

    private void copyAssets() throws IOException {
        for (String asset : ASSETS) {
            ClassPathResource resource = new ClassPathResource("static/" + asset);
            if (!resource.exists()) {
                log.warn("  skipped missing asset {}", asset);
                continue;
            }
            Path target = exportDir.resolve(asset);
            Files.createDirectories(target.getParent());
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("  {}", asset);
        }
    }
}
