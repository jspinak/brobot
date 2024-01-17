package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

@Embeddable
@Getter
@Setter
public class Scene {

    private String name = "";
    @Embedded
    private Image image;

    public Scene(String filename) {
        this.name = filename.substring(0, filename.lastIndexOf("."));
        image = new Image(filename);
    }

    public Scene(BufferedImage bufferedImage) {
        image = new Image(bufferedImage);
    }

    public Scene(Mat BGRmat) {
        image = new Image(BGRmat);
    }

    public Pattern getPatternBGR() {
        return new Pattern(image.get());
    }

    public static Scene getEmptyScene() {
        Region r = new Region();
        BufferedImage bufferedImage = new BufferedImage(r.w, r.h, TYPE_BYTE_BINARY);
        Scene scene = new Scene(bufferedImage);
        scene.name = "empty scene";
        return scene;
    }

}
