package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.Arrays;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Getter
@Setter
public class Illustrations {

    public enum Type {
        SCENE_MATCHES_SIDEBAR, CLASSES_LEGEND, MOTION
    }

    private String sceneName = "";
    private Mat sceneWithMatchesAndSidebar; // matches drawn on the scene together with the sidebar
    private Mat matchesOnScene; // matches and search regions drawn on the scene
    private Mat scene; // the scene
    private Mat sidebar; // shows matches in more detail
    private String filenameScene;
    private Mat classesWithMatchesAndLegend; // classes drawn on the scene together with the legend
    private Mat matchesOnClasses; // the scene with matches and classes
    private Mat classes; // the scene with classes drawn on it (segmentation by image)
    private Mat legend; // shows the classes: their underlying images and kMeans centers
    private String filenameClasses;
    private Mat motion; // pixels that have changed between scenes
    private Mat motionWithMatches; // matches depend on minSize and other parameters

    public Mat getMat(Type type) {
        switch (type) {
            case SCENE_MATCHES_SIDEBAR:
                return sceneWithMatchesAndSidebar;
            case CLASSES_LEGEND:
                return classesWithMatchesAndLegend;
            default:
                Report.println("No such type of Mat: " + type);
                return null;
        }
    }

    public String getFilename(Type type) {
        switch (type) {
            case SCENE_MATCHES_SIDEBAR:
                return filenameScene;
            case CLASSES_LEGEND:
                return filenameClasses;
            default:
                Report.println("No such type of filename: " + type);
                return null;
        }
    }

    public void write(Type type) {
        Mat mat = getMat(type);
        String filename = getFilename(type);
        if (mat == null || filename == null) {
            Report.println("Didn't write illustration. Mat or filename is null.");
            return;
        }
        imwrite(filename, mat);
    }

    public void write() {
        write(Type.SCENE_MATCHES_SIDEBAR);
        write(Type.CLASSES_LEGEND);
    }

    public void setScene(Mat scene) {
        this.scene = scene;
        this.matchesOnScene = scene.clone();
    }

    public void setClasses(Mat classes) {
        this.classes = classes;
        this.matchesOnClasses = classes.clone();
    }

    public void setFilenames(String filename) {
        this.filenameScene = filename + "-scene-";
        this.filenameClasses = filename + "-classes-";
    }

    public List<String> getFilenames() {
        return Arrays.asList(filenameScene, filenameClasses);
    }

    public List<Mat> getFinishedMats() {
        return Arrays.asList(sceneWithMatchesAndSidebar, classesWithMatchesAndLegend);
    }
}
