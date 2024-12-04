package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.repositories.ProjectRepository;
import io.github.jspinak.brobot.app.models.BuildModel;
import io.github.jspinak.brobot.app.services.AutomationService;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.datatypes.Project;
import lombok.Data;
import org.sikuli.script.ImagePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {
    private static final Logger log = LoggerFactory.getLogger(AutomationController.class);

    private final AutomationService automationService;
    private final BuildModel buildModel;
    private final ProjectRepository projectRepository;

    public AutomationController(AutomationService automationService,
                                BuildModel buildModel, ProjectRepository projectRepository) {
        this.automationService = automationService;
        this.buildModel = buildModel;
        this.projectRepository = projectRepository;
    }

    @Data
    public static class ApiError {
        private final String message;
        private final String details;

        public ApiError(String message, String details) {
            this.message = message;
            this.details = details;
        }
    }

    @PostMapping("/visit-all-states")
    public ResponseEntity<?> visitAllStates() {
        try {
            String result = automationService.visitAllStates();
            return ResponseEntity.ok(Map.of(
                    "message", result,
                    "status", "SUCCESS"
            ));
        } catch (Exception e) {
            log.error("Error in visit-all-states: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to execute automation",
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/run-transition-test")
    public ResponseEntity<String> testAllTransitions(@RequestBody ProjectRequest projectRequest) {
        String result = automationService.integrateModelIntoFramework(projectRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-mouse-move")
    public ResponseEntity<String> testMouseMove() {
        String result = automationService.testMouseMove();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-visit-images")
    public ResponseEntity<String> testVisitImages() {
        String result = automationService.testVisitAllImages();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/set-image-path")
    public ResponseEntity<String> setImagePath(@RequestBody Map<String, String> request) {
        String imagePath = request.get("imagePath");
        if (imagePath == null || imagePath.isEmpty()) {
            return ResponseEntity.badRequest().body("Image path cannot be empty");
        }

        try {
            ImagePath.setBundlePath(imagePath);
            return ResponseEntity.ok("Image path set successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error setting image path: " + e.getMessage());
        }
    }

    @PostMapping("/save-to-library/{projectId}")
    public ResponseEntity<?> saveToLibrary(@PathVariable Long projectId) {
        try {
            Optional<ProjectEntity> optionalProjectEntity = projectRepository.findById(projectId);
            if (optionalProjectEntity.isPresent()) {
                buildModel.build(projectId);
                return ResponseEntity.ok(Map.of(
                        "message", "State structure saved to library successfully",
                        "projectId", projectId
                ));
            }
            return ResponseEntity.badRequest().body("No such project.");
        } catch (Exception e) {
            log.error("Error saving state structure to library", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError(
                            "Failed to save state structure",
                            e.getMessage()
                    ));
        }
    }

}