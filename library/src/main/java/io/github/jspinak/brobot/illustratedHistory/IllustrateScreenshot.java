package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageOpenCV;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.*;

@Component
@Getter
public class IllustrateScreenshot {

    private ImageUtils imageUtils;
    private GetImageOpenCV getImageOpenCV;
    private Draw draw;
    private IllustrationManager illustrationManager;

    private List<ObjectCollection> lastCollections = new ArrayList<>();
    private ActionOptions.Action lastAction = ActionOptions.Action.TYPE;
    private ActionOptions.Find lastFind = ActionOptions.Find.UNIVERSAL;

    private Map<ActionOptions.Action, Boolean> actionPermissions = new HashMap<>();

    public IllustrateScreenshot(ImageUtils imageUtils, GetImageOpenCV getImageOpenCV, Draw draw,
                                IllustrationManager illustrationManager) {
        this.imageUtils = imageUtils;
        this.getImageOpenCV = getImageOpenCV;
        this.draw = draw;
        this.illustrationManager = illustrationManager;
    }

    private void setActionPermissions() {
        actionPermissions.put(FIND, BrobotSettings.drawFind);
        actionPermissions.put(CLICK, BrobotSettings.drawClick);
        actionPermissions.put(DRAG, BrobotSettings.drawDrag);
        actionPermissions.put(MOVE, BrobotSettings.drawMove);
        actionPermissions.put(HIGHLIGHT, BrobotSettings.drawHighlight);
        actionPermissions.put(CLASSIFY, BrobotSettings.drawClassify);
        actionPermissions.put(DEFINE, BrobotSettings.drawDefine);
    }

    /**
     * We might not want to illustrate an action every time it repeats, particularly
     * for Find operations. If the action is a Find and the previous action was also a Find,
     * and the Collections are the same, it is a repeated action. Repeated actions are not
     * illustrated if BrobotSettings.drawRepeatedActions is set to false (the default setting).
     *
     * @param actionOptions holds the action configuration.
     * @param objectCollections are the objects acted on.
     * @return true if illustration is allowed.
     */
    public boolean okToIllustrate(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        setActionPermissions();
        if (!BrobotSettings.saveHistory && actionOptions.getIllustrate() != ActionOptions.Illustrate.YES) return false;
        if (actionOptions.getIllustrate() == ActionOptions.Illustrate.NO) return false;
        ActionOptions.Action action = actionOptions.getAction();
        if (!actionPermissions.containsKey(action)) {
            Report.println(actionOptions.getAction() + " not available to illustrate in BrobotSettings.");
            return false;
        }
        if (!actionPermissions.get(action)) {
            Report.println(actionOptions.getAction() + " not set to illustrate in BrobotSettings.");
            return false;
        }
        if (BrobotSettings.drawRepeatedActions) return true;
        // otherwise, if the action is a repeat (same Action, same ObjectCollections), false
        return lastFind != actionOptions.getFind() ||
                lastAction != action ||
                !sameCollections(Arrays.asList(objectCollections));
    }

    private boolean sameCollections(List<ObjectCollection> objectCollections) {
        if (objectCollections.size() != lastCollections.size()) return false;
        for (int i=0; i<objectCollections.size(); i++) {
            if (!objectCollections.get(0).equals(lastCollections.get(i))) return false;
        }
        return true;
    }

    public boolean illustrateWhenAllowed(Matches matches, List<Region> searchRegions, ActionOptions actionOptions,
                                         ObjectCollection... objectCollections) {
        if (!okToIllustrate(actionOptions, objectCollections)) return false;
        lastAction = actionOptions.getAction();
        if (lastAction == FIND) lastFind = actionOptions.getFind();
        lastCollections = Arrays.asList(objectCollections);
        illustrationManager.draw(matches, searchRegions, actionOptions);
        return true;
    }

}
