package com.dev.apisandovalescloud.exception;

/**
 * Se lanza cuando la ruta solicitada intenta salir del directorio raíz
 * (path traversal, ej: "../../etc/passwd") o tiene un formato inválido.
 */
public class InvalidPathRequestException extends RuntimeException {
    public InvalidPathRequestException(String message) {
        super(message);
    }
}

