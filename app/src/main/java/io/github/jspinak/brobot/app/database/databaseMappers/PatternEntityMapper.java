package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
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
    
    public PatternEntity map(Pattern pattern) {
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
        patternEntity.setImage(imageEntityMapper.map(pattern.getImage()));
        return patternEntity;
    }

    public Pattern map(PatternEntity patternEntity) {
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
        pattern.setImage(imageEntityMapper.map(patternEntity.getImage()));
        return pattern;
    }

    public List<PatternEntity> mapToPatternEntityList(List<Pattern> patterns) {
        List<PatternEntity> patternEntityList = new ArrayList<>();
        patterns.forEach(pattern -> patternEntityList.add(map(pattern)));
        return patternEntityList;
    }

    public List<Pattern> mapToPatternList(List<PatternEntity> patternEntityList) {
        List<Pattern> patternList = new ArrayList<>();
        patternEntityList.forEach(patternEntity -> patternList.add(map(patternEntity)));
        return patternList;
    }
}
