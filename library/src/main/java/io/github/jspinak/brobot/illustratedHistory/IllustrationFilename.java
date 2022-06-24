package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import org.ghost4j.util.ImageUtil;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

@Component
public class IllustrationFilename {

    private ImageUtils imageUtils;

    public IllustrationFilename(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }

    public String getFilename(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        String name = "";
        if (objectCollections.length > 0) name = objectCollections[0].getFirstObjectName();
        // first, get the number to save as
        String outputPath = imageUtils.getFreePath(BrobotSettings.historyPath + BrobotSettings.historyFilename);
        // then, prepare the descriptive part of the name
        outputPath += "-" + actionOptions.getAction();
        if (actionOptions.getAction() == FIND) outputPath += "-" + actionOptions.getFind();
        outputPath += "-" + name + ".png";
        return outputPath;
    }
}
