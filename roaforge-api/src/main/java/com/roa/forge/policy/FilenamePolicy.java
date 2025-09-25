package com.roa.forge.policy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FilenamePolicy {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private FilenamePolicy() {}

    /**
     * @param op       작업명 예: "text", "watermark"
     * @param original 업로드된 원본 파일명 (null 가능)
     * @param requested 사용자가 명시한 출력 파일명(확장자 없이/있어도 OK, null 가능)
     */
    public static String build(String op, String original, String requested) {
        if (requested != null && !requested.isBlank()) {
            String name = stripIllegal(requested);
            return name.endsWith(".pdf") ? name : (name + ".pdf");
        }
        String base = (original == null || original.isBlank())
                ? "document"
                : original.replaceAll("(?i)\\.pdf$", "");
        String ts = LocalDateTime.now().format(TS);
        return "%s_%s_%s.pdf".formatted(base, op, ts);
    }

    private static String stripIllegal(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "_").trim();
    }
}