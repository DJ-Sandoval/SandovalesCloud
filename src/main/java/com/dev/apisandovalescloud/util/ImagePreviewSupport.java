package com.dev.apisandovalescloud.util;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Determina si una ruta corresponde a una imagen soportada para vista previa,
 * y resuelve el Content-Type correcto por extensión (más fiable que
 * Files.probeContentType para .svg y .ico, que el SO a veces no reconoce bien).
 */
public final class ImagePreviewSupport {

    private static final Map<String, String> SUPPORTED_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "ico", "image/x-icon",
            "svg", "image/svg+xml"
    );

    private ImagePreviewSupport() {
    }

    public static boolean isSupported(String fileName) {
        return contentTypeFor(fileName).isPresent();
    }

    public static Optional<String> contentTypeFor(String fileName) {
        String ext = extensionOf(fileName);
        return Optional.ofNullable(SUPPORTED_TYPES.get(ext));
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}


