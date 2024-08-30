package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.app.web.requests.ActionOptionsRequest;
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

    public ActionOptionsEntity fromRequest(ActionOptionsRequest request) {
        if (request == null) return null;
        ActionOptionsEntity entity = new ActionOptionsEntity();
        entity.setId(request.getId());
        entity.setAction(request.getAction());
        entity.setFind(request.getFind());
        entity.setKeepLargerMatches(request.isKeepLargerMatches());
        entity.setSimilarity(request.getSimilarity());
        return entity;
    }

    public ActionOptionsRequest toRequest(ActionOptionsEntity entity) {
        if (entity == null) {
            return null;
        }

        ActionOptionsRequest request = new ActionOptionsRequest();
        request.setId(entity.getId());
        request.setAction(entity.getAction());
        request.setFind(entity.getFind());
        request.setKeepLargerMatches(entity.isKeepLargerMatches());
        request.setSimilarity(entity.getSimilarity());

        // Note: We're only mapping a subset of fields here.
        // Add more fields as needed.

        return request;
    }
}
