package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SetColorProfile {

    private GetImage getImage;

    public SetColorProfile(GetImage getImage) {
        this.getImage = getImage;
    }

    public ColorProfile setProfile(Image image) {
        ColorProfile colorProfile = new ColorProfile();
        List<Mat> imgs = new ArrayList<>();
        List<Mat> hues = new ArrayList<>();
        List<Mat> sats = new ArrayList<>();
        List<Mat> vals = new ArrayList<>();
        image.getFilenames().forEach(f -> imgs.add(
                getImage.getMatFromFilename(ImagePath.getBundlePath()+"/"+f, true)));
        imgs.forEach(img -> {
            List<Mat> channels = new ArrayList<>();
            Core.split(img, channels);
            hues.add(channels.get(0));
            sats.add(channels.get(1));
            vals.add(channels.get(2));
        });
        Mat reshape = reshapeToOneD(hues);
        List<Double> values = getMinMaxMeanStddev(reshape);
        colorProfile.setH(values.get(0), values.get(1), values.get(2), values.get(3));
        reshape = reshapeToOneD(sats);
        values = getMinMaxMeanStddev(reshape);
        colorProfile.setS(values.get(0), values.get(1), values.get(2), values.get(3));
        reshape = reshapeToOneD(vals);
        values = getMinMaxMeanStddev(reshape);
        colorProfile.setV(values.get(0), values.get(1), values.get(2), values.get(3));
        //colorProfile.print();
        return colorProfile;
    }

    // reshapes Mats to 1 row Mats and then combines them.
    private Mat reshapeToOneD(List<Mat> mats) {
        List<Mat> reshaped = new ArrayList<>();
        for (Mat mat : mats) {
            reshaped.add(mat.reshape(1, 1));
        }
        Mat combined = new Mat();
        Core.hconcat(reshaped, combined);
        return combined;
    }

    private List<Double> getMinMaxMeanStddev(Mat mat) {
        List<Double> values = new ArrayList<>();
        Core.MinMaxLocResult result = Core.minMaxLoc(mat);
        values.add(result.minVal);
        values.add(result.maxVal);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(mat, mean, stddev);
        values.add(mean.get(0,0)[0]);
        values.add(stddev.get(0,0)[0]);
        return values;
    }
}
