package org.sburkett.urlcontentcache.service;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.sburkett.urlcontentcache.model.CachedPage;

/**
 * Java service that manages local file persistence
 */
public class CacheService {

    private static final Logger LOGGER = Logger.getLogger(CacheService.class.getName());

    private final Path cacheDirectory;

    public CacheService(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        ensureCacheDirectoryExists();
    }

    public CachedPage find(String url) throws IOException {
        Path cacheFilePath = getCachedFilePath(url);
        LOGGER.info("Checking cache for URL: " + url);

        if (Files.notExists(cacheFilePath)) {
            return null;
        }

        LOGGER.info("Cache hit. Loading content from local file: " + cacheFilePath.toAbsolutePath());
        return readFromCache(cacheFilePath);
    }

    /**
     * Saves a URL's original fetch timestamp, url, and content to a file
     */
    public void save(CachedPage cachedPage) throws IOException {
        Path cacheFilePath = getCachedFilePath(cachedPage.getUrl());

        try (BufferedWriter writer = Files.newBufferedWriter(cacheFilePath, StandardCharsets.UTF_8)) {

            // write fetch date
            writer.write("fetchedAt=");
            writer.write(cachedPage.getFetchedAt().toString());
            writer.newLine();

            // write url
            writer.write("url=");
            writer.write(cachedPage.getUrl());
            writer.newLine();
            writer.newLine();

            // write the content
            writer.write(cachedPage.getContent());
        }

        LOGGER.info("Saved fetched content to cache file: " + cacheFilePath.toAbsolutePath());
    }

    private CachedPage readFromCache(Path cacheFilePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(cacheFilePath, StandardCharsets.UTF_8)) {
            Map<String, String> metadata = readMetadata(reader);
            String url = metadata.get("url");
            String fetchedAtValue = metadata.get("fetchedAt");

            if (url == null || fetchedAtValue == null) {
                throw new IOException("Cache file is missing required metadata: " + cacheFilePath.toAbsolutePath());
            }

            return new CachedPage(url, Instant.parse(fetchedAtValue), readRemainingContent(reader));
        }
    }

    private Map<String, String> readMetadata(BufferedReader reader) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }

            int separatorIndex = line.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }

            String key = line.substring(0, separatorIndex);
            String value = line.substring(separatorIndex + 1);
            metadata.put(key, value);
        }

        return metadata;
    }

    private String readRemainingContent(BufferedReader reader) throws IOException {
        char[] buffer = new char[8192];
        int charactersRead;
        StringBuilder content = new StringBuilder();

        while ((charactersRead = reader.read(buffer)) != -1) {
            content.append(buffer, 0, charactersRead);
        }

        return content.toString();
    }

    private void ensureCacheDirectoryExists() {
        try {
            Files.createDirectories(cacheDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create cache directory: " + cacheDirectory.toAbsolutePath(), exception);
        }
    }

    private Path getCachedFilePath(String url) {
        String fileName = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(url.getBytes(StandardCharsets.UTF_8));

        return cacheDirectory.resolve(fileName + ".cache");
    }
}
