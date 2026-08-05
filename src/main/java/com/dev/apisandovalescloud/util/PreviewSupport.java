package com.dev.apisandovalescloud.util;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Determina si una ruta corresponde a una imagen soportada para vista previa,
 * y resuelve el Content-Type correcto por extensión (más fiable que
 * Files.probeContentType para .svg y .ico, que el SO a veces no reconoce bien).
 */
public final class PreviewSupport {

    public enum Kind {
        IMAGE,
        VIDEO
    }

    public record PreviewMeta(Kind kind, String contentType) {
    }

    private static final Map<String, PreviewMeta> SUPPORTED = Map.ofEntries(
            Map.entry("png", new PreviewMeta(Kind.IMAGE, "image/png")),
            Map.entry("jpg", new PreviewMeta(Kind.IMAGE, "image/jpeg")),
            Map.entry("jpeg", new PreviewMeta(Kind.IMAGE, "image/jpeg")),
            Map.entry("ico", new PreviewMeta(Kind.IMAGE, "image/x-icon")),
            Map.entry("svg", new PreviewMeta(Kind.IMAGE, "image/svg+xml")),
            Map.entry("mp4", new PreviewMeta(Kind.VIDEO, "video/mp4"))
    );

    private PreviewSupport() {
    }

    public static boolean isSupported(String fileName) {
        return resolve(fileName).isPresent();
    }

    public static Optional<PreviewMeta> resolve(String fileName) {
        return Optional.ofNullable(SUPPORTED.get(extensionOf(fileName)));
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

