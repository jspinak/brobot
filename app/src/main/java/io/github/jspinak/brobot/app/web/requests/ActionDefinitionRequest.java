package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ActionDefinitionRequest {
    private List<ActionStepRequest> steps = new ArrayList<>();
}