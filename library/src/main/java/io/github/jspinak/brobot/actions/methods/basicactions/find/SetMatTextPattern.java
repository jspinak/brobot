package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
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
        //setText(matches); // this method always returns "" despite doing OCR on a BufferedImage
    }

    public void setMat(Matches matches) {
        int sceneIndex = matches.getActionOptions().getSceneToUseForCaptureAfterFusingMatches();
        List<Image> scenes = matches.getSceneAnalysisCollection().getScenes();
        if (sceneIndex < 0 || sceneIndex >= scenes.size()) return;
        Image scene = matches.getSceneAnalysisCollection().getScenes().get(sceneIndex);
        for (Match m : matches.getMatchList()) {
            m.setScene(scene);
            m.setImageWithScene();
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
        return actionOptions.getAction() == ActionOptions.Action.FIND ||
                actionOptions.getFind() == ActionOptions.Find.ALL_WORDS;
                /*
                The Pattern disappears when match objects are fused, but this will be caught by match.getText() == null
                and in cases where a match was not fused and has text, we don't need to search it again.
                 */
                //actionOptions.getFusionMethod() != NONE;
    }

}
