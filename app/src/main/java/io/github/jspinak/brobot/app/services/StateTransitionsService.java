package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.StateTransitionsEntityMapper;
import io.github.jspinak.brobot.app.database.entities.*;
import io.github.jspinak.brobot.app.database.repositories.ProjectRepository;
import io.github.jspinak.brobot.app.database.repositories.StateTransitionsRepo;
import io.github.jspinak.brobot.app.exceptions.StateTransitionsNotFoundException;
import io.github.jspinak.brobot.app.web.requests.StateTransitionsCreateRequest;
import io.github.jspinak.brobot.app.web.requests.StateTransitionsUpdateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionUpdateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateTransitionsResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.TransitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateTransitionsResponse;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StateTransitionsService {

    private final StateTransitionsRepo stateTransitionsRepo;
    private final StateTransitionsResponseMapper stateTransitionsResponseMapper;
    private final TransitionService transitionService;
    private final TransitionResponseMapper transitionResponseMapper;
    private final StateService stateService;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateTransitionsEntityMapper stateTransitionsEntityMapper;
    private final ProjectRepository projectRepository;

    public StateTransitionsService(StateTransitionsRepo stateTransitionsRepo,
                                   StateTransitionsResponseMapper stateTransitionsResponseMapper,
                                   TransitionService transitionService,
                                   TransitionResponseMapper transitionResponseMapper,
                                   @Lazy StateService stateService,
                                   StateImageEntityMapper stateImageEntityMapper,
                                   StateTransitionsEntityMapper stateTransitionsEntityMapper,
                                   ProjectRepository projectRepository) {
        this.stateTransitionsRepo = stateTransitionsRepo;
        this.stateTransitionsResponseMapper = stateTransitionsResponseMapper;
        this.transitionService = transitionService;
        this.transitionResponseMapper = transitionResponseMapper;
        this.stateService = stateService;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateTransitionsEntityMapper = stateTransitionsEntityMapper;
        this.projectRepository = projectRepository;
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
        createRequest.setStateImageId(updateRequest.getStateImageId());
        createRequest.setStaysVisibleAfterTransition(updateRequest.getStaysVisibleAfterTransition());
        createRequest.setStatesToEnter(updateRequest.getStatesToEnter());
        createRequest.setStatesToExit(updateRequest.getStatesToExit());
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

        // create the finish transition if it doesn't already exist
        if (stateTransitions.getFinishTransition() == null) {
            Long stateId = request.getSourceStateId();
            TransitionEntity finishTransition = new TransitionEntity();
            
            Optional<ProjectEntity> projectEntityOptional = projectRepository.findById(request.getProjectId());
            projectEntityOptional.ifPresent(finishTransition::setProject);
            
            finishTransition.setSourceStateId(stateId);
            //finishTransition.setProject();
            finishTransition.setStatesToEnter(request.getStatesToEnter());
            finishTransition.setStaysVisibleAfterTransition(request.getStaysVisibleAfterTransition());
            if (!request.getStaysVisibleAfterTransition().equals(IStateTransition.StaysVisible.TRUE)) 
                finishTransition.setStatesToExit(Collections.singleton(stateId));
            
            // find one of the state images
            Optional<StateEntity> stateEntityOptional = stateService.getStateEntity(stateId);
            if (stateEntityOptional.isPresent()) {
                StateEntity stateEntity = stateEntityOptional.get();
                Set<StateImageEntity> stateImageEntitySet = stateEntity.getStateImages();
                ActionDefinitionEntity actionDefinitionEntity = new ActionDefinitionEntity();
                ActionOptionsEntity actionOptionsEntity = new ActionOptionsEntity();
                actionOptionsEntity.setAction(ActionOptions.Action.FIND);
                ObjectCollectionEntity objectCollectionEntity = new ObjectCollectionEntity();
                objectCollectionEntity.setStateImages(new ArrayList<>(stateImageEntitySet));
                actionDefinitionEntity.addStepEntity(actionOptionsEntity, objectCollectionEntity);
            }
            TransitionEntity savedFinishTransition = transitionService.save(finishTransition);
            stateTransitions.setFinishTransition(savedFinishTransition);
        }

        stateTransitionsRepo.save(stateTransitions);
        return transitionResponseMapper.toResponse(newTransition);
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

    public List<StateTransitions> getAllStateTransitionsForProject(Long projectId) {
        return stateTransitionsRepo.findByProjectId(projectId).stream()
                .map(stateTransitionsEntityMapper::map)
                .toList();
    }

    public List<StateTransitionsEntity> getAllStateTransitionsEntitiesForProject(Long projectId) {
        return stateTransitionsRepo.findByProjectId(projectId);
    }

    public void save(StateTransitionsEntity stateTransitionsEntity) {
        stateTransitionsRepo.save(stateTransitionsEntity);
    }
}