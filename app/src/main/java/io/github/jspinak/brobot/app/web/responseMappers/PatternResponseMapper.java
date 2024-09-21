package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import org.springframework.stereotype.Component;

@Component
public class PatternResponseMapper {

    private final SearchRegionsResponseMapper searchRegionsResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;
    private final PositionResponseMapper positionResponseMapper;
    private final AnchorsResponseMapper anchorsResponseMapper;

    public PatternResponseMapper(SearchRegionsResponseMapper searchRegionsResponseMapper,
                                 MatchHistoryResponseMapper matchHistoryResponseMapper,
                                 PositionResponseMapper positionResponseMapper,
                                 AnchorsResponseMapper anchorsResponseMapper) {
        this.searchRegionsResponseMapper = searchRegionsResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
        this.positionResponseMapper = positionResponseMapper;
        this.anchorsResponseMapper = anchorsResponseMapper;
    }

    public PatternResponse map(PatternEntity patternEntity) {
        if (patternEntity == null) {
            return null;
        }
        PatternResponse patternResponse = new PatternResponse();
        patternResponse.setId(patternEntity.getId());
        patternResponse.setUrl(patternEntity.getUrl());
        patternResponse.setImgpath(patternEntity.getImgpath());
        patternResponse.setName(patternEntity.getName());
        patternResponse.setFixed(patternEntity.isFixed());
        patternResponse.setSearchRegions(searchRegionsResponseMapper.map(patternEntity.getSearchRegions()));
        patternResponse.setSetKmeansColorProfiles(patternEntity.isSetKmeansColorProfiles());
        patternResponse.setMatchHistory(matchHistoryResponseMapper.map(patternEntity.getMatchHistory()));
        patternResponse.setIndex(patternEntity.getIndex());
        patternResponse.setDynamic(patternEntity.isDynamic());
        patternResponse.setPosition(positionResponseMapper.map(patternEntity.getPosition()));
        patternResponse.setAnchors(anchorsResponseMapper.map(patternEntity.getAnchors()));
        patternResponse.setImageId(patternEntity.getImageId());
        return patternResponse;
    }

    public PatternEntity map(PatternResponse patternResponse) {
        if (patternResponse == null) {
            return null;
        }
        PatternEntity patternEntity = new PatternEntity();
        patternEntity.setId(patternResponse.getId());
        patternEntity.setUrl(patternResponse.getUrl());
        patternEntity.setImgpath(patternResponse.getImgpath());
        patternEntity.setName(patternResponse.getName());
        patternEntity.setFixed(patternResponse.isFixed());
        patternEntity.setSearchRegions(searchRegionsResponseMapper.map(patternResponse.getSearchRegions()));
        patternEntity.setSetKmeansColorProfiles(patternResponse.isSetKmeansColorProfiles());
        patternEntity.setMatchHistory(matchHistoryResponseMapper.map(patternResponse.getMatchHistory()));
        patternEntity.setIndex(patternResponse.getIndex());
        patternEntity.setDynamic(patternResponse.isDynamic());
        patternEntity.setPosition(positionResponseMapper.map(patternResponse.getPosition()));
        patternEntity.setAnchors(anchorsResponseMapper.map(patternResponse.getAnchors()));
        patternEntity.setImageId(patternResponse.getImageId());
        return patternEntity;
    }

    public PatternEntity fromRequest(PatternRequest request) {
        if (request == null) return null;
        PatternEntity entity = new PatternEntity();
        entity.setId(request.getId());
        entity.setUrl(request.getUrl());
        entity.setImgpath(request.getImgpath());
        entity.setName(request.getName());
        entity.setFixed(request.isFixed());
        entity.setSearchRegions(searchRegionsResponseMapper.fromRequest(request.getSearchRegions()));
        entity.setSetKmeansColorProfiles(request.isSetKmeansColorProfiles());
        entity.setMatchHistory(matchHistoryResponseMapper.fromRequest(request.getMatchHistory()));
        entity.setIndex(request.getIndex());
        entity.setDynamic(request.isDynamic());
        entity.setPosition(positionResponseMapper.fromRequest(request.getPosition()));
        entity.setAnchors(anchorsResponseMapper.fromRequest(request.getAnchors()));
        entity.setImageId(request.getImageId());
        return entity;
    }

}


