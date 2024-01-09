package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class ActionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    @Test
    void findWords() {
        TestData testData = new TestData();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withScenes(testData.getFloranext1())
                .build();
        ActionOptions findWordsOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL_WORDS)
                .build();
        Matches matches = action.perform(findWordsOptions, objColl);
        matches.getMatchList().forEach(System.out::println);
        matches.getMatchList().forEach(match -> System.out.println(match.getText()));
        assertFalse(matches.isEmpty());
    }
}