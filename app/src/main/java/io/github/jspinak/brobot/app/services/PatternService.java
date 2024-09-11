package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.PatternEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.repositories.PatternRepo;
import io.github.jspinak.brobot.app.stateStructureBuilders.GetPatternsFromBundlePath;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.responseMappers.PatternResponseMapper;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import jakarta.transaction.Transactional;
import org.sikuli.script.ImagePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatternService {
    private static final Logger log = LoggerFactory.getLogger(PatternService.class);

    private final PatternRepo patternRepo;
    private final PatternEntityMapper patternEntityMapper;
    private final PatternResponseMapper patternResponseMapper;
    private final GetPatternsFromBundlePath getPatternsFromBundlePath;
    private final ImageService imageService;

    public PatternService(PatternRepo patternRepo, PatternEntityMapper patternEntityMapper,
                          PatternResponseMapper patternResponseMapper,
                          GetPatternsFromBundlePath getPatternsFromBundlePath,
                          ImageService imageService) {
        this.patternRepo = patternRepo;
        this.patternEntityMapper = patternEntityMapper;
        this.patternResponseMapper = patternResponseMapper;
        this.getPatternsFromBundlePath = getPatternsFromBundlePath;
        this.imageService = imageService;
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

    public void savePatternsFromBundlePath() {
        getPatternsFromBundlePath.savePatternsToList().forEach(pattern ->
                patternRepo.save(patternEntityMapper.map(pattern)));
    }

    @Transactional
    public List<PatternResponse> loadAndSavePatternsFromBundle(String bundlePath) {
        log.info("Loading patterns from bundle path: {}", bundlePath);
        ImagePath.setBundlePath(bundlePath);
        List<Pattern> patterns = getPatternsFromBundlePath.savePatternsToList();
        log.info("Loaded {} patterns from bundle", patterns.size());

        List<PatternEntity> savedEntities = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Optional<PatternEntity> existingPattern = patternRepo.findByImgpath(pattern.getImgpath());
            if (existingPattern.isPresent()) {
                log.info("Pattern with imgpath {} already exists, skipping", pattern.getImgpath());
                savedEntities.add(existingPattern.get());
            } else {
                PatternEntity entity = patternEntityMapper.map(pattern);
                PatternEntity saved = patternRepo.save(entity);
                log.info("Saved new pattern: id={}, name={}, imgpath={}", saved.getId(), saved.getName(), saved.getImgpath());
                savedEntities.add(saved);
            }
        }

        List<PatternResponse> responses = savedEntities.stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList());

        log.info("Returning {} pattern responses", responses.size());
        return responses;
    }

    @Transactional
    public PatternEntity createOrUpdatePattern(PatternRequest request) {
        log.info("Creating or updating pattern: id={}, name={}, imgpath={}", request.getId(), request.getName(), request.getImgpath());
        PatternEntity pattern;
        if (request.getId() != null) {
            pattern = patternRepo.findById(request.getId())
                    .orElseGet(() -> {
                        log.warn("Pattern with id {} not found, checking by imgpath", request.getId());
                        return patternRepo.findByImgpath(request.getImgpath())
                                .orElseGet(() -> {
                                    log.warn("Pattern with imgpath {} not found, creating new", request.getImgpath());
                                    return new PatternEntity();
                                });
                    });
        } else {
            pattern = patternRepo.findByImgpath(request.getImgpath())
                    .orElseGet(() -> {
                        log.info("Creating new pattern with imgpath {}", request.getImgpath());
                        return new PatternEntity();
                    });
        }

        // Update pattern fields
        pattern.setName(request.getName());
        pattern.setImgpath(request.getImgpath());
        // Set other fields...

        if (request.getImage() != null) {
            ImageEntity image = imageService.createOrUpdateImage(request.getImage());
            pattern.setImage(image);
        }

        PatternEntity savedPattern = patternRepo.save(pattern);
        log.info("Saved pattern: id={}, name={}, imgpath={}", savedPattern.getId(), savedPattern.getName(), savedPattern.getImgpath());
        return savedPattern;
    }

    public List<PatternEntity> getOrCreatePatterns(List<PatternRequest> requests) {
        return requests.stream()
                .map(this::createOrUpdatePattern)
                .collect(Collectors.toList());
    }
}
