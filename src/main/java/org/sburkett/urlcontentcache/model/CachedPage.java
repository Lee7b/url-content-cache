package org.sburkett.urlcontentcache.model;

import java.time.Instant;

/**
 * Model for a cached page
 * Contains the url, original fetch timestamp, and page content
 */
public class CachedPage {

    private final String url;
    private final Instant fetchedAt;
    private final String content;

    public CachedPage(String url, Instant fetchedAt, String content) {
        this.url = url;
        this.fetchedAt = fetchedAt;
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public String getContent() {
        return content;
    }
}
