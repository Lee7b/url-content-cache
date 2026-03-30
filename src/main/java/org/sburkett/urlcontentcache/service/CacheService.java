package org.sburkett.urlcontentcache.service;

import java.io.IOException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
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
    }

    public CachedPage find(String url) throws IOException {
        Files.createDirectories(cacheDirectory);

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
        Files.createDirectories(cacheDirectory);

        Path cacheFilePath = getCachedFilePath(cachedPage.getUrl());
        StringBuilder cacheFileContents = new StringBuilder();

        cacheFileContents.append("fetchedAt=")
                .append(cachedPage.getFetchedAt())
                .append(System.lineSeparator());

        cacheFileContents.append("url=")
                .append(cachedPage.getUrl())
                .append(System.lineSeparator());

        cacheFileContents.append(System.lineSeparator());
        cacheFileContents.append(cachedPage.getContent());

        Files.write(cacheFilePath, cacheFileContents.toString().getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Saved fetched content to cache file: " + cacheFilePath.toAbsolutePath());
    }

    private CachedPage readFromCache(Path cacheFilePath) throws IOException {
        List<String> lines = Files.readAllLines(cacheFilePath, StandardCharsets.UTF_8);
        int contentStartIndex = findContentStartIndex(lines);
        String url = readMetadataValue(lines, "url", contentStartIndex);
        String fetchedAtValue = readMetadataValue(lines, "fetchedAt", contentStartIndex);

        if (url == null || fetchedAtValue == null) {
            throw new IOException("Cache file is missing required metadata: " + cacheFilePath.toAbsolutePath());
        }

        String content = String.join(System.lineSeparator(), lines.subList(contentStartIndex, lines.size()));
        return new CachedPage(url, Instant.parse(fetchedAtValue), content);
    }

    private int findContentStartIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                return i + 1;
            }
        }

        return lines.size();
    }

    private String readMetadataValue(List<String> lines, String key, int metadataEndIndex) {
        String prefix = key + "=";

        for (int i = 0; i < metadataEndIndex; i++) {
            String line = lines.get(i);
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }

        return null;
    }

    private Path getCachedFilePath(String url) {
        String fileName = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(url.getBytes(StandardCharsets.UTF_8));

        return cacheDirectory.resolve(fileName + ".cache");
    }
}
