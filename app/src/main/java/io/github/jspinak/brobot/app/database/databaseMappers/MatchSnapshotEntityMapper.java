package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchSnapshotEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatchSnapshotEntityMapper {

    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final MatchEntityMapper matchEntityMapper;

    public MatchSnapshotEntityMapper(ActionOptionsEntityMapper actionOptionsEntityMapper,
                                     MatchEntityMapper matchEntityMapper) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.matchEntityMapper = matchEntityMapper;
    }
    
    public MatchSnapshotEntity map(MatchSnapshot matchSnapshot) {
        MatchSnapshotEntity matchSnapshotEntity = new MatchSnapshotEntity();
        matchSnapshotEntity.setActionOptions(actionOptionsEntityMapper.map(matchSnapshot.getActionOptions()));
        matchSnapshotEntity.setMatchList(matchEntityMapper.mapToMatchEntityList(matchSnapshot.getMatchList()));
        matchSnapshotEntity.setText(matchSnapshot.getText());
        matchSnapshotEntity.setDuration(matchSnapshot.getDuration());
        matchSnapshotEntity.setTimeStamp(matchSnapshot.getTimeStamp());
        matchSnapshotEntity.setActionSuccess(matchSnapshot.isActionSuccess());
        matchSnapshotEntity.setResultSuccess(matchSnapshot.isResultSuccess());
        matchSnapshotEntity.setState(matchSnapshot.getState());
        return matchSnapshotEntity;
    }

    public MatchSnapshot map(MatchSnapshotEntity matchSnapshotEntity) {
        MatchSnapshot matchSnapshot = new MatchSnapshot();
        matchSnapshot.setActionOptions(actionOptionsEntityMapper.map(matchSnapshotEntity.getActionOptions()));
        matchSnapshot.setMatchList(matchEntityMapper.mapToMatchList(matchSnapshotEntity.getMatchList()));
        matchSnapshot.setText(matchSnapshot.getText());
        matchSnapshot.setDuration(matchSnapshot.getDuration());
        matchSnapshot.setTimeStamp(matchSnapshot.getTimeStamp());
        matchSnapshot.setActionSuccess(matchSnapshot.isActionSuccess());
        matchSnapshot.setResultSuccess(matchSnapshot.isResultSuccess());
        matchSnapshot.setState(matchSnapshot.getState());
        return matchSnapshot;
    }

    public List<MatchSnapshotEntity> mapToMatchSnapshotEntityList(List<MatchSnapshot> snapshots) {
        List<MatchSnapshotEntity> matchSnapshotEntityList = new ArrayList<>();
        snapshots.forEach(snapshot -> matchSnapshotEntityList.add(map(snapshot)));
        return matchSnapshotEntityList;
    }

    public List<MatchSnapshot> mapToMatchSnapshotList(List<MatchSnapshotEntity> matchSnapshotEntityList) {
        List<MatchSnapshot> matchSnapshots = new ArrayList<>();
        matchSnapshotEntityList.forEach(entity -> matchSnapshots.add(map(entity)));
        return matchSnapshots;
    }
}
