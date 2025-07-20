package io.github.jspinak.brobot.runner.project;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Information about recently accessed projects.
 */
@Data
@Builder
public class RecentProject {
    private String id;
    private String name;
    private Path projectPath;
    private LocalDateTime lastAccessed;
    private int accessCount;
    private boolean pinned;
    private boolean exists;
}