package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class ActionOptionsResponse {

    private Long id;
    private ActionOptions.Action action = ActionOptions.Action.FIND;

}