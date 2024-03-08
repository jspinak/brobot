package com.brobot.app.services;

import com.brobot.app.database.entities.StateImageEntity;
import com.brobot.app.database.mappers.StateImageMapper;
import com.brobot.app.database.repositories.StateImageRepo;
import com.brobot.app.responses.StateImageResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StateImageService {

    private final StateImageRepo stateImageRepo;
    private final StateImageMapper stateImageMapper;

    public StateImageService(StateImageRepo stateImageRepo,
                             StateImageMapper stateImageMapper) {
        this.stateImageRepo = stateImageRepo;
        this.stateImageMapper = stateImageMapper;
    }

    public StateImage getStateImage(String name) {
        Optional<StateImageEntity> dto = stateImageRepo.findByName(name);
        return dto.map(stateImageMapper.INSTANCE::mapFromEntity).orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        return stateImageRepo.findAll().stream()
                .map(stateImageMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }

    public void saveStateImages(StateImage... stateImages) {
        saveStateImages(List.of(stateImages));
    }

    public void saveStateImages(List<StateImage> stateImages) {
        stateImages.forEach(stateImage -> stateImageRepo.save(stateImageMapper.INSTANCE.mapToEntity(stateImage)));
    }

    public boolean removeStateImage(String name) {
        Optional<StateImageEntity> dto = stateImageRepo.findByName(name);
        if (dto.isEmpty()) {
            System.out.println("StateImage does not exist.");
            return false;
        }
        stateImageRepo.delete(dto.get());
        return true;
    }

    public boolean removeStateImage(StateImage stateImage) {
        return removeStateImage(stateImage.getName());
    }

    public boolean removeStateImage(StateImageResponse stateImageResponse) {
        return removeStateImage(stateImageResponse.getName());
    }

    public List<StateImage> getAllInProject(Long projectId) {
        return stateImageRepo.findByProjectId(projectId).stream()
                .map(stateImageMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }
}
