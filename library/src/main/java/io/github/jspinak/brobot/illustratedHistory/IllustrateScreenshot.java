package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetBufferedImage;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class IllustrateScreenshot {

    private ImageUtils imageUtils;
    private GetBufferedImage getBufferedImage;
    private Draw draw;

    private BufferedImage bufferedImage;
    private String currentPath;
    private String outputPath;

    private boolean okToSave = false;
    private List<ObjectCollection> lastCollections = new ArrayList<>();
    private ActionOptions.Action lastAction = ActionOptions.Action.TYPE;
    private Location lastMove;

    public IllustrateScreenshot(ImageUtils imageUtils, GetBufferedImage getBufferedImage, Draw draw) {
        this.imageUtils = imageUtils;
        this.getBufferedImage = getBufferedImage;
        this.draw = draw;
    }

    public boolean okToIllustrate() {
        return BrobotSettings.saveHistory && !BrobotSettings.mock;
    }

    /**
     * Brobot currently only illustrates Find, Click, and Drag actions.
     * We generally do not want to illustrate an action every time it repeats, particularly
     * for Find operations. If the action is a Find and the previous action was also a Find,
     * and the Collections are the same, don't illustrate the screenshot.
     *
     * @param actionOptions
     * @param objectCollections
     * @return
     */
    public boolean okToIllustrate(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!okToIllustrate()) return false;
        ActionOptions.Action action = actionOptions.getAction();
        if (action != ActionOptions.Action.FIND &&
                action != ActionOptions.Action.CLICK &&
                action != ActionOptions.Action.DRAG &&
                action != ActionOptions.Action.MOVE &&
                action != ActionOptions.Action.HIGHLIGHT) return false;
        if (action != ActionOptions.Action.FIND) {
            //Report.println(" action is not FIND. it is " + action);
            return true;
        }
        if (lastAction != action) {
            //Report.println(" action is different ");
            return true;
        }
        if (!sameCollections(Arrays.asList(objectCollections))) {
            //Report.println(" collections are different ");
            return true;
        }
        return false;
    }

    private boolean sameCollections(List<ObjectCollection> objectCollections) {
        if (objectCollections.size() != lastCollections.size()) return false;
        for (int i=0; i<objectCollections.size(); i++) {
            if (!objectCollections.get(0).equals(lastCollections.get(i))) return false;
        }
        return true;
    }

    public boolean prepareScreenshot(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        String name = "";
        if (objectCollections.length > 0) name = objectCollections[0].getFirstObjectName();
        if (!okToIllustrate(actionOptions, objectCollections)) return false;
        try {
            currentPath = imageUtils.saveRegionToFile(new Region(),
                    BrobotSettings.historyPath + BrobotSettings.screenshotFilename);
            outputPath = currentPath.replace(BrobotSettings.screenshotFilename, BrobotSettings.historyFilename);
            outputPath = outputPath.replace(".png", "-"+actionOptions.getAction()+
                    "-"+name+".png");
            bufferedImage = getBufferedImage.fromFile(currentPath);
            okToSave = true;
            lastAction = actionOptions.getAction();
            lastCollections = new ArrayList<>();
            lastCollections = Arrays.asList(objectCollections);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveToFile(ActionOptions actionOptions) {
        if (!okToIllustrate(actionOptions) || !okToSave) return false;
        try {
            File outputfile = new File(outputPath);
            ImageIO.write(bufferedImage, "png", outputfile);
            okToSave = false;
            return true;
        } catch (IOException e) {
            Report.println(currentPath + " not saved. ");
            return false;
        }
    }

    public void drawMatch(Match match) {
        if (!okToIllustrate()) return;
        draw.match(match, bufferedImage.getGraphics(), Color.blue);
    }

    public void drawHighlight(Match match) {
        if (!okToIllustrate()) return;
        draw.match(match, bufferedImage.getGraphics(), Color.yellow);
    }

    public void drawClick(Location location) {
        if (!okToIllustrate()) return;
        draw.click(location, bufferedImage.getGraphics());
    }

    public void drawDrag(Location from, Location to) {
        if (!okToIllustrate()) return;
        draw.drag(from, to, bufferedImage.getGraphics());
    }

    public void drawMove(List<Location> moveTo) {
        if (!okToIllustrate() || moveTo.isEmpty()) return;
        List<Location> moves = new ArrayList<>(moveTo);
        if (lastAction == ActionOptions.Action.MOVE && lastMove != null) moves.add(lastMove);
        draw.move(moves, bufferedImage.getGraphics());
        lastMove = moveTo.get(0);
    }
}
