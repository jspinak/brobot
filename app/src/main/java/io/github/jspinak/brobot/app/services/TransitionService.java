package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.entities.*;
import io.github.jspinak.brobot.app.database.repositories.TransitionRepo;
import io.github.jspinak.brobot.app.web.requests.*;
import io.github.jspinak.brobot.app.web.responseMappers.TransitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import io.github.jspinak.brobot.dsl.DSLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransitionService {
    private final TransitionRepo transitionRepo;
    private final ActionDefinitionService actionDefinitionService;
    private final DSLParser dslParser;
    private final TransitionResponseMapper transitionResponseMapper;
    private final StateImageService stateImageService;

    private static final Logger logger = LoggerFactory.getLogger(TransitionService.class);

    public TransitionService(TransitionRepo transitionRepo,
                             ActionDefinitionService actionDefinitionService,
                             DSLParser dslParser, TransitionResponseMapper transitionResponseMapper,
                             StateImageService stateImageService) {
        this.transitionRepo = transitionRepo;
        this.actionDefinitionService = actionDefinitionService;
        this.dslParser = dslParser;
        this.transitionResponseMapper = transitionResponseMapper;
        this.stateImageService = stateImageService;
    }

    public List<TransitionResponse> getAllTransitionsAsResponses() {
        List<TransitionEntity> transitionEntities = transitionRepo.findAll();
        return transitionEntities.stream()
                .map(transitionResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TransitionEntity getTransitionEntity(Long id) {
        return transitionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transition not found"));
    }

    public TransitionResponse getTransitionResponse(Long id) {
        return transitionResponseMapper.toResponse(getTransitionEntity(id));
    }

    public TransitionEntity createAndSaveTransitionReturnEntity(TransitionCreateRequest request) {
        logger.info("Received create transition request: {}", request);
        try {
            TransitionEntity entity = transitionResponseMapper.toEntity(request);
            addActionDefinitionsToEntity(entity, request.getActionDefinition());

            logger.info("Saving transition entity: {}", entity);
            TransitionEntity savedEntity = transitionRepo.save(entity);
            logger.info("Saved transition entity: {}", savedEntity);
            stateImageService.addInvolvedTransition(request.getStateImageId(), savedEntity.getId());
            return entity;
        } catch (Exception e) {
            logger.error("Error creating transition", e);
            throw new RuntimeException("Failed to create transition", e);
        }
    }

    public TransitionResponse createAndSaveTransitionReturnResponse(TransitionCreateRequest request) {
        logger.info("Received create transition request: {}", request);
        return transitionResponseMapper.toResponse(createAndSaveTransitionReturnEntity(request));
    }

    public TransitionEntity updateTransitionEntityReturnEntity(Long id, TransitionUpdateRequest request) {
        TransitionEntity entity = transitionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transition not found"));
        transitionResponseMapper.updateEntityFromRequest(entity, request);
        if (request.getActionDefinition() != null) {
            addActionDefinitionsToEntity(entity, request.getActionDefinition());
        }
        TransitionEntity updatedEntity = transitionRepo.save(entity);
        return updatedEntity;
    }

    public TransitionResponse updateTransitionEntityReturnResponse(Long id, TransitionUpdateRequest request) {
        return transitionResponseMapper.toResponse(updateTransitionEntityReturnEntity(id, request));
    }

    public void deleteTransition(Long id) {
        if (!transitionRepo.existsById(id)) {
            throw new ResourceNotFoundException("Transition not found");
        }
        transitionRepo.deleteById(id);
    }

    private void addActionDefinitionsToEntity(TransitionEntity entity, ActionDefinitionRequest actionDefinitionRequest) {
        ActionDefinitionEntity actionDefinitionEntity = new ActionDefinitionEntity();
        List<ActionStepEntity> actionStepEntities = new ArrayList<>();
        for (ActionStepRequest stepRequest : actionDefinitionRequest.getSteps()) {
            ActionStepEntity stepEntity = new ActionStepEntity();
            // Convert ActionOptionsRequest to ActionOptionsEntity
            ActionOptionsEntity actionOptionsEntity = convertToActionOptionsEntity(stepRequest.getActionOptions());
            stepEntity.setActionOptions(actionOptionsEntity);
            // Convert ObjectCollectionRequest to ObjectCollectionEntity
            ObjectCollectionEntity objectCollectionEntity = convertToObjectCollectionEntity(stepRequest.getObjectCollection());
            stepEntity.setObjectCollection(objectCollectionEntity);
            actionStepEntities.add(stepEntity);
        }
        actionDefinitionEntity.setSteps(actionStepEntities);
        entity.setActionDefinition(actionDefinitionEntity);
    }

    private ActionOptionsEntity convertToActionOptionsEntity(ActionOptionsRequest request) {
        ActionOptionsEntity entity = new ActionOptionsEntity();
        entity.setAction(request.getAction());
        entity.setFind(request.getFind());
        entity.setKeepLargerMatches(request.isKeepLargerMatches());
        entity.setSimilarity(request.getSimilarity());
        // Set other fields as necessary
        return entity;
    }

    private ObjectCollectionEntity convertToObjectCollectionEntity(ObjectCollectionRequest request) {
        ObjectCollectionEntity entity = new ObjectCollectionEntity();
        // Convert and set state locations, images, regions, strings, matches, and scenes
        // You'll need to implement these conversions based on your entity structure
        return entity;
    }
}