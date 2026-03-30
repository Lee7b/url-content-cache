package org.sburkett.urlcontentcache.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sburkett.urlcontentcache.model.CachedPage;

import static org.junit.jupiter.api.Assertions.*;

class CacheServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void saveAndFind_PreservesCachedPage() throws IOException {
        CacheService cacheService = new CacheService(this.tempDir);
        String content = String.join(System.lineSeparator(), "<html>", "<body>cached content</body>", "</html>");

        CachedPage cachedPage = new CachedPage("https://example.com/articles?id=7", Instant.parse("2026-03-30T20:15:30Z"), content);

        cacheService.save(cachedPage);

        CachedPage loadedPage = cacheService.find(cachedPage.getUrl());

        assertNotNull(loadedPage);
        assertEquals(cachedPage.getUrl(), loadedPage.getUrl());
        assertEquals(cachedPage.getFetchedAt(), loadedPage.getFetchedAt());
        assertEquals(cachedPage.getContent(), loadedPage.getContent());
    }

    @Test
    void find_ReturnsNullWhenCacheFileDoesNotExist() throws IOException {
        CacheService cacheService = new CacheService(this.tempDir);
        CachedPage cachedPage = cacheService.find("https://missing.example.com");

        assertNull(cachedPage);
    }

    @Test
    void find_ThrowsExceptionWhenRequiredMetadataIsMissing() throws IOException {
        CacheService cacheService = new CacheService(this.tempDir);
        String url = "https://example.com/bad-cache";
        Path cacheFile = this.tempDir.resolve(this.toCacheFileName(url));

        Files.write(cacheFile, ("url=" + url + System.lineSeparator() + System.lineSeparator() + "content only").getBytes(StandardCharsets.UTF_8));
        IOException exception = Assertions.assertThrows(IOException.class, () -> cacheService.find(url));

        assertEquals("Cache file is missing required metadata: " + cacheFile.toAbsolutePath(), exception.getMessage());
    }

    private String toCacheFileName(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8)) + ".cache";
    }
}
