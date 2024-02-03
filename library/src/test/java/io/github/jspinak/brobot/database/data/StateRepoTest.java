package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StateRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    private StateRepo stateRepo;

    @Test
    void findAllAsList() {
        Iterable<State> stateList = stateRepo.findAll();
        List<State> states = new ArrayList<>();
        stateList.forEach(states::add);
        System.out.println("# of states = " + states.size());
        states.forEach(System.out::println);
        assertTrue(states.isEmpty());
    }

    @Test
    void findByName() {
    }

    @Test
    void findByNameContainingIgnoreCase() {
    }
}