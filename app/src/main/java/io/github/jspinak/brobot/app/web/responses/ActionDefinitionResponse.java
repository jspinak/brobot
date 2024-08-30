package io.github.jspinak.brobot.app.web.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActionDefinitionResponse {
    private Long id;
    private String actionType;
    private List<ActionStepResponse> steps;
}
