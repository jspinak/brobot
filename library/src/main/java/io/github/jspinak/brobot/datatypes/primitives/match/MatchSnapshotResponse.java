package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptionsResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchSnapshotResponse {

    private Long id = 0L;
    private ActionOptionsResponse actionOptions = new ActionOptionsResponse(null);
    private List<MatchResponse> matchList = new ArrayList<>();
    private String text = "";
    private double duration = 0.0;
    private LocalDateTime timeStamp = LocalDateTime.now();
    private boolean actionSuccess = false;
    private boolean resultSuccess = false;
    private String state = "";

    public MatchSnapshotResponse(MatchSnapshot matchSnapshot) {
        if (matchSnapshot == null) return;
        id = matchSnapshot.getId();
        actionOptions = new ActionOptionsResponse(matchSnapshot.getActionOptions());
        matchSnapshot.getMatchList().forEach(match -> matchList.add(new MatchResponse(match)));
        text = matchSnapshot.getText();
        duration = matchSnapshot.getDuration();
        timeStamp = matchSnapshot.getTimeStamp();
        actionSuccess = matchSnapshot.isActionSuccess();
        resultSuccess = matchSnapshot.isResultSuccess();
        state = matchSnapshot.getState();
    }
}
