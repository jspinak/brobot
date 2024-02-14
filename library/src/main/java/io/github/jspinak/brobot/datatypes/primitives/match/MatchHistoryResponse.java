package io.github.jspinak.brobot.datatypes.primitives.match;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchHistoryResponse {

    private Long id = 0L;
    private int timesSearched = 0;
    private int timesFound = 0;
    private List<MatchSnapshotResponse> snapshots = new ArrayList<>();

    public MatchHistoryResponse(MatchHistory matchHistory) {
        if (matchHistory == null) return;
        id = matchHistory.getId();
        timesSearched = matchHistory.getTimesSearched();
        timesFound = matchHistory.getTimesFound();
        matchHistory.getSnapshots().forEach(snapshot -> snapshots.add(new MatchSnapshotResponse(snapshot)));
    }
}
