package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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

    public StateIllustration drawState(State state, Mat screenshot) {
        StateIllustration stateIllustration = new StateIllustration(screenshot);
        Mat illustration = stateIllustration.getScreenshot().clone();
        drawRect.drawRectAroundRegion(illustration, state.getBoundaries(), OpenCVColor.BLUE.getScalar());
        Set<StateImage> sios = state.getStateImages();
        for (StateImage sio : sios) {
            boolean isTransition = !sio.getTransitionImage().getTransitionsTo().isEmpty();
            List<MatchSnapshot> snapshots = sio.getMatchHistory().getSnapshots();
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
