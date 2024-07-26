package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MatchHistoryResponse {
    private Long id;
    private int timesSearched;
    private int timesFound;
    private List<MatchSnapshotResponse> snapshots;
}
