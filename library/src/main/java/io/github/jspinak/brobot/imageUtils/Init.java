package io.github.jspinak.brobot.imageUtils;

public class ImagePath {

    public static void setBundlePath(String path) {
        org.sikuli.script.ImagePath.setBundlePath(path);
        System.out.println("path set to BundlePath: "+path);
    }

    public static void add(String path) {
        org.sikuli.script.ImagePath.add(path);
    }
}
