package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.TestConfig;
import io.github.jspinak.brobot.app.database.databaseMappers.PatternEntityMapper;
import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.app.database.entities.*;
import io.github.jspinak.brobot.app.services.*;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.manageStates.InitialStates;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.testingAUTs.StateTraversalService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")  // Loads application-test.properties
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestConfig.class)
class BuildModelTest {

    @Autowired
    private Init init;

    @Autowired
    private StateService stateService;

    @Autowired
    private StateTransitionsService stateTransitionsService;

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private BuildModel buildModel;

    @Autowired
    private StateTraversalService stateTraversalService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private InitialStates initialStates;

    @Autowired
    private PatternEntityMapper patternEntityMapper;

    @Autowired
    private ImageService imageService;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    public void testPathFinding() {
        BrobotSettings.mock = true;
        System.out.println("projects:");
        List<ProjectEntity> projectEntities = projectService.getAllProjects();
        projectEntities.forEach(p -> System.out.println(p.getName()+" "+p.getId()));

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("buildModelTest");
        ProjectEntity savedProject = projectService.save(projectEntity);

        // Check if the projectEntity was saved and assigned an ID
        assertNotNull(savedProject.getId());

        StateEntity menu = new StateEntity();
        menu.setName("menu");
        menu.setProject(savedProject);
        stateService.save(menu);

        StateEntity home = new StateEntity();
        home.setName("home");
        home.setProject(savedProject);
        stateService.save(home);

        List<StateEntity> allStateEntities = stateService.getAllStateEntities();
        assertEquals(2, allStateEntities.size());
        List<StateEntity> allStateEntitiesInProject = stateService.getStateEntitiesByProject(savedProject.getId());
        assertEquals(2, allStateEntitiesInProject.size());

        TransitionEntity menuToHome = new TransitionEntity();
        menuToHome.setProject(savedProject);
        menuToHome.setSourceStateId(1L);
        menuToHome.setStatesToEnter(Collections.singleton(2L));
        ActionDefinitionEntity actionDefinitionEntity = new ActionDefinitionEntity();
        actionDefinitionEntity.addStepEntity(makeActionOptions(), makeObjectCollection());
        menuToHome.setActionDefinition(actionDefinitionEntity);
        transitionService.save(menuToHome);

        ActionDefinitionEntity actionDefinitionEntity2 = new ActionDefinitionEntity();
        actionDefinitionEntity2.addStepEntity(makeActionOptions(), makeObjectCollection());
        TransitionEntity finishToMenu = new TransitionEntity();
        finishToMenu.setActionDefinition(actionDefinitionEntity2);

        ActionDefinitionEntity actionDefinitionEntity3 = new ActionDefinitionEntity();
        actionDefinitionEntity3.addStepEntity(makeActionOptions(), makeObjectCollection());
        TransitionEntity finishToHome = new TransitionEntity();
        finishToHome.setActionDefinition(actionDefinitionEntity3);

        // transitions are used to create StateTransitions when transferring the project to the library
        // setting up StateTransitionsEntities like this should not be necessary
        StateTransitionsEntity menuTransitions = new StateTransitionsEntity();
        menuTransitions.setProject(savedProject);
        menuTransitions.setTransitions(Collections.singletonList(menuToHome));
        menuTransitions.setFinishTransition(finishToMenu);
        menuTransitions.setStateId(1L);
        stateTransitionsService.save(menuTransitions);

        StateTransitionsEntity homeTransitions = new StateTransitionsEntity();
        homeTransitions.setProject(savedProject);
        homeTransitions.setFinishTransition(finishToHome);
        homeTransitions.setStateId(2L);
        stateTransitionsService.save(homeTransitions);

        // Verify that the transitions were saved correctly
        List<StateTransitions> allStateTransitions = stateTransitionsService.getAllStateTransitionsForProject(savedProject.getId());
        assertEquals(2, allStateTransitions.size());
        List<TransitionEntity> allTransitions = transitionService.getAllTransitionsForProject(savedProject.getId());
        assertEquals(1, allTransitions.size());

        /* this test should be split up into unit tests
        object collections are not correctly transferred from ActionDefinitionEntity

        BrobotSettings.sendLogs = false;
        Model model = buildModel.build(savedProject.getId());
        initialStates.addStateSet(100, model.getStates().getFirst());

        Set<Long> statesTraversed = stateTraversalService.traverseAllStates(false);
        assertEquals(2, statesTraversed.size());

         */

    }

    private ActionOptionsEntity makeActionOptions() {
        ActionOptionsEntity actionOptionsEntity = new ActionOptionsEntity();
        actionOptionsEntity.setAction(ActionOptions.Action.FIND);
        return actionOptionsEntity;
    }

    private ObjectCollectionEntity makeObjectCollection() {
        ObjectCollectionEntity objectCollectionEntity = new ObjectCollectionEntity();
        StateImageEntity stateImageEntity = new StateImageEntity();
        Pattern pattern = new Pattern.Builder()
                .setImage(new Image("topLeft"))
                //.addMatchSnapshot(new MatchSnapshot(40,40,200,200)) // causes TransientObjectException
                .build();
        PatternEntity patternEntity = patternEntityMapper.map(pattern, imageService);
        stateImageEntity.setPatterns(Collections.singletonList(patternEntity));
        objectCollectionEntity.setStateImages(Collections.singletonList(stateImageEntity));
        return objectCollectionEntity;
    }

}