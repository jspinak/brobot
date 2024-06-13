package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.MatchHistoryEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;

public class MatchHistoryEntityMapper {
    
    public static MatchHistoryEntity map(MatchHistory matchHistory) {
        MatchHistoryEntity matchHistoryEntity = new MatchHistoryEntity();
        matchHistoryEntity.setTimesSearched(matchHistory.getTimesSearched());
        matchHistoryEntity.setTimesFound(matchHistory.getTimesFound());
        matchHistoryEntity.setSnapshots(MatchSnapshotEntityMapper.mapToMatchSnapshotEntityList(matchHistory.getSnapshots()));
        return matchHistoryEntity;
    }

    public static MatchHistory map(MatchHistoryEntity matchHistoryEntity) {
        MatchHistory matchHistory = new MatchHistory();
        matchHistory.setTimesSearched(matchHistoryEntity.getTimesSearched());
        matchHistory.setTimesFound(matchHistoryEntity.getTimesFound());
        matchHistory.setSnapshots(MatchSnapshotEntityMapper.mapToMatchSnapshotList(matchHistoryEntity.getSnapshots()));
        return matchHistory;
    }
}
