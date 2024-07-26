package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchesEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatchesEntityMapper {

    private final MatchEntityMapper matchEntityMapper;
    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final RegionEmbeddableMapper regionEmbeddableMapper;

    public MatchesEntityMapper(MatchEntityMapper matchEntityMapper,
                               ActionOptionsEntityMapper actionOptionsEntityMapper,
                               RegionEmbeddableMapper regionEmbeddableMapper) {
        this.matchEntityMapper = matchEntityMapper;
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.regionEmbeddableMapper = regionEmbeddableMapper;
    }

    public MatchesEntity map(Matches matches) {
        MatchesEntity matchesEntity = new MatchesEntity();
        matchesEntity.setActionDescription(matches.getActionDescription());
        matchesEntity.setMatchList(matchEntityMapper.mapToMatchEntityList(matches.getMatchList()));
        matchesEntity.setInitialMatchList(matchEntityMapper.mapToMatchEntityList(matches.getInitialMatchList()));
        matchesEntity.setActionOptions(actionOptionsEntityMapper.map(matches.getActionOptions()));
        matchesEntity.setActiveStates(matches.getActiveStates());
        matchesEntity.setText(matches.getText());
        matchesEntity.setSelectedText(matches.getSelectedText());
        matchesEntity.setDuration(matches.getDuration());
        matchesEntity.setStartTime(matches.getStartTime());
        matchesEntity.setEndTime(matches.getEndTime());
        matchesEntity.setSuccess(matches.isSuccess());
        matchesEntity.setDefinedRegions(regionEmbeddableMapper.mapToRegionEmbeddableList(matches.getDefinedRegions()));
        matchesEntity.setMaxMatches(matches.getMaxMatches());
        matchesEntity.setSceneAnalysisCollection(matches.getSceneAnalysisCollection());
        matchesEntity.setMask(matches.getMask());
        matchesEntity.setOutputText(matches.getOutputText());
        matchesEntity.setActionLifecycle(matches.getActionLifecycle());
        return matchesEntity;
    }

    public Matches map(MatchesEntity matchesEntity) {
        Matches matches = new Matches();
        matches.setActionDescription(matchesEntity.getActionDescription());
        matches.setMatchList(matchEntityMapper.mapToMatchList(matchesEntity.getMatchList()));
        matches.setInitialMatchList(matchEntityMapper.mapToMatchList(matchesEntity.getInitialMatchList()));
        matches.setActionOptions(actionOptionsEntityMapper.map(matchesEntity.getActionOptions()));
        matches.setActiveStates(matchesEntity.getActiveStates());
        matches.setText(matchesEntity.getText());
        matches.setSelectedText(matchesEntity.getSelectedText());
        matches.setDuration(matchesEntity.getDuration());
        matches.setStartTime(matchesEntity.getStartTime());
        matches.setEndTime(matchesEntity.getEndTime());
        matches.setSuccess(matchesEntity.isSuccess());
        matches.setDefinedRegions(regionEmbeddableMapper.mapToRegionList(matchesEntity.getDefinedRegions()));
        matches.setMaxMatches(matchesEntity.getMaxMatches());
        matches.setSceneAnalysisCollection(matchesEntity.getSceneAnalysisCollection());
        matches.setMask(matchesEntity.getMask());
        matches.setOutputText(matchesEntity.getOutputText());
        matches.setActionLifecycle(matchesEntity.getActionLifecycle());
        return matches;
    }

    public List<MatchesEntity> mapToMatchesEntityList(List<Matches> matchesList) {
        List<MatchesEntity> matchesEntityList = new ArrayList<>();
        matchesList.forEach(matches -> matchesEntityList.add(map(matches)));
        return matchesEntityList;
    }

    public List<Matches> mapToMatchesList(List<MatchesEntity> matchesEntityList) {
        List<Matches> matchesList = new ArrayList<>();
        matchesEntityList.forEach(matchesEntity -> matchesList.add(map(matchesEntity)));
        return matchesList;
    }
}
