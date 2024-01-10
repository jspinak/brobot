package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetMatTextPattern {

    private final MockOrLive mockOrLive;

    public SetMatTextPattern(MockOrLive mockOrLive) {
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
        int sceneIndex = matches.getActionOptions().getSceneToUseForCaptureAfterFusingMatches();
        List<Scene> scenes = matches.getSceneAnalysisCollection().getScenes();
        if (sceneIndex < 0 || sceneIndex >= scenes.size()) return;
        Scene scene = matches.getSceneAnalysisCollection().getScenes().get(sceneIndex);
        for (Match m : matches.getMatchList()) {
            m.setScene(scene);
            m.setMatWithScene();
            setPattern(m, matches.getActionOptions());
        }
    }

    /**
     * Set the Pattern when
     *   - it doesn't exist
     *   - the operation is a text action
     * Other find operations are passed a Pattern as a parameter and we don't want to overwrite this Pattern.
     * @param match the match to update
     * @param actionOptions tells us if we're allowed to set the Pattern
     */
    public void setPattern(Match match, ActionOptions actionOptions) {
        if (match.getPattern() == null || isOkToReset(actionOptions)) {
            Pattern pattern = new Pattern.Builder()
                    .setFixedRegion(new Region(match))
                    .setName(match.getName())
                    .setMat(match.getMat())
                    .setFixed(true)
                    .build();
            match.setPattern(pattern);
        }
    }

    /**
     * Text is searched for when specified in the ActionOptions.
     * @param matches the match objects and action options.
     */
    public void setText(Matches matches) {
        for (Match match : matches.getMatchList()) {
            if (match.getText() == null || match.getText().isEmpty() || isOkToReset(matches.getActionOptions()))
                mockOrLive.setText(match);
        }
    }

    private boolean isOkToReset(ActionOptions actionOptions) {
        return actionOptions.getAction() == ActionOptions.Action.GET_TEXT ||
                actionOptions.getFind() == ActionOptions.Find.ALL_WORDS;
                /*
                The Pattern disappears when match objects are fused, but this will be caught by match.getText() == null
                and in cases where a match was not fused and has text, we don't need to search it again.
                 */
                //actionOptions.getFusionMethod() != NONE;
    }

}
