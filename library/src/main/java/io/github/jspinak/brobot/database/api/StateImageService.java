package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.database.data.StateImageRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
