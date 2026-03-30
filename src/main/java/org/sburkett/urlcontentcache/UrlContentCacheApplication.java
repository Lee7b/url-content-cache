package org.sburkett.urlcontentcache;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sburkett.urlcontentcache.model.CachedPage;
import org.sburkett.urlcontentcache.service.CacheService;
import org.sburkett.urlcontentcache.service.UrlContentService;

/**
 * Entry point for the url content cache application
 */
public class UrlContentCacheApplication {

    private static final Logger LOGGER = Logger.getLogger(UrlContentCacheApplication.class.getName());
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
    private static final String URL = "https://www.google.com/";

    public static void main(String[] args) {
        try {
            CacheService cacheService = new CacheService(Paths.get("cache"));
            UrlContentService urlContentService = new UrlContentService(cacheService);
            CachedPage cachedPage = urlContentService.loadFromCacheOrFetch(URL);
            printPage(cachedPage);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Technical exception occurred", exception);
        }
    }

    private static void printPage(CachedPage cachedPage) {
        System.out.println("Original fetch date: " + DISPLAY_FORMATTER.format(cachedPage.getFetchedAt()));
        System.out.println("URL: " + cachedPage.getUrl());
        System.out.println("Page content:");
        System.out.println(cachedPage.getContent());
    }
}
