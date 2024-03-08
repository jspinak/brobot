package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorStatProfile;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCORE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.SCENE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;

/**
 * A collection of PixelAnalysisCollection objects, each of which contains all analysis of a {scene, StateImage} pair.
 * SceneAnalysis is most commonly used to store the analysis of a scene with all of its StateImages.
 * The IllustratedScene is a writeable BGR image with the scene and analysis drawn on it (matches, regions, etc).
 */
@Getter
@Setter
public class SceneAnalysis {

    public enum Analysis {
        SCENE, INDICES_3D, INDICES_3D_TARGETS, INDICES_2D, BGR_FROM_INDICES_2D, BGR_FROM_INDICES_2D_TARGETS
    }
    /*
     SCENE is a 3d Mat of the scene (BGR, HSV).
     INDICES_3D mats have the corresponding indices for each cell in each channel (i.e. the value at (0,0) for HUE
        can be a different index than that at (0,0) for SATURATION).
     INDICES_3D_TARGETS is the same as INDICES_3D, but only has data for the target images in the scene (any cell that was
        classified as one of the additional images is here shown as 'no match').
     INDICES_2D are results Mats containing the selected class indices for each pixel. The selected indices
        are chosen from the INDICES_3D Mat. The HSV format is used as default for this analysis,
        since it is easy and effective to take the H channel and use it as the index results per pixel.
     BGR_FROM_INDICES_2D holds 3d BGR Mats used for writing to file or showing results on-screen (must be in BGR format).
        Each pixel is the mean color of the corresponding class (represented by the image index of the 2d results Mat).
     */

    private List<PixelAnalysisCollection> pixelAnalysisCollections;
    private Image scene;
    private Contours contours;
    // these matches are scene specific. for example, the matches from motion from the previous scene to this one.
    private List<Match> matchList = new ArrayList<>();
    private Illustrations illustrations; // the results in a writeable format
    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analysis = new HashMap<>();
    {
        analysis.put(BGR, new HashMap<>());
        analysis.put(HSV, new HashMap<>());
    }

    public SceneAnalysis(List<PixelAnalysisCollection> pixelAnalysisCollections, Image scene) {
        this.pixelAnalysisCollections = pixelAnalysisCollections;
        this.scene = scene;
        addAnalysis(BGR, SCENE, scene.getMatBGR());
        addAnalysis(HSV, SCENE, scene.getMatHSV());
        illustrations = new Illustrations();
        illustrations.setScene(scene.getMatBGR());
    }

    /**
     * This constructor is for a SceneAnalysis that does not require a PixelAnalysisCollection.
     * Usually, this would be used for an Action that can be fulfilled by Sikuli.
     * No color analysis is required.
     * @param scene
     */
    public SceneAnalysis(Image scene) {
        pixelAnalysisCollections = new ArrayList<>();
        this.scene = scene;
        addAnalysis(BGR, SCENE, scene.getMatBGR());
        addAnalysis(HSV, SCENE, scene.getMatHSV());
        illustrations = new Illustrations();
        illustrations.setScene(scene.getMatBGR());
    }

    public void addAnalysis(ColorCluster.ColorSchemaName colorSchemaName, Analysis analysis, Mat mat) {
        this.analysis.get(colorSchemaName).put(analysis, mat);
        if (analysis == BGR_FROM_INDICES_2D) illustrations.setClasses(mat);
    }

    public Mat getAnalysis(ColorCluster.ColorSchemaName colorSchemaName, Analysis analysis) {
        return this.analysis.get(colorSchemaName).get(analysis);
    }

    public List<StateImage> getStateImageObjects() {
        return pixelAnalysisCollections.stream().map(PixelAnalysisCollection::getStateImage).collect(Collectors.toList());
    }

    public String getImageNames() {
        return pixelAnalysisCollections.stream().map(PixelAnalysisCollection::getImageName).collect(Collectors.joining(""));
    }

    public int size() {
        return pixelAnalysisCollections.size();
    }

    public PixelAnalysisCollection getPixelAnalysisCollection(int index) {
        return pixelAnalysisCollections.get(index);
    }

    public int getLastIndex() {
        OptionalInt maxOpt = pixelAnalysisCollections.stream().mapToInt(pic -> pic.getStateImage().getIndex()).max();
        return maxOpt.isPresent() ? maxOpt.getAsInt() : 0;
    }

    public List<ColorStatProfile> getColorStatProfiles(ColorCluster.ColorSchemaName colorSchemaName,
                                                       ColorInfo.ColorStat colorStat) {
        List<ColorStatProfile> colorStatProfiles = new ArrayList<>();
        for (PixelAnalysisCollection pixelAnalysisCollection : pixelAnalysisCollections) {
            ColorCluster colorCluster = pixelAnalysisCollection.getStateImage().getColorCluster();
            ColorStatProfile colorStatProfile = colorCluster.getSchema(colorSchemaName).getColorStatProfile(colorStat);
            colorStatProfiles.add(colorStatProfile);
        }
        return colorStatProfiles;
    }

    public List<Integer> getColorValues(ColorCluster.ColorSchemaName colorSchemaName, ColorInfo.ColorStat colorStat,
                                        ColorSchema.ColorValue colorValue) {
        List<ColorStatProfile> colorStatProfiles = getColorStatProfiles(colorSchemaName, colorStat);
        return colorStatProfiles.stream().mapToDouble(profile -> profile.getStat(colorValue)).boxed()
                .mapToInt(Double::intValue).boxed().toList();
    }

    public Optional<PixelAnalysisCollection> getPixelAnalysisCollection(StateImage stateImage) {
        return pixelAnalysisCollections.stream()
                .filter(pic -> pic.getStateImage().equals(stateImage)).findFirst();
    }

    public Optional<Mat> getScores(StateImage stateImage) {
        Optional<PixelAnalysisCollection> coll = pixelAnalysisCollections.stream()
                .filter(pic -> pic.getStateImage().equals(stateImage))
                .findFirst();
        if (coll.isEmpty()) return Optional.empty();
        return Optional.of(coll.get().getAnalysis(SCORE, BGR));
    }

    public Mat getScoresMat(StateImage stateImage) {
        return getScores(stateImage).orElse(new Mat());
    }

}
