package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class StateResponseMapper {

    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateStringResponseMapper stateStringResponseMapper;
    private final StateRegionResponseMapper stateRegionResponseMapper;
    private final StateLocationResponseMapper stateLocationResponseMapper;
    private final ImageResponseMapper imageResponseMapper;
    private final StateIllustrationResponseMapper stateIllustrationResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;
    private final RegionResponseMapper regionResponseMapper;

    public StateResponseMapper(StateImageResponseMapper stateImageResponseMapper,
                               StateStringResponseMapper stateStringResponseMapper,
                               StateRegionResponseMapper stateRegionResponseMapper,
                               StateLocationResponseMapper stateLocationResponseMapper,
                               ImageResponseMapper imageResponseMapper,
                               StateIllustrationResponseMapper stateIllustrationResponseMapper,
                               MatchHistoryResponseMapper matchHistoryResponseMapper,
                               RegionResponseMapper regionResponseMapper) {
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateStringResponseMapper = stateStringResponseMapper;
        this.stateRegionResponseMapper = stateRegionResponseMapper;
        this.stateLocationResponseMapper = stateLocationResponseMapper;
        this.imageResponseMapper = imageResponseMapper;
        this.stateIllustrationResponseMapper = stateIllustrationResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
        this.regionResponseMapper = regionResponseMapper;
    }

    public StateResponse map(StateEntity stateEntity) {
        StateResponse stateResponse = new StateResponse();
        stateResponse.setId(stateEntity.getId());
        stateResponse.setName(stateEntity.getName());
        stateEntity.getStateImages().forEach(image ->
                stateResponse.getStateImages().add(stateImageResponseMapper.map(image)));
        stateResponse.setStateText(new HashSet<>(stateEntity.getStateText()));
        stateEntity.getStateStrings().forEach(string ->
                stateResponse.getStateStrings().add(stateStringResponseMapper.map(string)));
        stateEntity.getStateRegions().forEach(region ->
                stateResponse.getStateRegions().add(stateRegionResponseMapper.map(region)));
        stateEntity.getStateLocations().forEach(location ->
                stateResponse.getStateLocations().add(stateLocationResponseMapper.map(location)));
        stateResponse.setBlocking(stateEntity.isBlocking());
        stateResponse.setCanHide(new HashSet<>(stateEntity.getCanHide()));
        stateResponse.setHidden(new HashSet<>(stateEntity.getHidden()));
        stateResponse.setPathScore(stateEntity.getPathScore());
        stateResponse.setLastAccessed(stateEntity.getLastAccessed());
        stateResponse.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        stateResponse.setProbabilityExists(stateEntity.getProbabilityExists());
        stateResponse.setTimesVisited(stateEntity.getTimesVisited());
        stateEntity.getScenes().forEach(scene ->
                stateResponse.getScenes().add(imageResponseMapper.map(scene)));
        stateResponse.setUsableArea(regionResponseMapper.map(stateEntity.getUsableArea()));
        stateResponse.setMatchHistory(matchHistoryResponseMapper.map(stateEntity.getMatchHistory()));
        return stateResponse;
    }

    public StateEntity map(StateResponse stateResponse) {
        StateEntity stateEntity = new StateEntity();
        stateEntity.setId(stateResponse.getId());
        stateEntity.setName(stateResponse.getName());
        stateResponse.getStateImages().forEach(image ->
                stateEntity.getStateImages().add(stateImageResponseMapper.map(image)));
        stateEntity.setStateText(new HashSet<>(stateResponse.getStateText()));
        stateResponse.getStateStrings().forEach(string ->
                stateEntity.getStateStrings().add(stateStringResponseMapper.map(string)));
        stateResponse.getStateRegions().forEach(region ->
                stateEntity.getStateRegions().add(stateRegionResponseMapper.map(region)));
        stateResponse.getStateLocations().forEach(location ->
                stateEntity.getStateLocations().add(stateLocationResponseMapper.map(location)));
        stateEntity.setBlocking(stateResponse.isBlocking());
        stateEntity.setCanHide(new HashSet<>(stateResponse.getCanHide()));
        stateEntity.setHidden(new HashSet<>(stateResponse.getHidden()));
        stateEntity.setPathScore(stateResponse.getPathScore());
        stateEntity.setLastAccessed(stateResponse.getLastAccessed());
        stateEntity.setBaseProbabilityExists(stateResponse.getBaseProbabilityExists());
        stateEntity.setProbabilityExists(stateResponse.getProbabilityExists());
        stateEntity.setTimesVisited(stateResponse.getTimesVisited());
        stateResponse.getScenes().forEach(scene ->
                stateEntity.getScenes().add(imageResponseMapper.map(scene)));
        stateEntity.setUsableArea(regionResponseMapper.map(stateResponse.getUsableArea()));
        stateEntity.setMatchHistory(matchHistoryResponseMapper.map(stateResponse.getMatchHistory()));
        return stateEntity;
    }
}
