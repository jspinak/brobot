package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;

import java.util.ArrayList;
import java.util.List;

public class PatternEntityMapper {
    
    public static PatternEntity map(Pattern pattern) {
        PatternEntity patternEntity = new PatternEntity();
        patternEntity.setUrl(pattern.getUrl());
        patternEntity.setImgpath(pattern.getImgpath());
        patternEntity.setName(pattern.getName());
        patternEntity.setFixed(pattern.isFixed());
        patternEntity.setSearchRegions(SearchRegionsEmbeddableMapper.map(pattern.getSearchRegions()));
        patternEntity.setSetKmeansColorProfiles(pattern.isSetKmeansColorProfiles());
        patternEntity.setMatchHistory(MatchHistoryEntityMapper.map(pattern.getMatchHistory()));
        patternEntity.setIndex(pattern.getIndex());
        patternEntity.setDynamic(pattern.isDynamic());
        patternEntity.setPosition(PositionEmbeddableMapper.map(pattern.getTargetPosition()));
        patternEntity.setAnchors(AnchorsEntityMapper.map(pattern.getAnchors()));
        patternEntity.setImage(ImageEntityMapper.map(pattern.getImage()));
        return patternEntity;
    }

    public static Pattern map(PatternEntity patternEntity) {
        Pattern pattern = new Pattern();
        pattern.setUrl(patternEntity.getUrl());
        pattern.setImgpath(patternEntity.getImgpath());
        pattern.setName(patternEntity.getName());
        pattern.setFixed(patternEntity.isFixed());
        pattern.setSearchRegions(SearchRegionsEmbeddableMapper.map(patternEntity.getSearchRegions()));
        pattern.setSetKmeansColorProfiles(patternEntity.isSetKmeansColorProfiles());
        pattern.setMatchHistory(MatchHistoryEntityMapper.map(patternEntity.getMatchHistory()));
        pattern.setIndex(patternEntity.getIndex());
        pattern.setDynamic(patternEntity.isDynamic());
        pattern.setTargetPosition(PositionEmbeddableMapper.map(patternEntity.getPosition()));
        pattern.setAnchors(AnchorsEntityMapper.map(patternEntity.getAnchors()));
        pattern.setImage(ImageEntityMapper.map(patternEntity.getImage()));
        return pattern;
    }

    public static List<PatternEntity> mapToPatternEntityList(List<Pattern> patterns) {
        List<PatternEntity> patternEntityList = new ArrayList<>();
        patterns.forEach(pattern -> patternEntityList.add(map(pattern)));
        return patternEntityList;
    }

    public static List<Pattern> mapToPatternList(List<PatternEntity> patternEntityList) {
        List<Pattern> patternList = new ArrayList<>();
        patternEntityList.forEach(patternEntity -> patternList.add(map(patternEntity)));
        return patternList;
    }
}
