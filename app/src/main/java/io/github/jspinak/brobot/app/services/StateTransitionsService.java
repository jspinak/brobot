package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.StateTransitionsEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.app.database.repositories.StateTransitionsRepo;
import io.github.jspinak.brobot.app.exceptions.StateTransitionsNotFoundException;
import io.github.jspinak.brobot.app.web.requests.*;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.StateTransitionsResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.TransitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateTransitionsResponse;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StateTransitionsService {

    private final StateTransitionsRepo stateTransitionsRepo;
    private final StateTransitionsResponseMapper stateTransitionsResponseMapper;
    private final TransitionService transitionService;
    private final TransitionResponseMapper transitionResponseMapper;
    private final StateService stateService;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateImageService stateImageService;
    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateTransitionsEntityMapper stateTransitionsEntityMapper;

    public StateTransitionsService(StateTransitionsRepo stateTransitionsRepo,
                                   StateTransitionsResponseMapper stateTransitionsResponseMapper,
                                   TransitionService transitionService,
                                   TransitionResponseMapper transitionResponseMapper,
                                   StateService stateService,
                                   StateImageEntityMapper stateImageEntityMapper,
                                   StateImageService stateImageService,
                                   StateImageResponseMapper stateImageResponseMapper,
                                   StateTransitionsEntityMapper stateTransitionsEntityMapper) {
        this.stateTransitionsRepo = stateTransitionsRepo;
        this.stateTransitionsResponseMapper = stateTransitionsResponseMapper;
        this.transitionService = transitionService;
        this.transitionResponseMapper = transitionResponseMapper;
        this.stateService = stateService;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateImageService = stateImageService;
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateTransitionsEntityMapper = stateTransitionsEntityMapper;
    }

    public List<StateTransitions> getAllStateTransitions() {
        return stateTransitionsRepo.findAll().stream()
                .map(stateTransitionsEntityMapper::map)
                .collect(Collectors.toList());
    }

    public StateTransitionsEntity getStateTransitionsEntity(Long stateId) {
        return stateTransitionsRepo.findByStateId(stateId)
                .orElseThrow(() -> new StateTransitionsNotFoundException(
                        "StateTransitions not found for state ID: " + stateId));
    }

    public StateTransitionsResponse getStateTransitionsResponse(Long stateId) {
        return stateTransitionsResponseMapper.map(getStateTransitionsEntity(stateId));
    }

    public StateTransitionsEntity createAndSaveStateTransitionsEntityReturnEntity(StateTransitionsCreateRequest request) {
        StateTransitionsEntity entity = new StateTransitionsEntity();
        entity.setStateId(request.getStateId());
        entity.setTransitions(request.getTransitions().stream()
                .map(transitionService::createAndSaveTransitionReturnEntity)
                .toList());
        if (request.getFinishTransition() != null) {
            entity.setFinishTransition(transitionService.createAndSaveTransitionReturnEntity(request.getFinishTransition()));
        }
        entity = stateTransitionsRepo.save(entity);
        return entity;
    }

    public StateTransitionsResponse createStateTransitionsEntityReturnResponse(StateTransitionsCreateRequest request) {
        return stateTransitionsResponseMapper.map(createAndSaveStateTransitionsEntityReturnEntity(request));
    }

    public StateTransitionsEntity updateStateTransitionsEntityReturnEntity(Long stateId, StateTransitionsUpdateRequest request) {
        StateTransitionsEntity entity = stateTransitionsRepo.findByStateId(stateId)
                .orElseThrow(() -> new StateTransitionsNotFoundException("StateTransitions not found for state ID: " + stateId));
        List<TransitionEntity> updatedTransitions = updateExistingTransitions(entity, request);
        List<TransitionEntity> newTransitions = createNewTransitions(entity, request);
        updatedTransitions.addAll(newTransitions);
        entity.setTransitions(updatedTransitions);
        updateFinishTransition(entity, request);
        entity = stateTransitionsRepo.save(entity);
        return entity;
    }

    public StateTransitionsResponse updateStateTransitionsEntityReturnResponse(Long stateId, StateTransitionsUpdateRequest request) {
        return stateTransitionsResponseMapper.map(updateStateTransitionsEntityReturnEntity(stateId, request));
    }

    private List<TransitionEntity> updateExistingTransitions(StateTransitionsEntity entity, StateTransitionsUpdateRequest request) {
        List<TransitionEntity> updatedTransitions = new ArrayList<>();
        for (TransitionUpdateRequest updateRequest : request.getTransitions()) {
            TransitionEntity existingTransition = entity.getTransitions().stream()
                    .filter(t -> t.getId().equals(updateRequest.getId()))
                    .findFirst()
                    .orElse(null);
            if (existingTransition != null) {
                TransitionEntity updatedTransition = transitionService.updateTransitionEntityReturnEntity(
                        existingTransition.getId(), updateRequest);
                updatedTransitions.add(updatedTransition);
            }
        }
        return updatedTransitions;
    }

    private List<TransitionEntity> createNewTransitions(StateTransitionsEntity entity, StateTransitionsUpdateRequest request) {
        List<TransitionEntity> newTransitions = new ArrayList<>();
        for (TransitionUpdateRequest updateRequest : request.getTransitions()) {
            boolean isNew = entity.getTransitions().stream()
                    .noneMatch(t -> t.getId().equals(updateRequest.getId()));

            if (isNew) {
                TransitionCreateRequest createRequest = convertToCreateRequest(updateRequest);
                TransitionEntity newTransition = transitionService.createAndSaveTransitionReturnEntity(createRequest);
                newTransitions.add(newTransition);
            }
        }
        return newTransitions;
    }

    private TransitionCreateRequest convertToCreateRequest(TransitionUpdateRequest updateRequest) {
        TransitionCreateRequest createRequest = new TransitionCreateRequest();
        createRequest.setSourceStateId(updateRequest.getSourceStateId());
        createRequest.setTargetStateId(updateRequest.getTargetStateId());
        createRequest.setStateImageId(updateRequest.getStateImageId());
        createRequest.setStaysVisibleAfterTransition(updateRequest.getStaysVisibleAfterTransition());
        createRequest.setActivate(updateRequest.getActivateStateIds());
        createRequest.setExit(updateRequest.getExitStateIds());
        createRequest.setScore(updateRequest.getScore() != null ? updateRequest.getScore() : 0);
        createRequest.setTimesSuccessful(updateRequest.getTimesSuccessful() != null ? updateRequest.getTimesSuccessful() : 0);
        createRequest.setActionDefinition(updateRequest.getActionDefinition());
        return createRequest;
    }

    private void updateFinishTransition(StateTransitionsEntity entity, StateTransitionsUpdateRequest request) {
        if (request.getFinishTransition() != null) {
            if (entity.getFinishTransition() != null) {
                TransitionEntity updatedFinishTransition = transitionService.updateTransitionEntityReturnEntity(
                        entity.getFinishTransition().getId(), request.getFinishTransition());
                entity.setFinishTransition(updatedFinishTransition);
            } else {
                TransitionCreateRequest createRequest = convertToCreateRequest(request.getFinishTransition());
                TransitionEntity newFinishTransition = transitionService.createAndSaveTransitionReturnEntity(createRequest);
                entity.setFinishTransition(newFinishTransition);
            }
        } else {
            entity.setFinishTransition(null);
        }
    }

    public void deleteStateTransitions(Long stateId) {
        StateTransitionsEntity entity = stateTransitionsRepo.findByStateId(stateId)
                .orElseThrow(() -> new StateTransitionsNotFoundException("StateTransitions not found for state ID: " + stateId));
        stateTransitionsRepo.delete(entity);
    }

    public TransitionResponse createTransitionForState(TransitionCreateRequest request) {
        StateTransitionsEntity stateTransitions = stateTransitionsRepo.findByStateId(request.getSourceStateId())
                .orElseGet(() -> {
                    StateTransitionsEntity newStateTransitions = new StateTransitionsEntity();
                    newStateTransitions.setStateId(request.getSourceStateId());
                    return stateTransitionsRepo.save(newStateTransitions);
                });

        TransitionEntity newTransition = transitionService.createAndSaveTransitionReturnEntity(request);
        stateTransitions.getTransitions().add(newTransition);

        if (stateTransitions.getFinishTransition() == null) {
            TransitionCreateRequest finishRequest = createFinishTransitionRequest(request.getSourceStateId());
            TransitionEntity finishTransition = transitionService.createAndSaveTransitionReturnEntity(finishRequest);
            stateTransitions.setFinishTransition(finishTransition);
        }

        stateTransitionsRepo.save(stateTransitions);

        return transitionResponseMapper.toResponse(newTransition);
    }

    private TransitionCreateRequest createFinishTransitionRequest(Long stateId) {
        TransitionCreateRequest request = new TransitionCreateRequest();
        request.setSourceStateId(stateId);
        request.setTargetStateId(stateId);

        // Get all StateImageEntity objects for the state
        List<StateImageEntity> stateImageEntities = stateImageService.getAllStateImageEntities();

        // Convert StateImageEntity objects to StateImageRequest objects
        List<StateImageRequest> stateImages = stateImageResponseMapper.toRequestList(stateImageEntities);

        // Create ActionDefinitionRequest for the finish transition
        ActionDefinitionRequest actionDefinitionRequest = new ActionDefinitionRequest();
        ActionStepRequest actionStepRequest = new ActionStepRequest();

        ActionOptionsRequest actionOptionsRequest = new ActionOptionsRequest();
        actionStepRequest.setActionOptions(actionOptionsRequest);

        ObjectCollectionRequest objectCollectionRequest = new ObjectCollectionRequest();
        objectCollectionRequest.setStateImages(stateImages);
        actionStepRequest.setObjectCollection(objectCollectionRequest);

        actionDefinitionRequest.setSteps(List.of(actionStepRequest));
        request.setActionDefinition(actionDefinitionRequest);

        return request;
    }

    private List<StateImage> getAllStateImagesForState(Long stateId) {
        Optional<StateEntity> stateEntityOptional = stateService.getStateEntity(stateId);
        List<StateImage> stateImages = new ArrayList<>();
        if (stateEntityOptional.isEmpty()) return stateImages;
        StateEntity stateEntity = stateEntityOptional.get();
        stateEntity.getStateImages().forEach(stateImage ->
                stateImages.add(stateImageEntityMapper.map(stateImage)));
        return stateImages;
    }
}