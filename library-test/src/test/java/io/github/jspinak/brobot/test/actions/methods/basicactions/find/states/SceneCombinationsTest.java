package io.github.jspinak.brobot.test.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.methods.basicactions.find.states.SceneCombinations;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class SceneCombinationsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    SceneCombinations sceneCombinations;

    @Test
    void getAllWithScene() {

    }

    @Test
    void getImagesInSceneCombinations() {
    }

    @Test
    void getAllWithSceneAndImage() {
    }

    @Test
    void createAndAddStatesForSceneToStateRepo() {
    }
}