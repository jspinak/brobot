package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ActionOptionsEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.database.repositories.ActionDefinitionRepo;
import io.github.jspinak.brobot.app.web.requests.ActionStepRequest;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActionDefinitionService {


    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;
    private final ActionStepService actionStepService;
    private final ActionDefinitionRepo actionDefinitionRepo;
    private final SceneService sceneService;
    private final PatternService patternService;

    public ActionDefinitionService(ActionOptionsEntityMapper actionOptionsEntityMapper,
                                   ObjectCollectionEntityMapper objectCollectionEntityMapper,
                                   ActionStepService actionStepService, ActionDefinitionRepo actionDefinitionRepo,
                                   SceneService sceneService, PatternService patternService) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
        this.actionStepService = actionStepService;
        this.actionDefinitionRepo = actionDefinitionRepo;
        this.sceneService = sceneService;
        this.patternService = patternService;
    }

    public ActionDefinitionEntity createActionDefinitionEntity(ActionDefinition actionDefinition) {
        ActionDefinitionEntity entity = new ActionDefinitionEntity();

        List<ActionStepEntity> steps = actionDefinition.getSteps().stream()
                .map(this::createActionStepEntity)
                .collect(Collectors.toList());

        entity.setSteps(steps);

        return entity;
    }

    private ActionStepEntity createActionStepEntity(ActionStep step) {
        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptionsEntity(actionOptionsEntityMapper.map(step.getActionOptions()));
        entity.setObjectCollectionEntity(objectCollectionEntityMapper.map(step.getObjectCollection(), sceneService, patternService));
        return entity;
    }

    public ActionDefinition mapFromEntityToLibraryClass(ActionDefinitionEntity actionDefinitionEntity) {
        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setSteps(actionStepService.mapFromEntitiesToActionSteps(actionDefinitionEntity.getSteps()));
        return actionDefinition;
    }

    public ActionDefinitionEntity save(ActionDefinitionEntity entity) {
        return actionDefinitionRepo.save(entity);
    }

    public ActionDefinitionEntity findById(Long id) {
        return actionDefinitionRepo.findById(id).orElse(null);
    }

    public List<ActionDefinitionEntity> findByProjectId(Long projectId) {
        return actionDefinitionRepo.findByProjectId(projectId);
    }

    public void delete(Long id) {
        actionDefinitionRepo.deleteById(id);
    }

    public ActionDefinitionEntity updateSteps(Long id, List<ActionStepRequest> steps) {
        ActionDefinitionEntity entity = actionDefinitionRepo.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }

        List<ActionStepEntity> stepEntities = steps.stream()
                .map(actionStepService::createActionStepEntity)
                .collect(Collectors.toList());

        entity.setSteps(stepEntities);
        return actionDefinitionRepo.save(entity);
    }
}
