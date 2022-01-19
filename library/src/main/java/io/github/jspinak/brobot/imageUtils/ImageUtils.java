package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.primitives.region.Region;
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

    public void saveRegionToFile(Region region, String path) {
        if (BrobotSettings.mock) {
            Report.format("Save file with base path %s \n", path);
            return;
        }
        try {
            String newPath = getFreePath(path);
            System.out.println(path + " " + newPath);
            ImageIO.write(new Screen().capture(region).getImage(),
                    "png", new File("" + newPath + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFreePath(String path) {
        if (!fileExists(path + ".png")) {
            return path;
        }
        if (lastFilenumber.containsKey(path)) {
            int l = lastFilenumber.get(path);
            lastFilenumber.put(path, ++l);
            return path + lastFilenumber.get(path);
        }
        int i = 1;
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
