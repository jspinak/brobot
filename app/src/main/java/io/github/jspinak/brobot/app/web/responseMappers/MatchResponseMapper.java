package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchEntity;
import io.github.jspinak.brobot.app.web.requests.MatchRequest;
import io.github.jspinak.brobot.app.web.responses.MatchResponse;
import org.springframework.stereotype.Component;

@Component
public class MatchResponseMapper {

    private final LocationResponseMapper locationResponseMapper;
    private final ImageResponseMapper imageResponseMapper;
    private final RegionResponseMapper regionResponseMapper;
    private final AnchorsResponseMapper anchorsResponseMapper;
    private final StateObjectDataResponseMapper stateObjectDataResponseMapper;

    public MatchResponseMapper(LocationResponseMapper locationResponseMapper, ImageResponseMapper imageResponseMapper,
                               RegionResponseMapper regionResponseMapper, AnchorsResponseMapper anchorsResponseMapper,
                               StateObjectDataResponseMapper stateObjectDataResponseMapper) {
        this.locationResponseMapper = locationResponseMapper;
        this.imageResponseMapper = imageResponseMapper;
        this.regionResponseMapper = regionResponseMapper;
        this.anchorsResponseMapper = anchorsResponseMapper;
        this.stateObjectDataResponseMapper = stateObjectDataResponseMapper;
    }

    public MatchResponse map(MatchEntity matchEntity) {
        if (matchEntity == null) {
            return null;
        }
        MatchResponse matchResponse = new MatchResponse();
        matchResponse.setId(matchEntity.getId());
        matchResponse.setScore(matchEntity.getScore());
        matchResponse.setTarget(locationResponseMapper.map(matchEntity.getTarget()));
        matchResponse.setImage(imageResponseMapper.map(matchEntity.getImage()));
        matchResponse.setText(matchEntity.getText());
        matchResponse.setName(matchEntity.getName());
        matchResponse.setRegion(regionResponseMapper.map(matchEntity.getRegion()));
        matchResponse.setSearchImage(imageResponseMapper.map(matchEntity.getSearchImage()));
        matchResponse.setAnchors(anchorsResponseMapper.map(matchEntity.getAnchors()));
        matchResponse.setStateObjectData(stateObjectDataResponseMapper.map(matchEntity.getStateObjectData()));
        matchResponse.setSceneId(matchEntity.getSceneId());
        matchResponse.setTimeStamp(matchEntity.getTimeStamp());
        matchResponse.setTimesActedOn(matchEntity.getTimesActedOn());
        return matchResponse;
    }

    public MatchEntity map(MatchResponse matchResponse) {
        if (matchResponse == null) {
            return null;
        }
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setId(matchResponse.getId());
        matchEntity.setScore(matchResponse.getScore());
        matchEntity.setTarget(locationResponseMapper.map(matchResponse.getTarget()));
        matchEntity.setImage(imageResponseMapper.map(matchResponse.getImage()));
        matchEntity.setText(matchResponse.getText());
        matchEntity.setName(matchResponse.getName());
        matchEntity.setRegion(regionResponseMapper.map(matchResponse.getRegion()));
        matchEntity.setSearchImage(imageResponseMapper.map(matchResponse.getSearchImage()));
        matchEntity.setAnchors(anchorsResponseMapper.map(matchResponse.getAnchors()));
        matchEntity.setStateObjectData(stateObjectDataResponseMapper.map(matchResponse.getStateObjectData()));
        matchEntity.setSceneId(matchResponse.getSceneId());
        matchEntity.setTimeStamp(matchResponse.getTimeStamp());
        matchEntity.setTimesActedOn(matchResponse.getTimesActedOn());
        return matchEntity;
    }

    public MatchEntity fromRequest(MatchRequest request) {
        if (request == null) return null;
        MatchEntity entity = new MatchEntity();
        entity.setId(request.getId());
        entity.setScore(request.getScore());
        entity.setTarget(locationResponseMapper.fromRequest(request.getTarget()));
        entity.setImage(imageResponseMapper.fromRequest(request.getImage()));
        entity.setText(request.getText());
        entity.setName(request.getName());
        entity.setRegion(regionResponseMapper.fromRequest(request.getRegion()));
        entity.setSearchImage(imageResponseMapper.fromRequest(request.getSearchImage()));
        entity.setAnchors(anchorsResponseMapper.fromRequest(request.getAnchors()));
        entity.setStateObjectData(stateObjectDataResponseMapper.fromRequest(request.getStateObjectData()));
        entity.setSceneId(request.getSceneId());
        entity.setTimeStamp(request.getTimeStamp());
        entity.setTimesActedOn(request.getTimesActedOn());
        return entity;
    }
}
