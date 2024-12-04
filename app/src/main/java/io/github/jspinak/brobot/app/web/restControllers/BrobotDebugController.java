package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.BrobotDebugService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/brobot")
public class BrobotDebugController {
    private final BrobotDebugService debugService;

    public BrobotDebugController(BrobotDebugService debugService) {
        this.debugService = debugService;
    }

    @GetMapping("/states")
    public ResponseEntity<Map<String, Object>> getStateDebugInfo() {
        return ResponseEntity.ok(debugService.getLibraryDebugInfo());
    }
}
