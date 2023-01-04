package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IllustrationScenes store their filenames before they are written to file.
 * Since we may have multiple IllustrationScenes at any given time, and these
 * will not yet be written to file, we need to store their filenames somewhere
 * in order to keep track of which filenames are used and which are free.
 */
@Component
public class FilenameRepo {

    private ImageUtils imageUtils;

    private List<String> filenames = new ArrayList<>();
    private Map<String, Integer> indices = new HashMap<>();


    public FilenameRepo(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }

    public void addFilename(String filename) {
        filenames.add(filename);
    }

    public boolean filenameExists(String filename) {
        return filenames.contains(filename);
    }

    public String reserveFreePath(String prefix, String suffix) {
        int i = indices.containsKey(prefix) ? indices.get(prefix) + 1 : 0;
        String filename = prefix + suffix;
        while (imageUtils.fileExists(filename) || filenameExists(filename)) {
            i++;
            filename = prefix + suffix + "_" + i;
        }
        indices.put(prefix, i);
        filenames.add(filename);
        return filename;
    }
}
