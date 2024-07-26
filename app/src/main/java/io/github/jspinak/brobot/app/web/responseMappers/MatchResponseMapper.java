package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchEntity;
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
        matchResponse.setScene(imageResponseMapper.map(matchEntity.getScene()));
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
        matchEntity.setScene(imageResponseMapper.map(matchResponse.getScene()));
        matchEntity.setTimeStamp(matchResponse.getTimeStamp());
        matchEntity.setTimesActedOn(matchResponse.getTimesActedOn());
        return matchEntity;
    }
}
