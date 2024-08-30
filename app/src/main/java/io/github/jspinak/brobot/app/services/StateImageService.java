package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.repositories.StateImageRepo;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StateImageService {

    private final StateImageRepo stateImageRepo;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateImageResponseMapper stateImageResponseMapper;

    public StateImageService(StateImageRepo stateImageRepo,
                             StateImageEntityMapper stateImageEntityMapper,
                             StateImageResponseMapper stateImageResponseMapper) {
        this.stateImageRepo = stateImageRepo;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateImageResponseMapper = stateImageResponseMapper;
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
}
