package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.add;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * The same methods as in GetImageOpenCV, but for JavaCV.
 * JavaCV is compatible with the DL4J libraries, making it a better choice for Brobot than OpenCV.
 * It would be cleaner to migrate all OpenCV code to JavaCV, but this is a lower priority.
 */
@Component
public class GetImageJavaCV {

    private BufferedImageOps bufferedImageOps;

    public GetImageJavaCV(BufferedImageOps bufferedImageOps) {
        this.bufferedImageOps = bufferedImageOps;
    }

    public Mat convertToHSV(Mat bgr) {
        cvtColor(bgr, bgr, COLOR_BGR2HSV);
        return bgr;
    }

    public Mat getHSV(Mat bgr) {
        Mat hsv = new Mat();
        cvtColor(bgr, hsv, COLOR_BGR2HSV );
        return hsv;
    }

    public Mat getMatFromFilename(String imageName, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat mat = imread(imageName); // Mat [ 7*7*CV_8UC3 ...
        if (colorSchemaName == ColorCluster.ColorSchemaName.BGR) return mat;
        if (colorSchemaName == ColorCluster.ColorSchemaName.HSV) return getHSV(mat);
        throw new RuntimeException("ColorSchemaName not supported: " + colorSchemaName);
    }

    public Mat getMatFromBundlePath(String imageName, ColorCluster.ColorSchemaName colorSchemaName) {
        String path = ImagePath.getBundlePath()+"/"+imageName;
        Mat mat = getMatFromFilename(path, colorSchemaName);
        return mat;
    }

    public List<Mat> getMatsFromFilenames(List<String> filenames, ColorCluster.ColorSchemaName colorSchemaName) {
        List<Mat> mats = new ArrayList<>();
        for (String filename : filenames) {
            mats.add(getMatFromBundlePath(filename, colorSchemaName));
        }
        return mats;
    }

    public List<Mat> getMats(StateImage img, ColorCluster.ColorSchemaName colorSchemaName) {
        return getMats(img.getPatterns(), colorSchemaName);
    }

    public List<Mat> getMats(List<Pattern> patterns, ColorCluster.ColorSchemaName colorSchemaName) {
        List<String> filenames = new ArrayList<>();
        patterns.forEach(p -> filenames.add(p.getImgpath()));
        return getMatsFromFilenames(filenames, colorSchemaName);
    }

    /**
     * Returns one Mat masked by the regions.
     * @param imageName the name of the image to load
     * @param regions the regions to add to the Mat
     * @param colorSchemaName the color schema to use
     * @return a Mat with only the given regions selected
     */
    public Mat getMat(String imageName, List<Region> regions, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat image = getMatFromBundlePath(imageName, colorSchemaName);
        Mat mask = new Mat();
        for (Region region : regions) {
            add(mask, image, mask, new Mat(region.getJavaCVRect()), -1);
        }
        return mask;
    }

    /**
     * Returns one Mat per region.
     * @param imageName the name of the image to load
     * @param regions each region corresponds to a Mat
     * @param colorSchemaName the color schema to use
     * @return a List of Mats corresponding to the regions
     */
    public List<Mat> getMats(String imageName, List<Region> regions, ColorCluster.ColorSchemaName colorSchemaName) {
        List<Mat> mats = new ArrayList<>();
        Mat scene = getMatFromBundlePath(imageName, colorSchemaName);
        if (regions.isEmpty()) {
            mats.add(scene);
            return mats;
        }
        for (Region region : regions) {
            mats.add(scene.apply(region.getJavaCVRect()));
        }
        return mats;
    }

    public Mat getMatFromFile(String path, Region region, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat scene = getMatFromFilename(path, colorSchemaName);
        return scene.apply(region.getJavaCVRect());
    }

    public Mat getMatFromScreen(Region region, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat img = getMatFromScreen(region);
        if (colorSchemaName == ColorCluster.ColorSchemaName.BGR) return img;
        if (colorSchemaName == ColorCluster.ColorSchemaName.HSV) return getHSV(img);
        throw new RuntimeException("ColorSchemaName not supported: " + colorSchemaName);
    }

    public Mat getMatFromScreen(Region region) {
        BufferedImage bi = bufferedImageOps.getBuffImgFromScreen(region);
        return getMat(bi, false);
    }

    public Mat getMatFromScreen(ColorCluster.ColorSchemaName colorSchemaName) {
        return getMatFromScreen(new Region(), colorSchemaName);
    }

    public Mat getMatFromScreen() {
        return getMatFromScreen(new Region());
    }

    public Mat getMat(BufferedImage image, boolean hsv) {
        image = bufferedImageOps.convertTo3ByteBGRType(image);
        //byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = bufferedImage2Mat(image); //new Mat(data);
        if (hsv) return convertToHSV(mat);
        return mat;
    }

    public List<Mat> getMatsFromScreen(List<Region> regions, boolean hsv) {
        List<BufferedImage> bufferedImages = bufferedImageOps.getBuffImgsFromScreen(regions);
        List<Mat> mats = new ArrayList<>();
        bufferedImages.forEach(bI -> mats.add(getMat(bI, hsv)));
        return mats;
    }

    public Mat bufferedImage2Mat(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imdecode(new Mat(byteArrayOutputStream.toByteArray()), IMREAD_UNCHANGED);
    }

    public Mat getMatFromScreenWithJavaCV() {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        FrameGrabber grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(30);
        try {
            grabber.start();
            Frame frame = grabber.grab();
            grabber.stop();
            return converter.convert(frame);
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a Mat from the screen at regular intervals.
     * @param region the region to capture
     * @param intervalSeconds how often to capture the screen
     * @param totalSecondsToRun total time to capture screenshots
     * @return a collection of Mat objects
     */
    public MatVector getMatsFromScreen(Region region, double intervalSeconds, double totalSecondsToRun) {
        MatVector matVector = new MatVector();
        int totalIterations = (int) (totalSecondsToRun / intervalSeconds);
        for (int i = 0; i < totalIterations; i++) {
            long startTime = System.currentTimeMillis();
            Mat mat = getMatFromScreen(region); // take a screenshot
            matVector.push_back(mat);
            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(0, (long) (intervalSeconds * 1000 - elapsedTime));
            try {
                Thread.sleep(sleepTime); // Sleep for the adjusted sleep time
            } catch (InterruptedException e) {
                // Handle interrupted exception if needed
                e.printStackTrace();
            }
        }
        return matVector;
    }

}
