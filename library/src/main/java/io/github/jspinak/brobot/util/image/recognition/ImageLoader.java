package io.github.jspinak.brobot.util.image.recognition;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.model.element.Region;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
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
 * JavaCV-based image acquisition and conversion utilities for Brobot.
 * 
 * <p>ImageLoader provides comprehensive image loading, conversion, and capture 
 * functionality using the JavaCV library. JavaCV offers better integration with 
 * deep learning frameworks like DL4J (DeepLearning4J), making it the preferred 
 * choice for Brobot's computer vision operations. This class handles various 
 * image sources and color space conversions essential for pattern matching 
 * and state detection.</p>
 * 
 * <p>Key capabilities:
 * <ul>
 *   <li><b>File Loading</b>: Load images from files with automatic path resolution</li>
 *   <li><b>Screen Capture</b>: Capture screenshots of full screen or specific regions</li>
 *   <li><b>Color Conversion</b>: Convert between BGR and HSV color spaces</li>
 *   <li><b>Region Masking</b>: Extract specific regions from larger images</li>
 *   <li><b>Batch Processing</b>: Handle multiple images or regions efficiently</li>
 *   <li><b>Time-Series Capture</b>: Record screen changes over time</li>
 * </ul>
 * </p>
 * 
 * <p>Image sources supported:
 * <ul>
 *   <li>File system paths (absolute or bundle-relative)</li>
 *   <li>Screen captures (full or regional)</li>
 *   <li>BufferedImage conversions</li>
 *   <li>Pattern and StateImage collections</li>
 *   <li>Video frame grabbing via FFmpeg</li>
 * </ul>
 * </p>
 * 
 * <p>Color space support:
 * <ul>
 *   <li><b>BGR</b>: Default OpenCV color format (Blue-Green-Red)</li>
 *   <li><b>HSV</b>: Hue-Saturation-Value for color-based matching</li>
 *   <li>Automatic conversion based on ColorSchemaName parameter</li>
 *   <li>In-place or new Mat conversions available</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Loading pattern images for template matching</li>
 *   <li>Capturing screenshots for state detection</li>
 *   <li>Converting images for color-based analysis</li>
 *   <li>Extracting regions for focused processing</li>
 *   <li>Recording screen sequences for analysis</li>
 * </ul>
 * </p>
 * 
 * <p>Performance features:
 * <ul>
 *   <li>Efficient batch operations for multiple images</li>
 *   <li>Direct memory operations avoiding unnecessary copies</li>
 *   <li>Configurable capture intervals for time-series data</li>
 *   <li>Region-based capture to minimize data volume</li>
 * </ul>
 * </p>
 * 
 * <p>Integration advantages:
 * <ul>
 *   <li>Compatible with DL4J for deep learning models</li>
 *   <li>Unified API across different image sources</li>
 *   <li>Consistent color space handling</li>
 *   <li>Seamless integration with Brobot's pattern matching</li>
 * </ul>
 * </p>
 * 
 * <p>Time-series capture example:
 * <pre>
 * // Capture screen region every 0.5 seconds for 10 seconds
 * MatVector frames = getImageJavaCV.getMatsFromScreen(
 *     region, 0.5, 10.0
 * );
 * </pre>
 * </p>
 * 
 * <p>In the model-based approach, ImageLoader serves as the primary image 
 * acquisition layer, providing consistent access to visual data regardless of 
 * source. This abstraction enables the framework to work with files during 
 * development, live screens during execution, and recorded data during testing, 
 * all through the same interface.</p>
 * 
 * @since 1.0
 * @see Mat
 * @see ColorCluster
 * @see BufferedImageUtilities
 * @see Pattern
 * @see StateImage
 */
@Component
public class ImageLoader {

    private BufferedImageUtilities bufferedImageOps;

    public ImageLoader(BufferedImageUtilities bufferedImageOps) {
        this.bufferedImageOps = bufferedImageOps;
    }

    public Mat convertToHSV(Mat bgr) {
        cvtColor(bgr, bgr, COLOR_BGR2HSV);
        return bgr;
    }

    public Mat getHSV(Mat bgr) {
        if (bgr == null || bgr.empty()) {
            return new Mat(); // Return empty Mat for empty input
        }
        Mat hsv = new Mat();
        cvtColor(bgr, hsv, COLOR_BGR2HSV);
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
        if (regions == null || regions.isEmpty()) {
            return image;
        }
        
        // Create a black mask the same size as the image
        Mat mask = Mat.zeros(image.size(), image.type()).asMat();
        
        // Copy regions from the original image to the mask
        for (Region region : regions) {
            // Ensure region is within image bounds
            int x = Math.max(0, Math.min(region.x(), image.cols() - 1));
            int y = Math.max(0, Math.min(region.y(), image.rows() - 1));
            int width = Math.min(region.w(), image.cols() - x);
            int height = Math.min(region.h(), image.rows() - y);
            
            if (width > 0 && height > 0) {
                Rect rect = new Rect(x, y, width, height);
                Mat srcRegion = new Mat(image, rect);
                Mat dstRegion = new Mat(mask, rect);
                srcRegion.copyTo(dstRegion);
            }
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
