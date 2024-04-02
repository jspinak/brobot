package io.github.jspinak.brobot.app.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MatchHistoryResponse {

    private int timesSearched = 0;
    private int timesFound = 0;
    private List<MatchSnapshotResponse> snapshots = new ArrayList<>();

}
