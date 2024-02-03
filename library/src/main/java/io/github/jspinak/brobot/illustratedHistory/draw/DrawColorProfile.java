package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.illustratedHistory.IllustrationFilename;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

@Component
public class DrawColorProfile {
    private final GetImageJavaCV getImage;
    private final IllustrationFilename illustrationFilename;

    private int imgsWH = 40;
    private int averageColorX = 42;
    private int spaceBetween = 2;

    public DrawColorProfile(GetImageJavaCV getImage, IllustrationFilename illustrationFilename) {
        this.getImage = getImage;
        this.illustrationFilename = illustrationFilename;
    }

    public Mat getImagesAndProfileMat(List<StateImage> imgs) {
        int maxFiles = getMaxFilenames(imgs);
        averageColorX = maxFiles * (imgsWH + spaceBetween);
        int frameWidth = averageColorX + imgsWH + spaceBetween;
        int frameHeight = (imgsWH + spaceBetween) * imgs.size();
        Mat frame = new Mat(frameHeight, frameWidth, 16, new Scalar(0, 0, 0, 0));
        int y = 0;
        for (StateImage img : imgs) {
            drawImage(frame, img, y);
            drawProfiles(frame, img, y);
            y += imgsWH + spaceBetween;
        }
        return frame;
    }

    public void drawImagesAndProfiles(List<StateImage> imgs) {
        Mat frame = getImagesAndProfileMat(imgs);
        String savePath = illustrationFilename.getFilename(ActionOptions.Action.CLASSIFY,"colorProfiles");
        imwrite(savePath, frame);
    }

    private int getMaxFilenames(List<StateImage> imgs) {
        int maxFiles = 1;
        for (StateImage img : imgs) maxFiles = Math.max(maxFiles, img.getPatterns().size());
        return maxFiles;
    }

    private void drawImage(Mat frame, StateImage img, int y) {
        int amountOfFiles = img.getPatterns().size();
        for (int i=0; i<amountOfFiles; i++) {
            String filename = img.getPatterns().get(i).getImgpath();
            Mat imgFile = getImage.getMatFromBundlePath(filename, BGR);
            Mat resizedDown = new Mat();
            double scaleDown = (double) imgsWH / Math.max(imgFile.cols(), imgFile.rows());
            int newWidth = (int) (imgFile.cols() * scaleDown);
            int newHeight = (int) (imgFile.rows() * scaleDown);
            Size newSize = new Size(newWidth, newHeight);
            resize(imgFile, resizedDown, newSize);
            Rect rect = new Rect((imgsWH + spaceBetween) * i, y, newWidth, newHeight);
            Mat placeHere = frame.apply(rect);
            resizedDown.copyTo(placeHere);
        }
    }

    private void drawProfiles(Mat frame, StateImage img, int y) {
        Rect rect = new Rect(averageColorX, y, imgsWH, imgsWH);
        Mat imgMat = frame.apply(rect);
        img.getColorCluster().getMat(BGR, ColorInfo.ColorStat.MEAN, rect.size()).copyTo(imgMat);
    }
}
