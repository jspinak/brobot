package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patterns") // Base path for all endpoints in this controller
public class PatternController {

    private final PatternService patternService;

    public PatternController(PatternService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/") // Maps to GET /api/patterns/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/patterns/");
        return "Hello, World";
    }

    @GetMapping("/all") // Maps to GET /api/patterns/all
    public List<PatternResponse> getAllPatterns() {
        return patternService.getAllPatternResponses();
    }

    @GetMapping("/{name}") // Maps to GET /api/patterns/{name}
    public List<PatternResponse> getPatterns(@PathVariable String name) {
        return patternService.getPatternResponses(name);
    }

    @PostMapping("/load-from-bundle")
    public ResponseEntity<List<PatternResponse>> loadPatternsFromBundle(@RequestBody Map<String, String> request) {
        String bundlePath = request.get("bundlePath");
        if (bundlePath == null || bundlePath.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<PatternResponse> patterns = patternService.loadAndSavePatternsFromBundle(bundlePath);
        return ResponseEntity.ok(patterns);
    }

}
