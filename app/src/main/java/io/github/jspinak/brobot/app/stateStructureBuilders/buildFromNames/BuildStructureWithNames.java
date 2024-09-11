package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.babyStates.BabyStateRepo;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.findImages.FindImagesInScreenshot;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds a StateStructure using labeled images and screenshots of the automation environment.
 * The image filenames give instructions on how to organize States and what to do with the images;
 * for example, a name including '_d34' will define the Image's search area with the match found on
 * the screenshot with filename 'screen34.png'. A complete list of possible instructions is found
 * in the Brobot documentation website in GitHub.
 *
 * Output includes:
 * - Console output showing positive search results for images per screenshot,
 *   violations of filename instructions on a specific screenshot,
 *   the result of operations given by filename instructions
 * - StateImages are defined with MatchSnapshots from matches found in the screenshots. In
 *   addition, they may be initialized with SearchRegions defined from a match or from the matches
 *   of a group of images in the same State.
 * - StateRegions defined by matching an image on a specific screenshot
 *
 * Building a StateStructure this way allows you to
 * - Create accurate MatchHistories with real matches
 * - Create StateRegions of otherwise difficult to capture regions (usually due to variable imagery)
 * - Work directly with the client's environment
 * - Get feedback on the accuracy and efficacy of your captured images before running the app live
 * - Save lots of time in typing out the State structure and avoid errors
 */
@Component
public class BuildStructureWithNames {

    private final GetFiles getFiles;
    private final BabyStateRepo babyStateRepo;
    private final FindImagesInScreenshot findImagesInScreenshot;

    private List<String> screenshots;

    public BuildStructureWithNames(GetFiles getFiles, BabyStateRepo babyStateRepo,
                                   FindImagesInScreenshot findImagesInScreenshot) {
        this.getFiles = getFiles;
        this.babyStateRepo = babyStateRepo;
        this.findImagesInScreenshot = findImagesInScreenshot;
    }

    public void getFiles() {
        getFiles.addImagesToRepo();
        screenshots = getFiles.getScreenshots();
        Report.println("number of screenshots = "+screenshots.size());
    }

    private void findImagesInScreenshots() {
        screenshots.forEach(screen -> {
            Report.print("\n"+screen, ANSI.BLUE);
            Report.println(" coordinates in format x.y_w.h", ANSI.WHITE);
            babyStateRepo.getBabyStates().values().forEach(
                    state -> findImagesInScreenshot.findByState(state, screen));
        });
    }

    public void build() {
        getFiles();
        babyStateRepo.printStatesAndImages();
        findImagesInScreenshots();
    }
}