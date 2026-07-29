package com.dev.apisandovalescloud.service;

import com.dev.apisandovalescloud.config.StorageProperties;
import com.dev.apisandovalescloud.dto.FileItemDTO;
import com.dev.apisandovalescloud.exception.InvalidPathRequestException;
import com.dev.apisandovalescloud.exception.ResourceConflictException;
import com.dev.apisandovalescloud.exception.ResourceNotFoundException;
import com.dev.apisandovalescloud.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getRootDir()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio raíz de almacenamiento: " + rootLocation, e);
        }
    }

    /**
     * Convierte una ruta relativa (la que manda el cliente) en una ruta absoluta segura,
     * verificando que no se salga del directorio raíz (protección contra "../../etc/passwd").
     */
    private Path resolvePath(String relativePath) {
        String cleaned = (relativePath == null) ? "" : relativePath.trim();
        // Normaliza separadores y quita "/" inicial para tratar todo como relativo a la raíz.
        cleaned = cleaned.replace("\\", "/");
        while (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        Path resolved = rootLocation.resolve(cleaned).normalize();

        if (!resolved.startsWith(rootLocation)) {
            throw new InvalidPathRequestException("Ruta inválida: no puedes salir del directorio raíz.");
        }
        return resolved;
    }

    /** Devuelve la ruta relativa (para mostrar al cliente) a partir de una ruta absoluta. */
    private String toRelative(Path absolute) {
        String rel = rootLocation.relativize(absolute).toString().replace("\\", "/");
        return rel.isEmpty() ? "/" : rel;
    }

    // ---------- LISTAR ----------

    public List<FileItemDTO> listDirectory(String relativePath) {
        Path dir = resolvePath(relativePath);

        if (!Files.exists(dir)) {
            throw new ResourceNotFoundException("El directorio no existe: " + relativePath);
        }
        if (!Files.isDirectory(dir)) {
            throw new InvalidPathRequestException("La ruta indicada no es un directorio: " + relativePath);
        }

        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .map(this::toFileItemDTO)
                    .sorted(Comparator.comparing(FileItemDTO::directory).reversed()
                            .thenComparing(FileItemDTO::name, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException("No se pudo listar el directorio: " + relativePath, e);
        }
    }

    private FileItemDTO toFileItemDTO(Path path) {
        try {
            boolean isDir = Files.isDirectory(path);
            long size = isDir ? 0 : Files.size(path);
            Instant modified = Files.getLastModifiedTime(path).toInstant();
            return new FileItemDTO(path.getFileName().toString(), toRelative(path), isDir, size, modified);
        } catch (IOException e) {
            throw new StorageException("No se pudo leer metadata de: " + path, e);
        }
    }

    // ---------- CREAR DIRECTORIO ----------

    public FileItemDTO createDirectory(String relativePath) {
        Path dir = resolvePath(relativePath);

        if (Files.exists(dir)) {
            throw new ResourceConflictException("Ya existe un archivo o carpeta con ese nombre: " + relativePath);
        }
        try {
            Files.createDirectories(dir);
            return toFileItemDTO(dir);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio: " + relativePath, e);
        }
    }

    // ---------- SUBIR ARCHIVO ----------

    public FileItemDTO storeFile(String relativeDirPath, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPathRequestException("El archivo enviado está vacío.");
        }

        String originalName = Paths.get(file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename())
                .getFileName().toString();

        Path dir = resolvePath(relativeDirPath);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new StorageException("No se pudo preparar el directorio destino: " + relativeDirPath, e);
        }

        Path destination = dir.resolve(originalName).normalize();
        if (!destination.startsWith(rootLocation)) {
            throw new InvalidPathRequestException("Nombre de archivo inválido.");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            return toFileItemDTO(destination);
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar el archivo: " + originalName, e);
        }
    }

    // ---------- DESCARGAR ----------

    public Path loadFileForDownload(String relativePath) {
        Path file = resolvePath(relativePath);
        if (!Files.exists(file) || Files.isDirectory(file)) {
            throw new ResourceNotFoundException("Archivo no encontrado: " + relativePath);
        }
        return file;
    }

    // ---------- RENOMBRAR ----------

    public FileItemDTO rename(String relativePath, String newName) {
        if (newName == null || newName.isBlank() || newName.contains("/") || newName.contains("\\")) {
            throw new InvalidPathRequestException("El nuevo nombre no es válido.");
        }

        Path source = resolvePath(relativePath);
        if (!Files.exists(source)) {
            throw new ResourceNotFoundException("No existe el archivo o carpeta: " + relativePath);
        }

        Path target = source.resolveSibling(newName).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new InvalidPathRequestException("Ruta destino inválida.");
        }
        if (Files.exists(target)) {
            throw new ResourceConflictException("Ya existe un archivo o carpeta con el nombre: " + newName);
        }

        try {
            Files.move(source, target);
            return toFileItemDTO(target);
        } catch (IOException e) {
            throw new StorageException("No se pudo renombrar: " + relativePath, e);
        }
    }

    // ---------- ELIMINAR ----------

    public void delete(String relativePath) {
        Path target = resolvePath(relativePath);

        if (target.equals(rootLocation)) {
            throw new InvalidPathRequestException("No puedes eliminar el directorio raíz.");
        }
        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("No existe el archivo o carpeta: " + relativePath);
        }

        try {
            if (Files.isDirectory(target)) {
                deleteRecursively(target);
            } else {
                Files.delete(target);
            }
        } catch (IOException e) {
            throw new StorageException("No se pudo eliminar: " + relativePath, e);
        }
    }

    private void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path p : paths) {
                Files.delete(p);
            }
        }
    }

    // ---------- INFO DE UN ELEMENTO ----------

    public FileItemDTO getInfo(String relativePath) {
        Path target = resolvePath(relativePath);
        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("No existe el archivo o carpeta: " + relativePath);
        }
        return toFileItemDTO(target);
    }
}

