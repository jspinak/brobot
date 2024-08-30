package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import io.github.jspinak.brobot.app.database.repositories.StateTransitionsRepo;
import io.github.jspinak.brobot.app.models.StateAndTransitions;
import io.github.jspinak.brobot.app.models.StateStructure;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StateStructureService {

    private final StateRepo stateRepo;
    private final StateTransitionsRepo stateTransitionsRepo;

    public StateStructureService(StateRepo stateRepo, StateTransitionsRepo stateTransitionsRepo) {
        this.stateRepo = stateRepo;
        this.stateTransitionsRepo = stateTransitionsRepo;
    }

    public StateStructure getCompleteStateStructure() {
        List<StateEntity> states = stateRepo.findAll();
        List<StateTransitionsEntity> stateTransitions = stateTransitionsRepo.findAll();

        Map<Long, StateTransitionsEntity> transitionsMap = stateTransitions.stream()
                .collect(Collectors.toMap(StateTransitionsEntity::getStateId, st -> st));

        List<StateAndTransitions> statesAndTransitions = states.stream()
                .map(state -> new StateAndTransitions(state, transitionsMap.get(state.getName())))
                .collect(Collectors.toList());

        return new StateStructure(statesAndTransitions);
    }
}