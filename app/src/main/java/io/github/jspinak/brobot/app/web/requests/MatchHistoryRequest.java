package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MatchHistoryRequest {
    private Long id;
    private int timesSearched;
    private int timesFound;
    private List<MatchSnapshotRequest> snapshots = new ArrayList<>();
}
