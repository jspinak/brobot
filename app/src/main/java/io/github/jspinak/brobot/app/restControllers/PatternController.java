package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.responses.PatternResponse;
import io.github.jspinak.brobot.app.responses.PatternResponseMapper;
import io.github.jspinak.brobot.app.services.PatternService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patterns") // Base path for all endpoints in this controller
public class PatternController {

    private final PatternService patternService;
    private final PatternResponseMapper patternResponseMapper = PatternResponseMapper.INSTANCE;

    public PatternController(PatternService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/all") // Maps to GET /api/patterns/all
    public List<PatternResponse> getAllPatterns() {
        return patternService.getAllPatterns().stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}") // Maps to GET /api/patterns/{name}
    public List<PatternResponse> getPatterns(@PathVariable String name) {
        return patternService.getPatterns(name).stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/") // Maps to GET /api/patterns/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/patterns/");
        return "Hello, World";
    }
}
