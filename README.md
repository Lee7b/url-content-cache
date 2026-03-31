## Assumptions
- "use it as a java member" requirement is implemented using a constant in UrlContentCacheApplication.java
- Cached files are stored in a local cache directory under the project root
- Cache file names are based on a URL-safe Base64 encoding of the URL
- Page content is read and written as UTF-8
- The "fetchedAt" date is the first successful fetch timestamp and is preserved in the cache file