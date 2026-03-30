package org.sburkett.urlcontentcache.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Logger;
import org.sburkett.urlcontentcache.model.CachedPage;

/**
 * Service responsible for retrieving webpage content.
 * It first checks the local cache for an existing copy.
 * If none is found, it fetches the content from the URL, stores it in cache, and returns it as a CachedPage.
 */
public class UrlContentService {

    private static final Logger LOGGER = Logger.getLogger(UrlContentService.class.getName());
    private final CacheService cacheService;

    public UrlContentService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Retrieves a URL's content from either the web or local cache if stored
     * Saves URL content to local cache if fetching
     */
    public CachedPage loadFromCacheOrFetch(String url) throws IOException {
        CachedPage cachedPage = cacheService.find(url);

        // early return if the url content is found in cache
        if (cachedPage != null) {
            return cachedPage;
        }

        LOGGER.info("Url not found in local cache. Fetching content from URL: " + url);

        // fetch and save url content to cache
        String content = fetchContentFromWeb(url);
        CachedPage page = new CachedPage(url, Instant.now(), content);
        cacheService.save(page);

        return page;
    }

    /**
     * fetches the content of a webpage
     */
    private String fetchContentFromWeb(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(readAllBytes(inputStream), StandardCharsets.UTF_8);
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
}
