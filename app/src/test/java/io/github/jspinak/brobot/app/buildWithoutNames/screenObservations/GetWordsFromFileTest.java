package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
//@ComponentScan({"com.brobot.app", "io.github.jspinak.brobot"})
//@ComponentScan("com.brobot.app.database.mappers")
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