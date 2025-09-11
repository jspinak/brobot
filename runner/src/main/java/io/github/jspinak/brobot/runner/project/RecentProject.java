package io.github.jspinak.brobot.runner.project;

import java.nio.file.Path;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/** Information about recently accessed projects. */
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
