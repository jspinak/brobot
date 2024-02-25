package io.github.jspinak.brobot.database.services;

import io.github.jspinak.brobot.database.data.PatternRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatternService {

    private final PatternRepo patternRepo;

    public PatternService(PatternRepo patternRepo) {
        this.patternRepo = patternRepo;
    }

    public List<Pattern> getPatterns(String name) {
        return patternRepo.findByName(name).stream()
                .peek(Pattern::setBufferedImageFromBytes)
                .collect(Collectors.toList());
    }

    public List<Pattern> getAllPatterns() {
        return patternRepo.findAll().stream()
                .peek(Pattern::setBufferedImageFromBytes)
                .collect(Collectors.toList());
    }

    public void savePatterns(Pattern... patterns) {
        savePatterns(List.of(patterns));
    }

    public void savePatterns(List<Pattern> patterns) {
        patterns.forEach(pattern -> {
            pattern.setBytes();
            patternRepo.save(pattern);
        });
    }
}
