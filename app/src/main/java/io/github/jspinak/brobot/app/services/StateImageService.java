package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.repositories.StateImageRepo;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.requests.StateImageRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StateImageService {
    private static final Logger logger = LoggerFactory.getLogger(StateImageService.class);

    private final StateImageRepo stateImageRepo;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final PatternService patternService;
    private final StateImageResponseMapper stateImageResponseMapper;

    public StateImageService(StateImageRepo stateImageRepo,
                             StateImageEntityMapper stateImageEntityMapper,
                             PatternService patternService, StateImageResponseMapper stateImageResponseMapper) {
        this.stateImageRepo = stateImageRepo;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.patternService = patternService;
        this.stateImageResponseMapper = stateImageResponseMapper;
    }

    public StateImage mapWithImage(StateImageEntity stateImageEntity) {
        StateImage stateImage = stateImageEntityMapper.map(stateImageEntity); // Patterns do not have Image
        List<Pattern> patterns = new ArrayList<>();
        stateImageEntity.getPatterns().forEach(patternEntity -> patterns.add(patternService.mapWithImage(patternEntity)));
        stateImage.setPatterns(patterns);
        return stateImage;
    }

    public List<StateImage> mapWithImages(List<StateImageEntity> stateImageEntities) {
        List<StateImage> stateImages = new ArrayList<>();
        stateImageEntities.forEach(stateImageEntity -> stateImages.add(mapWithImage(stateImageEntity)));
        return stateImages;
    }

    public StateImageEntity getStateImage(Long id) {
        Optional<StateImageEntity> stateImageOpt = stateImageRepo.findById(id);
        return stateImageOpt.orElse(null);
    }

    public StateImage getStateImage(String name) {
        Optional<StateImageEntity> dto = stateImageRepo.findByName(name);
        //return dto.map(stateImageMapper::map).orElse(null);
        return dto.map(stateImageEntityMapper::map).orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        return stateImageRepo.findAll().stream()
                //.map(stateImageMapper::map)
                .map(stateImageEntityMapper::map)
                .collect(Collectors.toList());
    }

    public void saveStateImages(StateImage... stateImages) {
        saveStateImages(List.of(stateImages));
    }

    public void saveStateImages(List<StateImage> stateImages) {
        //stateImages.forEach(stateImage -> stateImageRepo.save(stateImageMapper.map(stateImage)));
        stateImages.forEach(System.out::println);
        stateImages.forEach(stateImage -> stateImageRepo.save(stateImageEntityMapper.map(stateImage)));
    }

    public StateImageEntity getStateImageEntity(String name) {
        Optional<StateImageEntity> entity = stateImageRepo.findByName(name);
        return entity.orElse(null);
    }

    public List<StateImageEntity> getAllStateImageEntities() {
        return stateImageRepo.findAll();
    }

    public void updateStateImage(Long id, String newName) {
        StateImageEntity entity = stateImageRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("StateImage not found"));
        entity.setName(newName);
        stateImageRepo.save(entity);
    }

    public boolean removeStateImage(String name) {
        Optional<StateImageEntity> entity = stateImageRepo.findByName(name);
        if (entity.isEmpty()) {
            System.out.println("StateImage does not exist.");
            return false;
        }
        stateImageRepo.delete(entity.get());
        return true;
    }

    @Transactional
    public void addInvolvedTransition(Long stateImageId, Long transitionId) {
        StateImageEntity stateImage = stateImageRepo.findById(stateImageId)
                .orElseThrow(() -> new EntityNotFoundException("StateImage not found"));
        stateImage.getInvolvedTransitionIds().add(transitionId);
        stateImageRepo.save(stateImage);
    }

    @Transactional
    public StateImageEntity createStateImage(StateImageRequest request) {
        logger.info("Creating new StateImage: {}", request);

        // Use the mapper to convert the request to an entity
        StateImageEntity stateImage = stateImageResponseMapper.fromRequest(request);

        // Handle patterns separately as they need to be retrieved
        List<PatternEntity> patterns = patternService.getPatternEntities(
                request.getPatterns().stream()
                    .map(PatternRequest::getId)
                    .collect(Collectors.toList()));
        stateImage.setPatterns(patterns);

        // Ensure the ownerStateId is set correctly
        if (stateImage.getOwnerStateId() == null || stateImage.getOwnerStateId() == -1L) {
            logger.warn("ownerStateId is not set or is invalid. Setting to null.");
            stateImage.setOwnerStateId(null);
        }

        StateImageEntity savedStateImage = stateImageRepo.save(stateImage);
        logger.info("Created StateImage with ID: {}", savedStateImage.getId());
        return savedStateImage;
    }

    @Transactional
    public StateImageEntity updateStateImage(StateImageEntity stateImage) {
        logger.info("Updating StateImage: {}", stateImage);
        if (stateImage.getOwnerStateId() == null || stateImage.getOwnerStateId() == -1L) {
            logger.error("Invalid ownerStateId for StateImage: {}", stateImage.getId());
            throw new IllegalArgumentException("Invalid ownerStateId for StateImage");
        }
        StateImageEntity updatedStateImage = stateImageRepo.save(stateImage);
        logger.info("Updated StateImage: {}", updatedStateImage);
        return updatedStateImage;
    }

    public Set<StateImageEntity> createStateImages(Set<StateImageRequest> requests) {
        return requests.stream()
                .map(this::createStateImage)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteStateImagesByStateId(Long stateId) {
        List<StateImageEntity> stateImages = stateImageRepo.findByOwnerStateId(stateId);
        stateImageRepo.deleteAll(stateImages);
    }

}
