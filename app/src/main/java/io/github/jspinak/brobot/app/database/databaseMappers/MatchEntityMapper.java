package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchEntity;
import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MatchEntityMapper {

    private final LocationEntityMapper locationEntityMapper;
    private final ImageEntityMapper imageEntityMapper;
    private final RegionEmbeddableMapper regionEmbeddableMapper;
    private final AnchorsEntityMapper anchorsEntityMapper;
    private final StateObjectDataEmbeddableMapper stateObjectDataEmbeddableMapper;

    public MatchEntityMapper(LocationEntityMapper locationEntityMapper,
                             ImageEntityMapper imageEntityMapper,
                             RegionEmbeddableMapper regionEmbeddableMapper,
                             AnchorsEntityMapper anchorsEntityMapper,
                             StateObjectDataEmbeddableMapper stateObjectDataEmbeddableMapper) {
        this.locationEntityMapper = locationEntityMapper;
        this.imageEntityMapper = imageEntityMapper;
        this.regionEmbeddableMapper = regionEmbeddableMapper;
        this.anchorsEntityMapper = anchorsEntityMapper;
        this.stateObjectDataEmbeddableMapper = stateObjectDataEmbeddableMapper;
    }
    
    public MatchEntity map(Match match) {
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setScore(match.getScore());
        matchEntity.setTarget(locationEntityMapper.map(match.getTarget()));
        if (match.getImage() != null) matchEntity.setImage(imageEntityMapper.map(match.getImage()));
        if (match.getText() != null) matchEntity.setText(match.getText());
        if (match.getName() != null) matchEntity.setName(match.getName());
        matchEntity.setRegion(regionEmbeddableMapper.map(match.getRegion()));
        if (match.getSearchImage() != null) matchEntity.setSearchImage(imageEntityMapper.map(match.getSearchImage()));
        if (match.getAnchors() != null) matchEntity.setAnchors(anchorsEntityMapper.map(match.getAnchors()));
        if (match.getStateObjectData() != null) matchEntity.setStateObjectData(stateObjectDataEmbeddableMapper.map(match.getStateObjectData()));
        if (match.getHistogram() != null) matchEntity.setHistogram(match.getHistogram());
        if (match.getScene() != null) matchEntity.setSceneId(match.getScene().getId());
        if (match.getTimeStamp() != null) matchEntity.setTimeStamp(match.getTimeStamp());
        matchEntity.setTimesActedOn(match.getTimesActedOn());
        return matchEntity;
    }

    public Match map(MatchEntity matchEntity) {
        Match match = new Match();
        match.setScore(matchEntity.getScore());
        match.setTarget(locationEntityMapper.map(matchEntity.getTarget()));
        if (matchEntity.getImage() != null) match.setImage(imageEntityMapper.map(matchEntity.getImage()));
        match.setText(matchEntity.getText());
        match.setName(matchEntity.getName());
        match.setRegion(regionEmbeddableMapper.map(matchEntity.getRegion()));
        if (matchEntity.getSearchImage() != null) match.setSearchImage(imageEntityMapper.map(matchEntity.getSearchImage()));
        if (matchEntity.getAnchors() != null) match.setAnchors(anchorsEntityMapper.map(matchEntity.getAnchors()));
        if (matchEntity.getStateObjectData() != null) match.setStateObjectData(stateObjectDataEmbeddableMapper.map(matchEntity.getStateObjectData()));
        if (matchEntity.getHistogram() != null) match.setHistogram(matchEntity.getHistogram());
        //if (matchEntity.getSceneId() != null)
        if (matchEntity.getTimeStamp() != null) match.setTimeStamp(matchEntity.getTimeStamp());
        match.setTimesActedOn(matchEntity.getTimesActedOn());
        return match;
    }

    public List<MatchEntity> mapToMatchEntityList(List<Match> matchList) {
        List<MatchEntity> matchEntityList = new ArrayList<>();
        matchList.forEach(match -> matchEntityList.add(map(match)));
        return matchEntityList;
    }

    public List<Match> mapToMatchList(List<MatchEntity> matchEntityList) {
        List<Match> matchList = new ArrayList<>();
        matchEntityList.forEach(match -> matchList.add(map(match)));
        return matchList;
    }
}
