package io.github.jspinak.brobot.test.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.GetSceneCombinations;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.PopulateSceneCombinations;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.SceneCombination;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class PopulateSceneCombinationsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    PopulateSceneCombinations populateSceneCombinations;

    @Autowired
    GetSceneCombinations getSceneCombinations;

    @Autowired
    Action action;

    @Test
    void populateSceneCombinationsWithImages() {
        List<ObjectCollection> objectCollections = new FindStatesData().getStateObjectCollections(action);
        List<SceneCombination> sceneCombinationList = getSceneCombinations.getAllSceneCombinations(objectCollections);
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.STATES)
                .setMinArea(25)
                .build();
        populateSceneCombinations.populateSceneCombinationsWithImages(
                sceneCombinationList, objectCollections, actionOptions);
        //sceneCombinationList.forEach(System.out::println);
        int images0 = objectCollections.get(0).getStateImages().size();
        int images1 = objectCollections.get(1).getStateImages().size();
        SceneCombination sceneCombinationWithDifferentScenes =
                getSceneCombinations.getSceneCombinationWithDifferentScenes(sceneCombinationList);
        assertNotNull(sceneCombinationWithDifferentScenes);
        int imagesInComb01 = sceneCombinationWithDifferentScenes.getImages().size();
        System.out.println("Obj.Coll.0: " + images0);
        System.out.println("Obj.Coll.1: "+ images1);
        System.out.println("State.0-1: "+ imagesInComb01);
        // the scenes are almost the same and a majority of the images in both ObjectCollections should be in the SceneCombination
        assertTrue(Math.max(images0,images1) < imagesInComb01);
        // it shouldn't have more than all images in both ObjectCollections
        assertTrue(images0 + images1 >= imagesInComb01);
    }

    @Test
    void imageSizesAreOk() {
        int minArea = 50;
        List<ObjectCollection> objectCollections = new FindStatesData().getStateObjectCollections(action);
        List<SceneCombination> sceneCombinationList = getSceneCombinations.getAllSceneCombinations(objectCollections);
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.STATES)
                .setMinArea(minArea)
                .build();
        populateSceneCombinations.populateSceneCombinationsWithImages(
                sceneCombinationList, objectCollections, actionOptions);
        for (SceneCombination sceneCombination : sceneCombinationList) {
            sceneCombination.getImages().forEach(img -> {
                int size = img.getPatterns().get(0).size();
                System.out.print(size + ",");
                assertTrue(minArea <= size);
            });
        }
    }
}