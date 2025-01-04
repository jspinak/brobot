package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.TimedAutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/automation")
public class TimedAutomationController {
    private static final Logger log = LoggerFactory.getLogger(AutomationController.class);

    private final TimedAutomationService timedAutomationService;

    public TimedAutomationController(TimedAutomationService timedAutomationService) {
        this.timedAutomationService = timedAutomationService;
    }

    @PostMapping("/start-timed")
    public ResponseEntity<?> startTimedAutomation(@RequestBody Map<String, Integer> request) {
        try {
            Integer seconds = request.get("seconds");
            if (seconds == null || seconds <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid duration. Please specify a positive number of seconds."));
            }

            if (timedAutomationService.isRunning()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Automation is already running"));
            }

            timedAutomationService.startTimedAutomation(seconds);
            return ResponseEntity.ok(Map.of(
                    "message", "Started timed automation for " + seconds + " seconds",
                    "duration", seconds
            ));
        } catch (Exception e) {
            log.error("Error starting timed automation", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to start automation: " + e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopAutomation() {
        try {
            timedAutomationService.stopTimedAutomation();
            return ResponseEntity.ok(Map.of("message", "Automation stopped successfully"));
        } catch (Exception e) {
            log.error("Error stopping automation", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to stop automation: " + e.getMessage()));
        }
    }
}
