package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MatchSnapshotResponse {
    private Long id;
    private ActionOptionsResponse actionOptions;
    private List<MatchResponse> matchList;
    private String text;
    private double duration;
    private LocalDateTime timeStamp;
    private boolean actionSuccess;
    private boolean resultSuccess;
    private String state;
}
