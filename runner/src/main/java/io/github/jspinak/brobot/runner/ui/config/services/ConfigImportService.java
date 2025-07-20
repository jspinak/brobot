package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.ConfigImportDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for importing configurations and browsing for configuration files.
 * Handles file selection and configuration discovery.
 */
@Slf4j
@Service
public class ConfigImportService {
    
    private final BrobotLibraryInitializer libraryInitializer;
    private final BrobotRunnerProperties runnerProperties;
    private final EventBus eventBus;
    
    @Getter
    @Setter
    private ImportConfiguration configuration;
    
    private Consumer<ConfigEntry> importSuccessHandler;
    
    /**
     * Configuration for import behavior.
     */
    @Getter
    @Setter
    public static class ImportConfiguration {
        private boolean autoDetectDslConfig;
        private boolean showImportDialog;
        private String[] dslFilePatterns;
        private String defaultProjectName;
        
        public static ImportConfigurationBuilder builder() {
            return new ImportConfigurationBuilder();
        }
        
        public static class ImportConfigurationBuilder {
            private boolean autoDetectDslConfig = true;
            private boolean showImportDialog = true;
            private String[] dslFilePatterns = {"dsl", "automation"};
            private String defaultProjectName = "Unknown";
            
            public ImportConfigurationBuilder autoDetectDslConfig(boolean detect) {
                this.autoDetectDslConfig = detect;
                return this;
            }
            
            public ImportConfigurationBuilder showImportDialog(boolean show) {
                this.showImportDialog = show;
                return this;
            }
            
            public ImportConfigurationBuilder dslFilePatterns(String... patterns) {
                this.dslFilePatterns = patterns;
                return this;
            }
            
            public ImportConfigurationBuilder defaultProjectName(String name) {
                this.defaultProjectName = name;
                return this;
            }
            
            public ImportConfiguration build() {
                ImportConfiguration config = new ImportConfiguration();
                config.autoDetectDslConfig = autoDetectDslConfig;
                config.showImportDialog = showImportDialog;
                config.dslFilePatterns = dslFilePatterns;
                config.defaultProjectName = defaultProjectName;
                return config;
            }
        }
    }
    
    @Autowired
    public ConfigImportService(BrobotLibraryInitializer libraryInitializer,
                               BrobotRunnerProperties runnerProperties,
                               EventBus eventBus) {
        this.libraryInitializer = libraryInitializer;
        this.runnerProperties = runnerProperties;
        this.eventBus = eventBus;
        this.configuration = ImportConfiguration.builder().build();
    }
    
    /**
     * Shows the import dialog for detailed configuration import.
     * @param parentWindow The parent window for the dialog
     * @return Optional containing the imported configuration
     */
    public Optional<ConfigEntry> showImportDialog(Window parentWindow) {
        if (!configuration.showImportDialog) {
            return Optional.empty();
        }
        
        ConfigImportDialog dialog = new ConfigImportDialog(
                libraryInitializer,
                runnerProperties,
                eventBus
        );
        
        Optional<ConfigEntry> result = dialog.showAndWait();
        
        if (result.isPresent() && importSuccessHandler != null) {
            importSuccessHandler.accept(result.get());
        }
        
        return result;
    }
    
    /**
     * Browses for a configuration file using a file chooser.
     * @param parentWindow The parent window for the file chooser
     * @return Optional containing the created configuration entry
     */
    public Optional<ConfigEntry> browseForConfiguration(Window parentWindow) {
        // Create file chooser for project config
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        File file = fileChooser.showOpenDialog(parentWindow);
        if (file == null) {
            return Optional.empty();
        }
        
        try {
            Path projectConfigPath = file.toPath();
            
            // Try to find DSL config
            Optional<Path> dslConfigPath = findDslConfig(projectConfigPath);
            
            if (dslConfigPath.isEmpty() && configuration.autoDetectDslConfig) {
                // If auto-detect failed, ask user to select DSL config
                dslConfigPath = selectDslConfig(projectConfigPath, parentWindow);
            }
            
            if (dslConfigPath.isPresent()) {
                // Create configuration entry
                ConfigEntry entry = createConfigEntry(projectConfigPath, dslConfigPath.get());
                
                if (importSuccessHandler != null) {
                    importSuccessHandler.accept(entry);
                }
                
                return Optional.of(entry);
            } else {
                log.warn("No DSL configuration file selected for project config: {}", projectConfigPath);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Error browsing for configuration", e);
            return Optional.empty();
        }
    }
    
    /**
     * Attempts to find a DSL configuration file in the same directory as the project config.
     * @param projectConfigPath The project configuration path
     * @return Optional containing the DSL config path if found
     */
    private Optional<Path> findDslConfig(Path projectConfigPath) throws IOException {
        Path parentDir = projectConfigPath.getParent();
        if (parentDir == null) {
            return Optional.empty();
        }
        
        // Find all JSON files in the directory
        List<Path> jsonFiles = Files.list(parentDir)
                .filter(p -> p.toString().endsWith(".json") && !p.equals(projectConfigPath))
                .toList();
        
        // If there's only one other JSON file, assume it's the DSL config
        if (jsonFiles.size() == 1) {
            return Optional.of(jsonFiles.get(0));
        }
        
        // If there are multiple files, try to find one with DSL patterns
        if (jsonFiles.size() > 1) {
            for (Path path : jsonFiles) {
                String filename = path.getFileName().toString().toLowerCase();
                for (String pattern : configuration.dslFilePatterns) {
                    if (filename.contains(pattern)) {
                        return Optional.of(path);
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Shows a file chooser to select the DSL configuration file.
     * @param projectConfigPath The project config path for context
     * @param parentWindow The parent window
     * @return Optional containing the selected DSL config path
     */
    private Optional<Path> selectDslConfig(Path projectConfigPath, Window parentWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DSL Configuration File");
        
        // Set initial directory to same as project config
        Path parentDir = projectConfigPath.getParent();
        if (parentDir != null) {
            fileChooser.setInitialDirectory(parentDir.toFile());
        }
        
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        File dslFile = fileChooser.showOpenDialog(parentWindow);
        return dslFile != null ? Optional.of(dslFile.toPath()) : Optional.empty();
    }
    
    /**
     * Creates a configuration entry from the given paths.
     * @param projectConfigPath The project configuration path
     * @param dslConfigPath The DSL configuration path
     * @return The created configuration entry
     */
    private ConfigEntry createConfigEntry(Path projectConfigPath, Path dslConfigPath) {
        String name = projectConfigPath.getFileName().toString();
        String project = configuration.defaultProjectName;
        
        // Try to extract project name from path or filename
        if (projectConfigPath.getParent() != null) {
            project = projectConfigPath.getParent().getFileName().toString();
        }
        
        Path imagePath = Paths.get(runnerProperties.getImagePath());
        
        return new ConfigEntry(
                name,
                project,
                projectConfigPath,
                dslConfigPath,
                imagePath,
                LocalDateTime.now()
        );
    }
    
    /**
     * Sets the handler for successful imports.
     * @param handler The success handler
     */
    public void setImportSuccessHandler(Consumer<ConfigEntry> handler) {
        this.importSuccessHandler = handler;
    }
    
    /**
     * Validates a configuration entry.
     * @param entry The entry to validate
     * @return true if valid, false otherwise
     */
    public boolean validateConfigEntry(ConfigEntry entry) {
        if (entry == null) {
            return false;
        }
        
        // Check that paths exist
        if (!Files.exists(entry.getProjectConfigPath())) {
            log.warn("Project config file does not exist: {}", entry.getProjectConfigPath());
            return false;
        }
        
        if (!Files.exists(entry.getDslConfigPath())) {
            log.warn("DSL config file does not exist: {}", entry.getDslConfigPath());
            return false;
        }
        
        // Check that files are readable
        if (!Files.isReadable(entry.getProjectConfigPath())) {
            log.warn("Project config file is not readable: {}", entry.getProjectConfigPath());
            return false;
        }
        
        if (!Files.isReadable(entry.getDslConfigPath())) {
            log.warn("DSL config file is not readable: {}", entry.getDslConfigPath());
            return false;
        }
        
        return true;
    }
}