package io.github.jspinak.brobot.app.responses;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchSnapshotResponse {

    private ActionOptionsResponse actionOptions = new ActionOptionsResponse();
    private List<MatchResponse> matchList = new ArrayList<>();
    private String text = "";
    private double duration = 0.0;
    private LocalDateTime timeStamp = LocalDateTime.now();
    private boolean actionSuccess = false;
    private boolean resultSuccess = false;
    private String state = "";

}
