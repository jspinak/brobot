package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.repositories.StateImageRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StateImageService {

    private final StateImageRepo stateImageRepo;
    //private final StateImageMapper stateImageMapper = StateImageMapper.INSTANCE;

    public StateImageService(StateImageRepo stateImageRepo) {
        this.stateImageRepo = stateImageRepo;
    }

    public StateImage getStateImage(String name) {
        Optional<StateImageEntity> dto = stateImageRepo.findByName(name);
        //return dto.map(stateImageMapper::map).orElse(null);
        return dto.map(StateImageEntityMapper::map).orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        return stateImageRepo.findAll().stream()
                //.map(stateImageMapper::map)
                .map(StateImageEntityMapper::map)
                .collect(Collectors.toList());
    }

    public void saveStateImages(StateImage... stateImages) {
        saveStateImages(List.of(stateImages));
    }

    public void saveStateImages(List<StateImage> stateImages) {
        //stateImages.forEach(stateImage -> stateImageRepo.save(stateImageMapper.map(stateImage)));
        stateImages.forEach(stateImage -> stateImageRepo.save(StateImageEntityMapper.map(stateImage)));
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

    public List<StateImage> getAllInProject(Long projectId) {
        return stateImageRepo.findByProjectId(projectId).stream()
                //.map(stateImageMapper::map)
                .map(StateImageEntityMapper::map)
                .collect(Collectors.toList());
    }
}
