package com.brobot.app.restControllers;

import com.brobot.app.responses.PatternResponse;
import com.brobot.app.responses.PatternResponseMapper;
import com.brobot.app.services.PatternService;
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
    private final PatternResponseMapper patternResponseMapper;

    public PatternController(PatternService patternService,
                             PatternResponseMapper patternResponseMapper) {
        this.patternService = patternService;
        this.patternResponseMapper = patternResponseMapper;
    }

    @GetMapping("/all") // Maps to GET /api/patterns/all
    public List<PatternResponse> getAllPatterns() {
        return patternService.getAllPatterns().stream()
                .map(patternResponseMapper.INSTANCE::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}") // Maps to GET /api/patterns/{name}
    public List<PatternResponse> getPatterns(@PathVariable String name) {
        return patternService.getPatterns(name).stream()
                .map(patternResponseMapper.INSTANCE::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/") // Maps to GET /api/patterns/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/patterns/");
        return "Hello, World";
    }
}
