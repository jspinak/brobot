package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class MatchesRequest {
    private Long id;
    private String actionDescription;
    private List<MatchRequest> matchList = new ArrayList<>();
    private List<MatchRequest> initialMatchList = new ArrayList<>();
    private ActionOptionsRequest actionOptions;
    private Set<String> activeStates;
    private String selectedText;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean success;
    private List<RegionRequest> definedRegions = new ArrayList<>();
    private int maxMatches;
    private String outputText;
}
