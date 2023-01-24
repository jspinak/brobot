package io.github.jspinak.brobot.desktopBackend.api;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.desktopBackend.data.StateImageObjectRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateImageObjectService {

    private StateImageObjectRepo stateImageObjectRepo;

    public StateImageObjectService(StateImageObjectRepo stateImageObjectRepo) {
        this.stateImageObjectRepo = stateImageObjectRepo;
    }

    public StateImageObject getStateImageObject(String name) {
        return stateImageObjectRepo.findByName(name).orElse(null);
    }

    public List<StateImageObject> getAllStateImageObjects() {
        return stateImageObjectRepo.findAll();
    }

    public void saveStateImageObject(StateImageObject stateImageObject) {
        stateImageObjectRepo.save(stateImageObject);
    }
}
