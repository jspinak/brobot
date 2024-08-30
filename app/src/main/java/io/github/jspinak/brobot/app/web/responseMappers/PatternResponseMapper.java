package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.responses.PatternResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatternResponseMapper {

    private final SearchRegionsResponseMapper searchRegionsResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;
    private final PositionResponseMapper positionResponseMapper;
    private final AnchorsResponseMapper anchorsResponseMapper;
    private final ImageResponseMapper imageResponseMapper;

    public PatternResponseMapper(SearchRegionsResponseMapper searchRegionsResponseMapper,
                                 MatchHistoryResponseMapper matchHistoryResponseMapper,
                                 PositionResponseMapper positionResponseMapper,
                                 AnchorsResponseMapper anchorsResponseMapper,
                                 ImageResponseMapper imageResponseMapper) {
        this.searchRegionsResponseMapper = searchRegionsResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
        this.positionResponseMapper = positionResponseMapper;
        this.anchorsResponseMapper = anchorsResponseMapper;
        this.imageResponseMapper = imageResponseMapper;
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
        patternResponse.setImage(imageResponseMapper.map(patternEntity.getImage()));
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
        if (patternResponse.getImage() != null) {
            ImageEntity imageEntity = imageResponseMapper.map(patternResponse.getImage());
            if (imageEntity != null) {
                patternEntity.setImage(imageEntity);
            }
        }
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
        entity.setImage(imageResponseMapper.fromRequest(request.getImage()));
        return entity;
    }

    public PatternRequest toRequest(PatternEntity entity) {
        if (entity == null) return null;
        PatternRequest request = new PatternRequest();
        request.setId(entity.getId());
        request.setUrl(entity.getUrl());
        request.setImgpath(entity.getImgpath());
        request.setName(entity.getName());
        request.setFixed(entity.isFixed());
        request.setSearchRegions(searchRegionsResponseMapper.toRequest(entity.getSearchRegions()));
        request.setSetKmeansColorProfiles(entity.isSetKmeansColorProfiles());
        request.setMatchHistory(matchHistoryResponseMapper.toRequest(entity.getMatchHistory()));
        request.setIndex(entity.getIndex());
        request.setDynamic(entity.isDynamic());
        request.setPosition(positionResponseMapper.toRequest(entity.getPosition()));
        request.setAnchors(anchorsResponseMapper.toRequest(entity.getAnchors()));
        request.setImage(imageResponseMapper.toRequest(entity.getImage()));
        return request;
    }

    public List<PatternRequest> toRequestList(List<PatternEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toRequest)
                .collect(Collectors.toList());
    }
}


