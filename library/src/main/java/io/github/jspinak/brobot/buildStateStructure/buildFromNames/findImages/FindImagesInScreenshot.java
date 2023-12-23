package io.github.jspinak.brobot.buildStateStructure.buildFromNames.findImages;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.PrintAttribute;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.UseAttribute;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyState;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Works with all Images in a State for a specific screenshot/page
 * - Finds all occurrences of an image in a screenshot
 * - Calls the methods necessary for processing Attributes and adding Snapshots
 * - Finishes the GroupDefine operation
 */
@Component
public class FindImagesInScreenshot {

    private UseAttribute useAttribute;
    private Action action;
    private AddSnapshots addSnapshots;
    private PrintAttribute printAttribute;
    private TransferRegion transferRegion;

    public FindImagesInScreenshot(UseAttribute useAttribute, Action action, AddSnapshots addSnapshots,
                                  PrintAttribute printAttribute, TransferRegion transferRegion) {
        this.useAttribute = useAttribute;
        this.action = action;
        this.addSnapshots = addSnapshots;
        this.printAttribute = printAttribute;
        this.transferRegion = transferRegion;
    }

    public void findByState(BabyState babyState, String screenshot) {
        ImageGroup imageGroup = new ImageGroup();
        int beginIndex = screenshot.indexOf('n') + 1; // n in screen
        int endIndex = screenshot.indexOf('.'); // . in .png
        int page = Integer.parseInt(screenshot.substring(beginIndex, endIndex));
        babyState.getImages().forEach(image -> findIn(image, screenshot, page, imageGroup));
        processImageGroup(imageGroup);
        transferRegion.processRegionTransfer(babyState.getImages(), page, babyState);
        babyState.getImages().forEach(image -> printAttribute.byImageAndPage(image, page));
    }

    public List<Match> findIn(StateImage image, String filename, int page, ImageGroup imageGroup) {
        List<Match> matches = getMatches(image, filename);
        image.getAttributes().getMatches().put(page, matches);
        List<AttributeTypes.Attribute> activeAttributes =
                useAttribute.processAttributes(image, imageGroup, matches, page);
        addSnapshots.ifNeededAddSnapshot(image, matches, activeAttributes);
        return matches;
    }

    private List<Match> getMatches(StateImage image, String filename) {
        File file = new File(BrobotSettings.screenshotPath + filename);
        String path = file.getAbsolutePath();
        List<Match> matches = new ArrayList<>();
        image.getImage().getAllPatterns().forEach(p -> {
            Finder f = new Finder(path);
            f.findAll(p);
            while (f.hasNext()) matches.add(f.next());
            f.destroy();
        });
        return matches;
    }

    private boolean processImageGroup(ImageGroup imageGroup) {
        if (!imageGroup.allImagesFound()) return false;
        imageGroup.print();
        Matches matches = action.perform(imageGroup.getActionOptions(), imageGroup.getObjectCollection());
        if (!matches.isSuccess()) return false;
        if (imageGroup.processNewRegion(matches.getDefinedRegion()))
            printAttribute.printDefinedRegion(matches);
        return true;
    }

}
