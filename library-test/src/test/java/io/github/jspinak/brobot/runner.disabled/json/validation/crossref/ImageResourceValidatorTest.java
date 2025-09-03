package io.github.jspinak.brobot.json.schemaValidation.resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.resource.ImageResourceValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageResourceValidatorTest {

    private ImageResourceValidator validator;

    @TempDir
    Path tempDir;

    private Path imageDir;
    private Path validImageFile;
    private JSONObject validProject;

    @BeforeEach
    void setUp() throws IOException, JSONException {
        validator = new ImageResourceValidator();

        // Create a temporary folder for images
        imageDir = tempDir.resolve("images");
        Files.createDirectories(imageDir);

        // Create a valid image file (PNG)
        validImageFile = imageDir.resolve("test-image.png");
        Files.write(validImageFile, new byte[]{
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'  // PNG-Header
        });

        // Create a valid project JSON object
        String projectJson = """
        {
            "id": 1,
            "name": "Test Project",
            "states": [
                {
                    "id": 1,
                    "name": "State1",
                    "stateImages": [
                        {
                            "id": 101,
                            "name": "TestImage",
                            "patterns": [
                                {
                                    "name": "Pattern1",
                                    "imgPath": "test-image.png"
                                }
                            ]
                        }
                    ]
                }
            ],
            "stateTransitions": [
                {
                    "id": 1,
                    "sourceStateId": 1,
                    "actionDefinition": {
                        "steps": [
                            {
                                "actionOptions": {"action": "CLICK"},
                                "objectCollection": {"stateImages": [101]}
                            }
                        ]
                    },
                    "statesToEnter": [2]
                }
            ]
        }
        """;

        validProject = new JSONObject(projectJson);
    }

    @Test
    void validateImageResources_whenValidProject_shouldReturnValidResult() {
        ValidationResult result = validator.validateImageResources(validProject, imageDir);
        System.out.println(result.getWarnings());
        System.out.println(result.getErrors());
        assertTrue(result.isValid());
        assertEquals(0, result.getErrorsAndCritical().size());
    }

    @Test
    void validate_whenImageBasePathIsInvalid_shouldReturnError() {
        // Use an invalid base path
        Path invalidBasePath = Paths.get("/nicht/existierender/pfad");

        // Validate the project with the invalid base path
        ValidationResult result = validator.validateImageResources(validProject, invalidBasePath);

        // Check that the result is not valid
        assertFalse(result.isValid());

        // Check if the error message is as expected
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid image base path")));
    }

    @Test
    void validateImageResources_withMissingImage_shouldReturnError() {
        // Create a project with a non-existing image
        JSONObject projectWithMissingImage = new JSONObject(validProject.toString());
        projectWithMissingImage.getJSONArray("states")
                .getJSONObject(0)
                .getJSONArray("stateImages")
                .getJSONObject(0)
                .getJSONArray("patterns")
                .getJSONObject(0)
                .put("imgPath", "non-existent.png");

        ValidationResult result = validator.validateImageResources(projectWithMissingImage, imageDir);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Missing image resource")));
    }

    @Test
    void validateImageResources_withInvalidImageFormat_shouldReturnWarning() throws IOException {
        // Create an invalid image file (not a valid PNG)
        Path invalidImageFile = imageDir.resolve("invalid-image.png");
        Files.writeString(invalidImageFile, "This is not a valid image file");

        // Create a project with an invalid image
        JSONObject projectWithInvalidImage = new JSONObject(validProject.toString());
        projectWithInvalidImage.getJSONArray("states")
                .getJSONObject(0)
                .getJSONArray("stateImages")
                .getJSONObject(0)
                .getJSONArray("patterns")
                .getJSONObject(0)
                .put("imgPath", invalidImageFile.getFileName().toString());

        ValidationResult result = validator.validateImageResources(projectWithInvalidImage, imageDir);

        assertTrue(result.hasWarnings());
        System.out.println(result.getWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid image format")));
    }

    @Test
    void validateImageResources_withoutStates_shouldReturnValidResult() {
        JSONObject projectWithoutStates = new JSONObject()
                .put("id", 1)
                .put("name", "Project without States");

        ValidationResult result = validator.validateImageResources(projectWithoutStates, imageDir);

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void validateImageResources_withNullProject_shouldReturnError() {
        ValidationResult result = validator.validateImageResources(null, imageDir);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.ERROR, result.getErrors().getFirst().severity());
    }

    @Test
    void validateImageResources_withNonExistingPath_shouldReturnError() {
        ValidationResult result = validator.validateImageResources(validProject, Path.of("non-existent-path"));

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid image base path")));
    }
}