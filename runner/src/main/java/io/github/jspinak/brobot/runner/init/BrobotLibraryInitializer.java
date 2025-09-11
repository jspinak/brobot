package io.github.jspinak.brobot.runner.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.FrameworkInitializer;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class BrobotLibraryInitializer {
    private static final Logger logger = LoggerFactory.getLogger(BrobotLibraryInitializer.class);

    private final FrameworkInitializer frameworkInitializer;
    private final BrobotRunnerProperties properties;
    private final ConfigurationParser jsonParser;
    private final ProjectConfigLoader projectConfigLoader;
    private final ConfigurationValidator configValidator;
    private final ImageResourceManager imageResourceManager;
    private final StateService allStatesInProjectService;

    private boolean initialized = false;
    private String lastErrorMessage = null;
    private ValidationResult lastValidationResult = null;

    @Autowired
    public BrobotLibraryInitializer(
            FrameworkInitializer initService,
            BrobotRunnerProperties properties,
            ConfigurationParser jsonParser,
            ProjectConfigLoader projectConfigLoader,
            ConfigurationValidator configValidator,
            ImageResourceManager imageResourceManager,
            StateService allStatesInProjectService) {
        this.frameworkInitializer = initService;
        this.properties = properties;
        this.jsonParser = jsonParser;
        this.projectConfigLoader = projectConfigLoader;
        this.configValidator = configValidator;
        this.imageResourceManager = imageResourceManager;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            setupDirectoriesAndProperties();

            // Initialize the image processing, but don't load configs yet
            // as the user may need to select config files first
            try {
                frameworkInitializer.setBundlePathAndPreProcessImages(properties.getImagePath());
            } catch (Exception e) {
                logger.warn(
                        "Initial image processing failed, will retry when path is set correctly",
                        e);
            }

            logger.info("Brobot library basic initialization completed");
        } catch (Exception e) {
            logger.error("Failed to initialize Brobot library", e);
            lastErrorMessage = "Initialization error: " + e.getMessage();
        }
    }

    /**
     * Initialize the Brobot library with the specified configuration files. This can be called from
     * the UI when the user selects configuration files.
     */
    public boolean initializeWithConfig(Path projectConfigPath, Path dslConfigPath) {
        lastErrorMessage = null;
        lastValidationResult = null;

        try {
            logger.info(
                    "Initializing Brobot library with config files: {} and {}",
                    projectConfigPath,
                    dslConfigPath);

            // Verify file existence
            if (!Files.exists(projectConfigPath)) {
                lastErrorMessage = "Project configuration file not found: " + projectConfigPath;
                logger.error(lastErrorMessage);
                return false;
            }

            if (!Files.exists(dslConfigPath)) {
                lastErrorMessage = "DSL configuration file not found: " + dslConfigPath;
                logger.error(lastErrorMessage);
                return false;
            }

            // Update the properties with the new paths
            properties.setConfigPath(projectConfigPath.getParent().toString());
            properties.setProjectConfigFile(projectConfigPath.getFileName().toString());
            properties.setDslConfigFile(dslConfigPath.getFileName().toString());

            // Validate the configuration files
            if (properties.isValidateConfiguration()) {
                try {
                    ValidationResult validationResult =
                            validateConfigurations(
                                    projectConfigPath,
                                    dslConfigPath,
                                    Paths.get(properties.getImagePath()));

                    lastValidationResult = validationResult;

                    if (validationResult.hasCriticalErrors()) {
                        List<ValidationError> criticalErrors = validationResult.getCriticalErrors();
                        lastErrorMessage =
                                "Configuration validation failed: "
                                        + criticalErrors.stream()
                                                .map(ValidationError::message)
                                                .collect(Collectors.joining(", "));

                        logger.error(
                                "Configuration validation failed: {}",
                                validationResult.getFormattedErrors());
                        return false;
                    }

                    if (validationResult.hasWarnings()) {
                        logger.warn("Configuration warnings: {}", validationResult.getWarnings());
                    }
                } catch (Exception e) {
                    lastErrorMessage = "Validation error: " + e.getMessage();
                    logger.error("Configuration validation error", e);
                    return false;
                }
            }

            // Build the project model from the configuration
            try {
                projectConfigLoader.loadAndValidate(
                        projectConfigPath, dslConfigPath, Paths.get(properties.getImagePath()));
                allStatesInProjectService
                        .getAllStates()
                        .forEach(
                                state -> {
                                    state.getStateImages()
                                            .forEach(imageResourceManager::updateStateImageCache);
                                });
            } catch (Exception e) {
                lastErrorMessage = "Failed to load project: " + e.getMessage();
                logger.error("Failed to load project configuration", e);
                return false;
            }

            // Initialize the state structure
            try {
                frameworkInitializer.initializeStateStructure();
            } catch (Exception e) {
                lastErrorMessage = "Failed to initialize state structure: " + e.getMessage();
                logger.error("Failed to initialize state structure", e);
                return false;
            }

            initialized = true;
            logger.info("Brobot library successfully initialized with configuration");
            return true;
        } catch (Exception e) {
            lastErrorMessage = "Unexpected error: " + e.getMessage();
            logger.error("Failed to initialize Brobot library with configuration", e);
            return false;
        }
    }

    /** Set up the required directories and system properties */
    private void setupDirectoriesAndProperties() {
        try {
            // Create the directories if they don't exist
            createDirectoryIfNotExists(properties.getConfigPath());
            createDirectoryIfNotExists(properties.getImagePath());
            createDirectoryIfNotExists(properties.getLogPath());
            createDirectoryIfNotExists(properties.getTempPath());

            // Set up system properties required by Brobot
            setupSystemProperties();

            logger.info("Directories and system properties set up successfully");
        } catch (IOException e) {
            logger.error("Error setting up directories", e);
            throw new RuntimeException("Failed to create required directories", e);
        }
    }

    /** Set up the system properties required by the Brobot library */
    private void setupSystemProperties() {
        // Set system properties that Brobot library components may need
        System.setProperty("brobot.imagePath", properties.getImagePath());
        System.setProperty("brobot.configPath", properties.getConfigPath());
        System.setProperty("brobot.logPath", properties.getLogPath());
    }

    /** Create a directory if it doesn't exist */
    private void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("Created directory: {}", path);
        }
    }

    /** Validate the configuration files */
    private ValidationResult validateConfigurations(
            Path projectPath, Path dslPath, Path imagePath) {
        try {
            String projectJson = Files.readString(projectPath);
            String dslJson = Files.readString(dslPath);

            return configValidator.validateConfiguration(projectJson, dslJson, imagePath);
        } catch (IOException e) {
            logger.error("Error reading configuration files for validation", e);
            ValidationResult result = new ValidationResult();
            result.addError(
                    new ValidationError(
                            "File read error",
                            "Could not read configuration files: " + e.getMessage(),
                            ValidationSeverity.CRITICAL));
            return result;
        }
    }

    /** Update the image path and reload images */
    public void updateImagePath(String newImagePath) {
        if (newImagePath == null || newImagePath.isEmpty()) {
            throw new IllegalArgumentException("Image path cannot be empty");
        }

        Path path = Paths.get(newImagePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Image path does not exist: " + newImagePath);
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Image path is not a directory: " + newImagePath);
        }

        properties.setImagePath(newImagePath);

        try {
            frameworkInitializer.setBundlePathAndPreProcessImages(newImagePath);
            logger.info("Updated image path to: {}", newImagePath);
        } catch (Exception e) {
            logger.error("Failed to process images in new path", e);
            throw new RuntimeException("Failed to process images in path: " + newImagePath, e);
        }
    }
}
