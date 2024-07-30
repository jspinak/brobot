package io.github.jspinak.brobot.stringUtils;

import java.io.File;

public class NameSelector {

    public static String getFilenameWithoutExtensionAndDirectory(String filename) {
        if (filename == null) return "";
        File file = new File(filename); // Create a File object from the image path
        return file.getName().replaceFirst("[.][^.]+$", "");
    }
}
