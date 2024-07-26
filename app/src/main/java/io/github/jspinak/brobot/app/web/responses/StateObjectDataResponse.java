package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StateObjectDataResponse {
    private String stateObjectId;
    private String objectType;
    private String stateObjectName;
    private String ownerStateName;
}

