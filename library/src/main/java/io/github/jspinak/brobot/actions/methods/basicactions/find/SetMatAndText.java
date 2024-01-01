package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.MatchFusionMethod.NONE;

@Component
public class SetMatAndText {

    private final MockOrLive mockOrLive;

    public SetMatAndText(MockOrLive mockOrLive) {
        this.mockOrLive = mockOrLive;
    }

    /**
     * This method should run after all adjustments are made to the match objects.
     * There are adjustments specified by the ActionOptions variable, like shifting the match position,
     * that affect the underlying Mat and text.
     * @param matches the matches to use and the action options.
     */
    public void set(Matches matches) {
        setMat(matches);
        setText(matches);
    }

    public void setMat(Matches matches) {
        if (matches.getActionOptions().getFusionMethod() == NONE) return;
        int sceneIndex = matches.getActionOptions().getSceneToUseForCaptureAfterFusingMatches();
        List<Scene> scenes = matches.getSceneAnalysisCollection().getScenes();
        if (sceneIndex < 0 || sceneIndex >= scenes.size()) return;
        Scene scene = matches.getSceneAnalysisCollection().getScenes().get(sceneIndex);
        for (Match m : matches.getMatchList()) {
            m.setScene(scene);
            m.setMatWithScene();
        }
    }

    /**
     * Text is searched for when specified in the ActionOptions.
     * @param matches the match objects and action options.
     */
    public void setText(Matches matches) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getAction() != ActionOptions.Action.GET_TEXT ||
                actionOptions.getFind() != ActionOptions.Find.ALL_WORDS) return;
        matches.getMatchList().forEach(mockOrLive::setText);
    }

}
