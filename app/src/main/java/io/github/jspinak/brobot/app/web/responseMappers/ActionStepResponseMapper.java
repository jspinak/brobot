package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.web.requests.ActionStepRequest;
import io.github.jspinak.brobot.app.web.responses.ActionStepResponse;
import org.springframework.stereotype.Component;

@Component
public class ActionStepResponseMapper {

    private final ActionOptionsResponseMapper actionOptionsResponseMapper;
    private final ObjectCollectionResponseMapper objectCollectionResponseMapper;

    public ActionStepResponseMapper(ActionOptionsResponseMapper actionOptionsResponseMapper,
                                    ObjectCollectionResponseMapper objectCollectionResponseMapper) {
        this.actionOptionsResponseMapper = actionOptionsResponseMapper;
        this.objectCollectionResponseMapper = objectCollectionResponseMapper;
    }

    public ActionStepResponse toResponse(ActionStepEntity entity) {
        if (entity == null) {
            return null;
        }

        ActionStepResponse response = new ActionStepResponse();
        response.setId(entity.getId());
        response.setActionOptions(actionOptionsResponseMapper.map(entity.getActionOptions()));
        response.setObjectCollection(objectCollectionResponseMapper.toResponse(entity.getObjectCollection()));

        return response;
    }

    public ActionStepEntity toEntity(ActionStepResponse response) {
        if (response == null) {
            return null;
        }

        ActionStepEntity entity = new ActionStepEntity();
        entity.setId(response.getId());
        entity.setActionOptions(actionOptionsResponseMapper.map(response.getActionOptions()));
        entity.setObjectCollection(objectCollectionResponseMapper.toEntity(response.getObjectCollection()));

        return entity;
    }

    public ActionStepEntity fromRequest(ActionStepRequest request) {
        if (request == null) return null;
        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptions(actionOptionsResponseMapper.fromRequest(request.getActionOptions()));
        entity.setObjectCollection(objectCollectionResponseMapper.fromRequest(request.getObjectCollection()));
        return entity;
    }

}
