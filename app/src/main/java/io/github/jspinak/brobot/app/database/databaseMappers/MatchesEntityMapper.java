package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchesEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;

import java.util.ArrayList;
import java.util.List;

public class MatchesEntityMapper {

    public static MatchesEntity map(Matches matches) {
        MatchesEntity matchesEntity = new MatchesEntity();
        matchesEntity.setActionDescription(matches.getActionDescription());
        matchesEntity.setMatchList(MatchEntityMapper.mapToMatchEntityList(matches.getMatchList()));
        matchesEntity.setInitialMatchList(MatchEntityMapper.mapToMatchEntityList(matches.getInitialMatchList()));
        matchesEntity.setActionOptions(ActionOptionsEntityMapper.map(matches.getActionOptions()));
        matchesEntity.setActiveStates(matches.getActiveStates());
        matchesEntity.setText(matches.getText());
        matchesEntity.setSelectedText(matches.getSelectedText());
        matchesEntity.setDuration(matches.getDuration());
        matchesEntity.setStartTime(matches.getStartTime());
        matchesEntity.setEndTime(matches.getEndTime());
        matchesEntity.setSuccess(matches.isSuccess());
        matchesEntity.setDefinedRegions(RegionEmbeddableMapper.mapToRegionEmbeddableList(matches.getDefinedRegions()));
        matchesEntity.setMaxMatches(matches.getMaxMatches());
        matchesEntity.setSceneAnalysisCollection(matches.getSceneAnalysisCollection());
        matchesEntity.setMask(matches.getMask());
        matchesEntity.setOutputText(matches.getOutputText());
        matchesEntity.setActionLifecycle(matches.getActionLifecycle());
        return matchesEntity;
    }

    public static Matches map(MatchesEntity matchesEntity) {
        Matches matches = new Matches();
        matches.setActionDescription(matchesEntity.getActionDescription());
        matches.setMatchList(MatchEntityMapper.mapToMatchList(matchesEntity.getMatchList()));
        matches.setInitialMatchList(MatchEntityMapper.mapToMatchList(matchesEntity.getInitialMatchList()));
        matches.setActionOptions(ActionOptionsEntityMapper.map(matchesEntity.getActionOptions()));
        matches.setActiveStates(matchesEntity.getActiveStates());
        matches.setText(matchesEntity.getText());
        matches.setSelectedText(matchesEntity.getSelectedText());
        matches.setDuration(matchesEntity.getDuration());
        matches.setStartTime(matchesEntity.getStartTime());
        matches.setEndTime(matchesEntity.getEndTime());
        matches.setSuccess(matchesEntity.isSuccess());
        matches.setDefinedRegions(RegionEmbeddableMapper.mapToRegionList(matchesEntity.getDefinedRegions()));
        matches.setMaxMatches(matchesEntity.getMaxMatches());
        matches.setSceneAnalysisCollection(matchesEntity.getSceneAnalysisCollection());
        matches.setMask(matchesEntity.getMask());
        matches.setOutputText(matchesEntity.getOutputText());
        matches.setActionLifecycle(matchesEntity.getActionLifecycle());
        return matches;
    }

    public static List<MatchesEntity> mapToMatchesEntityList(List<Matches> matchesList) {
        List<MatchesEntity> matchesEntityList = new ArrayList<>();
        matchesList.forEach(matches -> matchesEntityList.add(map(matches)));
        return matchesEntityList;
    }

    public static List<Matches> mapToMatchesList(List<MatchesEntity> matchesEntityList) {
        List<Matches> matchesList = new ArrayList<>();
        matchesEntityList.forEach(matchesEntity -> matchesList.add(map(matchesEntity)));
        return matchesList;
    }
}
