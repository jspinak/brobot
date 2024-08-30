package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.PatternEntityMapper;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.repositories.PatternRepo;
import io.github.jspinak.brobot.app.web.responseMappers.PatternResponseMapper;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatternService {

    private final PatternRepo patternRepo;
    private final PatternEntityMapper patternEntityMapper;
    private final PatternResponseMapper patternResponseMapper;

    public PatternService(PatternRepo patternRepo, PatternEntityMapper patternEntityMapper,
                          PatternResponseMapper patternResponseMapper) {
        this.patternRepo = patternRepo;
        this.patternEntityMapper = patternEntityMapper;
        this.patternResponseMapper = patternResponseMapper;
    }

    public List<Pattern> getPatterns(String name) {
        List<PatternEntity> patternEntities = patternRepo.findByName(name);
        return patternEntities.stream()
                .map(patternEntityMapper::map)
                .collect(Collectors.toList());
    }

    public List<Pattern> getAllPatterns() {
        List<PatternEntity> patternEntities = patternRepo.findAll();
        return patternEntities.stream()
                .map(patternEntityMapper::map)
                .collect(Collectors.toList());
    }

    public List<PatternResponse> getPatternResponses(String name) {
        List<PatternEntity> patternEntities = patternRepo.findByName(name);
        return patternEntities.stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList());
    }

    public List<PatternResponse> getAllPatternResponses() {
        List<PatternEntity> patternEntities = patternRepo.findAll();
        return patternEntities.stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList());
    }


    public void savePatterns(Pattern... patterns) {
        savePatterns(List.of(patterns));
    }

    public void savePatterns(List<Pattern> patterns) {
        patterns.forEach(pattern -> {
            //patternRepo.save(patternMapper.map(pattern));
            patternRepo.save(patternEntityMapper.map(pattern));
        });
    }
}
