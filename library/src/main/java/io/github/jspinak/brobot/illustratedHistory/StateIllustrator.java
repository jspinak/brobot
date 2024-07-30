package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawRect;
import io.github.jspinak.brobot.imageUtils.OpenCVColor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class StateIllustrator {

    private final DrawRect drawRect;

    public StateIllustrator(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    public StateIllustration drawState(State state, Image screenshot) {
        StateIllustration stateIllustration = new StateIllustration(screenshot);
        return drawState(state, stateIllustration);
    }

    public StateIllustration drawState(State state, StateIllustration stateIllustration) {
        Mat illustration = stateIllustration.getScreenshotAsMat().clone();
        drawRect.drawRectAroundRegion(illustration, state.getBoundaries(), OpenCVColor.BLUE.getScalar());
        Set<StateImage> sios = state.getStateImages();
        for (StateImage stateImage : sios) {
            boolean isTransition = false; //need methods to determine which images are involved with transitions
            List<MatchSnapshot> snapshots = stateImage.getAllMatchSnapshots();
            if (!snapshots.isEmpty()) {
                if (isTransition) drawRect.drawRectAroundMatch(
                        illustration, snapshots.get(0).getMatchList().get(0), OpenCVColor.GREEN.getScalar());
                else drawRect.drawRectAroundMatch(
                        illustration, snapshots.get(0).getMatchList().get(0), OpenCVColor.RED.getScalar());
            }
        }
        stateIllustration.setIllustratedScreenshot(illustration);
        return stateIllustration;
    }

}
