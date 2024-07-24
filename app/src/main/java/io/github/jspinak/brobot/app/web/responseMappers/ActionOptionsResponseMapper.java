package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.app.web.responses.ActionOptionsResponse;
import org.springframework.stereotype.Component;

@Component
public class ActionOptionsResponseMapper {

    public ActionOptionsResponse map(ActionOptionsEntity actionOptions) {
        ActionOptionsResponse actionOptionsResponse = new ActionOptionsResponse();
        actionOptionsResponse.setId(actionOptions.getId());
        actionOptionsResponse.setAction(actionOptions.getAction());
        return actionOptionsResponse;
    }

    public ActionOptionsEntity map(ActionOptionsResponse actionOptionsResponse) {
        ActionOptionsEntity actionOptions = new ActionOptionsEntity();
        actionOptions.setId(actionOptionsResponse.getId());
        actionOptions.setAction(actionOptionsResponse.getAction());
        return actionOptions;
    }
}
