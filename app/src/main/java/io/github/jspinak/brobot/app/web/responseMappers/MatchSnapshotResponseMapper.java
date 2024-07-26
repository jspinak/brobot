package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchSnapshotEntity;
import io.github.jspinak.brobot.app.web.responses.MatchSnapshotResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MatchSnapshotResponseMapper {

    private final ActionOptionsResponseMapper actionOptionsResponseMapper;
    private final MatchResponseMapper matchResponseMapper;

    public MatchSnapshotResponseMapper(ActionOptionsResponseMapper actionOptionsResponseMapper, MatchResponseMapper matchResponseMapper) {
        this.actionOptionsResponseMapper = actionOptionsResponseMapper;
        this.matchResponseMapper = matchResponseMapper;
    }

    public MatchSnapshotResponse map(MatchSnapshotEntity matchSnapshotEntity) {
        if (matchSnapshotEntity == null) {
            return null;
        }
        MatchSnapshotResponse matchSnapshotResponse = new MatchSnapshotResponse();
        matchSnapshotResponse.setId(matchSnapshotEntity.getId());
        matchSnapshotResponse.setActionOptions(actionOptionsResponseMapper.map(matchSnapshotEntity.getActionOptions()));
        matchSnapshotResponse.setMatchList(matchSnapshotEntity.getMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchSnapshotResponse.setText(matchSnapshotEntity.getText());
        matchSnapshotResponse.setDuration(matchSnapshotEntity.getDuration());
        matchSnapshotResponse.setTimeStamp(matchSnapshotEntity.getTimeStamp());
        matchSnapshotResponse.setActionSuccess(matchSnapshotEntity.isActionSuccess());
        matchSnapshotResponse.setResultSuccess(matchSnapshotEntity.isResultSuccess());
        matchSnapshotResponse.setState(matchSnapshotEntity.getState());
        return matchSnapshotResponse;
    }

    public MatchSnapshotEntity map(MatchSnapshotResponse matchSnapshotResponse) {
        if (matchSnapshotResponse == null) {
            return null;
        }
        MatchSnapshotEntity matchSnapshotEntity = new MatchSnapshotEntity();
        matchSnapshotEntity.setId(matchSnapshotResponse.getId());
        matchSnapshotEntity.setActionOptions(actionOptionsResponseMapper.map(matchSnapshotResponse.getActionOptions()));
        matchSnapshotEntity.setMatchList(matchSnapshotResponse.getMatchList().stream()
                .map(matchResponseMapper::map)
                .collect(Collectors.toList()));
        matchSnapshotEntity.setText(matchSnapshotResponse.getText());
        matchSnapshotEntity.setDuration(matchSnapshotResponse.getDuration());
        matchSnapshotEntity.setTimeStamp(matchSnapshotResponse.getTimeStamp());
        matchSnapshotEntity.setActionSuccess(matchSnapshotResponse.isActionSuccess());
        matchSnapshotEntity.setResultSuccess(matchSnapshotResponse.isResultSuccess());
        matchSnapshotEntity.setState(matchSnapshotResponse.getState());
        return matchSnapshotEntity;
    }
}