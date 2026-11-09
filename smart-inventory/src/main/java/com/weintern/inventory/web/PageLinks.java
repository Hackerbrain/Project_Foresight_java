package com.weintern.inventory.web;

import org.springframework.web.util.UriComponentsBuilder;

/** Builds the base URL for pager links so the current filters are kept when changing page. */
public final class PageLinks {

    private PageLinks() {}

    /** Returns e.g. "/products?q=pen&low=true&" - append "page=2". Pass key, value, key, value... */
    public static String of(String path, Object... keyValues) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            builder.queryParam(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        }
        String url = builder.build().encode().toUriString();
        return url + (url.contains("?") ? "&" : "?");
    }
}
