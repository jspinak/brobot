package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.ImageResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.FilenameOps;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

// todo: think about using Image instead. there's nothing that distinguishes Scene from Image.
@Entity
@Getter
@Setter
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name = "";
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    public Scene(String filename) {
        this.name = FilenameOps.getFileNameWithoutExtension(filename);
        image = new Image(filename);
    }

    public Scene(BufferedImage bufferedImage) {
        image = new Image(bufferedImage);
    }

    public Scene(Mat BGRmat) {
        image = new Image(BGRmat);
    }

    public Pattern getPatternBGR() {
        return new Pattern(image.getBufferedImage());
    }

    public static Scene getEmptyScene() {
        Region r = new Region();
        BufferedImage bufferedImage = new BufferedImage(r.w(), r.h(), TYPE_BYTE_BINARY);
        Scene scene = new Scene(bufferedImage);
        scene.name = "empty scene";
        return scene;
    }

    public ImageResponse toImageResponse() {
        return new ImageResponse(image);
    }

}
