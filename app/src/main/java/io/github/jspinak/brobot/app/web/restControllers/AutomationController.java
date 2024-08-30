package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.AutomationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    /*
    @PostMapping("/test-transitions")
    public ResponseEntity<String> testAllTransitions() {
        String result = automationService.runAutomation();
        return ResponseEntity.ok(result);
    }

     */
}