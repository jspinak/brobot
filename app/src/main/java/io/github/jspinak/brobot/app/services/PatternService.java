package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.PatternEntityMapper;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.repositories.PatternRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatternService {

    private final PatternRepo patternRepo;
    //private final PatternMapper patternMapper = PatternMapper.INSTANCE;

    public PatternService(PatternRepo patternRepo) {
        this.patternRepo = patternRepo;
    }

    public List<Pattern> getPatterns(String name) {
        List<PatternEntity> patternEntities = patternRepo.findByName(name);
        return patternEntities.stream()
                //.map(patternMapper::map)
                .map(PatternEntityMapper::map)
                .collect(Collectors.toList());
    }

    public List<Pattern> getAllPatterns() {
        List<PatternEntity> patternEntities = patternRepo.findAll();
        return patternEntities.stream()
                //.map(patternMapper::map)
                .map(PatternEntityMapper::map)
                .collect(Collectors.toList());
    }

    public void savePatterns(Pattern... patterns) {
        savePatterns(List.of(patterns));
    }

    public void savePatterns(List<Pattern> patterns) {
        patterns.forEach(pattern -> {
            //patternRepo.save(patternMapper.map(pattern));
            patternRepo.save(PatternEntityMapper.map(pattern));
        });
    }
}
