package io.github.jspinak.brobot.imageUtils;

import org.sikuli.script.Image;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import java.io.File;

public interface SaveToFile {

    public File createFolder(File folder);

    String saveImageWithDate(Image img, String baseFileName);

    void saveXML(Document doc, String fileName);
}
