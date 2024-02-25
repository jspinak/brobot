package io.github.jspinak.brobot.database.services;

import io.github.jspinak.brobot.database.data.StateImageRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImageResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StateImageService {

    private final StateImageRepo stateImageRepo;

    public StateImageService(StateImageRepo stateImageRepo) {
        this.stateImageRepo = stateImageRepo;
    }

    public StateImage getStateImage(String name) {
        return stateImageRepo.findByName(name).orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        List<StateImage> stateImages = new ArrayList<>();
        stateImageRepo.findAll().forEach(stateImages::add);
        return stateImages;
    }

    public void saveStateImages(StateImage... stateImages) {
        List.of(stateImages).forEach(stateImageRepo::save);
    }

    public void saveStateImages(List<StateImage> stateImages) {
        stateImages.forEach(stateImageRepo::save);
    }

    public void removeStateImage(StateImage stateImage) {
        if (stateImageRepo.findByName(stateImage.getName()).isEmpty()) {
            System.out.println("StateImage does not exist.");
            return;
        }
        stateImageRepo.delete(stateImage);
    }

    public void removeStateImage(StateImageResponse stateImageResponse) {
        Optional<StateImage> stateImage = stateImageRepo.findByName(stateImageResponse.getName());
        if (stateImage.isEmpty()) {
            System.out.println("StateImage does not exist.");
            return;
        }
        stateImageRepo.delete(stateImage.get());
    }

    public List<StateImage> getAllInProject(Long projectId) {
        return stateImageRepo.findByProjectId(projectId);
    }
}
