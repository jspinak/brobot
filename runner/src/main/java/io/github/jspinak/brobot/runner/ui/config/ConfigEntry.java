package io.github.jspinak.brobot.runner.ui.config;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents a configuration entry containing paths to project and DSL configuration files.
 */
@Getter
@Setter
public class ConfigEntry {
    private String name;
    private String project;
    private Path projectConfigPath;
    private Path dslConfigPath;
    private Path imagePath;
    private LocalDateTime lastModified;
    private String description;
    private String author;
    private String version;

    /**
     * Creates a new configuration entry.
     *
     * @param name The name of the configuration
     * @param project The project name
     * @param projectConfigPath The path to the project configuration file
     * @param dslConfigPath The path to the DSL configuration file
     * @param imagePath The path to the images directory
     * @param lastModified The last modification time
     */
    public ConfigEntry(
            String name,
            String project,
            Path projectConfigPath,
            Path dslConfigPath,
            Path imagePath,
            LocalDateTime lastModified) {
        this.name = name;
        this.project = project;
        this.projectConfigPath = projectConfigPath;
        this.dslConfigPath = dslConfigPath;
        this.imagePath = imagePath;
        this.lastModified = lastModified;
    }

    /**
     * Gets the project configuration file name.
     *
     * @return The project configuration file name
     */
    public String getProjectConfigFileName() {
        return projectConfigPath != null ? projectConfigPath.getFileName().toString() : "";
    }

    /**
     * Gets the DSL configuration file name.
     *
     * @return The DSL configuration file name
     */
    public String getDslConfigFileName() {
        return dslConfigPath != null ? dslConfigPath.getFileName().toString() : "";
    }

    /**
     * Gets the images directory name.
     *
     * @return The images directory name
     */
    public String getImageDirectoryName() {
        return imagePath != null ? imagePath.getFileName().toString() : "";
    }
}