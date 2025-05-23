package io.github.jspinak.brobot.runner.init;

import io.github.jspinak.brobot.json.schemaValidation.ConfigValidator;
import io.github.jspinak.brobot.json.schemaValidation.exception.ConfigValidationException;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and validates project and DSL configuration files.
 */
@Component
public class ProjectConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ProjectConfigLoader.class);

    private final ConfigValidator configValidator;

    public ProjectConfigLoader(ConfigValidator configValidator) {
        this.configValidator = configValidator;
    }

    /**
     * Load and validate both the project and DSL configuration files.
     *
     * @param projectConfigPath Path to the project configuration file
     * @param dslConfigPath Path to the DSL configuration file
     * @param imageBasePath Base folder for images
     * @return ValidationResult Holds the validation results
     * @throws IOException Reports any IO errors that occur while reading the files
     * @throws ConfigValidationException for any validation errors
     */
    public ValidationResult loadAndValidate(Path projectConfigPath, Path dslConfigPath, Path imageBasePath)
            throws IOException, ConfigValidationException {

        String projectJson = Files.readString(projectConfigPath);
        String dslJson = Files.readString(dslConfigPath);

        logger.info("Validate project and dsl config...");

        ValidationResult result = configValidator.validateConfiguration(projectJson, dslJson, imageBasePath);

        logger.info("Validation completed, number of errors found: {}", result.getErrors().size());
        return result;
    }
}