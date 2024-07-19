package io.github.jspinak.brobot.app.services.entityServices;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StateEntityService {

    private final StateRepo stateRepo;

    public StateEntityService(StateRepo stateRepo) {
        this.stateRepo = stateRepo;
    }

    public List<StateEntity> getAllStates() {
        return stateRepo.findAll();
        //return stateRepo.findByProjectId()
    }

    public Optional<StateEntity> getState(String name) {
        return stateRepo.findByName(name);
    }
}
