// ImageResourceValidator.java
package io.github.jspinak.brobot.schemaValidation.resource;

import io.github.jspinak.brobot.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.schemaValidation.model.ValidationSeverity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Validates that image resources referenced in the project configuration actually exist
 * and are valid image files.
 */
@Component
public class ImageResourceValidator {
    private static final Logger logger = LoggerFactory.getLogger(ImageResourceValidator.class);
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("png", "jpg", "jpeg", "gif", "bmp")
    );

    /**
     * Validates that all image resources referenced in the project exist and are valid.
     *
     * @param projectJson   The project JSON to validate
     * @param imageBasePath The base path where image files are located
     * @return ValidationResult containing any resource validation errors
     */
    public ValidationResult validateImageResources(JSONObject projectJson, Path imageBasePath) {
        ValidationResult result = new ValidationResult();

        // Check if project JSON is valid
        if (projectJson == null) {
            result.addError(new ValidationError(
                    "Invalid project JSON",
                    "Project JSON is null or invalid",
                    ValidationSeverity.ERROR
            ));
            return result;
        }

        // Check if image base path exists and is a directory
        if (imageBasePath == null || !Files.exists(imageBasePath) || !Files.isDirectory(imageBasePath)) {
            result.addError(new ValidationError(
                    "Invalid image base path",
                    "Image base path does not exist or is not a directory: " +
                    (imageBasePath != null ? imageBasePath.toString() : "null"),
                    ValidationSeverity.ERROR
            ));
            return result;
        }

        try {
            // Process the project configuration to find all image references
            Set<String> imageReferences = extractImageReferences(projectJson);

            // Validate each image reference
            for (String imageRef : imageReferences) {
                validateImageReference(imageRef, imageBasePath, result);
            }

        } catch (Exception e) {
            logger.error("Error validating image resources", e);
            result.addError(new ValidationError(
                    "Resource validation error",
                    "Error during image resource validation: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Extracts all image references from the project configuration.
     *
     * @param project Project configuration as JSONObject
     * @return Set of image path strings
     */
    private Set<String> extractImageReferences(JSONObject project) {
        Set<String> images = new HashSet<>();

        // Extract from states and their state images
        if (project.has("states")) {
            org.json.JSONArray states = project.getJSONArray("states");

            for (int i = 0; i < states.length(); i++) {
                JSONObject state = states.getJSONObject(i);
                if (state.has("stateImages")) {
                    org.json.JSONArray stateImages = state.getJSONArray("stateImages");

                    for (int j = 0; j < stateImages.length(); j++) {
                        JSONObject image = stateImages.getJSONObject(j);
                        if (image.has("patterns")) {
                            org.json.JSONArray patterns = image.getJSONArray("patterns");

                            for (int k = 0; k < patterns.length(); k++) {
                                JSONObject pattern = patterns.getJSONObject(k);
                                if (pattern.has("imgPath")) {
                                    String imgPath = pattern.getString("imgPath");
                                    if (imgPath != null && !imgPath.isEmpty()) {
                                        images.add(imgPath);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return images;
    }
    /**
     * Validates a single image reference.
     *
     * @param imageRef Image reference path
     * @param imageBasePath Base path where images are stored
     * @param result Validation result to update
     */
    private void validateImageReference(String imageRef, Path imageBasePath, ValidationResult result) {
        // Handle absolute and relative paths
        Path imagePath;
        if (Path.of(imageRef).isAbsolute()) {
            imagePath = Path.of(imageRef);
        } else {
            imagePath = imageBasePath.resolve(imageRef);
        }

        // Check if file exists
        if (!Files.exists(imagePath)) {
            result.addError(new ValidationError(
                    "Missing image resource",
                    String.format("Image file not found: %s", imageRef),
                    ValidationSeverity.ERROR
            ));
            return;
        }

        // Check if it's a file (not a directory)
        if (!Files.isRegularFile(imagePath)) {
            result.addError(new ValidationError(
                    "Invalid image resource",
                    String.format("Image path is not a file: %s", imageRef),
                    ValidationSeverity.ERROR
            ));
            return;
        }

        // Check file extension
        String fileName = imagePath.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            result.addError(new ValidationError(
                    "Invalid image format",
                    String.format("Image file has no extension: %s", imageRef),
                    ValidationSeverity.WARNING
            ));
            return;
        }

        String extension = fileName.substring(extensionIndex + 1).toLowerCase();
        if (!SUPPORTED_IMAGE_EXTENSIONS.contains(extension)) {
            result.addError(new ValidationError(
                    "Unsupported image format",
                    String.format("Image format '%s' is not supported: %s", extension, imageRef),
                    ValidationSeverity.WARNING
            ));
            return;
        }

        // Try to open the image to verify it's valid
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                result.addError(new ValidationError(
                        "Invalid image format",
                        String.format("File is not a valid image: %s", imageRef),
                        ValidationSeverity.WARNING
                ));
                return;
            }

            // Check for very small or zero-sized images
            if (image.getWidth() < 2 || image.getHeight() < 2) {
                result.addError(new ValidationError(
                        "Suspicious image dimensions",
                        String.format("Image is extremely small (%dx%d): %s",
                                image.getWidth(), image.getHeight(), imageRef),
                        ValidationSeverity.WARNING
                ));
            }

        } catch (IOException e) {
            result.addError(new ValidationError(
                    "Invalid image format",
                    String.format("Could not read image file %s: %s", imageRef, e.getMessage()),
                    ValidationSeverity.WARNING
            ));
        }
    }
}