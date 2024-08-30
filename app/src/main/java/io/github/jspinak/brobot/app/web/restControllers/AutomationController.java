package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.AutomationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @PostMapping("/run-transition-test")
    public ResponseEntity<String> testAllTransitions() {
        String result = automationService.runAutomation();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-mouse-move")
    public ResponseEntity<String> testMouseMove() {
        String result = automationService.testMouseMove();
        return ResponseEntity.ok(result);
    }

}