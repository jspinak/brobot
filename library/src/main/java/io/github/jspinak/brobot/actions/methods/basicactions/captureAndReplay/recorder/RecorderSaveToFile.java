package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder;

import io.github.jspinak.brobot.actions.BrobotSettings;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

@Component
public class RecorderSaveToFile implements SaveToFile {

    public File createFolder() {
        return new File(BrobotSettings.recordingFolder);
    }

    public File createFolder(File folder) {
        return new File(folder.getPath()); // todo: review quick replacement done bc not in Sikuli 2.0.5 -> //Commons.asFolder(folder.getPath());
    }

    /**
     * Save the Image as .png into folderToUse
     *
     * @param img the image
     * @return the absolute path of the saved image
     */
    public String saveImageWithDate(Image img, String baseFileName) {
        File fImage = new File(createFolder(), String.format("%s-%d.png", baseFileName, new Date().getTime()));
        try {
            ImageIO.write(img.get(), FilenameUtils.getExtension(fImage.getName()), fImage);
            Debug.log(3, "ScreenImage::saveImage: %s", fImage);
        } catch (Exception ex) {
            Debug.error("ScreenImage::saveImage: did not work: %s (%s)", fImage, ex.getMessage());
            return null;
        }
        return fImage.getAbsolutePath();
    }

    /**
     * Save as XML to file.
     *
     * @param doc the XML Document
     */
    public void saveXML(Document doc, String fileName) {
        if (doc == null) return;
        FileOutputStream output;
        try {
            output = new FileOutputStream(new File(createFolder(), fileName));
            writeXml(doc, output);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // write doc to output stream
    private void writeXml(Document doc, OutputStream output) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // makes it look nice
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}