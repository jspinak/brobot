package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

@Component
public class IllustrationManager {

    private GetImage getImage;
    private Sidebar sidebar;
    private Draw draw;
    private IllustrationFilename illustrationFilename;

    private ActionOptions.Action lastAction = ActionOptions.Action.TYPE; // init with an action without illustration
    private Location lastLocation;

    private Map<ActionOptions.Action, BiConsumer<Mat,Matches>> drawAction = new HashMap<>();
    {
        drawAction.put(ActionOptions.Action.FIND, this::drawMatches);
        drawAction.put(ActionOptions.Action.HIGHLIGHT, this::drawMatches);
        drawAction.put(ActionOptions.Action.CLICK, this::drawClicks);
        drawAction.put(ActionOptions.Action.DRAG, this::drawArrow);
        drawAction.put(ActionOptions.Action.MOVE, this::drawMove);
    }

    public IllustrationManager(GetImage getImage, Sidebar sidebar, Draw draw,
                               IllustrationFilename illustrationFilename) {
        this.getImage = getImage;
        this.sidebar = sidebar;
        this.draw = draw;
        this.illustrationFilename = illustrationFilename;
    }

    public void draw(Matches matches, List<Region> searchRegions, ActionOptions actionOptions,
                     ObjectCollection... objectCollections) {
        Mat screen = getImage.getMatFromScreen();
        Mat sidebarMat = sidebar.draw(screen, matches);
        searchRegions.forEach(r -> draw.drawRect(screen, r.toMatch(), new Scalar(200, 200, 200)));
        drawAction.get(actionOptions.getAction()).accept(screen, matches);
        lastAction = actionOptions.getAction();
        Mat fused = fuseScreenAndSidebar(screen, sidebarMat);
        writeToFile(fused, actionOptions, objectCollections);
    }

    private void writeToFile(Mat toWrite, ActionOptions actionOptions,
                             ObjectCollection... objectCollections) {
        String outputPath = illustrationFilename.getFilename(actionOptions, objectCollections);
        imwrite(outputPath, toWrite);
    }

    private Mat fuseScreenAndSidebar(Mat screen, Mat sidebarMat) {
        List<Mat> concatMats = new ArrayList<>();
        concatMats.add(screen);
        concatMats.add(sidebarMat);
        Mat result = new Mat();
        Core.hconcat(concatMats, result);
        return result;
    }

    private void drawMatches(Mat screen, Matches matches) {
        matches.getMatches().forEach(m -> draw.drawRect(screen, m, new Scalar(255, 150, 255)));
    }

    private void drawClicks(Mat screen, Matches matches) {
        matches.getMatches().forEach(m -> draw.drawPoint(screen, m, new Scalar(255, 150, 255)));
    }

    private void drawArrow(Mat screen, Matches matches) {
        if (matches.isEmpty()) return;
        Region reg = matches.getDefinedRegion();
        Location start = new Location(reg.x, reg.y);
        Location end = new Location(reg.getX2(), reg.getY2());
        draw.drawArrow(screen, start, end, new Scalar(255, 150, 255));
    }

    private void drawMove(Mat screen, Matches matches) {
        List<Location> moves = new ArrayList<>(matches.getMatchLocations());
        if (moves.isEmpty()) return;
        if (lastAction == ActionOptions.Action.MOVE && lastLocation != null) moves.add(0, lastLocation);
        draw.move(screen, moves, new Scalar(255, 150, 255));
        lastLocation = moves.get(moves.size()-1);
    }

}
