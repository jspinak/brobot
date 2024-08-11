package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MatchesResponse {
    private Long id;
    private String actionDescription;
    private List<MatchResponse> matchList = new ArrayList<>();
    private List<MatchResponse> initialMatchList = new ArrayList<>();
    private ActionOptionsResponse actionOptions;
    private Set<String> activeStates;
    private String selectedText;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean success;
    private List<RegionResponse> definedRegions = new ArrayList<>();
    private int maxMatches;
    private String outputText;
}
