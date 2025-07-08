package io.github.jspinak.brobot.runner.ui.config.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Model class representing a configuration entry.
 * This is a data-only class following single responsibility principle.
 */
@Data
@Builder
public class ConfigEntry {
    private String name;
    private String project;
    private String path;
    private LocalDateTime lastModified;
    private String projectConfig;
    private String dslConfig;
    private String imagePath;
    private String description;
    private String author;
    
    /**
     * Creates a new ConfigEntry with the given basic information.
     */
    public static ConfigEntry of(String name, String project, String path) {
        return ConfigEntry.builder()
                .name(name)
                .project(project)
                .path(path)
                .lastModified(LocalDateTime.now())
                .build();
    }
}