package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

/**
 * Mats used to illustrate the image and its color profiles can be stored together with
 * the image and referenced for illustration when needed.
 */
@Component
public class SetProfileMats {

    private final GetImageJavaCV getImage;

    private int imgsWH = 40;
    private int spaceBetween = 2;

    public SetProfileMats(GetImageJavaCV getImage) {
        this.getImage = getImage;
    }

    /**
     * Returns a Mat with the image files shown horizontally.
     * @param img the image with the files to be shown
     * @return Mat with the image files shown horizontally
     */
    public Mat getImagesMat(StateImage img) {
        List<Mat> imgMats = getImage.getMats(img, ColorCluster.ColorSchemaName.BGR);
        return new MatBuilder()
                .setName(img.getName() + "_imgMats")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .setSpaceBetween(spaceBetween)
                .setSubMats(imgMats)
                .build();
    }

    public Mat getProfilesMat(StateImage img) {
        Mat profile = img.getColorCluster().getMat(BGR, ColorInfo.ColorStat.MEAN, new Size(imgsWH, imgsWH));
        return new MatBuilder()
                .setName(img.getName() + "_profile")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .addSubMat(new Location(0,0), profile)
                .build();
    }

    public Mat getKmeansProfilesMat(StateImage img, int kMeans) {
        List<Mat> profiles = img.getKmeansProfilesAllSchemas().getColorProfileMats(
                kMeans, new Size(imgsWH, imgsWH));
        return new MatBuilder()
                .setName(img.getName() + "_kmeansProfile")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .addHorizontalSubmats(profiles)
                .build();
    }

    public Mat getProfilesMat(StateImage img, ActionOptions actionOptions) {
        int kMeans = 0;
        if (actionOptions.getKmeans() < 0) kMeans = BrobotSettings.kMeansInProfile;
        else kMeans = actionOptions.getKmeans();
        if (kMeans == 0) return getProfilesMat(img);
        return getKmeansProfilesMat(img, kMeans);
    }

    public void setMats(StateImage img) {
        img.setImagesMat(getImagesMat(img));
        img.setProfilesMat(getProfilesMat(img));
    }
}
