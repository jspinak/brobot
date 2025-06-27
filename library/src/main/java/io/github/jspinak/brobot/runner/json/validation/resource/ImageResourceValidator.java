// ImageResourceValidator.java
package io.github.jspinak.brobot.runner.json.validation.resource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Validates image resources referenced in Brobot project configurations.
 * 
 * <p>This validator ensures that all image files referenced in the configuration
 * actually exist, are accessible, and are valid image files. Image validation is
 * crucial for Brobot's visual automation capabilities, as missing or corrupted
 * images will cause automation failures at runtime.</p>
 * 
 * <h2>Validation Scope:</h2>
 * <ul>
 *   <li>Verifies image files exist at specified paths</li>
 *   <li>Validates files are actual images (not just named like images)</li>
 *   <li>Checks image format compatibility</li>
 *   <li>Detects suspicious image properties (e.g., 0x0 dimensions)</li>
 * </ul>
 * 
 * <h2>Supported Image Formats:</h2>
 * <ul>
 *   <li>PNG - Preferred format for UI automation</li>
 *   <li>JPG/JPEG - Common screenshot format</li>
 *   <li>GIF - Supported but not recommended</li>
 *   <li>BMP - Supported but larger file sizes</li>
 * </ul>
 * 
 * <h2>Path Resolution:</h2>
 * <p>The validator supports both absolute and relative image paths:</p>
 * <ul>
 *   <li><b>Absolute paths</b> - Used as-is (e.g., /home/user/images/button.png)</li>
 *   <li><b>Relative paths</b> - Resolved against the provided base path</li>
 * </ul>
 * 
 * <h2>Why Image Validation Matters:</h2>
 * <p>Brobot relies on image recognition to identify UI elements. Invalid images cause:</p>
 * <ul>
 *   <li>Runtime failures when patterns cannot be loaded</li>
 *   <li>False negatives when corrupt images don't match</li>
 *   <li>Performance issues with improperly formatted images</li>
 *   <li>Difficult debugging when images silently fail to load</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ImageResourceValidator validator = new ImageResourceValidator();
 * Path imageBase = Paths.get("/project/images");
 * 
 * ValidationResult result = validator.validateImageResources(
 *     projectJson, imageBase);
 * 
 * // Handle missing images
 * result.getErrors().stream()
 *     .filter(e -> e.errorCode().equals("Missing image resource"))
 *     .forEach(e -> {
 *         logger.error("Image not found: {}", e.message());
 *         // Could attempt to find image in alternate locations
 *     });
 * }</pre>
 * 
 * @see ConfigurationValidator for integration in the validation pipeline
 * @see ValidationResult for handling validation outcomes
 * @author jspinak
 */
@Component
public class ImageResourceValidator {
    private static final Logger logger = LoggerFactory.getLogger(ImageResourceValidator.class);
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("png", "jpg", "jpeg", "gif", "bmp")
    );

    /**
     * Validates all image resources referenced in the project configuration.
     * 
     * <p>This method scans the project configuration for all image references and
     * validates each one. It performs both existence checks and content validation
     * to ensure images are usable by the automation framework.</p>
     * 
     * <h3>Validation Process:</h3>
     * <ol>
     *   <li>Validates input parameters (non-null JSON and valid base path)</li>
     *   <li>Extracts all image references from the configuration</li>
     *   <li>For each image reference:
     *     <ul>
     *       <li>Resolves the full path (handling relative/absolute)</li>
     *       <li>Checks file existence and readability</li>
     *       <li>Validates image format and content</li>
     *       <li>Warns about suspicious properties</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Error Types:</h3>
     * <ul>
     *   <li><b>ERROR</b> - Missing files, invalid paths, unreadable images</li>
     *   <li><b>WARNING</b> - Unsupported formats, suspicious dimensions, corrupt files</li>
     * </ul>
     * 
     * @param projectJson The project configuration as a JSONObject containing
     *                    state definitions with image references
     * @param imageBasePath Base directory path for resolving relative image paths.
     *                      Must be an existing, readable directory
     * @return ValidationResult containing all discovered image resource issues.
     *         Check {@link ValidationResult#hasErrors()} to determine if any
     *         critical issues were found
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
     * Extracts all unique image path references from the project configuration.
     * 
     * <p>This method performs a deep scan of the project structure to find all
     * image paths. Images in Brobot configurations are typically found in state
     * definitions as patterns used for visual recognition.</p>
     * 
     * <h3>Configuration Structure:</h3>
     * <pre>
     * {
     *   "states": [{
     *     "stateImages": [{
     *       "patterns": [{
     *         "imgPath": "buttons/submit.png"
     *       }]
     *     }]
     *   }]
     * }
     * </pre>
     * 
     * <h3>Extraction Logic:</h3>
     * <p>The method traverses the nested structure: states → stateImages → patterns → imgPath,
     * collecting all non-empty image paths into a set to avoid duplicates.</p>
     * 
     * @param project Project configuration containing state definitions
     * @return Set of unique image path strings found in the configuration,
     *         may be empty if no images are referenced
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
     * Validates a single image reference for existence and validity.
     * 
     * <p>This method performs comprehensive validation of an individual image file,
     * including path resolution, existence checking, format validation, and content
     * verification. It adds appropriate errors or warnings to the validation result
     * based on what issues are found.</p>
     * 
     * <h3>Validation Steps:</h3>
     * <ol>
     *   <li><b>Path Resolution</b> - Converts relative paths to absolute using base path</li>
     *   <li><b>Existence Check</b> - Ensures file exists at the resolved path</li>
     *   <li><b>File Type Check</b> - Verifies it's a regular file, not a directory</li>
     *   <li><b>Extension Check</b> - Validates against supported image formats</li>
     *   <li><b>Content Validation</b> - Attempts to read the image to verify validity</li>
     *   <li><b>Dimension Check</b> - Warns about suspiciously small images</li>
     * </ol>
     * 
     * <h3>Error Conditions:</h3>
     * <ul>
     *   <li><b>ERROR</b> - File not found or not a regular file</li>
     *   <li><b>WARNING</b> - Missing extension, unsupported format, invalid content,
     *       suspicious dimensions</li>
     * </ul>
     * 
     * @param imageRef The image path reference from the configuration, either
     *                 absolute or relative
     * @param imageBasePath Base directory for resolving relative paths
     * @param result ValidationResult to accumulate errors and warnings found
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