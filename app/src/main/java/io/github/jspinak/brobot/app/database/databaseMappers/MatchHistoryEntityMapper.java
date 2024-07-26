package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchHistoryEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import org.springframework.stereotype.Component;

@Component
public class MatchHistoryEntityMapper {

    private final MatchSnapshotEntityMapper matchSnapshotEntityMapper;

    public MatchHistoryEntityMapper(MatchSnapshotEntityMapper matchSnapshotEntityMapper) {
        this.matchSnapshotEntityMapper = matchSnapshotEntityMapper;
    }
    
    public MatchHistoryEntity map(MatchHistory matchHistory) {
        MatchHistoryEntity matchHistoryEntity = new MatchHistoryEntity();
        matchHistoryEntity.setTimesSearched(matchHistory.getTimesSearched());
        matchHistoryEntity.setTimesFound(matchHistory.getTimesFound());
        matchHistoryEntity.setSnapshots(matchSnapshotEntityMapper.mapToMatchSnapshotEntityList(matchHistory.getSnapshots()));
        return matchHistoryEntity;
    }

    public MatchHistory map(MatchHistoryEntity matchHistoryEntity) {
        MatchHistory matchHistory = new MatchHistory();
        matchHistory.setTimesSearched(matchHistoryEntity.getTimesSearched());
        matchHistory.setTimesFound(matchHistoryEntity.getTimesFound());
        matchHistory.setSnapshots(matchSnapshotEntityMapper.mapToMatchSnapshotList(matchHistoryEntity.getSnapshots()));
        return matchHistory;
    }
}
