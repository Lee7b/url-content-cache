package org.sburkett.urlcontentcache.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sburkett.urlcontentcache.model.CachedPage;

class UrlContentServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void loadFromCacheOrFetch_ReturnsCachedContentOnSubsequentCalls() throws IOException {
        Path sourceFile = this.tempDir.resolve("page.html");
        Files.write(sourceFile, "first version".getBytes(StandardCharsets.UTF_8));

        CacheService cacheService = new CacheService(this.tempDir.resolve("cache"));
        UrlContentService urlContentService = new UrlContentService(cacheService);
        String url = sourceFile.toUri().toURL().toString();

        CachedPage firstLoad = urlContentService.loadFromCacheOrFetch(url);

        Files.write(sourceFile, "second version".getBytes(StandardCharsets.UTF_8));

        CachedPage secondLoad = urlContentService.loadFromCacheOrFetch(url);

        // first and second load should be equal since we are loading results from cache
        assertEquals("first version", firstLoad.getContent());
        assertEquals("first version", secondLoad.getContent());
        assertEquals(firstLoad.getFetchedAt(), secondLoad.getFetchedAt());
    }
}
