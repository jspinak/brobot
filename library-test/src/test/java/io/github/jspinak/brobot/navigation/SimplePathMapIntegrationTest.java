package io.github.jspinak.brobot.navigation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.path.Path;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;

/**
 * Simplified integration test for path map setup and path finding. Uses actual API methods
 * available in the codebase.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {"brobot.logging.verbosity=QUIET", "brobot.mock=true"})
@DisplayName("Simple Path Map Integration Tests")
class SimplePathMapIntegrationTest {

    @Autowired private StateService stateService;

    @Autowired private StateTransitionStore transitionStore;

    @Autowired private StateTransitionsJointTable jointTable;

    @Autowired private PathFinder pathFinder;

    @BeforeEach
    void setup() {
        // Clear repositories
        stateService.deleteAllStates();
        transitionStore.emptyRepos();
        jointTable.emptyRepos();
    }

    @Test
    @DisplayName("Should build path map and find simple path")
    void testSimplePathMap() {
        // Create states with path scores
        State start = new State.Builder("Start").setPathCost(0).build();
        State middle = new State.Builder("Middle").setPathCost(10).build();
        State end = new State.Builder("End").setPathCost(0).build();

        // Save states
        stateService.save(start);
        stateService.save(middle);
        stateService.save(end);

        // Create transitions: Start -> Middle -> End
        StateTransitions startTrans =
                new StateTransitions.Builder("Start").addTransition(() -> true, "Middle").build();
        startTrans.setStateId(start.getId());

        StateTransitions middleTrans =
                new StateTransitions.Builder("Middle").addTransition(() -> true, "End").build();
        middleTrans.setStateId(middle.getId());

        StateTransitions endTrans = new StateTransitions.Builder("End").build();
        endTrans.setStateId(end.getId());

        // Add transitions to store
        transitionStore.add(startTrans);
        transitionStore.add(middleTrans);
        transitionStore.add(endTrans);

        // Build joint table
        jointTable.addToJointTable(startTrans);
        jointTable.addToJointTable(middleTrans);
        jointTable.addToJointTable(endTrans);

        // Find path from Start to End
        Set<Long> fromStates = new HashSet<>(Arrays.asList(start.getId()));
        Paths paths = pathFinder.getPathsToState(fromStates, end.getId());

        // Verify path found
        assertFalse(paths.isEmpty());

        // Get the path
        List<Path> allPaths = paths.getPaths();
        assertFalse(allPaths.isEmpty());

        Path path = allPaths.get(0);
        assertEquals(3, path.size());
        assertEquals(start.getId(), path.get(0));
        assertEquals(middle.getId(), path.get(1));
        assertEquals(end.getId(), path.get(2));

        // Check path score includes middle state score
        assertTrue(path.getPathCost() >= 10);
    }

    @Test
    @DisplayName("Should find shortest path among multiple paths")
    void testMultiplePaths() {
        // Create diamond pattern
        State start = new State.Builder("Start").setPathCost(0).build();
        State pathA = new State.Builder("PathA").setPathCost(20).build();
        State pathB = new State.Builder("PathB").setPathCost(5).build();
        State end = new State.Builder("End").setPathCost(0).build();

        stateService.save(start);
        stateService.save(pathA);
        stateService.save(pathB);
        stateService.save(end);

        // Start can go to PathA or PathB
        StateTransitions startTrans =
                new StateTransitions.Builder("Start")
                        .addTransition(() -> true, "PathA", "PathB")
                        .build();
        startTrans.setStateId(start.getId());

        // PathA goes to End
        StateTransitions pathATrans =
                new StateTransitions.Builder("PathA").addTransition(() -> true, "End").build();
        pathATrans.setStateId(pathA.getId());

        // PathB goes to End
        StateTransitions pathBTrans =
                new StateTransitions.Builder("PathB").addTransition(() -> true, "End").build();
        pathBTrans.setStateId(pathB.getId());

        StateTransitions endTrans = new StateTransitions.Builder("End").build();
        endTrans.setStateId(end.getId());

        // Add all transitions
        transitionStore.add(startTrans);
        transitionStore.add(pathATrans);
        transitionStore.add(pathBTrans);
        transitionStore.add(endTrans);

        // Build joint table
        jointTable.addToJointTable(startTrans);
        jointTable.addToJointTable(pathATrans);
        jointTable.addToJointTable(pathBTrans);
        jointTable.addToJointTable(endTrans);

        // Find paths
        Set<Long> fromStates = new HashSet<>(Arrays.asList(start.getId()));
        Paths paths = pathFinder.getPathsToState(fromStates, end.getId());

        // Should find 2 paths
        List<Path> allPaths = paths.getPaths();
        assertEquals(2, allPaths.size());

        // Find path through B (lower score)
        Path pathThroughB =
                allPaths.stream().filter(p -> p.contains(pathB.getId())).findFirst().orElseThrow();

        // Find path through A (higher score)
        Path pathThroughA =
                allPaths.stream().filter(p -> p.contains(pathA.getId())).findFirst().orElseThrow();

        // Path through B should have lower score
        assertTrue(pathThroughB.getPathCost() < pathThroughA.getPathCost());
    }

    @Test
    @DisplayName("Should handle complex graph with cycles")
    void testComplexGraphWithCycles() {
        // Create states
        State hub = new State.Builder("Hub").setPathCost(5).build();
        State nodeA = new State.Builder("NodeA").setPathCost(10).build();
        State nodeB = new State.Builder("NodeB").setPathCost(15).build();
        State target = new State.Builder("Target").setPathCost(0).build();

        stateService.save(hub);
        stateService.save(nodeA);
        stateService.save(nodeB);
        stateService.save(target);

        // Hub -> NodeA, NodeB
        StateTransitions hubTrans =
                new StateTransitions.Builder("Hub")
                        .addTransition(() -> true, "NodeA", "NodeB")
                        .build();
        hubTrans.setStateId(hub.getId());

        // NodeA -> Target, Hub (cycle)
        StateTransitions nodeATrans =
                new StateTransitions.Builder("NodeA")
                        .addTransition(() -> true, "Target", "Hub")
                        .build();
        nodeATrans.setStateId(nodeA.getId());

        // NodeB -> Target
        StateTransitions nodeBTrans =
                new StateTransitions.Builder("NodeB").addTransition(() -> true, "Target").build();
        nodeBTrans.setStateId(nodeB.getId());

        StateTransitions targetTrans = new StateTransitions.Builder("Target").build();
        targetTrans.setStateId(target.getId());

        // Add transitions
        transitionStore.add(hubTrans);
        transitionStore.add(nodeATrans);
        transitionStore.add(nodeBTrans);
        transitionStore.add(targetTrans);

        // Build joint table
        jointTable.addToJointTable(hubTrans);
        jointTable.addToJointTable(nodeATrans);
        jointTable.addToJointTable(nodeBTrans);
        jointTable.addToJointTable(targetTrans);

        // Find paths from Hub to Target
        Set<Long> fromStates = new HashSet<>(Arrays.asList(hub.getId()));
        Paths paths = pathFinder.getPathsToState(fromStates, target.getId());

        // Should find paths despite cycle
        assertFalse(paths.isEmpty());

        // Should have at least 2 paths (through A and through B)
        List<Path> allPaths = paths.getPaths();
        assertTrue(allPaths.size() >= 2);

        // All paths should end at target
        for (Path path : allPaths) {
            assertEquals(target.getId(), path.get(path.size() - 1));
        }
    }

    @Test
    @DisplayName("Should correctly use joint table for path queries")
    void testJointTableQueries() {
        // Create simple chain
        State s1 = new State.Builder("S1").build();
        State s2 = new State.Builder("S2").build();
        State s3 = new State.Builder("S3").build();

        stateService.save(s1);
        stateService.save(s2);
        stateService.save(s3);

        // S1 -> S2 -> S3
        StateTransitions t1 =
                new StateTransitions.Builder("S1").addTransition(() -> true, "S2").build();
        t1.setStateId(s1.getId());

        StateTransitions t2 =
                new StateTransitions.Builder("S2").addTransition(() -> true, "S3").build();
        t2.setStateId(s2.getId());

        StateTransitions t3 = new StateTransitions.Builder("S3").build();
        t3.setStateId(s3.getId());

        transitionStore.add(t1);
        transitionStore.add(t2);
        transitionStore.add(t3);

        // Build joint table
        jointTable.addToJointTable(t1);
        jointTable.addToJointTable(t2);
        jointTable.addToJointTable(t3);

        // Query incoming transitions
        Set<Long> parentsOfS2 = jointTable.getStatesWithTransitionsTo(s2.getId());
        assertEquals(1, parentsOfS2.size());
        assertTrue(parentsOfS2.contains(s1.getId()));

        Set<Long> parentsOfS3 = jointTable.getStatesWithTransitionsTo(s3.getId());
        assertEquals(1, parentsOfS3.size());
        assertTrue(parentsOfS3.contains(s2.getId()));

        // Query outgoing transitions
        Set<Long> childrenOfS1 = jointTable.getStatesWithTransitionsFrom(s1.getId());
        assertEquals(1, childrenOfS1.size());
        assertTrue(childrenOfS1.contains(s2.getId()));

        Set<Long> childrenOfS2 = jointTable.getStatesWithTransitionsFrom(s2.getId());
        assertEquals(1, childrenOfS2.size());
        assertTrue(childrenOfS2.contains(s3.getId()));

        // S3 has no outgoing transitions
        Set<Long> childrenOfS3 = jointTable.getStatesWithTransitionsFrom(s3.getId());
        assertTrue(childrenOfS3.isEmpty());
    }
}
