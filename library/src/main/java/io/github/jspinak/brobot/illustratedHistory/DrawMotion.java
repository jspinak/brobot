package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Component
public class DrawMotion {

    private IllustrationFilename illustrationFilename;

    public DrawMotion(IllustrationFilename illustrationFilename) {
        this.illustrationFilename = illustrationFilename;
    }

    public void draw(Matches matches) {
        String outputPath = illustrationFilename.getFilename(ActionOptions.Action.FIND, "motion");
        imwrite(outputPath, matches.getMask());
    }
}
