package com.dev.apisandovalescloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lee la propiedad "homedrive.root-dir" desde application.yml
 * (o la variable de entorno HOMEDRIVE_ROOT).
 * Esta es la carpeta raíz donde vivirán todos los archivos del usuario.
 */
@ConfigurationProperties(prefix = "homedrive")
public class StorageProperties {

    private String rootDir;

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }
}
