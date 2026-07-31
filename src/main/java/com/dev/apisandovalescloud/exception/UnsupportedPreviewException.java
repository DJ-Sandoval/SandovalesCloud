package com.dev.apisandovalescloud.exception;

/**
 * Se lanza cuando se pide vista previa (/preview) de un archivo cuyo formato
 * no está soportado (solo se soportan imágenes: png, jpg, jpeg, ico, svg).
 */
public class UnsupportedPreviewException extends RuntimeException {
    public UnsupportedPreviewException(String message) {
        super(message);
    }
}
