package com.brobot.app.responses;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchHistoryResponse {

    private int timesSearched = 0;
    private int timesFound = 0;
    private List<MatchSnapshotResponse> snapshots = new ArrayList<>();

}
