package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class StateRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StateRepo stateRepo;

    @Autowired
    AllStatesInProjectService allStatesInProjectService;

    @Autowired
    StateEntityMapper stateEntityMapper;

    @Autowired
    SceneService sceneService;

    @Autowired
    PatternService patternService;

    @Autowired
    ProjectRepository projectRepo;

    @Test
    void findAllAsList() {
        ProjectEntity project = new ProjectEntity();
        project.setName("TestProject");
        project = projectRepo.save(project);

        ProjectEntity finalProject = project;
        allStatesInProjectService.getAllStates().forEach(state -> {
            StateEntity stateEntity = stateEntityMapper.map(state, sceneService, patternService);
            stateEntity.setProject(finalProject);
            stateRepo.save(stateEntity);
        });

        Iterable<StateEntity> stateList = stateRepo.findAll();
        List<StateEntity> states = new ArrayList<>();
        stateList.forEach(states::add);
        System.out.println("# of states = " + states.size());
        states.forEach(System.out::println);
        assertEquals(1, states.size());
    }

    @Test
    void findByName() {
    }

    @Test
    void findByNameContainingIgnoreCase() {
    }
}