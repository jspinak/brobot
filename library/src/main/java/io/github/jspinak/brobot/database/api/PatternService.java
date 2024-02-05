package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.database.data.PatternRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatternService {

    private final PatternRepo patternRepo;

    public PatternService(PatternRepo patternRepo) {
        this.patternRepo = patternRepo;
    }

    public Pattern getPattern(String name) {
        Pattern pattern = patternRepo.findByName(name).orElse(null);
        if (pattern != null) pattern.setBufferedImageFromBytes();
        return pattern;
    }

    public List<Pattern> getAllPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patternRepo.findAll().forEach(patterns::add);
        for (Pattern pattern : patterns) {
            pattern.setBufferedImageFromBytes();
        }
        return patterns;
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
