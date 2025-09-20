package com.roa.forge.support;


import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public final class JsonMediaTypes {
    private JsonMediaTypes() {}
    public static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType("application", "json", StandardCharsets.UTF_8);
}