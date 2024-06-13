package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class StateRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StateRepo stateRepo;

    @Autowired
    AllStatesInProjectService allStatesInProjectService;

    @Test
    void findAllAsList() {
        allStatesInProjectService.getAllStates().forEach(state -> stateRepo.save(StateEntityMapper.map(state)));
        Iterable<StateEntity> stateList = stateRepo.findAll();
        List<StateEntity> states = new ArrayList<>();
        stateList.forEach(states::add);
        System.out.println("# of states = " + states.size());
        states.forEach(System.out::println);
        assertEquals(1, states.size()); // the unknown state is always in the state repo. the null state is not.
    }

    @Test
    void findByName() {
    }

    @Test
    void findByNameContainingIgnoreCase() {
    }
}