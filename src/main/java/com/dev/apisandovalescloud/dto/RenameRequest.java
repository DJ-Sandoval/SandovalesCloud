package com.dev.apisandovalescloud.dto;
import jakarta.validation.constraints.NotBlank;

public record RenameRequest(
        // Ruta relativa actual del archivo/carpeta, ej: "documentos/informe.pdf"
        @NotBlank(message = "La ruta actual es obligatoria")
        String path,

        // Solo el nuevo nombre (no la ruta completa), ej: "informe-final.pdf"
        @NotBlank(message = "El nuevo nombre es obligatorio")
        String newName
) {
}
