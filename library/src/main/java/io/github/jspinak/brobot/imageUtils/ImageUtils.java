package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ImageUtils {

    private Map<String, Integer> lastFilenumber = new HashMap<>();
    private GetBufferedImage getBufferedImage;

    public ImageUtils(GetBufferedImage getBufferedImage) {
        this.getBufferedImage = getBufferedImage;
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
            ImageIO.write(getBufferedImage.fromScreen(region),
                    "png", new File("" + newPath));
            return newPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches for the first free filename with the given base path name.
     * @param path the base path name
     * @return the path name with the first free filename
     */
    private String getFreePath(String path) {
        int i = lastFilenumber.containsKey(path)? lastFilenumber.get(path) + 1 : 0;
        while (fileExists(path + i + ".png")) {
            i++;
        }
        lastFilenumber.put(path, i);
        return path + i;
    }

    private boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }
}
