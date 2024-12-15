package io.github.jspinak.brobot.testingAUTs;

import lombok.Data;

import java.util.Set;

// Record state visit information
@Data
public class StateVisit {
    private final Long stateId;
    private final String stateName;
    private final long timestamp;
    private final boolean successful;

    public StateVisit(Long stateId, String stateName, boolean successful) {
        this.stateId = stateId;
        this.stateName = stateName;
        this.timestamp = System.currentTimeMillis();
        this.successful = successful;
    }

}
