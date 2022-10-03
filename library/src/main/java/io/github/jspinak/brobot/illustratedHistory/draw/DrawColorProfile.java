package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorProfile.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

@Component
public class DrawColorProfile {
    private GetImageJavaCV getImage;
    private IllustrationFilename illustrationFilename;

    private int imgsWH = 40;
    private int averageColorX = 42;
    private int spaceBetween = 2;
    private Mat frame;

    public DrawColorProfile(GetImageJavaCV getImage, IllustrationFilename illustrationFilename) {
        this.getImage = getImage;
        this.illustrationFilename = illustrationFilename;
    }

    public Mat getImagesAndProfileMat(List<StateImageObject> imgs) {
        int maxFiles = getMaxFilenames(imgs);
        averageColorX = maxFiles * (imgsWH + spaceBetween);
        int frameWidth = averageColorX + imgsWH + spaceBetween;
        int frameHeight = (imgsWH + spaceBetween) * imgs.size();
        frame = new Mat(frameHeight, frameWidth, 16, new Scalar(0, 0, 0, 0));
        MatOps.info(frame, "frame");
        int y = 0;
        for (StateImageObject img : imgs) {
            drawImage(img, y);
            drawProfiles(img, y);
            y += imgsWH + spaceBetween;
        }
        return frame;
    }

    public void drawImagesAndProfiles(List<StateImageObject> imgs) {
        getImagesAndProfileMat(imgs);
        String savePath = illustrationFilename.getFilename(ActionOptions.Action.CLASSIFY,"colorProfiles");
        imwrite(savePath, frame);
    }

    private int getMaxFilenames(List<StateImageObject> imgs) {
        int maxFiles = 1;
        for (StateImageObject img : imgs) maxFiles = Math.max(maxFiles, img.getImage().getFilenames().size());
        return maxFiles;
    }

    private void drawImage(StateImageObject img, int y) {
        int amountOfFiles = img.getImage().getFilenames().size();
        System.out.println("amountOfFiles = " + amountOfFiles);
        for (int i=0; i<amountOfFiles; i++) {
            String filename = img.getImage().getFilenames().get(i);
            System.out.println("filename: " + filename);
            Mat imgFile = getImage.getMat(filename, BGR);
            Mat resizedDown = new Mat();
            double scaleDown = (double) imgsWH / Math.max(imgFile.cols(), imgFile.rows());
            int newWidth = (int) (imgFile.cols() * scaleDown);
            int newHeight = (int) (imgFile.rows() * scaleDown);
            Size newSize = new Size(newWidth, newHeight);
            Report.println("newSize = " + newSize.height() + "x" + newSize.width());
            resize(imgFile, resizedDown, newSize);
            //resize(imgFile, resizedDown, new Size(imgsWH, imgsWH));
            //imgFile.create(placeHere.size(), imgFile.type());
            MatOps.info(imgFile, "imgFile");
            Rect rect = new Rect((imgsWH + spaceBetween) * i, y, newWidth, newHeight);
            Mat placeHere = frame.apply(rect);
            MatOps.info(placeHere, "placeHere");
            resizedDown.copyTo(placeHere);
            MatOps.info(imgFile, "imgFile");
            System.out.println("frame: " + frame.cols() + " " + frame.rows());
            System.out.println("rect: " + rect.x() + " " + rect.y() + " " + rect.width() + " " + rect.height());
            System.out.println("placeHere: " + placeHere.cols() + " " + placeHere.rows());
            System.out.println("imgFile: " + imgFile.cols() + " " + imgFile.rows());
            //imgFile.copyTo(frame);
        }
    }

    private void drawProfiles(StateImageObject img, int y) {
        Rect rect = new Rect(averageColorX, y, imgsWH, imgsWH);
        MatOps.info(frame, "frame");
        System.out.println("frame: " + frame.cols() + " " + frame.rows());
        System.out.println("rect: " + rect.x() + " " + rect.y() + " " + rect.width() + " " + rect.height());
        Mat imgMat = frame.apply(rect);
        img.getColorProfile().getMat(BGR, ColorInfo.ColorStat.MEAN, rect.size()).copyTo(imgMat);
    }
}
