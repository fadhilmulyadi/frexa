package com.diellabs.frexa.data.remote.model;

import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String text) {
        contents = Collections.singletonList(new Content(text));
    }

    public static class Content {
        public List<Part> parts;
        public Content(String t) { parts = Collections.singletonList(new Part(t)); }
    }

    public static class Part {
        public String text;
        public Part(String t) { text = t; }
    }
}
