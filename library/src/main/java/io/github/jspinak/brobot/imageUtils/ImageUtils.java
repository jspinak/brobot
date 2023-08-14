package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Component
public class ImageUtils {

    private Map<String, Integer> lastFilenumber = new HashMap<>();
    private GetImage getImage;

    public ImageUtils(GetImage getImage) {
        this.getImage = getImage;
    }

    /**
     * Saves the region to file as a .png file.
     * @param region The region to save
     * @param path The base path name: the first free filename will be used based on the base path name
     *             and an available number.
     * @return The path name used to save the file.
     */
    public String saveRegionToFile(Region region, String path) {
        try {
            String newPath = getFreePath(path) + ".png";
            if (BrobotSettings.mock) {
                Report.format("Save file as %s \n", newPath);
                return newPath;
            }
            if (!BrobotSettings.saveHistory) System.out.println(newPath); // don't print when running live
            ImageIO.write(getImage.getBuffImgFromScreen(region),
                    "png", new File(newPath));
            return newPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String saveBuffImgToFile(BufferedImage bufferedImage, String path) {
        try {
            String newPath = getFreePath(path) + ".png";
            ImageIO.write(bufferedImage,"png", new File(newPath));
            return newPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String saveScreenshotToFile(String path) {
        return saveRegionToFile(new Region(), path);
    }

    /**
     * Searches for the first free filename with the given base path name.
     * @param path the base path name
     * @return the path name with the first free filename
     */
    public String getFreePath(String path) {
        int i = lastFilenumber.containsKey(path)? lastFilenumber.get(path) + 1 : 0;
        while (fileExists(path + i + ".png")) {
            i++;
        }
        lastFilenumber.put(path, i);
        return path + i;
    }

    public String getFreePath(String prefix, String suffix) {
        int i = lastFilenumber.containsKey(prefix) ? lastFilenumber.get(prefix) + 1 : 0;
        String filename = prefix + i + "_" + suffix + ".png";
        while (fileExists(filename)) {
            i++;
            filename = prefix + i + "_" + suffix + ".png";
        }
        return filename;
    }

    public String getFreePath() {
        return getFreePath(BrobotSettings.historyPath + BrobotSettings.historyFilename);
    }

    boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }

    public int getFreeNumber() {
        return getFreeNumber(BrobotSettings.historyPath + BrobotSettings.historyFilename);
    }

    public int getFreeNumber(String path) {
        getFreePath(path);
        return lastFilenumber.get(path);
    }

    public boolean writeWithUniqueFilename(Mat mat, String nameWithoutFiletype) {
        nameWithoutFiletype = getFreePath(nameWithoutFiletype) + ".png";
        return imwrite(nameWithoutFiletype, mat);
    }

    public boolean writeAllWithUniqueFilename(List<Mat> mats, List<String> filenames) {
        if (mats.size() != filenames.size()) {
            Report.println("Error: number of mats and filenames must be equal.");
            return false;
        }
        List<Mat> nonNullMats = new ArrayList<>();
        List<String> nonNullFilenames = new ArrayList<>();
        for (int i=0; i<mats.size(); i++) {
            if (mats.get(i) != null) {
                nonNullMats.add(mats.get(i));
                nonNullFilenames.add(filenames.get(i));
            }
            //else Report.println("Mat at index " + i + " is null.");
        }
        for (int i=0; i<nonNullMats.size(); i++) {
            if (!writeWithUniqueFilename(nonNullMats.get(i), nonNullFilenames.get(i))) {
                return false;
            }
        }
        return true;
    }
}
