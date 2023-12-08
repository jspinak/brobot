package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

@Getter
@Setter
public class SceneAnalysisCollection {

    private double secondsBetweenScenes = 0;
    private List<SceneAnalysis> sceneAnalyses = new ArrayList<>();
    /**
     * The results of a multi-scene analysis are stored here.
     * This can be for actions such as motion analysis, or for aggregating matches across multiple scenes.
     */
    private Mat results;
    private Contours contours;

    public void add(SceneAnalysis sceneAnalysis) {
        sceneAnalyses.add(sceneAnalysis);
    }

    /**
    The scene that represents the collection, and is used for illustration.
    For motion analysis, the last scene is used.
    */
    public Optional<Scene> getLastScene() {
        if (sceneAnalyses.isEmpty()) return Optional.empty();
        return Optional.of(sceneAnalyses.get(sceneAnalyses.size() - 1).getScene());
    }

    public Optional<SceneAnalysis> getLastSceneAnalysis() {
        if (sceneAnalyses.isEmpty()) return Optional.empty();
        return Optional.of(sceneAnalyses.get(sceneAnalyses.size() - 1));
    }

    public Optional<Mat> getLastResultsBGR() {
        return getLastSceneAnalysis().map(sceneAnalysis -> sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D));
    }

    public List<Scene> getScenes() {
        return sceneAnalyses.stream().map(SceneAnalysis::getScene).toList();
    }

    public List<Mat> getAllScenesAsBGR() {
        List<Mat> mats = new ArrayList<>();
        for (SceneAnalysis sceneAnalysis : sceneAnalyses) {
            mats.add(sceneAnalysis.getScene().getBgr());
        }
        return mats;
    }

    public Optional<Mat> getLastSceneBGR() {
        List<Scene> scenes = getScenes();
        if (scenes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(scenes.get(scenes.size() - 1).getBgr());
    }

    public List<Illustrations> getAllIllustratedScenes() {
        return sceneAnalyses.stream().map(SceneAnalysis::getIllustrations).toList();
    }

    public boolean isEmpty() {
        return sceneAnalyses.isEmpty();
    }

    public void print() {
        Report.println("Scene analysis collection with "+ sceneAnalyses.size() + " scenes");
    }
}
