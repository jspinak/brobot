package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.MatOps;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

@Getter
public class Scene {

    private String name = "";
    private String filename = "";
    private String absolutePath = "";
    private Mat bgr;
    private Mat hsv;
    private BufferedImage bufferedImageBGR; // used for Find operations

    public Scene(String filename, Mat bgr, Mat hsv) {
        this.filename = filename;
        this.name = filename.substring(0, filename.lastIndexOf("."));
        this.bgr = bgr;
        this.hsv = hsv;
    }

    public Scene(String filename, Mat bgr, BufferedImage bufferedImage) {
        this.filename = filename;
        this.name = filename.substring(0, filename.lastIndexOf("."));
        this.bgr = bgr;
        this.hsv = new Mat();
        if (!bgr.empty()) cvtColor(bgr, hsv, COLOR_BGR2HSV);
        this.bufferedImageBGR = bufferedImage;
    }

    public Scene(String filename, String absolutePath, Mat bgr, BufferedImage bufferedImage) {
        this.filename = filename;
        this.absolutePath = absolutePath;
        this.name = filename.substring(0, filename.lastIndexOf("."));
        this.bgr = bgr;
        this.hsv = new Mat();
        if (!bgr.empty()) cvtColor(bgr, hsv, COLOR_BGR2HSV);
        this.bufferedImageBGR = bufferedImage;
    }

    public Scene() {}

    public static Scene getEmptyScene() {
        Scene scene = new Scene();
        int height = new Region().h;
        int width = new Region().w;
        scene.name = "empty scene";
        scene.bgr = new Mat(height, width, 16);
        scene.hsv = new Mat(height, width, 16);
        return scene;
    }

    public Pattern getPatternBGR() {
        return new Pattern(bufferedImageBGR);
    }

    public static class Builder {
        private String name = "";
        private String filename = "";
        private String absolutePath = "";
        private Mat bgr;
        private Mat hsv;
        private BufferedImage bufferedImageBGR;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
            return this;
        }

        public Builder setBGR(Mat bgr) {
            this.bgr = bgr;
            return this;
        }

        public Builder setHSV(Mat hsv) {
            this.hsv = hsv;
            return this;
        }

        public Builder setBufferedImageBGR(BufferedImage bufferedImageBGR) {
            this.bufferedImageBGR = bufferedImageBGR;
            return this;
        }

        public Scene build() {
            Scene scene = new Scene();
            scene.name = name;
            scene.filename = filename;
            scene.absolutePath = absolutePath;
            scene.bgr = bgr;
            scene.hsv = hsv;
            scene.bufferedImageBGR = bufferedImageBGR;
            return scene;
        }
    }

}
