package io.github.jspinak.brobot.app.database.databaseMappers.javaMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.MatchEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchEntityMapper {
    
    public static MatchEntity map(Match match) {
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setScore(match.getScore());
        matchEntity.setTarget(LocationEntityMapper.map(match.getTarget()));
        matchEntity.setImage(ImageEntityMapper.map(match.getImage()));
        matchEntity.setText(match.getText());
        matchEntity.setName(match.getName());
        matchEntity.setRegion(RegionEmbeddableMapper.map(match.getRegion()));
        matchEntity.setSearchImage(ImageEntityMapper.map(match.getSearchImage()));
        matchEntity.setAnchors(AnchorsEntityMapper.map(match.getAnchors()));
        matchEntity.setStateObjectData(match.getStateObjectData());
        matchEntity.setHistogram(match.getHistogram());
        matchEntity.setScene(ImageEntityMapper.map(match.getScene()));
        matchEntity.setTimeStamp(match.getTimeStamp());
        matchEntity.setTimesActedOn(match.getTimesActedOn());
        return matchEntity;
    }

    public static Match map(MatchEntity matchEntity) {
        Match match = new Match();
        match.setScore(matchEntity.getScore());
        match.setTarget(LocationEntityMapper.map(matchEntity.getTarget()));
        match.setImage(ImageEntityMapper.map(matchEntity.getImage()));
        match.setText(matchEntity.getText());
        match.setName(matchEntity.getName());
        match.setRegion(RegionEmbeddableMapper.map(matchEntity.getRegion()));
        match.setSearchImage(ImageEntityMapper.map(matchEntity.getSearchImage()));
        match.setAnchors(AnchorsEntityMapper.map(matchEntity.getAnchors()));
        match.setStateObjectData(matchEntity.getStateObjectData());
        match.setHistogram(matchEntity.getHistogram());
        match.setScene(ImageEntityMapper.map(matchEntity.getScene()));
        match.setTimeStamp(matchEntity.getTimeStamp());
        match.setTimesActedOn(matchEntity.getTimesActedOn());
        return match;
    }

    public static List<MatchEntity> mapToMatchEntityList(List<Match> matchList) {
        List<MatchEntity> matchEntityList = new ArrayList<>();
        matchList.forEach(match -> matchEntityList.add(MatchEntityMapper.map(match)));
        return matchEntityList;
    }

    public static List<Match> mapToMatchList(List<MatchEntity> matchEntityList) {
        List<Match> matchList = new ArrayList<>();
        matchEntityList.forEach(match -> matchList.add(MatchEntityMapper.map(match)));
        return matchList;
    }
}
