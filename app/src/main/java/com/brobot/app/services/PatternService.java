package com.brobot.app.services;

import com.brobot.app.database.entities.PatternEntity;
import com.brobot.app.database.mappers.PatternMapper;
import com.brobot.app.database.repositories.PatternRepo;
import com.brobot.app.responses.PatternResponseMapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatternService {

    private final PatternRepo patternRepo;
    private final PatternMapper patternMapper;

    public PatternService(PatternRepo patternRepo,
                          PatternMapper patternMapper) {
        this.patternRepo = patternRepo;
        this.patternMapper = patternMapper;
    }

    public List<Pattern> getPatterns(String name) {
        List<PatternEntity> patternEntities = patternRepo.findByName(name);
        return patternEntities.stream()
                .map(patternMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }

    public List<Pattern> getAllPatterns() {
        List<PatternEntity> patternEntities = patternRepo.findAll();
        return patternEntities.stream()
                .map(patternMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }

    public void savePatterns(Pattern... patterns) {
        savePatterns(List.of(patterns));
    }

    public void savePatterns(List<Pattern> patterns) {
        patterns.forEach(pattern -> {
            patternRepo.save(patternMapper.INSTANCE.mapToEntity(pattern));
        });
    }
}
