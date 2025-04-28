package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.TransitionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.*;
import io.github.jspinak.brobot.app.database.repositories.TransitionRepo;
import io.github.jspinak.brobot.app.web.requests.ActionOptionsRequest;
import io.github.jspinak.brobot.app.web.requests.ActionStepRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionUpdateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.TransitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.manageStates.ActionDefinitionStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

@Service
public class TransitionService {
    private final TransitionRepo transitionRepo;
    private final TransitionResponseMapper transitionResponseMapper;
    private final TransitionEntityMapper transitionEntityMapper;
    private final StateImageService stateImageService;
    private final ActionDefinitionService actionDefinitionService;
    private final SceneService sceneService;
    private final PatternService patternService;

    private static final Logger logger = LoggerFactory.getLogger(TransitionService.class);

    public TransitionService(TransitionRepo transitionRepo,
                             TransitionResponseMapper transitionResponseMapper,
                             TransitionEntityMapper transitionEntityMapper,
                             StateImageService stateImageService, ActionDefinitionService actionDefinitionService,
                             SceneService sceneService, PatternService patternService) {
        this.transitionRepo = transitionRepo;
        this.transitionResponseMapper = transitionResponseMapper;
        this.transitionEntityMapper = transitionEntityMapper;
        this.stateImageService = stateImageService;
        this.actionDefinitionService = actionDefinitionService;
        this.sceneService = sceneService;
        this.patternService = patternService;
    }

    public List<TransitionResponse> getAllTransitionsAsResponses() {
        List<TransitionEntity> transitionEntities = transitionRepo.findAll();
        return transitionEntities.stream()
                .map(transitionResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TransitionEntity> getAllTransitionsForProject(Long projectId) {
        return transitionRepo.findAllByProjectId(projectId);
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

            addActionDefinitionsToEntity(entity, request);

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

    public TransitionEntity save(TransitionEntity transitionEntity) {
        return transitionRepo.save(transitionEntity);
    }

    public TransitionEntity updateTransitionEntityReturnEntity(Long id, TransitionUpdateRequest request) {
        TransitionEntity entity = transitionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transition not found"));
        transitionResponseMapper.updateEntityFromRequest(entity, request);
        if (request.getActionDefinition() != null) {
            //addActionDefinitionsToEntity(entity, request); // TODO
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

    private void addActionDefinitionsToEntity(TransitionEntity entity, TransitionCreateRequest request) {
        ActionDefinitionEntity actionDefinitionEntity = new ActionDefinitionEntity();
        List<ActionStepEntity> actionStepEntities = new ArrayList<>();
        for (ActionStepRequest stepRequest : request.getActionDefinition().getSteps()) {
            ActionStepEntity stepEntity = new ActionStepEntity();
            // Convert ActionOptionsRequest to ActionOptionsEntity
            ActionOptionsEntity actionOptionsEntity = convertToActionOptionsEntity(stepRequest.getActionOptions());
            stepEntity.setActionOptionsEntity(actionOptionsEntity);
            // Convert ObjectCollectionRequest to ObjectCollectionEntity
            ObjectCollectionEntity objectCollectionEntity = convertToObjectCollectionEntity(request);
            stepEntity.setObjectCollectionEntity(objectCollectionEntity);
            actionStepEntities.add(stepEntity);
            logger.debug(stepEntity.toString());
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

    private ObjectCollectionEntity convertToObjectCollectionEntity(TransitionCreateRequest request) {
        ObjectCollectionEntity entity = new ObjectCollectionEntity();
        // Convert and set state locations, images, regions, strings, matches, and scenes
        // This is only for the most basic click transition
        StateImageEntity stateImageEntity = stateImageService.getStateImage(request.getStateImageId());
        entity.setStateImages(Collections.singletonList(stateImageEntity));
        return entity;
    }

    public List<StateTransitions> buildStateTransitionsForProject(List<State> states, Long projectId) {
        List<TransitionEntity> transitions = getAllTransitionsForProject(projectId);
        logger.debug("there are " + transitions.size() + " transitions");
        logger.debug("all transitions = " + transitionRepo.findAll().size());
        List<StateTransitions> stateTransitions = new ArrayList<>();
        for (State state : states) {
            StateTransitions sT = new StateTransitions();
            sT.setStateId(state.getId());
            sT.setStateName(state.getName());
            for (TransitionEntity transitionEntity : transitions) addTransition(sT, transitionEntity, state);
            addStandardFinishTransition(sT, state);
            stateTransitions.add(sT);
        }
        return stateTransitions;
    }

    private void addStandardFinishTransition(StateTransitions sT, State state) {
        ActionDefinitionStateTransition finishTransition = new ActionDefinitionStateTransition();
        ActionOptions find = new ActionOptions.Builder().setAction(FIND).build();
        ObjectCollection objColl = new ObjectCollection.Builder().
                withImages(state.getStateImages().toArray(new StateImage[0])).build();
        finishTransition.setActionDefinition(new ActionDefinition(find, objColl));
        sT.setTransitionFinish(finishTransition);
    }

    private void addTransition(StateTransitions sT, TransitionEntity transitionEntity, State state) {
        logger.debug(" state.id=" + state.getId() + " transition source state id = " + transitionEntity.getSourceStateId());
        if (Objects.equals(transitionEntity.getSourceStateId(), state.getId())) {
            ActionDefinitionStateTransition adst = transitionEntityToADST(transitionEntity);
            sT.addTransition(adst);
        }
    }

    public ActionDefinitionStateTransition transitionEntityToADST(TransitionEntity transitionEntity) {
        ActionDefinitionStateTransition adst = transitionEntityMapper.map(transitionEntity, sceneService, patternService);
        adst.setActionDefinition(actionDefinitionService.mapFromEntityToLibraryClass(transitionEntity.getActionDefinition()));
        return adst;
    }

    /**
     * Deletes any transition with the source state.
     * @param sourceStateId source state id
     */
    public void deleteBySourceStateId(Long sourceStateId) {
        logger.info("Deleting transitions for sourceStateId: {}", sourceStateId);
        if (sourceStateId == null) {
            logger.error("sourceStateId is null!");
            throw new IllegalArgumentException("sourceStateId cannot be null");
        }
        transitionRepo.deleteBySourceStateId(sourceStateId);
    }

    @Transactional
    public void safeDeleteTransition(Long transitionId) {
        TransitionEntity transition = transitionRepo.findById(transitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transition not found"));

        // Clear associations in ActionDefinitionEntity
        if (transition.getActionDefinition() != null) {
            ActionDefinitionEntity actionDefinition = transition.getActionDefinition();
            for (ActionStepEntity step : actionDefinition.getSteps()) {
                if (step.getObjectCollectionEntity() != null) {
                    step.getObjectCollectionEntity().getStateImages().clear();
                }
            }
            actionDefinition.getSteps().clear();
        }

        // Now it's safe to delete the transition
        transitionRepo.delete(transition);
    }

    @Transactional
    public void deleteTransitionsInvolvingStateImage(Long stateImageId) {
        List<TransitionEntity> transitionsToDelete = findTransitionsInvolvingStateImage(stateImageId);
        for (TransitionEntity transition : transitionsToDelete) {
            safeDeleteTransition(transition.getId());
        }
    }
    public List<TransitionEntity> findTransitionsInvolvingStateImage(Long stateImageId) {
        List<TransitionEntity> involvedTransitions = new ArrayList<>();
        List<TransitionEntity> allTransitions = transitionRepo.findAll();

        for (TransitionEntity transition : allTransitions) {
            if (transition.getActionDefinition() != null) {
                for (ActionStepEntity step : transition.getActionDefinition().getSteps()) {
                    if (step.getObjectCollectionEntity() != null) {
                        if (step.getObjectCollectionEntity().getStateImages().stream()
                                .anyMatch(si -> si.getId().equals(stateImageId))) {
                            involvedTransitions.add(transition);
                            break;
                        }
                    }
                }
            }
        }

        return involvedTransitions;
    }


}