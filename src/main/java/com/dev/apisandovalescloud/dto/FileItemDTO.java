package com.dev.apisandovalescloud.dto;

import java.time.Instant;

public record FileItemDTO(
        String name,
        String path,        // ruta relativa desde la raíz (la que usará el cliente en próximas llamadas)
        boolean directory,
        long sizeInBytes,
        Instant lastModified,
        boolean previewable,
        String previewType
) {
}

