package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class GetWordsFromFileTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetWordsFromFile getWordsFromFile;

    @Test
    void getWordMatchesFromFile() {
        List<Match> matchList = getWordsFromFile.getWordMatchesFromFile(
                new Region(), BrobotSettings.screenshotPath + "floranext1.png");
        assertFalse(matchList.isEmpty());
    }
}