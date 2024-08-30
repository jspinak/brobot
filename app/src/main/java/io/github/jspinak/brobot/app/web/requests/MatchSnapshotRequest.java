package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MatchSnapshotRequest {
    private Long id;
    private ActionOptionsRequest actionOptions;
    private List<MatchRequest> matchList = new ArrayList<>();
    private String text;
    private double duration;
    private LocalDateTime timeStamp;
    private boolean actionSuccess;
    private boolean resultSuccess;
    private String state;
}
