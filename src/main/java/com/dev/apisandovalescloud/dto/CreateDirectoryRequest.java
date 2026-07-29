package com.dev.apisandovalescloud.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDirectoryRequest(
        // Ruta relativa donde se creará la carpeta, ej: "documentos" o "documentos/fotos"
        @NotBlank(message = "El nombre de la carpeta no puede estar vacío")
        String path
) {
}