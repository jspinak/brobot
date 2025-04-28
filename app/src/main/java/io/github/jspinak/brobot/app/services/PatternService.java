package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ImageEntityMapper;
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
    private final ImageEntityMapper imageEntityMapper;

    public PatternService(PatternRepo patternRepo, PatternEntityMapper patternEntityMapper,
                          PatternResponseMapper patternResponseMapper,
                          GetPatternsFromBundlePath getPatternsFromBundlePath,
                          ImageService imageService, ImageEntityMapper imageEntityMapper) {
        this.patternRepo = patternRepo;
        this.patternEntityMapper = patternEntityMapper;
        this.patternResponseMapper = patternResponseMapper;
        this.getPatternsFromBundlePath = getPatternsFromBundlePath;
        this.imageService = imageService;
        this.imageEntityMapper = imageEntityMapper;
    }

    public List<Pattern> getPatterns(String name) {
        List<PatternEntity> patternEntities = patternRepo.findByName(name);
        return patternEntities.stream()
                .map(patternEntity -> patternEntityMapper.map(patternEntity, imageService))  // Use a method that handles image fetching
                .collect(Collectors.toList());
    }

    public List<PatternEntity> getPatternEntities(List<Long> patternIds) {
        return new ArrayList<>(patternRepo.findAllById(patternIds));
    }

    public PatternEntity map(Pattern pattern) {
        return patternEntityMapper.map(pattern, imageService);
    }

    public Pattern map(PatternEntity patternEntity) {
        return patternEntityMapper.map(patternEntity, imageService);
    }

    public List<PatternEntity> map(List<Pattern> patterns) {
        return patterns.stream()
                .map(pattern -> patternEntityMapper.map(pattern, imageService))
                .collect(Collectors.toList());
    }

    public List<Pattern> mapToPatterns(List<PatternEntity> patternEntities) {
        return patternEntityMapper.mapToPatternList(patternEntities, imageService);
    }

    public List<Pattern> getAllPatterns() {
        List<PatternEntity> patternEntities = patternRepo.findAll();
        return patternEntities.stream()
                .map(patternEntity -> patternEntityMapper.map(patternEntity, imageService))  // Use a method that handles image fetching
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
            patternRepo.save(patternEntityMapper.map(pattern, imageService));
        });
    }

    public void savePatternsFromBundlePath() {
        getPatternsFromBundlePath.savePatternsToList().forEach(pattern ->
                patternRepo.save(patternEntityMapper.map(pattern, imageService)));
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
                PatternEntity patternEntity = patternEntityMapper.map(pattern, imageService);
                ImageEntity imageEntity = imageService.saveImage(pattern.getImage());
                patternEntity.setImageId(imageEntity.getId());
                PatternEntity saved = patternRepo.save(patternEntity);
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

    public PatternEntity saveRequestAsEntity(PatternRequest patternRequest) {
        PatternEntity patternEntity = patternResponseMapper.fromRequest(patternRequest);
        Optional<ImageEntity> imageEntity = imageService.getImageEntity(patternRequest.getImageId());
        imageEntity.ifPresent(image -> patternEntity.setImageId(image.getId())); // no image is passed, just an id. it should be in the image repo.
        return patternRepo.save(patternEntity);
    }

    @Transactional
    public PatternEntity createOrUpdatePattern(PatternRequest request) {
        log.info("Creating or updating pattern: id={}, name={}, imageId={}", request.getId(), request.getName(), request.getImageId());
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

        PatternEntity savedPattern = saveRequestAsEntity(request);
        log.info("Saved pattern: id={}, name={}", savedPattern.getId(), savedPattern.getName());
        return savedPattern;
    }

    public List<PatternEntity> getOrCreatePatterns(List<PatternRequest> requests) {
        return requests.stream()
                .map(this::createOrUpdatePattern)
                .collect(Collectors.toList());
    }
}
