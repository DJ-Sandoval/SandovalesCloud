package com.dev.apisandovalescloud.controller;

import com.dev.apisandovalescloud.dto.CreateDirectoryRequest;
import com.dev.apisandovalescloud.dto.FileItemDTO;
import com.dev.apisandovalescloud.dto.RenameRequest;
import com.dev.apisandovalescloud.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

/**
 * API para tu "Google Drive casero".
 * Todas las rutas ("path") son relativas al directorio raíz configurado
 * en application.yml (homedrive.root-dir). Usa "/" o vacío para la raíz.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    /** Lista el contenido de una carpeta. Ej: GET /api/files?path=documentos/fotos */
    @GetMapping
    public ResponseEntity<List<FileItemDTO>> list(@RequestParam(defaultValue = "") String path) {
        return ResponseEntity.ok(storageService.listDirectory(path));
    }

    /** Info de un archivo o carpeta puntual. Ej: GET /api/files/info?path=documentos/informe.pdf */
    @GetMapping("/info")
    public ResponseEntity<FileItemDTO> info(@RequestParam String path) {
        return ResponseEntity.ok(storageService.getInfo(path));
    }

    /** Crea una carpeta (y sus padres si hacen falta). */
    @PostMapping("/directories")
    public ResponseEntity<FileItemDTO> createDirectory(@Valid @RequestBody CreateDirectoryRequest request) {
        FileItemDTO created = storageService.createDirectory(request.path());
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Sube un archivo a una carpeta destino.
     * Ej: POST /api/files/upload?path=documentos  (multipart/form-data, campo "file")
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileItemDTO> upload(@RequestParam(defaultValue = "") String path,
                                              @RequestParam("file") MultipartFile file) {
        FileItemDTO stored = storageService.storeFile(path, file);
        return ResponseEntity.status(201).body(stored);
    }

    /** Descarga un archivo. Ej: GET /api/files/download?path=documentos/informe.pdf */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) {
        Path filePath = storageService.loadFileForDownload(path);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Renombra un archivo o carpeta. */
    @PutMapping("/rename")
    public ResponseEntity<FileItemDTO> rename(@Valid @RequestBody RenameRequest request) {
        return ResponseEntity.ok(storageService.rename(request.path(), request.newName()));
    }

    /** Elimina un archivo o carpeta (recursivo si es carpeta). */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String path) {
        storageService.delete(path);
        return ResponseEntity.noContent().build();
    }
}

