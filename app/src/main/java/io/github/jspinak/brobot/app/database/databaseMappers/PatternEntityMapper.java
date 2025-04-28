package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.services.ImageService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatternEntityMapper {

    private final SearchRegionsEmbeddableMapper searchRegionsEmbeddableMapper;
    private final MatchHistoryEntityMapper matchHistoryEntityMapper;
    private final PositionEmbeddableMapper positionEmbeddableMapper;
    private final AnchorsEntityMapper anchorsEntityMapper;
    private final ImageEntityMapper imageEntityMapper;

    public PatternEntityMapper(SearchRegionsEmbeddableMapper searchRegionsEmbeddableMapper,
                               MatchHistoryEntityMapper matchHistoryEntityMapper,
                               PositionEmbeddableMapper positionEmbeddableMapper,
                               AnchorsEntityMapper anchorsEntityMapper,
                               ImageEntityMapper imageEntityMapper) {
        this.searchRegionsEmbeddableMapper = searchRegionsEmbeddableMapper;
        this.matchHistoryEntityMapper = matchHistoryEntityMapper;
        this.positionEmbeddableMapper = positionEmbeddableMapper;
        this.anchorsEntityMapper = anchorsEntityMapper;
        this.imageEntityMapper = imageEntityMapper;
    }
    
    private PatternEntity map(Pattern pattern) {
        PatternEntity patternEntity = new PatternEntity();
        patternEntity.setUrl(pattern.getUrl());
        patternEntity.setImgpath(pattern.getImgpath());
        patternEntity.setName(pattern.getName());
        patternEntity.setFixed(pattern.isFixed());
        patternEntity.setSearchRegions(searchRegionsEmbeddableMapper.map(pattern.getSearchRegions()));
        patternEntity.setSetKmeansColorProfiles(pattern.isSetKmeansColorProfiles());
        patternEntity.setMatchHistory(matchHistoryEntityMapper.map(pattern.getMatchHistory()));
        patternEntity.setIndex(pattern.getIndex());
        patternEntity.setDynamic(pattern.isDynamic());
        patternEntity.setPosition(positionEmbeddableMapper.map(pattern.getTargetPosition()));
        patternEntity.setAnchors(anchorsEntityMapper.map(pattern.getAnchors()));
        return patternEntity;
    }

    private PatternEntity map(Pattern pattern, ImageEntity imageEntity) {
        PatternEntity patternEntity = map(pattern);
        patternEntity.setImageId(imageEntity.getId());
        return patternEntity;
    }

    public PatternEntity map(Pattern pattern, ImageService imageService) {
        PatternEntity patternEntity = map(pattern);
        if (pattern.getImage() != null) {
            // Save the image and set the image ID
            ImageEntity imageEntity = imageService.saveImage(pattern.getImage());
            patternEntity.setImageId(imageEntity.getId());
        }
        return patternEntity;
    }

    private Pattern map(PatternEntity patternEntity) {
        Pattern pattern = new Pattern();
        pattern.setUrl(patternEntity.getUrl());
        pattern.setImgpath(patternEntity.getImgpath());
        pattern.setName(patternEntity.getName());
        pattern.setFixed(patternEntity.isFixed());
        pattern.setSearchRegions(searchRegionsEmbeddableMapper.map(patternEntity.getSearchRegions()));
        pattern.setSetKmeansColorProfiles(patternEntity.isSetKmeansColorProfiles());
        pattern.setMatchHistory(matchHistoryEntityMapper.map(patternEntity.getMatchHistory()));
        pattern.setIndex(patternEntity.getIndex());
        pattern.setDynamic(patternEntity.isDynamic());
        pattern.setTargetPosition(positionEmbeddableMapper.map(patternEntity.getPosition()));
        pattern.setAnchors(anchorsEntityMapper.map(patternEntity.getAnchors()));
        return pattern;
    }

    private Pattern map(PatternEntity patternEntity, ImageEntity imageEntity) {
        Pattern pattern = map(patternEntity);
        pattern.setImage(imageEntityMapper.map(imageEntity));
        return pattern;
    }

    public Pattern map(PatternEntity patternEntity, ImageService imageService) {
        return imageService.getImageEntity(patternEntity.getImageId())
                .map(imageEntity -> map(patternEntity, imageEntity))
                .orElseGet(() -> map(patternEntity));
    }

    public List<PatternEntity> mapToPatternEntityList(List<Pattern> patterns, ImageService imageService) {
        List<PatternEntity> patternEntityList = new ArrayList<>();
        for (Pattern pattern : patterns) {
            PatternEntity patternEntity = map(pattern, imageService);
            patternEntityList.add(patternEntity);
        }
        return patternEntityList;
    }

    public List<Pattern> mapToPatternList(List<PatternEntity> patternEntityList, ImageService imageService) {
        List<Pattern> patternList = new ArrayList<>();
        for (PatternEntity patternEntity : patternEntityList) {
            if (patternEntity.getImageId() != null) {
                imageService.getImageEntity(patternEntity.getImageId())
                    .ifPresent(imageEntity -> patternList.add(map(patternEntity, imageEntity)));
            } else {
                patternList.add(map(patternEntity));
            }
        }
        return patternList;
    }
}
