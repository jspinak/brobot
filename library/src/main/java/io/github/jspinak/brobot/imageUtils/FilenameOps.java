package io.github.jspinak.brobot.imageUtils;

public class FilenameOps {

    public static String addPngExtensionIfNeeded(String fileName) {
        // Check if the filename has an extension
        if (!fileName.contains(".")) {
            // If there's no extension, add ".png"
            fileName += ".png";
        }
        return fileName;
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1) {
            return filename.substring(0, dotIndex);
        } else {
            // If there's no extension, return the full filename
            return filename;
        }
    }
}
