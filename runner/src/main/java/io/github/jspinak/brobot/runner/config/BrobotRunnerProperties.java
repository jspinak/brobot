package io.github.jspinak.brobot.runner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "brobot.runner")
public class BrobotRunnerProperties {
    private String configPath = "config";
    @Getter
    private String imagePath = "images";
    private String logPath = "logs";
    private String tempPath = "temp";

    // Project configuration file locations
    private String projectConfigFile = "project.json";
    private String dslConfigFile = "automation.json";

    // Runtime configuration
    private boolean validateConfiguration = true;
    private boolean autoStartAutomation = false;

    // Convenience methods
    public Path getProjectConfigPath() {
        return Paths.get(configPath, projectConfigFile);
    }

    public Path getDslConfigPath() {
        return Paths.get(configPath, dslConfigFile);
    }

    public Path getImageDirectoryPath() {
        return Paths.get(imagePath);
    }
}