package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.FilenameRepo;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

@Component
public class IllustrationFilename {

    private final FilenameRepo filenameRepo;

    public IllustrationFilename(FilenameRepo filenameRepo) {
        this.filenameRepo = filenameRepo;
    }

    /**
     * Matches may contain multiple (or no) SceneAnalyses and thus multiple illustrated scenes.
     * This method returns the filename of one illustrated scene. In the absense of SceneAnalyses
     * there will still be a scene and matches to illustrate. To get filenames for
     * each scene, the getFilename method with SceneAnalysis as a parameter should be called
     * for each SceneAnalysis.
     *
     * The filename is built as follows:
     * <p>The action is added to the name (i.e. FIND, CLICK, CLASSIFY, etc.)
     * <p>in-NameOfScene: the name of the scene will be the image name if passed as a parameter, or 'screenshot' if
     * a screenshot was used.
     * <p>The names of target images are then written, separated by '_'
     * <p>A description of the type of analysis comes at the end. This could be 'classes', if the analysis shows
     * the classification of pixels in the scene, or 'scene' if the location of an image is shown resulting from
     * a Find.COLOR operation.
     * @param matches contain the images searched for
     * @param actionOptions holds the action being performed
     * @return the filename
     */
    public String getSingleFilename(Matches matches, ActionOptions actionOptions) {
        if (matches.getSceneAnalysisCollection().isEmpty()) return getFilenameFromMatchObjects(matches, actionOptions);
        SceneAnalysis sceneAnalysis = matches.getSceneAnalysisCollection().getSceneAnalyses().get(0);
        return getFilenameFromSceneAnalysis(sceneAnalysis, actionOptions);
    }

    public String getFilenameFromMatchObjects(Matches matches, ActionOptions actionOptions) {
        String prefix = BrobotSettings.historyPath + BrobotSettings.historyFilename;
        ActionOptions.Action action = actionOptions.getAction();
        Set<String> imageNames = matches.getMatchList().stream().map(
                m -> m.getStateObjectData().getStateObjectName()).collect(Collectors.toSet());
        String names = String.join("", imageNames);
        String suffix = action.toString() + "_" + names;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    public String getFilenameFromSceneAnalysis(SceneAnalysis sceneAnalysis, ActionOptions actionOptions, String... additionalDescription) {
        String prefix = BrobotSettings.historyPath;
        ActionOptions.Action action = actionOptions.getAction();
        String sceneName = sceneAnalysis.getScene().getName();
        String imageNames = sceneAnalysis.getImageNames();
        String names = String.join("_", imageNames);
        String suffix = action.toString() + "_in-" + sceneName + "_" + names + String.join("_", additionalDescription);
        Report.println("Filename: " + prefix + suffix);
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    private String composeName(String freepath, ActionOptions.Action action, String imageNames) {
        return freepath + "_" + action.toString() + "_" + imageNames + ".png";
    }

    private String composeName(String path, String baseName, int number, ActionOptions.Action action, String imageNames) {
        return path + baseName + number + "_" + action.toString() + "_" + imageNames + ".png";
    }

    public String getFilename(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        String prefix = BrobotSettings.historyPath + BrobotSettings.historyFilename;
        ActionOptions.Action action = actionOptions.getAction();
        List<String> names = new ArrayList<>();
        for (ObjectCollection objectCollection : objectCollections) {
            names.addAll(objectCollection.getStateImages().stream().map(StateImage::getName).toList());
        }
        String allNames = String.join("", names);
        String suffix = action.toString();
        if (actionOptions.getAction() == FIND) suffix += "_" + actionOptions.getFind();
        if (!allNames.isEmpty()) suffix += "_" + allNames;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    public String getFilename(ActionOptions.Action action, String name) {
        String prefix = BrobotSettings.historyPath + BrobotSettings.historyFilename;
        String suffix = action.toString() + "_" + name;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }
}
