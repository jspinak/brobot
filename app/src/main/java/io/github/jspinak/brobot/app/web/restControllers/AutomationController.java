package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder.Recorder;
import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.repositories.ProjectRepository;
import io.github.jspinak.brobot.app.models.BuildModel;
import io.github.jspinak.brobot.app.services.AutomationService;
import io.github.jspinak.brobot.app.web.requests.CaptureScreenshotsRequest;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import lombok.Data;
import org.sikuli.script.ImagePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {
    private static final Logger log = LoggerFactory.getLogger(AutomationController.class);

    private final AutomationService automationService;
    private final Recorder recorder;
    private final BuildModel buildModel;
    private final ProjectRepository projectRepository;

    public AutomationController(AutomationService automationService, Recorder recorder,
                                BuildModel buildModel, ProjectRepository projectRepository) {
        this.automationService = automationService;
        this.recorder = recorder;
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

    @PostMapping("/visit-all-images")
    public ResponseEntity<?> visitAllImages() {
        try {
            String result = automationService.visitAllStates(true);
            return ResponseEntity.ok(Map.of(
                    "message", result,
                    "status", "SUCCESS"
            ));
        } catch (Exception e) {
            log.error("Error in visit-all-images: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to execute automation",
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/visit-all-states")
    public ResponseEntity<?> visitAllStates() {
        try {
            String result = automationService.visitAllStates(false);
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

    @PostMapping("/capture-screenshots")
    public ResponseEntity<?> captureScreenshots(@RequestBody CaptureScreenshotsRequest request) {
        try {
            automationService.captureScreenshots(request.getSecondsToCapture(), request.getCaptureFrequency());
            return ResponseEntity.ok(Map.of(
                    "message", "Screenshots captured successfully",
                    "status", "SUCCESS"
            ));
        } catch (Exception e) {
            log.error("Error capturing screenshots", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to capture screenshots",
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/stop-capture-screenshots")
    public ResponseEntity<?> stopCaptureScreenshots() {
        try {
            automationService.stopCaptureScreenshots();
            return ResponseEntity.ok(Map.of(
                    "message", "Screenshot capture stopped successfully",
                    "status", "SUCCESS"
            ));
        } catch (Exception e) {
            log.error("Error stopping screenshot capture", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to stop screenshot capture",
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/set-recording-location")
    public ResponseEntity<?> setRecordingLocation(@RequestBody Map<String, String> request) {
        String location = request.get("location");
        if (location == null || location.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Location cannot be empty"
            ));
        }

        try {
            recorder.setRecordingDirectory(location);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Recording location set successfully"
            ));
        } catch (Exception e) {
            log.error("Error setting recording location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "Failed to set recording location: " + e.getMessage()
                    ));
        }
    }
}