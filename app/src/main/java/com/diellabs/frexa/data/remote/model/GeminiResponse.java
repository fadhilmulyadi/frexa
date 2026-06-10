package com.diellabs.frexa.data.remote.model;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;

    public static class Candidate { public Content content; }
    public static class Content { public List<Part> parts; }
    public static class Part { public String text; }

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) return "";
        Content c = candidates.get(0).content;
        if (c == null || c.parts == null || c.parts.isEmpty()) return "";
        return c.parts.get(0).text;
    }
}
