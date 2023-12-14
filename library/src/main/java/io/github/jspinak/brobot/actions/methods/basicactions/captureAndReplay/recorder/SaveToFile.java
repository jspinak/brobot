package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder;

import org.sikuli.script.Image;
import org.w3c.dom.Document;
import java.io.File;

public interface SaveToFile {

    public File createFolder(File folder);

    String saveImageWithDate(Image img, String baseFileName);

    void saveXML(Document doc, String fileName);
}
