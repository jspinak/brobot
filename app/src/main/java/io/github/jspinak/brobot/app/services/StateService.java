package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the State repository.
 * Saves new states and retrieves states given the state name. StateEnums are typically used as state names
 *   for state structures built with named image files, or manually built state structures. Strings are used
 *   for state structures created at run-time or dynamically evolving state structures.
 **/
@Service
public class StateService {

    private final StateRepo stateRepo;
    private final StateEntityMapper stateEntityMapper;
    //private StateMapper stateMapper = StateMapper.INSTANCE;

    public StateService(StateRepo stateRepo, StateEntityMapper stateEntityMapper) {
        this.stateRepo = stateRepo;
        this.stateEntityMapper = stateEntityMapper;
    }

    @Transactional(readOnly = true)
    public Optional<State> getState(String name) {
        Optional<StateEntity> state = stateRepo.findByName(name);
        //return state.map(stateMapper::map);
        return state.map(stateEntityMapper::map);
    }

    @Transactional(readOnly = true)
    public Optional<StateEntity> getStateEntity(String name) {
        return stateRepo.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<StateEntity> getAllStateEntities() {
        return stateRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<State> getAllStates() {
        List<State> stateList = new ArrayList<>();
        for (StateEntity stateEntity : stateRepo.findAll()) {
            //stateList.add(stateMapper.map(stateEntity));
            stateList.add(stateEntityMapper.map(stateEntity));
        }
        return stateList;
    }

    @Transactional(readOnly = true)
    public Set<String> getAllStateNames() {
        return getAllStates().stream()
                .map(State::getName)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<State> findSetByName(String... stateNames) {
        Set<State> states = new HashSet<>();
        Stream.of(stateNames).forEach(name -> getState(name).ifPresent(states::add));
        return states;
    }

    @Transactional(readOnly = true)
    public Set<State> findSetByName(Set<String> stateNames) {
        return findSetByName(stateNames.toArray(new String[0]));
    }

    @Transactional(readOnly = true)
    public State[] findArrayByName(Set<String> stateNames) {
        return findArrayByName(stateNames.toArray(new String[0]));
    }

    @Transactional(readOnly = true)
    public State[] findArrayByName(String... stateNames) {
        List<State> states = new ArrayList<>();
        Stream.of(stateNames).forEach(name -> getState(name).ifPresent(states::add));
        return states.toArray(new State[0]);
    }

    /**
     * Adds a State to the repository.
     * Initial image processing can't take place here because, in the client app, the beans
     * are loaded before the bundle path can be set. Instead, initialization takes place in the
     * class "Init".
     *
     * @param state The State to add.
     */
    @Transactional
    public void save(State state) {
        if (state == null) return;
        stateRepo.save(stateEntityMapper.map(state));
    }

    @Transactional
    public void resetTimesVisited() {getAllStates().forEach(state -> state.setTimesVisited(0));
    }

    @Transactional
    public void deleteAllStates() {
        stateRepo.deleteAll();
    }

    @Transactional
    public boolean removeState(String stateName) {
        Optional<StateEntity> stateDTO = stateRepo.findByName(stateName);
        if (stateDTO.isEmpty()) {
            System.out.println("State does not exist.");
            return false;
        }
        stateRepo.delete(stateDTO.get());
        return true;
    }

    @Transactional
    public boolean removeState(State state) {
        if (state == null) return false;
        return removeState(state.getName());
    }

    @Transactional(readOnly = true)
    public List<State> getAllInProject(Long projectId) {
        return stateRepo.findByProjectId(projectId).stream()
                //.map(stateMapper::map)
                .map(stateEntityMapper::map)
                .collect(Collectors.toList());
    }
}
