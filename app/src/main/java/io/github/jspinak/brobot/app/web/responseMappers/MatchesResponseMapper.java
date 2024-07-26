package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchesEntity;
import io.github.jspinak.brobot.app.web.responses.MatchesResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MatchesResponseMapper {

    private final MatchResponseMapper matchResponseMapper;
    private final ActionOptionsResponseMapper actionOptionsResponseMapper;
    private final RegionResponseMapper regionResponseMapper;

    public MatchesResponseMapper(MatchResponseMapper matchResponseMapper, ActionOptionsResponseMapper actionOptionsResponseMapper,
                                 RegionResponseMapper regionResponseMapper) {
        this.matchResponseMapper = matchResponseMapper;
        this.actionOptionsResponseMapper = actionOptionsResponseMapper;
        this.regionResponseMapper = regionResponseMapper;
    }

    public MatchesResponse map(MatchesEntity matchesEntity) {
        if (matchesEntity == null) {
            return null;
        }
        MatchesResponse matchesResponse = new MatchesResponse();
        matchesResponse.setId(matchesEntity.getId());
        matchesResponse.setActionDescription(matchesEntity.getActionDescription());
        matchesResponse.setMatchList(matchesEntity.getMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchesResponse.setInitialMatchList(matchesEntity.getInitialMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchesResponse.setActionOptions(actionOptionsResponseMapper.map(matchesEntity.getActionOptions()));
        matchesResponse.setActiveStates(matchesEntity.getActiveStates());
        matchesResponse.setSelectedText(matchesEntity.getSelectedText());
        matchesResponse.setDuration(matchesEntity.getDuration());
        matchesResponse.setStartTime(matchesEntity.getStartTime());
        matchesResponse.setEndTime(matchesEntity.getEndTime());
        matchesResponse.setSuccess(matchesEntity.isSuccess());
        matchesResponse.setDefinedRegions(matchesEntity.getDefinedRegions().stream()
                .map(regionResponseMapper::map)
                .collect(Collectors.toList()));
        matchesResponse.setMaxMatches(matchesEntity.getMaxMatches());
        matchesResponse.setOutputText(matchesEntity.getOutputText());
        return matchesResponse;
    }

    public MatchesEntity map(MatchesResponse matchesResponse) {
        if (matchesResponse == null) {
            return null;
        }
        MatchesEntity matchesEntity = new MatchesEntity();
        matchesEntity.setId(matchesResponse.getId());
        matchesEntity.setActionDescription(matchesResponse.getActionDescription());
        matchesEntity.setMatchList(matchesResponse.getMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchesEntity.setInitialMatchList(matchesResponse.getInitialMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchesEntity.setActionOptions(actionOptionsResponseMapper.map(matchesResponse.getActionOptions()));
        matchesEntity.setActiveStates(matchesResponse.getActiveStates());
        matchesEntity.setSelectedText(matchesResponse.getSelectedText());
        matchesEntity.setDuration(matchesResponse.getDuration());
        matchesEntity.setStartTime(matchesResponse.getStartTime());
        matchesEntity.setEndTime(matchesResponse.getEndTime());
        matchesEntity.setSuccess(matchesResponse.isSuccess());
        matchesEntity.setDefinedRegions(matchesResponse.getDefinedRegions().stream()
                .map(regionResponseMapper::map)
                .collect(Collectors.toList()));
        matchesEntity.setMaxMatches(matchesResponse.getMaxMatches());
        matchesEntity.setOutputText(matchesResponse.getOutputText());
        return matchesEntity;
    }
}
