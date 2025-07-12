package io.github.jspinak.brobot.runner.project;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Lightweight project information for display and selection.
 */
@Data
@Builder
public class ProjectInfo {
    private String id;
    private String name;
    private String description;
    private String version;
    private Path projectPath;
    private LocalDateTime lastModified;
    private LocalDateTime lastAccessed;
    private long sizeInBytes;
    private boolean valid;
    private String validationMessage;
}