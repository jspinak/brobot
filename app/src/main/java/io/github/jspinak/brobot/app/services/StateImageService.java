package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.repositories.StateImageRepo;
import io.github.jspinak.brobot.app.log.StateImageDTO;
import io.github.jspinak.brobot.app.log.StateImageDTOMapper;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.requests.StateImageRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StateImageService {
    private static final Logger logger = LoggerFactory.getLogger(StateImageService.class);

    private final StateImageRepo stateImageRepo;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final PatternService patternService;
    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateImageDTOMapper stateImageDTOMapper;
    private final StateImageSenderService stateImageSenderService;

    public StateImageService(StateImageRepo stateImageRepo,
                             StateImageEntityMapper stateImageEntityMapper,
                             PatternService patternService,
                             StateImageResponseMapper stateImageResponseMapper,
                             StateImageDTOMapper stateImageDTOMapper,
                             StateImageSenderService stateImageSenderService) {
        this.stateImageRepo = stateImageRepo;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.patternService = patternService;
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateImageDTOMapper = stateImageDTOMapper;
        this.stateImageSenderService = stateImageSenderService;
    }

    @Transactional(readOnly = true)
    public List<StateImageEntity> getAllStateImagesForProject(Long projectId) {
        logger.info("Fetching state images for project {}", projectId);
        try {
            List<StateImageEntity> stateImages = stateImageRepo.findByProjectId(projectId);
            logger.info("Found {} state images for project {} (actual ID: {})",
                    stateImages.size(), projectId, projectId);
            return stateImages;
        } catch (Exception e) {
            logger.error("Error fetching state images for project {}",
                    projectId, e);
            throw new RuntimeException("Failed to fetch state images for project " + projectId, e);
        }
    }

    /**
     * Translates the internal database ID to the external project ID.
     * Use this when sending data back to the client.
     */
    private Long translateToExternalProjectId(Long internalProjectId) {
        if (internalProjectId == null) return null;
        // If internal ID is 0, translate to 1
        return internalProjectId + 1;
    }

    @Transactional(readOnly = true)
    public List<StateImageDTO> getAllStateImageDTOsForProject(Long projectId) {
        logger.info("Fetching all state image DTOs for project {}", projectId);
        try {
            List<StateImageEntity> stateImages = getAllStateImagesForProject(projectId);
            stateImages.forEach(dto -> logger.debug("State image name: {}, stateOwnerName: {}, id: {}, projectId: {}",
                    dto.getName(), dto.getOwnerStateName(), dto.getId(), dto.getProjectId()));
            List<StateImageDTO> dtos = stateImageDTOMapper.toDTOList(stateImages);
            logger.info("Converted {} state images to DTOs for project {}", dtos.size(), projectId);
            dtos.forEach(dto -> logger.debug("State image DTO name: {}, stateOwnerName: {}, id: {}, projectId: {}",
                    dto.getName(), dto.getStateOwnerName(), dto.getId(), dto.getProjectId()));
            return dtos;
        } catch (Exception e) {
            logger.error("Error converting state images to DTOs for project {}", projectId, e);
            throw new RuntimeException("Failed to convert state images to DTOs for project " + projectId, e);
        }
    }

    private void sendStateImageUpdate(StateImageEntity stateImage) {
        try {
            StateImageDTO dto = stateImageDTOMapper.toDTO(stateImage);
            stateImageSenderService.sendStateImage(dto);
            logger.debug("Sent state image update: {}", dto.getName());
        } catch (Exception e) {
            logger.error("Failed to send state image update for ID: {}", stateImage.getId(), e);
        }
    }

    private void sendStateImagesUpdate(Collection<StateImageEntity> stateImages) {
        if (stateImages.isEmpty()) return;

        try {
            List<StateImageDTO> dtos = stateImageDTOMapper.toDTOList(new ArrayList<>(stateImages));
            stateImageSenderService.sendStateImages(dtos);
            logger.debug("Sent {} state images", dtos.size());
        } catch (Exception e) {
            logger.error("Failed to send bulk state image update", e);
        }
    }

    @Transactional
    public StateImageEntity createStateImage(StateImageRequest request) {
        logger.info("Creating new StateImage: {}", request);

        StateImageEntity stateImage = stateImageResponseMapper.fromRequest(request);
        List<PatternEntity> patterns = patternService.getPatternEntities(
                request.getPatterns().stream()
                        .map(PatternRequest::getId)
                        .collect(Collectors.toList()));
        stateImage.setPatterns(patterns);

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
        Set<StateImageEntity> createdImages = requests.stream()
                .map(this::createStateImage)
                .collect(Collectors.toSet());
        return createdImages;
    }

    @Transactional
    public void deleteStateImagesByStateId(Long stateId) {
        List<StateImageEntity> stateImages = stateImageRepo.findByOwnerStateId(stateId);
        stateImageRepo.deleteAll(stateImages);
    }

    public void updateImageFoundStatus(Long stateImageId, boolean found) {
        Optional<StateImageEntity> stateImageOpt = stateImageRepo.findById(stateImageId);
        if (stateImageOpt.isPresent()) {
            StateImageEntity stateImage = stateImageOpt.get();
        }
    }

    public StateImage mapWithImage(StateImageEntity stateImageEntity) {
        StateImage stateImage = stateImageEntityMapper.map(stateImageEntity);
        List<Pattern> patterns = new ArrayList<>();
        stateImageEntity.getPatterns()
                .forEach(patternEntity -> patterns.add(patternService.mapWithImage(patternEntity)));
        stateImage.setPatterns(patterns);
        return stateImage;
    }

    public List<StateImage> mapWithImages(List<StateImageEntity> stateImageEntities) {
        List<StateImage> stateImages = new ArrayList<>();
        stateImageEntities.forEach(stateImageEntity -> stateImages.add(mapWithImage(stateImageEntity)));
        return stateImages;
    }

    public StateImageEntity getStateImage(Long id) {
        return stateImageRepo.findById(id).orElse(null);
    }

    public StateImage getStateImage(String name) {
        return stateImageRepo.findByName(name)
                .map(stateImageEntityMapper::map)
                .orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        return stateImageRepo.findAll().stream()
                .map(stateImageEntityMapper::map)
                .collect(Collectors.toList());
    }

    public void saveStateImages(StateImage... stateImages) {
        saveStateImages(List.of(stateImages));
    }

    public void saveStateImages(List<StateImage> stateImages) {
        stateImages.forEach(stateImage -> {
            StateImageEntity entity = stateImageEntityMapper.map(stateImage);
            StateImageEntity savedEntity = stateImageRepo.save(entity);
        });
    }

    public StateImageEntity getStateImageEntity(String name) {
        return stateImageRepo.findByName(name).orElse(null);
    }

    public List<StateImageEntity> getAllStateImageEntities() {
        return stateImageRepo.findAll();
    }

    public void updateStateImage(Long id, String newName) {
        StateImageEntity entity = stateImageRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("StateImage not found"));
        entity.setName(newName);
        StateImageEntity updatedEntity = stateImageRepo.save(entity);
    }

    public boolean removeStateImage(String name) {
        Optional<StateImageEntity> entity = stateImageRepo.findByName(name);
        if (entity.isEmpty()) {
            logger.warn("StateImage does not exist: {}", name);
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
        StateImageEntity updatedEntity = stateImageRepo.save(stateImage);
    }

    // This method could be useful for checking sync status
    @Transactional(readOnly = true)
    public Map<String, Object> getStateImageStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<StateImageEntity> images = getAllStateImagesForProject(projectId);
            stats.put("totalCount", images.size());
            stats.put("byState", images.stream()
                    .collect(Collectors.groupingBy(
                            StateImageEntity::getOwnerStateName,
                            Collectors.counting()
                    )));
            stats.put("withImages", images.stream()
                    .filter(img -> !img.getPatterns().isEmpty())
                    .count());
            return stats;
        } catch (Exception e) {
            logger.error("Error getting state image stats for project {}", projectId, e);
            throw new RuntimeException("Failed to get state image stats for project " + projectId, e);
        }
    }
}