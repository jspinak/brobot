package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.database.data.StateImageRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateImageService {

    private final StateImageRepo stateImageRepo;

    public StateImageService(StateImageRepo stateImageRepo) {
        this.stateImageRepo = stateImageRepo;
    }

    public StateImage getStateImage(String name) {
        return stateImageRepo.findByName(name).orElse(null);
    }

    public List<StateImage> getAllStateImages() {
        return stateImageRepo.findAllAsList();
    }

    public void saveStateImage(StateImage stateImage) {
        stateImageRepo.save(stateImage);
    }
}
